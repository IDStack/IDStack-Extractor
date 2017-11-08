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
import org.idstack.feature.document.Document;
import org.idstack.feature.response.SignedResponse;
import org.idstack.feature.sign.pdf.JsonPdfMapper;
import org.idstack.feature.sign.pdf.PdfCertifier;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SignedResponse signedResponse;

    protected String extractDocument(FeatureImpl feature, String json, String pdfUrl, String configFilePath, String pvtCertFilePath, String pvtCertType, String pvtCertPasswordType, String pubCertFilePath, String pubCertType, String tempFilePath, String storeFilePath, String requestId) {
        PdfCertifier pdfCertifier = new PdfCertifier(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType), feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType), feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));
        JsonPdfMapper mapper = new JsonPdfMapper();
        try {
            String sigID = UUID.randomUUID().toString();
            String pdfPath = feature.createTempFile(pdfUrl, tempFilePath, UUID.randomUUID().toString() + Constant.FileExtenstion.PDF).getPath();

            String signedPdfPath = tempFilePath + Constant.SIGNED + File.separator;
            Files.createDirectories(Paths.get(signedPdfPath));

            signedPdfPath = pdfCertifier.signPdf(pdfPath, signedPdfPath, sigID);
            String pdfHash = mapper.getHashOfTheOriginalContent(signedPdfPath);

            String formattedJson = new JsonBuilder().constructAsNestedJson(json, pdfHash, feature);

            JsonExtractor jsonExtractor = new JsonExtractor(feature.getPrivateCertificateFilePath(configFilePath, pvtCertFilePath, pvtCertType),
                    feature.getPassword(configFilePath, pvtCertFilePath, pvtCertPasswordType),
                    feature.getPublicCertificateURL(configFilePath, pubCertFilePath, pubCertType));

            Document extractedDocument = Parser.parseDocumentJson(jsonExtractor.signExtactedJson(formattedJson));

            Path jsonFilePath = Files.write(Paths.get(tempFilePath).resolve(Paths.get(UUID.randomUUID().toString() + Constant.FileExtenstion.JSON)), new Gson().toJson(extractedDocument).getBytes());
            String finalJsonUrl = feature.parseLocalFilePathAsOnlineUrl(jsonFilePath.toString(), configFilePath);
            String finalPdfUrl = feature.parseLocalFilePathAsOnlineUrl(signedPdfPath, configFilePath);

            String message = populateEmailBody(requestId, extractedDocument.getMetaData().getDocumentType().toUpperCase(), finalJsonUrl, finalPdfUrl);
            feature.sendEmail(feature.getEmailByRequestId(storeFilePath, requestId), "IDStack Document Extraction", message);

            signedResponse.setJson(Parser.parseDocumentJson(jsonExtractor.signExtactedJson(formattedJson)));
            signedResponse.setPdf(feature.parseLocalFilePathAsOnlineUrl(signedPdfPath, configFilePath));

            // This will add the request id into request configuration list
            feature.saveRequestConfiguration(configFilePath, requestId);

            return new Gson().toJson(signedResponse);
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

    private String populateEmailBody(String requestId, String documentType, String jsonUrl, String pdfUrl) {
        return String.join(
                System.getProperty("line.separator"),
                "<p>Hi,</p>",
                "<p></p>",
                "<p>Please find the extracted files.</p>",
                "<p><b>Request ID :</b> " + requestId + "<br>",
                "<b>Document Type :</b> " + documentType + "<br>",
                "<b>JSON :</b> " + jsonUrl + "<br>",
                "<b>PDF :</b> " + pdfUrl + "</p>",
                "<p></p>",
                "<p>Thank you.<br>",
                "Team IDStack<br>",
                "<a href='http://idstack.one'>http://www.idstack.one</a></p>"
        );
    }

    /**
     * This is a test method only.
     *
     * @param feature feature
     * @return status of sending email
     */
    protected String sendEmail(FeatureImpl feature) {
        feature.sendEmail("ldclakmal@gmail.com", "IDStack Document Extraction", "Test Message");
        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, Constant.Status.SUCCESS));
    }
}
