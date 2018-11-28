/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.hue.internal.discovery;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ManualBridgeJsonParameters} class defines JSON object, which
 * contains bridge attributes like IP address. It is used for forced bridge
 * Discovery.
 *
 * @author MAW initial contribution
 */
public class ManualBridgeJsonParameters {

    @SerializedName("bridgeid")
    private final String bridgeId;
    private final String mac;
    private final String name;

    public ManualBridgeJsonParameters(String bridgeId, String mac, String name) {
        this.bridgeId = bridgeId;
        this.mac = mac;
        this.name = name;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

}
