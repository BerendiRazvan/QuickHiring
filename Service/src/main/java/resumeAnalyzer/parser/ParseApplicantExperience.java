package resumeAnalyzer.parser;

import resumeAnalyzer.AnalyzeRegex;
import resumeAnalyzer.entity.ApplicantExperience;
import resumeAnalyzer.entity.ResumeDocument;

import java.util.List;

public class ParseApplicantExperience {
    private ParserHelper helper = new ParserHelper();

    private String findWorkExperiences(String line) {
        ParserHelper parser = new ParserHelper();
        /*
         * Algorithm: copy texts starting from experience section index to the
         * following section index experience index is LESS THAN the following
         * section index, therefore
         *
         * Example: section indexes [24, 355, 534, 669] index of experience
         * section = 355 therefore, the following section index would be 534 we
         * can get the texts that encompasses experience section by substring =>
         * (indexOfExperience, beginIndexOfFollowingSection)
         *
         */
        int indexOfExperience = parser.getIndexOfThisSection(AnalyzeRegex.EXPERIENCE, line);
        if (indexOfExperience != -1) {
            int nextSectionIndex = 0; // index that follows experience section
            List<Integer> listOfSectionIndexes = parser.getAllSectionIndexes(line);
            String experiencesText = line.replaceFirst(AnalyzeRegex.EXPERIENCE.toString(), "");
            return helper.getSectionContent(indexOfExperience, listOfSectionIndexes, experiencesText, nextSectionIndex);
        }
        return null;
    }

    public ApplicantExperience getApplicantExperience(ResumeDocument resumeDocument) {
        ApplicantExperience applicantExperience = new ApplicantExperience();
        applicantExperience.setExperience(findWorkExperiences(resumeDocument.getDocumentContent()));
        return applicantExperience;
    }
}
