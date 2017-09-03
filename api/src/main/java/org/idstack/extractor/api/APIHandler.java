package org.idstack.extractor.api;

import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chanaka Lakmal
 * @date 31/8/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    Router router;

    @RequestMapping("/")
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://idstack.one/extractor");
    }

    @RequestMapping(value = "/{version}/saveconfig/basic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBasicConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return FeatureImpl.getFactory().saveBasicConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/saveconfig/document", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveDocumentConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return FeatureImpl.getFactory().saveDocumentConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/saveconfig/whitelist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveWhiteListConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return FeatureImpl.getFactory().saveWhiteListConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/saveconfig/blacklist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBlackListConfiguration(@PathVariable("version") String version, @RequestBody String json) {
        return FeatureImpl.getFactory().saveBlackListConfiguration(router.configFilePath, json);
    }

    @RequestMapping(value = "/{version}/getconfig/{type}/{property}", method = RequestMethod.GET)
    @ResponseBody
    public Object getConfigurationFile(@PathVariable("version") String version, @PathVariable("type") String type, @PathVariable("property") String property) {
        return FeatureImpl.getFactory().getConfiguration(router.configFilePath, router.getConfigFileName(type), property);
    }

    @RequestMapping(value = "/{version}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @RequestParam(value = "cert") final MultipartFile certificate) {
        return FeatureImpl.getFactory().savePublicCertificate(certificate, router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/getpubcert", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public String getPublicCertificate(@PathVariable("version") String version) {
        return FeatureImpl.getFactory().getPublicCertificateURL(router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        return FeatureImpl.getFactory().savePrivateCertificate(certificate, password, router.configFilePath, router.pvtCertFilePath, router.pvtCertType, router.pvtCertPasswordType);
    }

    @RequestMapping(value = "/{version}/extract", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createMR(@PathVariable("version") String version, @RequestBody String json) {
        return router.createMR(json);
    }
}