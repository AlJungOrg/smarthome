/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.mapdb.internal;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.types.State;

/**
 * This is a Java bean used to persist item states with timestamps in the
 * database.
 *
 * @author Jens Viebig
 * @since 1.7.0
 *
 */
public class MapDBItem implements HistoricItem, PersistenceItemInfo, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private State state;

    private Date timestamp;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return DateFormat.getDateTimeInstance().format(timestamp) + ": " + name + " -> " + state.toString();
    }

    @Override
    public Integer getCount() {
        return null;
    }

    @Override
    public Date getEarliest() {
        return null;
    }

    @Override
    public Date getLatest() {
        return null;
    }
}
