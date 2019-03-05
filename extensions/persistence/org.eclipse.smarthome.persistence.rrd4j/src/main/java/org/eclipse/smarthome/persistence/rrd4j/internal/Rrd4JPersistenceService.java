/**
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.persistence.rrd4j.internal;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Double.NaN;
import static java.util.stream.Collectors.toList;
import static org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering.ASCENDING;
import static org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering.DESCENDING;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JConstants.SERVICE_NAME;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.createRoot;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.databasePath;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.databasePaths;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.itemName;
import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.DsType.GAUGE;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemStateConverter;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.ArcDef;
import org.rrd4j.core.Archive;
import org.rrd4j.core.DsDef;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the RRD4J {@link PersistenceService}.
 * To learn more about RRD4J please visit
 * <a href="https://github.com/rrd4j/rrd4j">their website</a>.
 *
 * @author Martin Kühl - Initial contribution
 */
@NonNullByDefault
@Component(service = { PersistenceService.class, QueryablePersistenceService.class })
public class Rrd4JPersistenceService implements QueryablePersistenceService {
    static final String DATASOURCE_NAME = "state";
    static final DsDef DATASOURCE = new DsDef(DATASOURCE_NAME, GAUGE, 60, NaN, NaN);
    static final ArcDef[] ARCHIVES = {
        new ArcDef(AVERAGE, .5, 1, 600), //    1s over 10m
        new ArcDef(AVERAGE, .5, 5, 360), //    5s over 30m
        new ArcDef(AVERAGE, .5, 15, 480), //  15s over  2h 
        new ArcDef(AVERAGE, .5, 60, 720), //   1m over 12h
        new ArcDef(AVERAGE, .5, 600, 720), // 10m over  5d
        new ArcDef(AVERAGE, .5, 7200, 720) //  2h over 60d  
    };

	private final Logger logger = LoggerFactory.getLogger(Rrd4JPersistenceService.class);

    @NonNullByDefault({})
    private ItemRegistry itemRegistry;
    @NonNullByDefault({})
    private ItemStateConverter itemStateConverter;

    public void activate() {
        try {
        	createRoot();
        } catch (IOException e) {
            logger.error("Failed to create the service root directory: ", e);
            throw new IllegalStateException(e);
        }
    }

    public void deactivate() {
    }

    @Reference
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference
    protected void setItemStateConverter(ItemStateConverter itemStateConverter) {
        this.itemStateConverter = itemStateConverter;
    }

    protected void unsetItemStateConverter(ItemStateConverter itemStateConverter) {
        this.itemStateConverter = null;
    }

