package resumeAnalyzer.resumeReader;

import java.io.File;

public class FileReaderFactory {
    public static ResumeFileReader createFileReader(File file) {
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "txt":
                return new TxtFileReader();
            case "pdf":
                return new PdfFileReader();
            case "doc":
                return new DocFileReader();
            case "docx":
                return new DocxFileReader();
            default:
                throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }
}
