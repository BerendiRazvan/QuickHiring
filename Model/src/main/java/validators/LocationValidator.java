package validators;

import domain.Location;

public class LocationValidator implements Validator<Location> {
    @Override
    public void validate(Location location) throws ValidatorException {
        if (location.getCountry().isEmpty() || location.getCity().isEmpty())
            throw new ValidatorException("Invalid location!");
    }
}