    @Override
    public String getId() {
        return SERVICE_NAME;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public void store(Item item) {
        store(item, item.getName());
    }

    @Override
    public void store(Item item, String alias) {
        // PersistenceManager passes SimpleItemConfiguration.alias which can be null
        if (alias == null) {
            alias = item.getName();
        }
        long time = Util.getTime();
        Path path = databasePath(alias);
        try (RrdDb db = write(path)) {
            double value = value(item, alias);
            // if the db timestamp is too recent, pretend that we're from the future
            long lastTime = db.getLastUpdateTime();
            if (time <= lastTime) {
                time = lastTime + 1;
            }
            ConsolFun function = consolidationFunction(db);
            // if more than one second has passed, repeat the last value
            // this ensures that queries starting in between them see it
            if (time > lastTime + 1 && isIdempotent(function)) {
                double lastValue = db.getLastDatasourceValue(DATASOURCE_NAME);
                insert(db, time - 1, lastValue);
            }
            // store the new value
            insert(db, time, value);
        } catch (IOException e) {
            logger.warn("Failed to store '{}'({}): {}", alias, item, e);
        }
    }

    private boolean isIdempotent(ConsolFun function) {
        switch (function) {
            case AVERAGE:
            case TOTAL:
                return false;
            default:
                return true;
        }
    }

    private double value(Item item, String alias) {
        State state = item.getStateAs(DecimalType.class);
        checkState(state instanceof DecimalType, "Failed to convert state of '%s'(%s)", alias, item);
        return ((DecimalType) state).doubleValue();
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        try (DirectoryStream<Path> stream = databasePaths()) {
            Set<PersistenceItemInfo> results = new HashSet<>();
            for (Path path : stream) {
                results.add(itemInfo(path));
            }
            return results;
        } catch (IOException e) {
            logger.warn("Failed to get item info: {}", e);
            return Collections.emptySet();
        }
    }

    private PersistenceItemInfo itemInfo(Path path) throws IOException {
        String name = itemName(path);
        try (RrdDb db = read(path)) {
            Archive archive = archive(db);
            long startTime = archive.getStartTime();
            long endTime = archive.getEndTime();
            int count = archive.getRows();
            Date earliest = Util.getDate(startTime);
            Date latest = Util.getDate(endTime);
            return itemInfo(name, count, earliest, latest);
        }
    }

    private PersistenceItemInfo itemInfo(String name, int count, Date earliest, Date latest) {
        return new Rrd4JPersistenceItemInfo(name, count, earliest, latest);
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        normalizeFilter(filter);
        try {
            if (filter.getBeginDateZoned() == null) {
                return queryLatest(filter.getItemName());
            }
            return queryFilter(filter);
        } catch (IOException e) {
            logger.warn("Failed to query '{}': {}", filter, e);
            return Collections.emptyList();
        }
    }

    private void normalizeFilter(FilterCriteria filter) {
        if (filter.getItemName() == null) {
            throw new UnsupportedOperationException("Filters without item name are not supported");
        }
        if (filter.getState() != null) {
            throw new UnsupportedOperationException("Filters with operator and state are not supported");
        }
        if (filter.getBeginDateZoned() == null) {
            if (filter.getPageNumber() != 0 || filter.getPageSize() != 1 || filter.getOrdering() != DESCENDING) {
                throw new UnsupportedOperationException("Filters without begin date may only query the latest value");
            }
            filter.setBeginDate(filter.getEndDateZoned());
        }
    }

    private Iterable<HistoricItem> queryLatest(String itemName) throws IOException {
        Path path = databasePath(itemName);
        try (RrdDb db = read(path)) {
            double value = db.getLastDatasourceValue(DATASOURCE_NAME);
            long time = db.getLastArchiveUpdateTime();
            Optional<HistoricItem> item = convertToHistoric(itemName, value, time);
            return streamOptional(item)
                    .collect(toList());
        }
    }

    private Iterable<HistoricItem> queryFilter(FilterCriteria filter) throws IOException {
        String itemName = filter.getItemName();
        Path path = databasePath(itemName);
        try (RrdDb db = read(path)) {
            ConsolFun function = consolidationFunction(db);
            long beginTime = timestamp(filter.getBeginDateZoned());
            long endTime = timestamp(filter.getEndDateZoned());
            FetchRequest request = db.createFetchRequest(function, beginTime, endTime);
            FetchData data = request.fetchData();
            int rowCount = data.getRowCount();
            double[] values = data.getValues(DATASOURCE_NAME);
            long[] timestamps = data.getTimestamps();
            return indices(filter, rowCount)
                    .mapToObj(row -> convertToHistoric(itemName, values[row], timestamps[row]))
                    .flatMap(Rrd4JPersistenceService::streamOptional)
                    .collect(toList());
        }
    }

    private IntStream indices(FilterCriteria filter, int max) {
        IntStream indices = IntStream.range(0, max)
                .skip(filter.getPageNumber() * filter.getPageSize())
                .limit(filter.getPageSize());
        if (filter.getOrdering() == ASCENDING) {
            return indices;
        }
        return indices.map(index -> max - index - 1);
    }

    private Optional<HistoricItem> convertToHistoric(String name, double value, long time) {
        if (Double.isNaN(value)) {
            return Optional.empty();
        }
        State state = convertToState(name, value);
        Date timestamp = Util.getDate(time);
        Rrd4JHistoricItem item = new Rrd4JHistoricItem(name, state, timestamp);
        return Optional.of(item);
    }

    private State convertToState(String itemName, double value) {
        DecimalType state = new DecimalType(value);
        Item item = itemRegistry.get(itemName);
        if (item == null) {
            return state;
        }
        return itemStateConverter.convertToAcceptedState(state, item);
    }

    private long timestamp(@Nullable ZonedDateTime date) {
        if (date == null) {
            return Util.getTime();
        }
        
        return Util.getTimestamp(GregorianCalendar.from(date));
    }

    private Archive archive(RrdDb db) {
        return db.getArchive(0);
    }

    private ConsolFun consolidationFunction(RrdDb db) throws IOException {
        return archive(db).getConsolFun();
    }

    private void insert(RrdDb db, long time, double value) throws IOException {
        Sample sample = db.createSample(time);
        sample.setValue(DATASOURCE_NAME, value);
        sample.update();
    }

    private RrdDb read(Path path) throws IOException {
        return new RrdDb(path.toString(), true);
    }

    private RrdDb write(Path path) throws IOException {
        if (Files.exists(path)) {
            return new RrdDb(path.toString());
        }
        RrdDef def = new RrdDef(path.toString(), Util.getTime(), 1L);
        def.addDatasource(DATASOURCE);
        def.addArchive(ARCHIVES);
        return new RrdDb(def);
    }

    private static <T> Stream<T> streamOptional(Optional<T> opt) {
        if (!opt.isPresent()) {
            return Stream.empty();
        }
        return Stream.of(opt.get());
    }
}
