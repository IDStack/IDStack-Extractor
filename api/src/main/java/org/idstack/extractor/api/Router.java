package org.idstack.extractor.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Chanaka Lakmal
 * @date 31/8/2017
 * @since 1.0
 */

@Component
public class Router {

    public final String configFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.CONFIG_FILE_PATH);
    public final String pvtCertFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_FILE_PATH);
    public final String pvtCertType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_TYPE);
    public final String pvtCertPasswordType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PVT_CERTIFICATE_PASSWORD_TYPE);
    public final String pubCertFilePath = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PUB_CERTIFICATE_FILE_PATH);
    public final String pubCertType = FeatureImpl.getFactory().getProperty(getPropertiesFile(), Constant.GlobalAttribute.PUB_CERTIFICATE_TYPE);

    public String createMR(String json) {
        return Constant.Status.OK;
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
