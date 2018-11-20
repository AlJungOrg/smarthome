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

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.types.State;

/**
 * RRD4J {@link HistoricItem}
 *
 * @author Martin Kühl - Initial contribution
 */
@NonNullByDefault
class Rrd4JHistoricItem implements HistoricItem {
    private final String name;
    private final State state;
    private final Date timestamp;

    public Rrd4JHistoricItem(String name, State state, Date timestamp) {
        this.name = name;
        this.state = state;
        this.timestamp = timestamp;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return DateFormat.getDateTimeInstance().format(timestamp) + ": " + name + " -> " + state;
    }
}
