package org.idstack.extractor.api.controller;

import com.google.gson.Gson;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chanaka Lakmal
 * @date 31/8/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    Router router;

    @Autowired
    private FeatureImpl feature;

    @Value(value = "classpath:" + Constant.Configuration.SYSTEM_PROPERTIES_FILE_NAME)
    private Resource resource;

    private String apiKey;
    private String configFilePath;
    private String pvtCertFilePath;
    private String pvtCertType;
    private String pvtCertPasswordType;
    private String pubCertFilePath;
    private String pubCertType;
    private String storeFilePath;
    private String pubFilePath;
    private String tmpFilePath;

    @PostConstruct
    void init() throws IOException {
        apiKey = feature.getProperty(resource.getInputStream(), Constant.Configuration.API_KEY);
        configFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.CONFIG_FILE_PATH);
        pvtCertFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.PVT_CERTIFICATE_FILE_PATH);
        pvtCertType = feature.getProperty(resource.getInputStream(), Constant.Configuration.PVT_CERTIFICATE_TYPE);
        pvtCertPasswordType = feature.getProperty(resource.getInputStream(), Constant.Configuration.PVT_CERTIFICATE_PASSWORD_TYPE);
        pubCertFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.PUB_CERTIFICATE_FILE_PATH);
        pubCertType = feature.getProperty(resource.getInputStream(), Constant.Configuration.PUB_CERTIFICATE_TYPE);
        storeFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.STORE_FILE_PATH);
        pubFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.PUB_FILE_PATH);
        tmpFilePath = feature.getProperty(resource.getInputStream(), Constant.Configuration.TEMP_FILE_PATH);
    }

    @RequestMapping(value = {"/", "/{version}", "/{version}/{apikey}"})
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://docs.idstack.apiary.io/");
    }

    /**
     * Save the configurations received at the configured URL at idstack.properties file
     *
     * @param version api version
     * @param apikey  api key
     * @param json    json of configuration
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/saveconfig/{type}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @RequestBody String json) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        switch (type) {
            case Constant.Configuration.BASIC_CONFIG:
                return feature.saveBasicConfiguration(configFilePath, json);
            case Constant.Configuration.AWS_CONFIG:
                return feature.saveAWSConfiguration(configFilePath, json);
            default:
                return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER));
        }
    }

    /**
     * Return the saved configurations for the given type
     *
     * @param version api version
     * @param apikey  api key
     * @return configuration as json
     */
    @RequestMapping(value = {"/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        return new Gson().toJson(feature.getConfiguration(configFilePath, router.getConfigFileName(type)));
    }

    /**
     * Save public certificate of the extractor
     *
     * @param version     api version
     * @param apikey      api key
     * @param certificate public certificate file
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        return feature.savePublicCertificate(certificate, configFilePath, pubCertFilePath, pubCertType);
    }

    /**
     * Save private certificate of the extractor
     *
     * @param version     api version
     * @param apikey      api key
     * @param certificate private certificate
     * @param password    password of private certificate
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        if (password.isEmpty()) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        }
        return feature.savePrivateCertificate(certificate, password, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType);
    }

    /**
     * Create machine readable document after extracting
     *
     * @param version   api version
     * @param apikey    api key
     * @param json      json of extracted data
     * @param requestId request id
     * @return signed json + pdf documents
     */
    @RequestMapping(value = "/{version}/{apikey}/extract", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String extractDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "json") String json, @RequestParam(value = "request_id") String requestId) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        if (requestId.isEmpty()) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        }
        return router.extractDocument(feature, json, requestId, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType, tmpFilePath, storeFilePath, pubFilePath).replaceAll(pubFilePath, File.separator);
    }

    /**
     * Get the document types that protocol facilitates
     *
     * @param version api version
     * @param apikey  api key
     * @return document types
     */
    @RequestMapping(value = "/{version}/{apikey}/getdoctypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDocumentTypeList(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        return feature.getDocumentTypes();
    }

    /**
     * Get the stored documents in the configured store path
     *
     * @param version api version
     * @param apikey  api key
     * @return document list
     */
    @RequestMapping(value = "/{version}/{apikey}/getdocstore", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getStoredDocuments(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        return feature.getDocumentStore(storeFilePath, configFilePath, true).replaceAll(pubFilePath, File.separator);
    }

    /**
     * Get the parsed json document by docparser
     *
     * @param version api version
     * @param apikey  api key
     * @return parsed json by docparser
     */
    @RequestMapping(value = "/{version}/{apikey}/docparse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String parseDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "pdf") String pdfUrl, @RequestParam(value = "doc_type") String documentType) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        if (pdfUrl.isEmpty() || documentType.isEmpty()) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        }
        return router.parseDocument(feature, pdfUrl, documentType);
    }

    @RequestMapping(value = "/{version}/{apikey}/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "pdf") final MultipartFile pdf) throws IOException {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        return router.saveDocument(feature, pdf, configFilePath, tmpFilePath);
    }

    @RequestMapping(value = "/{version}/{apikey}/cleardocstore", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String clearDocStore(@PathVariable("version") String version, @PathVariable("apikey") String apikey) throws IOException {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (!feature.validateRequest(apiKey, apikey)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        }
        return feature.clearDocStore(configFilePath, storeFilePath);
    }

    //*************************************************** PUBLIC API ***************************************************

    /**
     * Store the pdf documents received for extraction
     *
     * @param version      api version
     * @param pdf          pdf document
     * @param email        email of sender
     * @param documentType document type
     * @return status of saving
     * @throws IOException if file cannot be converted into bytes
     */
    @RequestMapping(value = "/{version}/extract", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String storeDocument(@PathVariable("version") String version, @RequestParam(value = "pdf") final MultipartFile pdf, @RequestParam(value = "email") String email, @RequestParam(value = "doc_type") String documentType) throws IOException {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        if (email.isEmpty() || documentType.isEmpty()) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_PARAMETER_NULL));
        }
        feature.storeDocuments(pdf.getBytes(), storeFilePath, configFilePath, pubFilePath, email, documentType, Constant.FileExtenstion.PDF, UUID.randomUUID().toString(), 1);
        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.SUCCESS));
    }

    /**
     * Get public certificate of the extractor
     *
     * @param version api version
     * @return URL of the public certificate
     */
    @RequestMapping(value = "/{version}/getpubcert", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getPublicCertificate(@PathVariable("version") String version) {
        if (!feature.validateRequest(version)) {
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        }
        return feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType).replaceAll(pubFilePath, File.separator);
    }
}