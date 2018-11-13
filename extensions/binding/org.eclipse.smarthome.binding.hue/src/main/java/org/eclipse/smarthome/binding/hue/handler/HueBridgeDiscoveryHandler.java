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
package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.smarthome.binding.hue.internal.discovery.HueBridgeNupnpDiscovery;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;

/**
 * {@link HueBridgeDiscoveryHandler} is the handler for a manual hue bridge discovery.
 *
 * @author MAW - Initial contribution of hue binding
 */
public class HueBridgeDiscoveryHandler {

    /**
     * Add a manual discovery into the discovery inbox.
     * @param ip
     * @return DiscoveryResult
     */
    public DiscoveryResult addDiscovery(String ip) {
        HueBridgeNupnpDiscovery discovery = new HueBridgeNupnpDiscovery();
        return discovery.addDiscovery(ip);
    }
}
