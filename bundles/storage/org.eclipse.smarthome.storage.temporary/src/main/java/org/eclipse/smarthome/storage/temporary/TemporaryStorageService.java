/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.storage.temporary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TemporaryStorageService} returns {@link TemporaryStorage}s
 * which stores their data in-memory.
 *
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 * @author MAW - moved to new bundle and renamed
 */
@Component(property={"type=temporaryStorage"})
public class TemporaryStorageService implements StorageService {

    @SuppressWarnings("rawtypes")
    Map<String, Storage> storages = new ConcurrentHashMap<String, Storage>();

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> Storage<T> getStorage(String name) {
        if (!storages.containsKey(name)) {
            storages.put(name, new TemporaryStorage<T>());
        }
        return storages.get(name);
    }

    @Override
    public <T> Storage<T> getStorage(String name, ClassLoader classLoader) {
        return getStorage(name);
    }

}
