package resumeAnalyzer.resumeReader;

import org.apache.poi.hwpf.extractor.WordExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocFileReader implements ResumeFileReader {
    public String readFile(File file) {
        String text = "";

        try (WordExtractor extractor = new WordExtractor(new FileInputStream(file))) {
            text = extractor.getText();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        return text;
    }
}