package resumeAnalyzer.entity;

public class ApplicantEducation {

    private String education;

    public ApplicantEducation() {
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    @Override
    public String toString() {
        return education;
    }
}
