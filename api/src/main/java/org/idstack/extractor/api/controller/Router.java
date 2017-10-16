package org.idstack.extractor.api.controller;

import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.idstack.extractor.DocParserHandler;
import org.idstack.extractor.JsonBuilder;
import org.idstack.extractor.JsonExtractor;
import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.idstack.feature.response.SignedResponse;
import org.idstack.feature.sign.pdf.JsonPdfMapper;
import org.idstack.feature.sign.pdf.PdfCertifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.UUID;

/**
 * @author Chanaka Lakmal
 * @date 31/8/2017
 * @since 1.0
 */

@Component
public class Router {

    @Autowired
    private SignedResponse signedResponse;

    protected String extractDocument(FeatureImpl feature, String json, String pdfUrl, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String tempFilePath) {
        PdfCertifier pdfCertifier = new PdfCertifier(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType), feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType), feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));
        JsonPdfMapper mapper = new JsonPdfMapper();
        try {
            String sigID = UUID.randomUUID().toString();
            String pdfPath = feature.createTempFile(pdfUrl, tempFilePath, UUID.randomUUID().toString() + Constant.FileExtenstion.PDF).getPath();
            pdfCertifier.signPdf(pdfPath, sigID);

            String signedPdfPath = Paths.get(pdfPath).getParent() + File.separator + Constant.SIGNED + Paths.get(pdfPath).getFileName().toString();
            String pdfHash = mapper.getHashOfTheOriginalContent(signedPdfPath);

            String formattedJson = new JsonBuilder().constructAsNestedJson(json, pdfHash, feature);
            JsonExtractor jsonExtractor = new JsonExtractor(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            signedResponse.setJson(jsonExtractor.signExtactedJson(formattedJson));
            signedResponse.setPdf(signedPdfPath);
            return new Gson().toJson(signedResponse);
        } catch (CMSException | OperatorCreationException | IOException | DocumentException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected String parseDocument(FeatureImpl feature, String pdfUrl, String documentType) {
        try {
            return new DocParserHandler().getExtractedDocument(feature, pdfUrl, documentType);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
