package resumeAnalyzer;


import domain.Resume;
import resumeAnalyzer.entity.*;
import resumeAnalyzer.parser.ParseApplicantEducation;
import resumeAnalyzer.parser.ParseApplicantExperience;
import resumeAnalyzer.parser.ParseApplicantProfileDetails;
import resumeAnalyzer.parser.ParseApplicantSkill;
import resumeAnalyzer.resumeReader.FileReaderFactory;
import resumeAnalyzer.resumeReader.ResumeFileReader;

import java.io.File;

public class ResumeParser {

    private String profile = "";
    private String education = "";
    private String experience = "";
    private String skill = "";

    public ResumeParser() {
    }

    private void analyzeResumeContent(String content) {
        ParseApplicantProfileDetails profileParser = new ParseApplicantProfileDetails();
        ParseApplicantEducation educationParser = new ParseApplicantEducation();
        ParseApplicantExperience experienceParser = new ParseApplicantExperience();
        ParseApplicantSkill skillParser = new ParseApplicantSkill();

        ResumeDocument resumeDocument = new ResumeDocument(content);

        ApplicantProfileDetails applicantProfileDetails = profileParser.getApplicantProfileDetails(resumeDocument);
        ApplicantEducation applicantEducation = educationParser.getApplicantEducation(resumeDocument);
        ApplicantExperience applicantExperience = experienceParser.getApplicantExperience(resumeDocument);
        ApplicantSkill applicantSkill = skillParser.getApplicantSkills(resumeDocument);

        profile = applicantProfileDetails.toString();
        education = applicantEducation.toString();
        experience = applicantExperience.toString();
        skill = applicantSkill.toString();
    }


    public Resume extractData(File resumeFile) {
        Resume resumeResult = new Resume();

        // Read from file txt, pdf, doc, docx using factory pattern
        ResumeFileReader resumeFileReader = FileReaderFactory.createFileReader(resumeFile);
        String resumeContent = resumeFileReader.readFile(resumeFile);

        // Analyze resume content with the analyzer method
        analyzeResumeContent(resumeContent);

        resumeResult.setProfileExtractedData(profile);
        resumeResult.setEducationExtractedData(education);
        resumeResult.setExperienceExtractedData(experience);
        resumeResult.setSkillsExtractedData(skill);

        return resumeResult;
    }


}
