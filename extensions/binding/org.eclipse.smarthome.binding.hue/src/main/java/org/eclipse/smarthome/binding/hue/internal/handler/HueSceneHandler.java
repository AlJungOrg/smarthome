package org.eclipse.smarthome.binding.hue.internal.handler;

import static org.eclipse.smarthome.binding.hue.internal.HueBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.Scene;
import org.eclipse.smarthome.binding.hue.internal.SceneConfigUpdate;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Scene Handler
 *
 * @author MAW
 */
public class HueSceneHandler extends BaseThingHandler implements SceneStatusListener {

    private @NonNullByDefault({}) String sceneId;

    private final Logger logger = LoggerFactory.getLogger(HueSceneHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_SCENE).
        collect(Collectors.toSet());

    private @Nullable HueClient hueClient;

    public HueSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue scene handler.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configSceneId = (String) getConfig().get(SCENE_ID);
        if (configSceneId != null) {
            sceneId = configSceneId;
            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getHueClient() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-scene-id");
        }
    }

    protected synchronized @Nullable HueClient getHueClient() {
        if (hueClient == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueClient) {
                hueClient = (HueClient) handler;
                hueClient.registerSceneStatusListener(this);
            } else {
                return null;
            }
        }
        return hueClient;
    }

    @Override
    public void dispose() {
        logger.debug("Hue scene handler disposes. Unregistering listener.");
        if (sceneId != null) {
            HueClient bridgeHandler = getHueClient();
            if (bridgeHandler != null) {
                bridgeHandler.unregisterSceneStatusListener(this);
                hueClient = null;
            }
            sceneId = null;
        }
    }

    private @Nullable Scene getScene() {
        HueClient bridgeHandler = getHueClient();
        if (bridgeHandler != null) {
            return bridgeHandler.getSceneById(sceneId);
        }
        return null;
    }

    @Override
    public void onSceneStateChanged(@Nullable HueBridge bridge, Scene scene) {
        // nothing to do
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        HueClient handler = getHueClient();
        if (handler != null) {
            Scene scene = handler.getSceneById(sceneId);
            if (scene != null) {
                onSceneStateChanged(null, scene);
            }
        }
    }

    @Override
    public void onSceneAdded(@Nullable HueBridge bridge, Scene scene) {
        if (scene.getId().equals(sceneId)) {
            onSceneStateChanged(bridge, scene);
        }
    }

    @Override
    public void onSceneRemoved(@Nullable HueBridge bridge, Scene scene) {
        if (scene.getId().equals(sceneId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "offline.scene-removed");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        HueClient hueBridge = getHueClient();
        if (hueBridge == null) {
            logger.warn("hue bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        Scene scene = getScene();
        if (scene == null) {
            logger.debug("hue scene not known on bridge. Cannot handle command.");
            return;
        }

        SceneConfigUpdate sceneUpdate = null;
        if (channelUID.getId() != null) {
            logger.trace("handling command {}", command);
            sceneUpdate = new SceneConfigUpdate().setScene(scene);
        } else {
            logger.debug("Scene channel has no ID.");
        }
        if (sceneUpdate != null) {
            hueBridge.updateSceneConfig(scene, sceneUpdate);
        } else {
            logger.warn("Command sent to an unknown channel id: {}", channelUID);
        }
    }
}
