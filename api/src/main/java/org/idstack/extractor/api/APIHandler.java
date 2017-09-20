package org.idstack.extractor.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    @RequestMapping(value = "/{version}/{apikey}/saveconfig/basic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().saveBasicConfiguration(router.configFilePath, json);
    }

    /**
     * Returned the saved configurations. If property is mentioned this returns only the mentioned property from the given type otherwise everything
     *
     * @param version  api version
     * @param apikey   api key
     * @param type     type of configuration [basic, document, whitelist, blacklist]
     * @param property property of configuration
     * @return configuration as json
     */
    @RequestMapping(value = {"/{version}/{apikey}/getconfig/{type}/{property}", "/{version}/{apikey}/getconfig/{type}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @PathVariable("property") Optional<String> property) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().getConfigurationAsJson(router.configFilePath, router.getConfigFileName(type), property);
    }

    /**
     * Save public certificate of the extractor
     *
     * @param version     api version
     * @param apikey      api key
     * @param certificate public certificate file
     * @return status of saving
     */
    @RequestMapping(value = "/{version}/{apikey}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().savePublicCertificate(certificate, router.configFilePath, router.pubCertFilePath, router.pubCertType);
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
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return FeatureImpl.getFactory().getPublicCertificateURL(router.configFilePath, router.pubCertFilePath, router.pubCertType);
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
    @RequestMapping(value = "/{version}/{apikey}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().savePrivateCertificate(certificate, password, router.configFilePath, router.pvtCertFilePath, router.pvtCertType, router.pvtCertPasswordType);
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
    @RequestMapping(value = "/{version}/{apikey}/extract", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String createMR(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "json") String json, @RequestParam(value = "pdf") final MultipartFile pdf) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return router.createMR(json, pdf);
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
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().getDocumentTypes();
    }

    //Access by the owner

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
    @RequestMapping(value = "/{version}/store", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String storeDocuments(@PathVariable("version") String version, @RequestParam(value = "pdf") final MultipartFile pdf, @RequestParam(value = "email") String email, @RequestParam(value = "doc-type") String documentType) throws IOException {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        return FeatureImpl.getFactory().storeDocuments(pdf.getBytes(), router.storeFilePath, email, documentType, Constant.FileExtenstion.PDF, UUID.randomUUID().toString());
    }

    /**
     * Get the stored documents in the configured store path
     *
     * @param version api version
     * @param apikey  api key
     * @return document list
     */
    @RequestMapping(value = "/{version}/{apikey}/getdocstore", method = RequestMethod.GET)
    @ResponseBody
    public String getStoredDocuments(@PathVariable("version") String version, @PathVariable("apikey") String apikey) {
        if (!FeatureImpl.getFactory().validateRequest(version))
            return Constant.Status.STATUS_ERROR_VERSION;
        if (!FeatureImpl.getFactory().validateRequest(router.apiKey, apikey))
            return Constant.Status.STATUS_ERROR_API_KEY;
        return FeatureImpl.getFactory().getDocumentStore(router.storeFilePath);
    }
}