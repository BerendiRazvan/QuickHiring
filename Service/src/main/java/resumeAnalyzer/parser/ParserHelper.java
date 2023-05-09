package resumeAnalyzer.parser;


import resumeAnalyzer.AnalyzeRegex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserHelper {

    public int getIndexOfThisSection(AnalyzeRegex regEx, String fileContent) {
        AnalyzeRegex[] sectionRegex = {
                AnalyzeRegex.OBJECTIVE,
                AnalyzeRegex.EDUCATION,
                AnalyzeRegex.EXPERIENCE,
                AnalyzeRegex.SKILLS,
                AnalyzeRegex.LANGUAGE,
                AnalyzeRegex.INTEREST,
                AnalyzeRegex.MEMBERSHIP,
                AnalyzeRegex.ADDITIONAL
        };
        List<Integer> indexOfThisSection = new ArrayList<>();
        for (AnalyzeRegex r : sectionRegex) {
            if (r.equals(regEx)) {
                storeSectionIndexes(fileContent, indexOfThisSection, r);
            }
        }
        if (!indexOfThisSection.isEmpty()) {
            return indexOfThisSection.get(0);
        }
        return -1;
    }

    private void storeSectionIndexes(String line, List<Integer> indexOfThisSection, AnalyzeRegex r) {
        Pattern pattern = Pattern.compile(r.toString(), Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            indexOfThisSection.add(matcher.start());
        }
    }

    List<Integer> getAllSectionIndexes(String content) {
        AnalyzeRegex[] sectionRegex = {
                AnalyzeRegex.OBJECTIVE,
                AnalyzeRegex.EDUCATION,
                AnalyzeRegex.EXPERIENCE,
                AnalyzeRegex.SKILLS,
                AnalyzeRegex.LANGUAGE,
                AnalyzeRegex.INTEREST,
                AnalyzeRegex.MEMBERSHIP,
                AnalyzeRegex.ADDITIONAL
        };
        List<Integer> indexesOfSection = new ArrayList<>();
        for (AnalyzeRegex r : sectionRegex) {
            storeSectionIndexes(content, indexesOfSection, r);
        }
        Collections.sort(indexesOfSection);
        return indexesOfSection;
    }

    public List<Integer> getSectionIndexesExcludeOne(AnalyzeRegex regEx, String line) {
        AnalyzeRegex[] sectionRegex = {
                AnalyzeRegex.OBJECTIVE,
                AnalyzeRegex.EDUCATION,
                AnalyzeRegex.EXPERIENCE,
                AnalyzeRegex.SKILLS,
                AnalyzeRegex.LANGUAGE,
                AnalyzeRegex.INTEREST,
                AnalyzeRegex.MEMBERSHIP,
                AnalyzeRegex.ADDITIONAL
        };
        List<Integer> indexesOfSection = new ArrayList<>();
        for (AnalyzeRegex r : sectionRegex) {
            if (!r.equals(regEx)) {
                storeSectionIndexes(line, indexesOfSection, r);
            }
        }
        Collections.sort(indexesOfSection);
        return indexesOfSection;
    }

    String getSectionContent(int sectionIndex, List<Integer> sectionIndexes, String content, int nextSectionIndex) {
        for (int index = 0; index < sectionIndexes.size(); index++) {
            if (sectionIndexes.get(index) == sectionIndex) {
                if (index == sectionIndexes.size() - 1) {
                    return content.substring(sectionIndex);
                } else {
                    nextSectionIndex = sectionIndexes.get(index + 1);
                    break;
                }
            }
        }
        return content.substring(sectionIndex, nextSectionIndex);
    }

}
