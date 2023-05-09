package resumeAnalyzer.parser;

import resumeAnalyzer.AnalyzeRegex;
import resumeAnalyzer.entity.ApplicantEducation;
import resumeAnalyzer.entity.ResumeDocument;

import java.util.List;

public class ParseApplicantEducation {

    private ParserHelper helper = new ParserHelper();

    private String findEducations(String line) {
        ParserHelper parser = new ParserHelper();
        int indexOfEducation = parser.getIndexOfThisSection(AnalyzeRegex.EDUCATION, line);
        if (indexOfEducation != -1) {
            int nextSectionIndex = 0;
            List<Integer> listOfSectionIndexes = parser.getAllSectionIndexes(line);
            String educationsText = line.replaceFirst(AnalyzeRegex.EDUCATION.toString(), "");
            return helper.getSectionContent(indexOfEducation, listOfSectionIndexes, educationsText, nextSectionIndex);
        }
        return null;
    }

    public ApplicantEducation getApplicantEducation(ResumeDocument resumeDocument) {
        ApplicantEducation applicantEducation = new ApplicantEducation();
        applicantEducation.setEducation(findEducations(resumeDocument.getDocumentContent()));
        return applicantEducation;
    }
}
