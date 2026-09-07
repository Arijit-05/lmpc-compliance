package com.sih.lmpc_compliance.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OcrService {

    public String extractText(String imagePath) {
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            throw new RuntimeException("Image file not found: " + imagePath);
        }

        ITesseract tesseract = new Tesseract();

        // IMPORTANT: path containing the tessdata folder
        tesseract.setDatapath(
                "C:\\Program Files\\Tesseract-OCR\\tessdata"
        );

        tesseract.setLanguage("eng");

        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            throw new RuntimeException("OCR processing failed", e);
        }
    }
}