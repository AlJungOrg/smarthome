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
package org.eclipse.smarthome.binding.hue.internal;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

/**
 * Detailed scene information.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author MAW - initial contribution
 */
public class Scene extends FullHueObject {

    public static final Type GSON_TYPE = new TypeToken<Map<String, Scene>>() {
    }.getType();

    private String group;
    //artificial attribute to store room name for the gui
    private String room;

    public Scene() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
