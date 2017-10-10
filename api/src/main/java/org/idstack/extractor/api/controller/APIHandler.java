package org.idstack.extractor.api.controller;

import com.google.gson.Gson;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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
    @RequestMapping(value = "/{version}/{apikey}/saveconfig/basic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.saveBasicConfiguration(configFilePath, json);
    }

    /**
     * Returned the saved configurations. If property is mentioned this returns only the mentioned property from the given type otherwise everything
     *
     * @param version  api version
     * @param apikey   api key
     * @param property property of configuration
     * @return configuration as json
     */
    @RequestMapping(value = {"/{version}/{apikey}/getconfig/basic/{property}", "/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("property") Optional<String> property) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.getConfigurationAsJson(configFilePath, Constant.Configuration.BASIC_CONFIG_FILE_NAME, property);
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
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
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
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.savePrivateCertificate(certificate, password, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType);
    }

    /**
     * Create machine readable document after extracting
     *
     * @param version api version
     * @param apikey  api key
     * @param json    json of extracted data
     * @param pdf     pdf of document
     * @return signed json + pdf documents
     */
    //TODO : return both signed MR + signed PDF
    @RequestMapping(value = "/{version}/{apikey}/extract", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String extractDocument(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "json") String json, @RequestParam(value = "pdf") final MultipartFile pdf) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return router.extractDocument(feature, json, pdf, configFilePath, pvtCertFilePath, pvtCertType, pvtCertPasswordType, pubCertFilePath, pubCertType);
    }

    /**
     * Get the document types that protocol facilitates
     *
     * @param version api version
     * @param apikey  api key
     * @return document types
     */
    //TODO : pass this to lambda function
    @RequestMapping(value = "/{version}/{apikey}/getdoctypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDocumentTypeList(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
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
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        if (!feature.validateRequest(apiKey, apikey))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_API_KEY));
        return feature.getDocumentStore(storeFilePath);
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
    public String storeDocument(@PathVariable("version") String version, @RequestParam(value = "pdf") final MultipartFile pdf, @RequestParam(value = "email") String email, @RequestParam(value = "doc-type") String documentType) throws IOException {
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        return feature.storeDocuments(pdf.getBytes(), storeFilePath, email, documentType, Constant.FileExtenstion.PDF, UUID.randomUUID().toString(), 1);
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
        if (!feature.validateRequest(version))
            return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.ERROR_VERSION));
        return feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType);
    }
}