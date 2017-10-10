package org.idstack.extractor.api.controller;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.extractor.JsonBuilder;
import org.idstack.extractor.JsonExtractor;
import org.idstack.feature.FeatureImpl;
import org.springframework.stereotype.Component;

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

    protected String extractDocument(FeatureImpl feature, String json, String pdfUrl, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType) {
        //TODO : get the pdf and get the pdf_hash
        String pdfHash = "/FJPadt2/9o1hDV5zcKAmcfVaGWn8+jcpcYknhhCU7I=";

        String formattedJson = new JsonBuilder().constructAsNestedJson(json, pdfHash, feature);
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
