package resumeAnalyzer.entity;

public class ResumeDocument {

    private String documentContent;

    public ResumeDocument(String line) {
        this.documentContent = line;
    }

    public String getDocumentContent() {
        return documentContent;
    }

}
