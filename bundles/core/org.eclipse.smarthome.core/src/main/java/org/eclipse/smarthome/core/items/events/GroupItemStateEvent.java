package org.eclipse.smarthome.core.items.events;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link GroupItemStateEvent}s can be used to deliver groupitem status updates through the Eclipse SmartHome event bus.
 * State events must be created with the {@link ItemEventFactory}.
 *
 * @author MAW
 *
 */
public class GroupItemStateEvent extends AbstractEvent {

    public final static String TYPE = ItemStateEvent.class.getSimpleName();

    private final String itemName;

    private final State itemState;

    /**
     * Constructs a new groupitem state event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param itemName the item name
     * @param itemState the item state
     * @param source the source, can be null
     */
    protected GroupItemStateEvent(String topic, String payload, String itemName, State itemState, String source) {
        super(topic, payload, source);
        this.itemName = itemName;
        this.itemState = itemState;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Gets the item name.
     *
     * @return the item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Gets the item state.
     *
     * @return the item state
     */
    public State getItemState() {
        return itemState;
    }

    @Override
    public String toString() {
        return String.format("%s updated to %s", itemName, itemState);
    }
}
