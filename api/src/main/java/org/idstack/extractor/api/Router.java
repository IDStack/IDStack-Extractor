package org.idstack.extractor.api;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.extractor.JsonCreator;
import org.idstack.extractor.JsonExtractor;
import org.idstack.feature.FeatureImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * @author Chanaka Lakmal
 * @date 31/8/2017
 * @since 1.0
 */

@Component
public class Router {

    protected String extractDocument(FeatureImpl feature, String json, MultipartFile pdf, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType) {
        // Format document.config.idstack file as you wish
        String formattedJson = new JsonCreator().constructAsNestedJson(json);
        JsonExtractor jsonExtractor = new JsonExtractor(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));
        //TODO : call sign pdf method and return pdf as well
        try {
            return jsonExtractor.signExtactedJson(formattedJson);
        } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | CMSException | OperatorCreationException | NoSuchProviderException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
