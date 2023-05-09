package resumeAnalyzer.parser;

import resumeAnalyzer.AnalyzeRegex;
import resumeAnalyzer.entity.ApplicantSkill;
import resumeAnalyzer.entity.ResumeDocument;

import java.util.List;

public class ParseApplicantSkill {
    private ParserHelper helper = new ParserHelper();

    private String findApplicantSkills(String line) {
        ParserHelper parser = new ParserHelper();
        int indexOfSkillsSection = parser.getIndexOfThisSection(AnalyzeRegex.SKILLS, line);

        if (indexOfSkillsSection != -1) {
            List<Integer> sectionIndexes = parser.getAllSectionIndexes(line);
            String skillsText = line.replaceFirst(AnalyzeRegex.SKILLS.toString(), "");
            int nextSectionIndex = 0;
            return helper.getSectionContent(indexOfSkillsSection, sectionIndexes, skillsText, nextSectionIndex);
        }
        return null;
    }

    public ApplicantSkill getApplicantSkills(ResumeDocument resumeDocument) {
        ApplicantSkill applicantSkill = new ApplicantSkill();
        applicantSkill.setSkills(findApplicantSkills(resumeDocument.getDocumentContent()));
        return applicantSkill;
    }
}
