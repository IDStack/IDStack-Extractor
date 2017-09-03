package org.idstack.extractor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

/**
 * @author Chandu Herath
 * @date 30/08/2017
 * @since 1.0
 */

public class JsonCreator {

    public String constructAsNestedJson(String receivedJson) {
        JsonObject obj = new JsonParser().parse(receivedJson).getAsJsonObject();
        LinkedHashMap<String, Object> finalVersion = constructJson(obj);
        String jsonStringToSign = new Gson().toJson(finalVersion);
        return jsonStringToSign;
    }

    private LinkedHashMap<String, Object> constructJson(JsonObject obj) {
        Set<Map.Entry<String, JsonElement>> set = obj.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            if (key.contains(":")) {
                String[] keyArray = key.split(":");
                getNestedHashMap(keyArray, map, entry.getValue());
            } else {
                map.put(key, entry.getValue());
            }

        }
        return map;
    }

    private LinkedHashMap<String, Object> getNestedHashMap(String[] keyArray, LinkedHashMap<String, Object> originalMap, JsonElement value) {
        if (originalMap.containsKey(keyArray[0])) {
            LinkedHashMap<String, Object> map1 = (LinkedHashMap<String, Object>) originalMap.get(keyArray[0]);
            String[] childArray = Arrays.copyOfRange(keyArray, 1, keyArray.length);
            getNestedHashMap(childArray, map1, value);
        } else {
            if (keyArray.length != 1) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                String keyValue = keyArray[0];
                String[] childArray = Arrays.copyOfRange(keyArray, 1, keyArray.length);
                originalMap.put(keyValue, getNestedHashMap(childArray, map, value));
            } else {
                originalMap.put(keyArray[0], value);
            }
        }
        return originalMap;
    }
}
