/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.mapdb.internal;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.osgi.framework.BundleContext;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is the implementation of the MapDB {@link PersistenceService}. To learn
 * more about MapDB please visit their <a
 * href="http://www.mapdb.org/">website</a>.
 *
 * @author Jens Viebig
 * @since 1.7.0
 */
public class MapDbPersistenceService implements QueryablePersistenceService {

    private static final String SERVICE_NAME = "mapdb";

    protected final static String DB_FOLDER_NAME = ConfigConstants.getUserDataFolder() + File.separator + "persistence" + File.separator + "mapdb";

    private static final String DB_FILE_NAME = "storage.mapdb";

    private static final String SCHEDULER_GROUP = "MapDB_SchedulerGroup";

    private static int commitInterval = 5;

    private static boolean commitSameState = false;

    private static boolean needsCommit = false;

    private static final Logger logger = LoggerFactory.getLogger(MapDbPersistenceService.class);

    /** holds the local instance of the MapDB database */
    private static DB db;
    private static Map<String, String> map;

    private transient Gson mapper = new GsonBuilder()
            .registerTypeAdapter(State.class, new StateTypeAdapter())
            .create();

    public void activate(final BundleContext bundleContext, final Map<String, Object> config) {
        logger.debug("mapdb persistence service is being activated");

        if (config != null) {
            String commitIntervalString = (String) config.get("commitinterval");
            if (StringUtils.isNotBlank(commitIntervalString)) {
                try {
                    commitInterval = Integer.valueOf(commitIntervalString);
                } catch (IllegalArgumentException iae) {
                    logger.warn("couldn't parse '{}' to an integer");
                }
            }
            String commitSameStateString = (String) config.get("commitsamestate");
            if (StringUtils.isNotBlank(commitSameStateString)) {
                try {
                    commitSameState = Boolean.valueOf(commitSameStateString);
                } catch (IllegalArgumentException iae) {
                    logger.warn("couldn't parse '{}' to an integer");
                }
            }
        }

        File folder = new File(DB_FOLDER_NAME);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
              logger.error("Failed to create one or more directories in the path '{}'", DB_FOLDER_NAME);
              logger.error("MapDB persistence service activation has failed.");
              return;
            }
        }

        File dbFile = new File(DB_FOLDER_NAME, DB_FILE_NAME);
        db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
        map = db.createTreeMap("itemStore").makeOrGet();
        scheduleJob();
        logger.debug("mapdb persistence service is now activated");
    }

    public void deactivate(final int reason) {
        logger.debug("mapdb persistence service deactivated");
        if (db != null) {
            db.close();
        }
        cancelAllJobs();
    }

    @Override
    public String getId() {
        return SERVICE_NAME;
    }

    @Override
    public String getLabel(Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return map.values().stream()
                .map(this::deserialize)
                .collect(Collectors.<PersistenceItemInfo>toSet());
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public void store(Item item, String alias) {

        if (item.getState() instanceof UnDefType) {
            return;
        }

        if (alias == null) {
            alias = item.getName();
        }

        logger.debug("store called for {}", alias);

        State state = item.getState();
        MapDbItem mItem = new MapDbItem();
        mItem.setName(alias);
        mItem.setState(state);
        mItem.setTimestamp(new Date());
        String json = serialize(mItem);
        String oldJson = map.put(alias, json);

        if (!commitSameState) {
            if (oldJson != null) {
                MapDbItem oldItem = deserialize(oldJson);
                if (!oldItem.getState().toString().equals(state.toString())) {
                    needsCommit = true;
                }
            }
        }
        logger.debug("Stored '{}' with state '{}' in mapdb database", alias, state.toString());
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        String json = map.get(filter.getItemName());
        if (json == null) {
            return Collections.emptyList();
        }
        MapDbItem item = deserialize(json);
        return Collections.singletonList(item);
    }

    private String serialize(MapDbItem item) {
        return mapper.toJson(item);
    }

    private MapDbItem deserialize(String json) {
        return mapper.fromJson(json, MapDbItem.class);
    }

    /**
     * Schedules new quartz scheduler jobs for committing transactions and
     * backing up the database
     */
    private void scheduleJob() {
        try {
            Scheduler sched = StdSchedulerFactory.getDefaultScheduler();

            // schedule commit-job
            JobDetail job = newJob(CommitJob.class).withIdentity("Commit_Transaction", SCHEDULER_GROUP).build();

            SimpleTrigger trigger = newTrigger().withIdentity("Commit_Transaction", SCHEDULER_GROUP)
                    .withSchedule(repeatSecondlyForever(commitInterval)).build();

            sched.scheduleJob(job, trigger);
            logger.debug("Scheduled Commit-Job with interval {}sec.", commitInterval);

        } catch (SchedulerException e) {
            logger.warn("Could not create Job: {}", e.getMessage());
        }
    }

    /**
     * Delete all quartz scheduler jobs of the group <code>Dropbox</code>.
     */
    private void cancelAllJobs() {
        try {
            Scheduler sched = StdSchedulerFactory.getDefaultScheduler();
            Set<JobKey> jobKeys = sched.getJobKeys(jobGroupEquals(SCHEDULER_GROUP));
            if (jobKeys.size() > 0) {
                sched.deleteJobs(new ArrayList<JobKey>(jobKeys));
                logger.debug("Found {} MapDB-Jobs to delete from DefaultScheduler (keys={})", jobKeys.size(), jobKeys);
            }
        } catch (SchedulerException e) {
            logger.warn("Couldn't remove Commit-Job: {}", e.getMessage());
        }
    }

    /**
     * A quartz scheduler job to commit the mapdb transaction frequently. There
     * can be only one instance of a specific job type running at the same time.
     *
     * @author Jens Viebig
     * @since 1.7.0
     */
    @DisallowConcurrentExecution
    public static class CommitJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            long startTime = System.currentTimeMillis();
            try {
                if (!db.isClosed() && (needsCommit || commitSameState)) {
                    needsCommit = false;
                    db.commit();
                    logger.trace("successfully commited mapdb transaction in {}ms",
                            System.currentTimeMillis() - startTime);
                }
            } catch (Exception e) {
                try {
                    logger.warn("Error committing transaction : {}", e.getMessage());
                    if (!db.isClosed()) {
                        db.rollback();
                    }
                } catch (Exception re) {
                    logger.debug("Rollback Exception: {}", e.getMessage());
                }
            }
        }

    }
}
