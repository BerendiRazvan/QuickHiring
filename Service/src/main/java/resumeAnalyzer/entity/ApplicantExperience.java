package resumeAnalyzer.entity;

public class ApplicantExperience {

    private String experiences;

    public ApplicantExperience() {
    }

    public void setExperience(String experienceData) {
        this.experiences = experienceData;
    }

    @Override
    public String toString() {
        return experiences;
    }
}
