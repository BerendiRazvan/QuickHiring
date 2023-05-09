package validators;

import domain.ApplicationForJob;

public class ApplicationValidator implements Validator<ApplicationForJob> {

    @Override
    public void validate(ApplicationForJob application) throws ValidatorException {
        if (application.getJobApplied() == null || application.getCandidateResume() == null)
            throw new ValidatorException("Invalid application!");
    }
}
