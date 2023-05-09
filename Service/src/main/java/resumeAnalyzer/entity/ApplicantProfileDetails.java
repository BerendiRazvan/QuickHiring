package resumeAnalyzer.entity;


public class ApplicantProfileDetails {

    private String profile;
    private String email;
    private String phoneNumber;
    private String links;
    private String objective;

    public ApplicantProfileDetails() {
        this.profile = null;
        this.email = null;
        this.phoneNumber = null;
        this.links = null;
        this.objective = null;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    @Override
    public String toString() {
        return profile + ", email=" + email + ", phoneNumber=" + phoneNumber
                + ", links=" + links + ", objective=" + objective + "]";
    }
}
