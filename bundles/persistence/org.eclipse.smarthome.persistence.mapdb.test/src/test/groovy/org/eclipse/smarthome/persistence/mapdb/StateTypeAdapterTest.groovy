package org.eclipse.smarthome.persistence.mapdb

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.library.types.HSBType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.PercentType
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.persistence.mapdb.internal.StateTypeAdapter
import org.junit.Test

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * 
 * @author Martin Kühl - Initial contribution
 */
class StateTypeAdapterTest {
    Gson mapper = new GsonBuilder()
        .registerTypeHierarchyAdapter(State, new StateTypeAdapter())
        .create()

    @Test
    void "assert read-write roundtrip recreates the written State"() {
        assertThat roundtrip(OnOffType.ON), is(equalTo(OnOffType.ON))
        assertThat roundtrip(PercentType.HUNDRED), is(equalTo(PercentType.HUNDRED))
        assertThat roundtrip(HSBType.GREEN), is(equalTo(HSBType.GREEN))
        assertThat roundtrip(StringType.valueOf("test")), is(equalTo(StringType.valueOf("test")))
    }

    private State roundtrip(State state) {
        return mapper.fromJson(mapper.toJson(state), State)
    }
}
