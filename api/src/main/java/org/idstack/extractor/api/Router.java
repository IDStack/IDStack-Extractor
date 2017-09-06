package org.idstack.extractor.api;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.extractor.JsonCreator;
import org.idstack.extractor.JsonExtractor;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public final String apiKey = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.API_KEY);
    public final String configFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.CONFIG_FILE_PATH);
    public final String pvtCertFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
    public final String pvtCertType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
    public final String pvtCertPasswordType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE);
    public final String pubCertFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
    public final String pubCertType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);

    public String createMR(String json) {
        //TODO : check for document config and check whether this is automatically extractable
        // Format document.config.idstack file as you wish
        String formattedJson = new JsonCreator().constructAsNestedJson(json);
        JsonExtractor jsonExtractor = new JsonExtractor(FeatureImpl.getFactory().getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                FeatureImpl.getFactory().getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                FeatureImpl.getFactory().getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));
        try {
            return jsonExtractor.signExtactedJson(formattedJson);
        } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | CMSException | OperatorCreationException | NoSuchProviderException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileInputStream getPropertiesFile() {
        try {
            return new FileInputStream(getClass().getClassLoader().getResource(Constant.GlobalAttribute.SYSTEM_PROPERTIES_FILE_NAME).getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getConfigFileName(String type) {
        switch (type) {
            case Constant.GlobalAttribute.BASIC_CONFIG:
                return Constant.GlobalAttribute.BASIC_CONFIG_FILE_NAME;
            case Constant.GlobalAttribute.DOCUMENT_CONFIG:
                return Constant.GlobalAttribute.DOCUMENT_CONFIG_FILE_NAME;
            case Constant.GlobalAttribute.WHITELIST_CONFIG:
                return Constant.GlobalAttribute.WHITELIST_CONFIG_FILE_NAME;
            case Constant.GlobalAttribute.BLACKLIST_CONFIG:
                return Constant.GlobalAttribute.BLACKLIST_CONFIG_FILE_NAME;
            default:
                return null;
        }
    }
}
