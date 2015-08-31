package org.eclipse.smarthome.core.thing.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;

public class DTOUtil {
	
    public static Map<String, Object> toMap(Configuration configuration) {

        if (configuration == null) {
            return null;
        }

        Map<String, Object> configurationMap = new HashMap<>(configuration.keySet().size());
        for (String key : configuration.keySet()) {
            configurationMap.put(key, configuration.get(key));
        }
        return configurationMap;
    }
    
}
