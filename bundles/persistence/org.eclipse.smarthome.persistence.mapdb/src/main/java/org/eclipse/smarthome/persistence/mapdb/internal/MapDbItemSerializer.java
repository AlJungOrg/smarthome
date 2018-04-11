/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.mapdb.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.mapdb.Serializer;

/**
 * Serializer to serialize items to and from Mapdb format
 *
 * @author Jens Viebig
 * @since 1.7.0
 *
 */
public class MapDbItemSerializer implements Serializer<MapDbItem>, Serializable {

    private static final long serialVersionUID = 1L;

    public MapDbItemSerializer() {
    }

    @Override
    public void serialize(DataOutput out, MapDbItem item) throws IOException {
        out.writeUTF(item.getName());
        out.writeUTF(item.getState().getClass().getSimpleName());
        String stateStr = item.getState().toString();
        out.writeUTF(stateStr == null ? "" : stateStr);
        out.writeLong(item.getTimestamp().getTime());
    }

    @Override
    public MapDbItem deserialize(DataInput in, int available) throws IOException {
        String name = in.readUTF();

        String stateType = in.readUTF();
        String stateStr = in.readUTF();
        State state = null;
        switch (stateType) {
            case "DateTimeType":
                state = DateTimeType.valueOf(stateStr);
                break;
            case "DecimalType":
                state = DecimalType.valueOf(stateStr);
                break;
            case "HSBType":
                state = HSBType.valueOf(stateStr);
                break;
            case "OnOffType":
                state = OnOffType.valueOf(stateStr);
                break;
            case "OpenClosedType":
                state = OpenClosedType.valueOf(stateStr);
                break;
            case "PercentType":
                state = PercentType.valueOf(stateStr);
                break;
            default:
                state = StringType.valueOf(stateStr);
                break;
        }

        Date date = new Date(in.readLong());

        MapDbItem item = new MapDbItem();
        item.setName(name);
        item.setState(state);
        item.setTimestamp(date);
        return item;
    }

    @Override
    public int fixedSize() {
        return -1;
    }

}
