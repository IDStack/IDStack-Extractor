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
import org.idstack.feature.Parser;
import org.idstack.feature.configuration.BasicConfig;
import org.idstack.feature.document.Document;
import org.idstack.feature.sign.pdf.PdfCertifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

/**
 * @author Chanaka Lakmal
 * @date 31/8/2017
 * @since 1.0
 */

@Component
public class Router {

    protected String extractDocument(FeatureImpl feature, String json, String requestId, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String tempFilePath, String storeFilePath, String pubFilePath) {
        PdfCertifier pdfCertifier = new PdfCertifier(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType), feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType), feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));
        try {
            String pdfUrl = feature.getPdfByRequestId(storeFilePath, configFilePath, requestId).replaceAll(pubFilePath, File.separator);
            String pdfPath = feature.parseUrlAsLocalFilePath(pdfUrl, pubFilePath);

            String signedPdfPath = tempFilePath + Constant.SIGNED + File.separator;
            Files.createDirectories(Paths.get(signedPdfPath));
            signedPdfPath = pdfCertifier.signPdf(pdfPath, signedPdfPath, UUID.randomUUID().toString());
            String pdf = feature.convertPdfToBytesToString(Paths.get(signedPdfPath));

            String formattedJson = new JsonBuilder().constructJson(json, "", feature);

            JsonExtractor jsonExtractor = new JsonExtractor(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            Document extractedDocument = Parser.parseDocumentJson(jsonExtractor.signExtactedJson(formattedJson));
            extractedDocument.getMetaData().setPdf(pdf);

            Path jsonFilePath = Files.write(Paths.get(tempFilePath).resolve(Paths.get(UUID.randomUUID().toString() + Constant.FileExtenstion.JSON)), new Gson().toJson(extractedDocument).getBytes());
            String jsonUrl = feature.parseLocalFilePathAsOnlineUrl(jsonFilePath.toString(), configFilePath);

            // This will send an email to owner with files
            BasicConfig basicConfig = (BasicConfig) feature.getConfiguration(configFilePath, Constant.Configuration.BASIC_CONFIG_FILE_NAME);
            String body = feature.populateEmailBody(requestId, extractedDocument.getMetaData().getDocumentType().toUpperCase(), jsonUrl, basicConfig);
            feature.sendEmail(feature.getEmailByRequestId(storeFilePath, requestId), "EXTRACTOR - IDStack Document Extraction", body);

            // This will add the request id into request configuration list
            feature.saveRequestConfiguration(configFilePath, requestId);

            return new Gson().toJson(extractedDocument);
        } catch (CMSException | OperatorCreationException | IOException | DocumentException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected String saveDocument(FeatureImpl feature, MultipartFile pdf, String configFilePath, String tmpFilePath) throws IOException {
        String tmpPath = feature.createTempFile(pdf.getBytes(), tmpFilePath, UUID.randomUUID().toString() + Constant.FileExtenstion.PDF).toString();
        String tmpUrl = feature.parseLocalFilePathAsOnlineUrl(tmpPath, configFilePath);
        return new Gson().toJson(Collections.singletonMap(Constant.TEMP_URL, tmpUrl));
    }

    protected String parseDocument(FeatureImpl feature, String pdfUrl, String documentType) {
        try {
            return new DocParserHandler().getExtractedDocument(feature, pdfUrl, documentType);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
