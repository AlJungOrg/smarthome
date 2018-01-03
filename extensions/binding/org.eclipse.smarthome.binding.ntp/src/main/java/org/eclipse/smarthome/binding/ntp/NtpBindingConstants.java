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
package org.eclipse.smarthome.binding.ntp;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ntpBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
public class NtpBindingConstants {

    public static final String BINDING_ID = "ntp";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_NTP = new ThingTypeUID(BINDING_ID, "ntp");

    // List of all Channel ids
    public final static String CHANNEL_DATE_TIME = "dateTime";
    public final static String CHANNEL_STRING = "string";

    // Custom Properties
    public final static String PROPERTY_NTP_SERVER_HOST = "hostname";
    public final static String PROPERTY_REFRESH_INTERVAL = "refreshInterval";
    public final static String PROPERTY_REFRESH_NTP = "refreshNtp";
    public final static String PROPERTY_TIMEZONE = "timeZone";
    public final static String PROPERTY_LOCALE = "locale";
    public final static String PROPERTY_DATE_TIME_FORMAT = "DateTimeFormat";
    public final static String PROPERTY_NTP_SERVER_PORT = "serverPort";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_NTP);

}
