package org.eclipse.smarthome.binding.hue.internal;

/**
 * Collection of updates to the scene configuration.
 *
 * @author MAW - Initial contribution
 */
public class SceneConfigUpdate extends ConfigUpdate {
    /**
     *
     * @param Scene
     * @return SceneConfigUpdate
     */
    public SceneConfigUpdate setScene(Scene scene) {
        commands.add(new Command("scene", scene.getId()));
        return this;
    }
}
