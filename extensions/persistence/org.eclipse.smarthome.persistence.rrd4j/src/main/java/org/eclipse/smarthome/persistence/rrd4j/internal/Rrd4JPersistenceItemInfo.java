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

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;

/**
 * RRD4J {@link PersistenceItemInfo}
 *
 * @author Martin Kühl - Initial contribution
 */
@NonNullByDefault
class Rrd4JPersistenceItemInfo implements PersistenceItemInfo {
    private final String name;
    private final int count;
    private final Date earliest;
    private final Date latest;

    public Rrd4JPersistenceItemInfo(String name, int count, Date earliest, Date latest) {
        this.name = name;
        this.count = count;
        this.earliest = earliest;
        this.latest = latest;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable Integer getCount() {
        return count;
    }

    @Override
    public @Nullable Date getEarliest() {
        return earliest;
    }

    @Override
    public @Nullable Date getLatest() {
        return latest;
    }
}
