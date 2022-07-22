package jmu.reu.ode;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that automates some basic operation of a map for our SeriesSettings.  Aiding in code 
 * organization.
 * 
 * @author Benjamin Huber
 * @version 7/14/2022
 */
public class SeriesSettingsMap {
    private Map<String, SeriesSettings> map;

    /**
     * Default constructor.
     */
    public SeriesSettingsMap() {
        map = new HashMap<>();
    }

    /**
     * Gets the settings at a certain key.  If it doesn't exist, it constructs a SeriesSettings 
     * object and inserts it into the map, and then returns it.
     * 
     * @param key the key to get the settings at.
     * @return the SeriesSettings object
     */
    public SeriesSettings getSettings(String key) {
        if (!map.containsKey(key)) {
            map.put(key, new SeriesSettings());
        }
        return map.get(key);
    }
}
