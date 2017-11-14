/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.typesystem.internal.FeatureScopeTracker;
import org.eclipse.xtext.xbase.typesystem.internal.IFeatureScopeTracker;
import org.eclipse.xtext.xbase.typesystem.internal.OptimizingFeatureScopeTrackerProvider;

/**
 * {@link OptimizingFeatureScopeTrackerProvider} implementation
 *
 * ...with a workaround for https://github.com/eclipse/xtext-extras/issues/144
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class OptimizingFeatureScopeTrackerProvider2 extends OptimizingFeatureScopeTrackerProvider {

    @Override
    public IFeatureScopeTracker track(EObject root) {
        return new FeatureScopeTracker() {
        };
    }

}