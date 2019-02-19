/**
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.persistence.config;

/**
 * This class represents the configuration that is used for tag items.
 *
 * @author Martin Kühl - Initial contribution and API
 */
public class SimpleTagConfig extends SimpleConfig {

    private final String tag;

    public SimpleTagConfig(final String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return String.format("%s [tag=%s]", getClass().getSimpleName(), tag);
    }

}
