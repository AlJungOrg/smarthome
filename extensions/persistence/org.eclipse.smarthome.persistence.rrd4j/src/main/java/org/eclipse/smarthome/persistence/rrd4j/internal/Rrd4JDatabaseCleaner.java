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

import static java.nio.file.Files.deleteIfExists;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JConstants.SERVICE_NAME;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.databasePath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.items.events.ItemRemovedEvent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(service = EventSubscriber.class, configurationPid = "org.eclipse.smarthome.persistence.rrd4j.cleanup")
public class Rrd4JDatabaseCleaner implements EventSubscriber {
    private final Logger logger = LoggerFactory.getLogger(Rrd4JDatabaseCleaner.class);
    private final Set<String> subscribedEventTypes = singleton(ItemRemovedEvent.TYPE);

    private boolean enabled = false;
    private long delaySeconds = SECONDS.convert(1, MINUTES);

    @NonNullByDefault({})
    private ScheduledExecutorService scheduler;

    @NonNullByDefault({})
    private ItemRegistry items;

    @Reference
    protected void setItemRegistry(ItemRegistry items) {
        this.items = items;
    }

    protected void unsetItemRegistry(ItemRegistry items) {
        this.items = null;
    }

    protected void activate(Map<String, String> properties) {
        if (properties == null) return;
        enabled = Boolean.parseBoolean(properties.get(Configuration.enabled));
        if (!enabled) return;
        try {
            delaySeconds = Long.parseLong(properties.get(Configuration.delaySeconds));
        } catch (NumberFormatException e) {
            logger.warn("invalid configuration property '{}', using default value: {}",
                    Configuration.delaySeconds, delaySeconds);
        }
        scheduler = ThreadPoolManager.getScheduledPool(SERVICE_NAME);
        logger.info("enabled RRD4J database cleanup");
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        if (!enabled) return;
        if (event instanceof ItemRemovedEvent) {
            handleItemRemoved((ItemRemovedEvent) event);
        }
    }

    private void handleItemRemoved(ItemRemovedEvent event) {
        ItemDTO item = event.getItem();
        String name = item.name;
        scheduler.schedule(() -> deleteDatabaseForName(name), delaySeconds, SECONDS);
    }

    private void deleteDatabaseForName(String name) {
        Item item = items.get(name);
        // if the Item has reappeared, keep its data around
        if (item != null) return;
        // otherwise delete the associated database file
        Path path = databasePath(name);
        try {
            deleteIfExists(path);
        } catch (IOException e) {
            logger.warn("Failed to delete database '{}'({}): {}", path, name, e);
        }
    }

    private static interface Configuration {
        String enabled = "enabled";
        String delaySeconds = "delay.seconds";
    }
}
