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
package org.eclipse.smarthome.binding.hue.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.Scene;
import org.eclipse.smarthome.binding.hue.internal.FullSensor;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;

/**
 * The {@link SceneStatusListener} is notified when a scene status has changed or a scene has been removed or added.
 *
 * @author MAW - Initial contribution
 */
@NonNullByDefault
public interface SceneStatusListener {

    /**
     * This method is called whenever the state of the given scene has changed. The new state can be obtained by
     * {@link Scene#getState()}.
     *
     * @param bridge The bridge the changed scene is connected to.
     * @param scene The scene which received the state update.
     */
    void onSceneStateChanged(@Nullable HueBridge bridge, Scene scene);

    /**
     * This method is called whenever a scene is removed.
     *
     * @param bridge The bridge the removed scene was connected to.
     * @param scene The removed scene
     */
    void onSceneRemoved(@Nullable HueBridge bridge, Scene scene);

    /**
     * This method is called whenever a scene is added.
     *
     * @param bridge The bridge the added scene was connected to.
     * @param scene The added scene
     */
    void onSceneAdded(@Nullable HueBridge bridge, Scene scene);
}
