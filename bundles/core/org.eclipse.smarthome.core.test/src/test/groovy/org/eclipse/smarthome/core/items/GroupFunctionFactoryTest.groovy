/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.util.Set
import javax.management.InstanceOfQueryExp
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.events.GroupItemStateChangedEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.library.GroupFunctionFactory;
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test


/**
 * The GroupItemTest tests functionality of the GroupFunctions.  
 * 
 * @author Sebastian Janzen - Initial contribution  
 */
class GroupFunctionFactoryTest {
    
    def twoParamSuffixInvalid = [";ON;", ";ON;INVALID", "", ";;", ";ON;OFF;CLOSE;OPEN"]
    def twoParamSuffixValid = [";ON;OFF", ";OPEN;CLOSED"]
    
    def countParamSuffixInvalid = [";..", ";;", ""]
    def countParamSuffixValid = [";.", ";[5-9]", ";.*", ";(.*)"]

    @Test
    void 'expect invalid XOR GroupFunction expression'() {
        twoParamSuffixInvalid.each { param -> 
            GroupFunction groupFunction = GroupFunctionFactory.create("XOR" + param)
            assertEquals("EQUALITY", groupFunction.toString())
        }
    }
    
    @Test
    void 'expect valid XOR GroupFunction expression'() {
        twoParamSuffixValid.each { param ->
            GroupFunction groupFunction = GroupFunctionFactory.create("XOR" + param)
            assertEquals("XOR" + param, groupFunction.toString())
        }
    }
    
    @Test
    void 'expect invalid AND GroupFunction expression'() {
        twoParamSuffixInvalid.each { param ->
            GroupFunction groupFunction = GroupFunctionFactory.create("AND" + param)
            assertEquals("EQUALITY", groupFunction.toString())
        }
    }
    
    @Test
    void 'expect valid AND GroupFunction expression'() {
        twoParamSuffixValid.each { param ->
            GroupFunction groupFunction = GroupFunctionFactory.create("AND" + param)
            assertEquals("AND" + param, groupFunction.toString())
        }
    }
    
    @Test
    void 'expect invalid OR GroupFunction expression'() {
        twoParamSuffixInvalid.each { param ->
            GroupFunction groupFunction = GroupFunctionFactory.create("OR" + param)
            assertEquals("EQUALITY", groupFunction.toString())
        }
    }
    
    @Test
    void 'expect valid OR GroupFunction expression'() {
        twoParamSuffixValid.each { param ->
            GroupFunction groupFunction = GroupFunctionFactory.create("OR" + param)
            assertEquals("OR" + param, groupFunction.toString())
        }
    }
    
    
    @Test
    void 'expect invalid COUNT GroupFunction expression'() {
        countParamSuffixInvalid.each { param ->
            GroupFunction groupFunction = GroupFunctionFactory.create("COUNT" + param)
            assertEquals("EQUALITY", groupFunction.toString())
        }
    }
    
    @Test
    void 'expect valid EQUALITY GroupFunction expression'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("EQUALITY")
        assertEquals("EQUALITY", groupFunction.toString())
    }
    
    
    @Test
    @Ignore("ATM These are not supported, GroupFunctionFactory does not handle params like regex correctly")
    void 'expect valid COUNT GroupFunction expression'() {
        countParamSuffixValid.each { param ->
            GroupFunction groupFunction = GroupFunctionFactory.create("COUNT" + param)
            assertEquals("COUNT" + param, groupFunction.toString())
        }
    }
    
    @Test
    @Ignore("ATM These are not supported, GroupFunctionFactory does not handle params like regex correctly")
    void 'expect fallback to Equality on wrong GroupFunction without param - positive'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("AVG")
        assertEquals("AVG", groupFunction.toString())
    }
    
    @Test
    @Ignore("ATM These are not supported, GroupFunctionFactory does not handle params like regex correctly")
    void 'expect valid SUM GroupFunction without param'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("SUM")
        assertEquals("SUM", groupFunction.toString())
    }
    
}
