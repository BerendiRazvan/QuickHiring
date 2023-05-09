package validators;

import domain.Job;

public class JobValidator implements Validator<Job> {
    @Override
    public void validate(Job job) throws ValidatorException {
        if (job.getTitle().isEmpty() || job.getDescription().isEmpty() ||
                job.getCompany() == null || job.getLocation() == null ||
                job.getRecruiter() == null)
            throw new ValidatorException("Invalid job!");
    }
}
