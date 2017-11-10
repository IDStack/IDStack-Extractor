package org.idstack.extractor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.document.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Chandu Herath
 * @date 30/08/2017
 * @since 1.0
 */

public class JsonBuilder {

    public String constructJson(String receivedJson, String pdf, FeatureImpl feature) {
        JsonObject obj = new JsonParser().parse(receivedJson).getAsJsonObject();
        JsonObject metadataObject = obj.getAsJsonObject(Constant.JsonAttribute.META_DATA);
        JsonObject contentObject = obj.getAsJsonObject(Constant.JsonAttribute.CONTENT);

        //create metadata object
        MetaData metaData = feature.getMetaData(metadataObject.get(Constant.JsonAttribute.MetaData.DOCUMENT_TYPE).getAsString(), pdf);

        //create linked hash map
        LinkedHashMap<String, String> contentMap = new Gson().fromJson(
                contentObject, new TypeToken<LinkedHashMap<String, Object>>() {
                }.getType()
        );

        //create empty extractor
        Extractor extractor = new Extractor("", new Signature("", ""));

        //create empty validator array
        ArrayList<Validator> validators = new ArrayList<>();

        Document doc = new Document(metaData, contentMap, extractor, validators);

        String jsonStringToSign = new Gson().toJson(doc);

        return jsonStringToSign;
    }

//    private LinkedHashMap<String, String> constructJsonContent(JsonObject obj) {
//        Set<Map.Entry<String, JsonElement>> set = obj.entrySet();
//        Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
//        LinkedHashMap<String, String> map = new LinkedHashMap<>();
//        while (iterator.hasNext()) {
//            Map.Entry<String, JsonElement> entry = iterator.next();
//            String key = entry.getKey();
//            if (key.contains(":")) {
//                String[] keyArray = key.split(":");
//                getNestedHashMap(keyArray, map, entry.getValue());
//            } else {
//                map.put(key, entry.getValue());
//            }
//
//        }
//        return map;
//    }

//    private LinkedHashMap<String, Object> getNestedHashMap(String[] keyArray, LinkedHashMap<String, Object> originalMap, JsonElement value) {
//        if (originalMap.containsKey(keyArray[0])) {
//            LinkedHashMap<String, Object> map1 = (LinkedHashMap<String, Object>) originalMap.get(keyArray[0]);
//            String[] childArray = Arrays.copyOfRange(keyArray, 1, keyArray.length);
//            getNestedHashMap(childArray, map1, value);
//        } else {
//            if (keyArray.length != 1) {
//                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//                String keyValue = keyArray[0];
//                String[] childArray = Arrays.copyOfRange(keyArray, 1, keyArray.length);
//                originalMap.put(keyValue, getNestedHashMap(childArray, map, value));
//            } else {
//                originalMap.put(keyArray[0], value);
//            }
//        }
//        return originalMap;
//    }
//
//    public String includeHashInJson(String jsonString, String hash) {
//        Document digitalDocument = Parser.parseDocumentJson(jsonString);
//        digitalDocument.getMetaData().setPdfHash(hash);
//        return new Gson().toJson(digitalDocument);
//    }
}
