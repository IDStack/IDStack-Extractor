package org.idstack.extractor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Sachithra Dangalla
 * @date 10/14/2017
 * @since 1.0
 */
public class DocParserHandler {
    private String SECRET_KEY = "fd718fbca8e7abf39a02f4eb032db8f9b6f50a1c";
    private String PASSWORD = "";

    private String UPLOAD_DOC_URL = "https://api.docparser.com/v1/document/fetch/";
    private String GET_DATA_URL = "https://api.docparser.com/v1/results/";

    private String getAuthorizationKey() throws UnsupportedEncodingException {
        String authString = SECRET_KEY + ":" + PASSWORD;
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes("UTF-8"));
        String authStringEnc = new String(authEncBytes);
        return "Basic " + authStringEnc;
    }

    /**
     * uploads document to docparser and returns id
     *
     * @param documentURL eg - https://sachi-d.github.io/4296817/PDFTextExtractor/3Passport.pdf
     * @throws IOException
     */
    private String uploadDocumentToDocParser(FeatureImpl feature, String documentURL, String parserID) throws IOException {
        //TODO update to upload local documents 1-replace fetch with upload, 2-replace url with file
        HashMap<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Authorization", getAuthorizationKey());
        String body = "{}";

        String response = feature.callAPI(UPLOAD_DOC_URL + parserID + "?url=" + documentURL, "POST", requestProperties, body);
        JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
        String documentID = obj.get("id").getAsString();
        return documentID;
    }

    /**
     * Uploads the document to docparser and get extracted information as a formatted JSON
     *
     * @param documentURL
     * @param parserType  eg: PASSPORT, NIC, STUDENT_ID
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getExtractedDocument(FeatureImpl feature, String documentURL, String parserType) throws IOException, InterruptedException {


        String parserID = Constant.Extractor.DOCPARSER_PARSERS.get(parserType);
        String documentID = uploadDocumentToDocParser(feature, documentURL, parserID);

        HashMap<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Authorization", getAuthorizationKey());
        String body = "{}";


        //TODO keep a map of docURL vs docID and check if same URL appears again
        //Temp update document ID with previous docID
        Map<String, String> DOCPARSER_TEMP_DOCID = new HashMap();
        DOCPARSER_TEMP_DOCID.put(Constant.DocumentType.PASSPORT, "5f32c93215c83b25601944983113ec76");
        DOCPARSER_TEMP_DOCID.put(Constant.DocumentType.UNIVERSITY_ID, "15834ca8741af3fa6d71774ceb86c778");
        DOCPARSER_TEMP_DOCID.put(Constant.DocumentType.TRANSCRIPT, "a1dbfc221fbae1e953744476afc5ec01");
        documentID = DOCPARSER_TEMP_DOCID.get(parserType);

        //wait for 10 seconds
//        TimeUnit.SECONDS.sleep(15);

        //call get data API
        String response = feature.callAPI(GET_DATA_URL + parserID + "/" + documentID + "?format=flat", "GET", requestProperties, body);
        String formattedResponse = getFormattedResponse(response);
        return formattedResponse;
    }

    private String getFormattedResponse(String json) {
        JsonObject obj = new JsonParser().parse(json).getAsJsonArray().get(0).getAsJsonObject();
        JsonObject result = new JsonObject();

        Set<Map.Entry<String, JsonElement>> set = obj.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                if (key.contains("_0_formatted")) {
                    key = key.split("_0_formatted")[0];
                }
                result.add(key, value);
            }
        }

        return result.toString();
    }
}
