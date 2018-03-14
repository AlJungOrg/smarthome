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
package org.eclipse.smarthome.binding.astro.internal.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for the rise and set ranges.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class RiseSet {

    protected Map<SunPhaseName, Range> ranges = new HashMap<SunPhaseName, Range>();

    /**
     * Returns the rise range.
     */
    public Range getRise() {
        return ranges.get(SunPhaseName.SUN_RISE);
    }

    /**
     * Sets the rise range.
     */
    public void setRise(Range rise) {
        ranges.put(SunPhaseName.SUN_RISE, rise);
    }

    /**
     * Returns the set range.
     */
    public Range getSet() {
        return ranges.get(SunPhaseName.SUN_SET);
    }

    /**
     * Sets the set range.
     */
    public void setSet(Range set) {
        ranges.put(SunPhaseName.SUN_SET, set);
    }

}
