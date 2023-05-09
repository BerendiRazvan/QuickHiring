package validators;

import domain.Company;

public class CompanyValidator implements Validator<Company> {
    @Override
    public void validate(Company company) throws ValidatorException {
        if (company.getName().isEmpty() || company.getLocations().size() == 0)
            throw new ValidatorException("Invalid company!");
    }
}