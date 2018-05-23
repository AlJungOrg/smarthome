package org.eclipse.smarthome.persistence.mapdb

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty
import static org.hamcrest.collection.IsEmptyCollection.empty
import static org.hamcrest.collection.IsIterableContainingInOrder.contains
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.ConfigConstants
import org.eclipse.smarthome.core.library.items.ColorItem
import org.eclipse.smarthome.core.library.items.DimmerItem
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.HSBType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.PercentType
import org.eclipse.smarthome.core.persistence.FilterCriteria
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test

/**
 * 
 * @author Martin Kühl - Initial contribution
 */
class MapDbPersistenceServiceOSGiTest extends OSGiTest {
    static File userDataFolder = new File(ConfigConstants.userDataFolder)

    QueryablePersistenceService persistenceService

    @Before
    void setUp() {
        registerVolatileStorageService()
        persistenceService = getService(QueryablePersistenceService)
    }

    @After
    void tearDown() {
        unregisterService(persistenceService)
    }

    @AfterClass
    static void cleanUp() {
        userDataFolder.deleteDir()
    }

    @Test
    void "assert store stores the Item"() {
        def name = "switch1"
        def alias = "switch2"
        def state = OnOffType.ON

        def item = new SwitchItem(name)
        item.setState(state)

        assertThat persistenceService.getItemInfo(), is(empty())

        persistenceService.store(item)

        assertThat persistenceService.getItemInfo(),
                contains(hasProperty("name", equalTo(name)))

        persistenceService.store(item, alias)

        assertThat persistenceService.getItemInfo(),
                containsInAnyOrder(hasProperty("name", equalTo(name)), hasProperty("name", equalTo(alias)))
    }

    @Test
    void "assert query finds a stored Item by name"() {
        def name = "dimmer"
        def state = PercentType.HUNDRED

        def item = new DimmerItem(name)
        item.setState(state)

        def filter = new FilterCriteria()
        filter.setItemName(name)

        assertThat persistenceService.query(filter), is(empty())

        persistenceService.store(item)

        assertThat persistenceService.query(filter),
                contains(allOf(hasProperty("name", equalTo(name)), hasProperty("state", equalTo(state))))
    }

    @Test
    void "assert query finds a stored Item by alias"() {
        def name = "color"
        def alias = "alias"
        def state = HSBType.GREEN

        def item = new ColorItem(name)
        item.setState(state)

        def filterByName = new FilterCriteria()
        filterByName.setItemName(name)

        def filterByAlias = new FilterCriteria()
        filterByAlias.setItemName(alias)

        assertThat persistenceService.query(filterByName), is(empty())
        assertThat persistenceService.query(filterByAlias), is(empty())

        persistenceService.store(item, alias)

        assertThat persistenceService.query(filterByName), is(empty())
        assertThat persistenceService.query(filterByAlias),
                contains(allOf(hasProperty("name", equalTo(alias)), hasProperty("state", equalTo(state))))
    }
}
