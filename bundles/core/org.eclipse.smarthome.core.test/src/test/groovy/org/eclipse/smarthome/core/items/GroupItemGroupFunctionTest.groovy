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
import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
class GroupItemGroupFunctionTest {
    
    @Test
    void 'assert ON on XOR with only one member'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("XOR;ON;OFF")
        GroupItem groupItem = new GroupItem("root", new SwitchItem("x"), groupFunction)
        groupItem.addMember(new SwitchItem("member1"))
        groupItem.getMembers().getAt(0).setState(OnOffType.ON)
        assertEquals(OnOffType.ON, groupItem.getState())
        groupItem.getMembers().getAt(0).setState(OnOffType.OFF)
        assertEquals(OnOffType.OFF, groupItem.getState())
    }
    
    @Test
    void 'assert correct XOR results'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("XOR;ON;OFF")
        GroupItem groupItem = new GroupItem("root", new SwitchItem("x"), groupFunction)
        
        groupItem.addMember(new SwitchItem("member1"))
        groupItem.addMember(new SwitchItem("member2"))
        groupItem.addMember(new SwitchItem("member3"))
        
        for (SwitchItem item : groupItem.getMembers()) {
            item.setState(OnOffType.OFF)
        }
        assertEquals(OnOffType.OFF, groupItem.getState())
        
        groupItem.getMembers().getAt(0).setState(OnOffType.ON)
        assertEquals(OnOffType.ON, groupItem.getState())
        
        groupItem.getMembers().getAt(1).setState(OnOffType.ON)
        assertEquals(OnOffType.OFF, groupItem.getState())
        
        groupItem.getMembers().getAt(2).setState(OnOffType.ON)
        assertEquals(OnOffType.OFF, groupItem.getState())
    }
    
    @Test
    void 'assert correct XOR results - open closed'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("XOR;OPEN;CLOSED")
        GroupItem groupItem = new GroupItem("root", null, groupFunction)
        
        groupItem.addMember(new SwitchItem("member1"))
        groupItem.addMember(new SwitchItem("member2"))
        groupItem.addMember(new SwitchItem("member3"))
        groupItem.addMember(new SwitchItem("member4"))
        
        for (SwitchItem item : groupItem.getMembers()) {
            item.setState(OpenClosedType.CLOSED)
        }
        assertEquals(OpenClosedType.CLOSED, groupItem.getState())
        
        groupItem.getMembers().getAt(1).setState(OpenClosedType.OPEN)
        assertEquals(OpenClosedType.OPEN, groupItem.getState())
        
        groupItem.getMembers().getAt(1).setState(OpenClosedType.CLOSED)
        assertEquals(OpenClosedType.CLOSED, groupItem.getState())
        
        groupItem.getMembers().getAt(2).setState(OpenClosedType.OPEN)
        assertEquals(OpenClosedType.OPEN, groupItem.getState())
        
        groupItem.getMembers().getAt(2).setState(OpenClosedType.OPEN)
        groupItem.getMembers().getAt(3).setState(OpenClosedType.OPEN)
        
        assertEquals(OpenClosedType.CLOSED, groupItem.getState())
    }
    
    @Test
    void 'assert correct OR results'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("OR;ON;OFF")
        GroupItem groupItem = new GroupItem("root", null, groupFunction)
        
        groupItem.addMember(new SwitchItem("member1"))
        groupItem.addMember(new SwitchItem("member2"))
        groupItem.addMember(new SwitchItem("member3"))
        
        for (SwitchItem item : groupItem.getMembers()) {
            item.setState(OnOffType.OFF)
        }
        assertEquals(OnOffType.OFF, groupItem.getState())
        
        groupItem.getMembers().getAt(0).setState(OnOffType.ON)
        assertEquals(OnOffType.ON, groupItem.getState())
        
        groupItem.getMembers().getAt(1).setState(OnOffType.ON)
        assertEquals(OnOffType.ON, groupItem.getState())
        
        groupItem.getMembers().getAt(0).setState(OnOffType.OFF)
        groupItem.getMembers().getAt(1).setState(OnOffType.OFF)
        assertEquals(OnOffType.OFF, groupItem.getState())
    }
    
    @Test
    void 'assert correct AND results'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("AND;ON;OFF")
        GroupItem groupItem = new GroupItem("root", null, groupFunction)
        
        groupItem.addMember(new SwitchItem("member1"))
        groupItem.addMember(new SwitchItem("member2"))
        groupItem.addMember(new SwitchItem("member3"))
        
        for (SwitchItem item : groupItem.getMembers()) {
            item.setState(OnOffType.OFF)
        }
        assertEquals(OnOffType.OFF, groupItem.getState())
        
        groupItem.getMembers().getAt(0).setState(OnOffType.ON)
        assertEquals(OnOffType.OFF, groupItem.getState())
        
        groupItem.getMembers().getAt(1).setState(OnOffType.ON)
        assertEquals(OnOffType.OFF, groupItem.getState())
        
        groupItem.getMembers().getAt(2).setState(OnOffType.ON)
        assertEquals(OnOffType.ON, groupItem.getState())
        
        groupItem.getMembers().getAt(0).setState(OnOffType.OFF)
        assertEquals(OnOffType.OFF, groupItem.getState())
    }
    
    @Test
    @Ignore("No delegations work - need eventpublisher?")
    void 'assert groupItem forwards state'() {
        GroupFunction groupFunction = GroupFunctionFactory.create("OR;ON;OFF")
        GroupItem groupItem = new GroupItem("root", new SwitchItem("x"), groupFunction)
        
        groupItem.addMember(new SwitchItem("member1"))
        groupItem.addMember(new SwitchItem("member2"))
        groupItem.addMember(new SwitchItem("member3"))
        
        for (SwitchItem item : groupItem.getMembers()) {
            item.setState(OnOffType.OFF)
        }
        assertEquals(OnOffType.OFF, groupItem.getState())
        
        groupItem.setState(OnOffType.ON)
        assertEquals(OnOffType.ON, groupItem.getState())
        assertEquals(OnOffType.ON, groupItem.getMembers().getAt(0).getState())
        assertEquals(OnOffType.ON, groupItem.getMembers().getAt(1).getState())
        assertEquals(OnOffType.ON, groupItem.getMembers().getAt(2).getState())
        
        groupItem.setState(OnOffType.OFF)
        assertEquals(OnOffType.OFF, groupItem.getState())
        assertEquals(OnOffType.OFF, groupItem.getMembers().getAt(0).getState())
        assertEquals(OnOffType.OFF, groupItem.getMembers().getAt(1).getState())
        assertEquals(OnOffType.OFF, groupItem.getMembers().getAt(2).getState())
    }

}
