package org.eclipse.smarthome.core.items.events;

import org.eclipse.smarthome.core.types.State;

/**
 * {@link GroupItemStateEvent}s can be used to deliver groupitem status updates through the Eclipse SmartHome event bus.
 * State events must be created with the {@link ItemEventFactory}.
 *
 * @author MAW
 *
 */
public class GroupItemStateEvent extends ItemStateEvent {

    public final static String TYPE = GroupItemStateEvent.class.getSimpleName();

    private final String memberName;

    /**
     * Constructs a new groupitem state event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param itemName the item name
     * @param itemState the item state
     * @param source the source, can be null
     */
    protected GroupItemStateEvent(String topic, String payload, String itemName, String memberName, State itemState, String source) {
        super(topic, payload, itemName, itemState, source);
        this.memberName = memberName;
    }

    /**
     * @return the name of the changed group member
     */
    public String getMemberName() {
        return this.memberName;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
