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

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HueBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Jochen Hiller - Added OSRAM Classic A60 RGBW
 * @author Markus Mazurczak - Added OSRAM PAR16 50
 * @author Andre Fuechsel - changed to generic thing types
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
public class HueBindingConstants {

    public static final String BINDING_ID = "hue";

    // List all Thing Type UIDs, related to the Hue Binding

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "0000");
    public static final ThingTypeUID THING_TYPE_ON_OFF_PLUG = new ThingTypeUID(BINDING_ID, "0010");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0200");
    public static final ThingTypeUID THING_TYPE_COLOR_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID, "0220");
    public static final ThingTypeUID THING_TYPE_EXTENDED_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0210");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "0100");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_PLUG = new ThingTypeUID(BINDING_ID, "0110");

    public static final ThingTypeUID THING_TYPE_DIMMER_SWITCH = new ThingTypeUID(BINDING_ID, "0820");
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "0107");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_SENSOR = new ThingTypeUID(BINDING_ID, "0302");
    public static final ThingTypeUID THING_TYPE_LIGHT_LEVEL_SENSOR = new ThingTypeUID(BINDING_ID, "0106");

    //---> ESH-Mod see IBN-638 --->
    // List all Thing Type UIDs, related to the Hue Binding
    public static final ThingTypeUID THING_TYPE_LCT001 = new ThingTypeUID(BINDING_ID, "LCT001");
    public static final ThingTypeUID THING_TYPE_LCT002 = new ThingTypeUID(BINDING_ID, "LCT002");
    public static final ThingTypeUID THING_TYPE_LCT003 = new ThingTypeUID(BINDING_ID, "LCT003");
    public static final ThingTypeUID THING_TYPE_LCT007 = new ThingTypeUID(BINDING_ID, "LCT007");
    public static final ThingTypeUID THING_TYPE_LCT010 = new ThingTypeUID(BINDING_ID, "LCT010");
    public static final ThingTypeUID THING_TYPE_LCT011 = new ThingTypeUID(BINDING_ID, "LCT011");
    public static final ThingTypeUID THING_TYPE_LCT012 = new ThingTypeUID(BINDING_ID, "LCT012");
    public static final ThingTypeUID THING_TYPE_LCT014 = new ThingTypeUID(BINDING_ID, "LCT014");
    public static final ThingTypeUID THING_TYPE_LTC001 = new ThingTypeUID(BINDING_ID, "LTC001");
    public static final ThingTypeUID THING_TYPE_LTC002 = new ThingTypeUID(BINDING_ID, "LTC002");
    public static final ThingTypeUID THING_TYPE_LTC003 = new ThingTypeUID(BINDING_ID, "LTC003");
    public static final ThingTypeUID THING_TYPE_LTC004 = new ThingTypeUID(BINDING_ID, "LTC004");
    public static final ThingTypeUID THING_TYPE_LLC001 = new ThingTypeUID(BINDING_ID, "LLC001");
    public static final ThingTypeUID THING_TYPE_LLC006 = new ThingTypeUID(BINDING_ID, "LLC006");
    public static final ThingTypeUID THING_TYPE_LLC007 = new ThingTypeUID(BINDING_ID, "LLC007");
    public static final ThingTypeUID THING_TYPE_LLC010 = new ThingTypeUID(BINDING_ID, "LLC010");
    public static final ThingTypeUID THING_TYPE_LLC011 = new ThingTypeUID(BINDING_ID, "LLC011");
    public static final ThingTypeUID THING_TYPE_LLC012 = new ThingTypeUID(BINDING_ID, "LLC012");
    public static final ThingTypeUID THING_TYPE_LLC013 = new ThingTypeUID(BINDING_ID, "LLC013");
    public static final ThingTypeUID THING_TYPE_LLC020 = new ThingTypeUID(BINDING_ID, "LLC020");
    public static final ThingTypeUID THING_TYPE_LST001 = new ThingTypeUID(BINDING_ID, "LST001");
    public static final ThingTypeUID THING_TYPE_LST002 = new ThingTypeUID(BINDING_ID, "LST002");
    public static final ThingTypeUID THING_TYPE_LWB004 = new ThingTypeUID(BINDING_ID, "LWB004");
    public static final ThingTypeUID THING_TYPE_LWB006 = new ThingTypeUID(BINDING_ID, "LWB006");
    public static final ThingTypeUID THING_TYPE_LWB007 = new ThingTypeUID(BINDING_ID, "LWB007");
    public static final ThingTypeUID THING_TYPE_LWB010 = new ThingTypeUID(BINDING_ID, "LWB010");
    public static final ThingTypeUID THING_TYPE_LWB014 = new ThingTypeUID(BINDING_ID, "LWB014");
    public static final ThingTypeUID THING_TYPE_LWL001 = new ThingTypeUID(BINDING_ID, "LWL001");
    public static final ThingTypeUID THING_TYPE_LTW001 = new ThingTypeUID(BINDING_ID, "LTW001");
    public static final ThingTypeUID THING_TYPE_LTW004 = new ThingTypeUID(BINDING_ID, "LTW004");
    public static final ThingTypeUID THING_TYPE_LTW012 = new ThingTypeUID(BINDING_ID, "LTW012");
    public static final ThingTypeUID THING_TYPE_LTW013 = new ThingTypeUID(BINDING_ID, "LTW013");
    public static final ThingTypeUID THING_TYPE_LTW014 = new ThingTypeUID(BINDING_ID, "LTW014");
    public static final ThingTypeUID THING_TYPE_CLASSIC_A60_RGBW = new ThingTypeUID(BINDING_ID, "Classic_A60_RGBW");
    public static final ThingTypeUID THING_TYPE_SURFACE_LIGHT_TW = new ThingTypeUID(BINDING_ID, "Surface_Light_TW");
    public static final ThingTypeUID THING_TYPE_ZLL_LIGHT = new ThingTypeUID(BINDING_ID, "ZLL_Light");
    public static final ThingTypeUID THING_TYPE_PAR16_50_TW = new ThingTypeUID(BINDING_ID, "PAR16_50_TW");
    public static final ThingTypeUID THING_TYPE_PAR16_50_RGBW = new ThingTypeUID(BINDING_ID, "PAR_16_50_RGBW___LIGHTIFY");
    public static final ThingTypeUID THING_TYPE_CLASSIC_B40_TW = new ThingTypeUID(BINDING_ID, "Classic_B40_TW___LIGHTIFY");
    public static final ThingTypeUID THING_TYPE_CLASSIC_A60_TW = new ThingTypeUID(BINDING_ID, "Classic_A60_TW");
    public static final ThingTypeUID THING_TYPE_FLS_H3 = new ThingTypeUID(BINDING_ID, "FLS_H3");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_OUTDOOR_FLEX_RGBW = new ThingTypeUID(BINDING_ID, "LIGHTIFY_Outdoor_Flex_RGBW");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GARDENPOLE_RGBW_LIGHTIFY = new ThingTypeUID(BINDING_ID, "Gardenpole_RGBW_Lightify");
    public static final ThingTypeUID THING_TYPE_FLS_PP3 = new ThingTypeUID(BINDING_ID, "FLS_PP3");
    public static final ThingTypeUID THING_TYPE_FLEX_RGBW = new ThingTypeUID(BINDING_ID, "Flex_RGBW");
    //Paul Neuhaus Things
    public static final ThingTypeUID THING_TYPE_PN_NLG_CCT = new ThingTypeUID(BINDING_ID, "NLG_CCT_light_");
    public static final ThingTypeUID THING_TYPE_PN_NLG_RGBW = new ThingTypeUID(BINDING_ID, "NLG_RGBW_light_");
    public static final ThingTypeUID THING_TYPE_PN_JZD60_J4W150 = new ThingTypeUID(BINDING_ID, "JZD60_J4W150");
    public static final ThingTypeUID THING_TYPE_PN_JZD60_J4R150 = new ThingTypeUID(BINDING_ID, "JZD60_J4R150");
    public static final ThingTypeUID THING_TYPE_PN_JZ_RGBW_Z01 = new ThingTypeUID(BINDING_ID, "JZ_RGBW_Z01");
    //<--- ESH-Mod see IBN-638 <---

    //Thing Type UID for hue scene
    public static final ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, "scene");

    // List all channels
    public static final String CHANNEL_COLORTEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ALERT = "alert";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_DIMMER_SWITCH = "dimmer_switch";
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_LAST_UPDATED = "last_updated";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";
    public static final String CHANNEL_LIGHT_LEVEL = "light_level";
    public static final String CHANNEL_DARK = "dark";
    public static final String CHANNEL_DAYLIGHT = "daylight";

    // List all triggers
    public static final String EVENT_DIMMER_SWITCH = "dimmer_switch_event";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";

    // Light config properties
    public static final String LIGHT_ID = "lightId";
    public static final String SENSOR_ID = "sensorId";
    public static final String SCENE_ID = "sceneId";
    public static final String PRODUCT_NAME = "productName";
    public static final String UNIQUE_ID = "uniqueId";
}
