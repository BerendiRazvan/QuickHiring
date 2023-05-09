package resumeAnalyzer;

public enum AnalyzeRegex {
    // How RegEx works
    // These regular expressions must be strictly imposed, this is due to one particular reason:
    // For instance, an experience section might contain multiple instances of the word experience. 
    // Therefore, a strict regular expression for section headings must apply.
    // For experience, the solution was to get the index of the word experience 
    // which matches exactly to the following
    // Experience|Experiences|EXPERIENCE|EXPERIENCES. This excluded
    // experience or experiences to be valid. The same notion applies to other section headings.

    LINK("(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)"),
    EMAIL("[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}"),
    PHONE("(\\+4|)?(07[0-8]{1}[0-9]{1}|02[0-9]{2}|03[0-9]{2}){1}?(\\s|\\.|\\-)?([0-9]{3}(\\s|\\.|\\-|)){2}"),
    OBJECTIVE("\\b(Objective(s?)|OBJECTIVE(S?)|Summary|SUMMARY)([^-!@#$%^&*()+.,?])\\b"), // summary included here
    EDUCATION("\\b(Education(s?)|EDUCATION(S?)|Diploma(s?)|DIPLOMA(S?))\\b"),
    EXPERIENCE("\\b(Experience(s?)|EXPERIENCE(S?))\\b"),
    SKILLS("\\b(Skill(s?) & Expertise(s?)|Tool(s?) & Technolog(y?|ies?)|Skill(s?)|SKILL(S?))\\b"),
    LANGUAGE("\\b(Language(s?)|LANGUAGE(S?))\\b"),
    INTEREST("\\b(Interest(s?)|INTEREST(S?)|Activity|Activities|ACTIVITY|ACTIVITIES)\\b"),
    MEMBERSHIP("\\b(Membership(s?)|MEMBERSHIP(S?))\\b"),
    ADDITIONAL("\\b(Award(s?)|AWARD(S)|Honor(s?)|HONOR(S?)|Certification(s?)|CERTIFICATION(S?)|Accomplishment(s?)|ACCOMPLISHMENT(S?)|Project(s?)|PROJECT(S?))\\b"),
    DATEFROMTO("([A-Za-z]+\\s)?([0-9]{4})\\s[-]\\s\\b((P|p)resent|(C|c)urrent)\\b|([A-Za-z]+\\s)?([0-9]{4})");

    /**
     * Note:
     * - if you have a combination of words make sure you put them in the beginning of the list
     */

    private final String name;

    AnalyzeRegex(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}