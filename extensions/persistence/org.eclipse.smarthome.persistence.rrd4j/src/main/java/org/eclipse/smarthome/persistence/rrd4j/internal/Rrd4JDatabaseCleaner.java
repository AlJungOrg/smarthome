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

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Collections.singleton;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.items.events.ItemRemovedEvent;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(service = EventSubscriber.class)
public class Rrd4JDatabaseCleaner implements EventSubscriber {
	private final Set<String> subscribedEventTypes = singleton(ItemRemovedEvent.TYPE);

	private final Logger logger = LoggerFactory.getLogger(Rrd4JDatabaseCleaner.class);

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
		if (event instanceof ItemRemovedEvent) {
			handleItemRemoved((ItemRemovedEvent) event);
		}
	}

	private void handleItemRemoved(ItemRemovedEvent event) {
		ItemDTO item = event.getItem();
		String name = item.name;
		Path path = databasePath(name);
		try {
			deleteIfExists(path);
		} catch (IOException e) {
			logger.warn("Failed to delete database '{}'({}): {}", path, name, e);
		}
	}
}
