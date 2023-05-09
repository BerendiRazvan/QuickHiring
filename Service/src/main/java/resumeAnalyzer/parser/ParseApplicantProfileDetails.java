package resumeAnalyzer.parser;

import resumeAnalyzer.AnalyzeRegex;
import resumeAnalyzer.entity.ApplicantProfileDetails;
import resumeAnalyzer.entity.ResumeDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseApplicantProfileDetails {

    private String findEmail(String details) {
        List<String> emailList = new ArrayList<>();
        Pattern pattern = Pattern.compile(AnalyzeRegex.EMAIL.toString(), Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(details);
        while (matcher.find()) {
            emailList.add(matcher.group());
        }
        return emailList.toString();
    }

    private String findLinks(String line) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile(AnalyzeRegex.LINK.toString(),
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            links.add(matcher.group());
        }
        return links.toString();
    }

    /**
     * The introduction of an applicant may contains the name and the address
     *
     * @return the profile containing name (most likely) and address (possibly)
     */
    private String findProfile(String line) {
        ParserHelper helper = new ParserHelper();
        List<Integer> indexes = helper.getAllSectionIndexes(line);
        int beginIndex = 0;
        int endIndex = indexes.get(0);
        return line.substring(beginIndex, endIndex);
    }

    /**
     * Find phone numbers in the resume
     *
     * @param line to search for
     * @return phone numbers found from resume
     */
    private String findPhoneNumber(String line) {
        List<String> phoneNumbers = new ArrayList<>();
        Pattern pattern = Pattern.compile(AnalyzeRegex.PHONE.toString(), Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            phoneNumbers.add(matcher.group());
        }
        return phoneNumbers.toString();
    }

    private String findObjective(String line) {
        AnalyzeRegex obj = AnalyzeRegex.OBJECTIVE;
        ParserHelper helper = new ParserHelper();
        int beginIndex = helper.getIndexOfThisSection(obj, line);
        String objective = line.replaceFirst(AnalyzeRegex.OBJECTIVE.toString(), "");
        // get(0) which ensures objective heading is excluded
        int endIndex = helper.getSectionIndexesExcludeOne(obj, objective).get(0);
        return objective.substring(beginIndex, endIndex);
    }

    /**
     * Gathers basic applicant info such as phone number, email, links, profile
     * (introduction), and objective. These information are saved in a list of Applicant object.
     */
    public ApplicantProfileDetails getApplicantProfileDetails(ResumeDocument resumeDocument) {
        ApplicantProfileDetails applicant = new ApplicantProfileDetails();

        applicant.setPhoneNumber(findPhoneNumber(resumeDocument.getDocumentContent()));
        applicant.setEmail(findEmail(resumeDocument.getDocumentContent()));
        applicant.setLinks(findLinks(resumeDocument.getDocumentContent()));
        applicant.setProfile(findProfile(resumeDocument.getDocumentContent()));

        if (new ParserHelper().getIndexOfThisSection(AnalyzeRegex.OBJECTIVE, resumeDocument.getDocumentContent()) != -1) {
            applicant.setObjective(findObjective(resumeDocument.getDocumentContent()));
        } else {
            applicant.setObjective(null);
        }

        return applicant;
    }

}
