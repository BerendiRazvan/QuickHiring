package validators;

import domain.Resume;

public class ResumeValidator implements Validator<Resume> {

    @Override
    public void validate(Resume resume) throws ValidatorException {
        if (resume.getEducationExtractedData().isEmpty() ||
                resume.getExperienceExtractedData().isEmpty() ||
                resume.getSkillsExtractedData().isEmpty() ||
                resume.getProfileExtractedData().isEmpty()
        )
            throw new ValidatorException("Invalid resume!");

        if (resume.getOwner() == null)
            throw new ValidatorException("This resume does not have an owner!");
    }
}
