package resumeAnalyzer.resumeReader;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfFileReader implements ResumeFileReader {
    public String readFile(File file) {
        String text = "";
        String removedPageNumberRegex = "\\bPage\\d\\b";

        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document).replaceAll(removedPageNumberRegex, "");
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        return text;
    }
}