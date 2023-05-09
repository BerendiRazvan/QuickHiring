package resumeAnalyzer.resumeReader;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocxFileReader implements ResumeFileReader {
    public String readFile(File file) {
        String text = "";

        try (XWPFDocument document = new XWPFDocument(new FileInputStream(file))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            text = extractor.getText();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        return text;
    }
}