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
package org.eclipse.smarthome.binding.hue.internal.discovery;

import static org.eclipse.smarthome.binding.hue.internal.HueBindingConstants.*;
import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_SERIAL_NUMBER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link HueBridgeNupnpDiscovery} is responsible for discovering new hue bridges. It uses the 'NUPnP service
 * provided by Philips'.
 *
 * @author Awelkiyar Wehabrebi - Initial contribution
 * @author Christoph Knauf - Refactorings
 * @author Andre Fuechsel - make {@link #startScan()} asynchronous
 */
@NonNullByDefault
public class HueBridgeNupnpDiscovery extends AbstractDiscoveryService {

    private static final String MODEL_NAME_PHILIPS_HUE = "<modelName>Philips hue";

    protected static final String BRIDGE_INDICATOR = "fffe";

    protected static final String PHOSCON_GW_INDICATOR = "FFFF";

    private static final String[] DISCOVERY_URLS = {"https://discovery.meethue.com", "http://dresden-light.appspot.com/discover"};

    protected static final String LABEL_PATTERN = "Philips hue (IP)";

    private static final String DESC_URL_PATTERN = "http://HOST/description.xml";

    private static final int REQUEST_TIMEOUT = 5000;

    private static final int DISCOVERY_TIMEOUT = 10;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(HueBridgeNupnpDiscovery.class);

    public HueBridgeNupnpDiscovery() {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT, false);
    }

    @Override
    protected void startScan() {
        scheduler.schedule(this::discoverHueBridges, 0, TimeUnit.SECONDS);
    }

    /**
     * Discover available Hue Bridges and then add them in the discovery inbox
     */
    private void discoverHueBridges() {
        for (BridgeJsonParameters bridge : getBridgeList()) {
            if (isReachableAndValidHueBridge(bridge)) {
                String host = bridge.getInternalIpAddress();
                String serialNumber = bridge.getId().substring(0, 6) + bridge.getId().substring(10);
                ThingUID uid = new ThingUID(THING_TYPE_BRIDGE, serialNumber);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                        .withProperties(buildProperties(host, serialNumber))
                        .withLabel(LABEL_PATTERN.replace("IP", host)).withRepresentationProperty(PROPERTY_SERIAL_NUMBER)
                        .build();
                thingDiscovered(result);
            }
        }
    }

    /**
     * Add a manual discovery into the discovery inbox.
     * @param ip
     * @return DiscoveryResult
     */
    @Nullable
    public DiscoveryResult addDiscovery(String ip) {
        BridgeJsonParameters bridge;
        try {
            String json = doGetRequest("http://" + ip + "/api/config");
            Gson gson = new Gson();
            ManualBridgeJsonParameters manualBridge = gson.fromJson(json, new TypeToken<ManualBridgeJsonParameters>() {
            }.getType());
            // The requested json data has always uppercase id for a hue or phoscon bridge. Since in discovery a
            // hue bridge has an lowercase id and a phoscon bridge has an uppercase id, this issue is addressed
            // here. The desired case, if lower or upper, is determined by the discovery mapping and then adapted
            // for in the manual discovery and validly checks.
            String correctedId = manualBridge.getBridgeId().substring(6, 10).equals(PHOSCON_GW_INDICATOR) ?
                manualBridge.getBridgeId() :
                manualBridge.getBridgeId().toLowerCase();
            bridge = new BridgeJsonParameters(correctedId, ip, manualBridge.getMac() , manualBridge.getName());
            if (isReachableAndValidHueBridge(bridge)) {
                String host = ip;
                String serialNumber = bridge.getId().substring(0, 6) + bridge.getId().substring(10);
                ThingUID uid = new ThingUID(THING_TYPE_BRIDGE, serialNumber);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                    .withProperties(buildProperties(host, serialNumber))
                    .withLabel(LABEL_PATTERN.replace("IP", host)).withRepresentationProperty(PROPERTY_SERIAL_NUMBER).build();
                thingDiscovered(result);
                return result;
            }
        } catch (IOException e) {
            logger.debug("Philips Hue Bridge config not reachable. Can't discover manual bridge");
        } catch (JsonParseException je) {
            logger.debug("Invalid json response from Hue Bridge. Can't discover manual bridge");
        }
        return null;
    }

    /**
     * Builds the bridge properties.
     *
     * @param host the ip of the bridge
     * @param serialNumber the id of the bridge
     * @return the bridge properties
     */
    private Map<String, Object> buildProperties(String host, String serialNumber) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(HOST, host);
        properties.put(PROPERTY_SERIAL_NUMBER, serialNumber);
        return properties;
    }

    /**
     * Checks if the Bridge is a reachable Hue Bridge with a valid id.
     *
     * @param bridge the {@link BridgeJsonParameters}s
     * @return true if Bridge is a reachable Hue Bridge with a id containing
     *         BRIDGE_INDICATOR longer then 10
     */
    private boolean isReachableAndValidHueBridge(BridgeJsonParameters bridge) {
        String host = bridge.getInternalIpAddress();
        String id = bridge.getId();
        String description;
        if (host == null) {
            logger.debug("Bridge not discovered: ip is null");
            return false;
        }
        if (id == null) {
            logger.debug("Bridge not discovered: id is null");
            return false;
        }
        if (id.length() < 10) {
            logger.debug("Bridge not discovered: id {} is shorter then 10.", id);
            return false;
        }
        if (!id.substring(6, 10).equals(BRIDGE_INDICATOR) && !id.substring(6, 10).equals(PHOSCON_GW_INDICATOR)) {
            logger.debug(
                    "Bridge not discovered: id {} does not contain bridge indicator {} or its at the wrong position.",
                    id, BRIDGE_INDICATOR);
            return false;
        }
        try {
            description = doGetRequest(DESC_URL_PATTERN.replace("HOST", host));
        } catch (IOException e) {
            logger.debug("Bridge not discovered: Failure accessing description file for ip: {}", host);
            return false;
        }
        if (!description.contains(MODEL_NAME_PHILIPS_HUE)) {
            logger.debug("Bridge not discovered: Description does not containing the model name: {}", description);
            return false;
        }
        return true;
    }

    /**
     * Use the Philips Hue NUPnP service and the Dresden-Light Discovery to find Hue Bridges and Phoscon Gateways in local Network.
     *
     * @return a list of available Hue Bridges
     */
    private List<BridgeJsonParameters> getBridgeList() {
        try {
            Gson gson = new Gson();
            List<BridgeJsonParameters> bridgeList = new ArrayList<BridgeJsonParameters>();
            for (String discovery_url : DISCOVERY_URLS) {
                List<BridgeJsonParameters> bridgeListPart = gson.fromJson(doGetRequest(discovery_url), new TypeToken<List<BridgeJsonParameters>>() {
                }.getType());
                for (BridgeJsonParameters bridgeJsonParams : bridgeListPart) {
                    bridgeList.add(bridgeJsonParams);
                }
            }
            return bridgeList;

        } catch (IOException e) {
            logger.debug("Philips Hue NUPnP service not reachable. Can't discover bridges");
        } catch (JsonParseException je) {
            logger.debug("Invalid json respone from Hue NUPnP service. Can't discover bridges");
        }
        return new ArrayList<>();
    }

    /**
     * Introduced in order to enable testing.
     *
     * @param url the url
     * @return the http request result as String
     * @throws IOException if request failed
     */
    protected String doGetRequest(String url) throws IOException {
        return HttpUtil.executeUrl("GET", url, REQUEST_TIMEOUT);
    }

}
