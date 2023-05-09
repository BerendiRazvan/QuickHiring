package resumeAnalyzer.resumeReader;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TxtFileReader implements ResumeFileReader {

    public String readFile(File file) {
        StringBuilder text = new StringBuilder("");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        return String.join(" ", text.toString());
    }
}