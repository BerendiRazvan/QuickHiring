package resumeAnalyzer.entity;

public class ApplicantSkill {

    private String skills;

    public ApplicantSkill() {
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    @Override
    public String toString() {
        return skills;
    }

}
