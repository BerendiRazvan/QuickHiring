package validators;

import domain.User;

import java.time.LocalDateTime;
import java.time.Period;

public class UserValidator implements Validator<User> {
    private static final String MAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final String PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";
    private static final String NAME_REGEX = "[a-zA-Z- ]*";
    private static final String PHONE_NUMBER_REGEX = "^(\\+4|)?(07[0-8]{1}[0-9]{1}|02[0-9]{2}|03[0-9]{2}){1}?(\\s|\\.|\\-)?([0-9]{3}(\\s|\\.|\\-|)){2}$";

    public boolean isValidMail(String mail) {
        return mail.matches(MAIL_REGEX);
    }

    public boolean isValidPassword(String password) {
        return password.matches(PASSWORD_REGEX);
    }

    public boolean isValidName(String firstName) {
        return firstName.matches(NAME_REGEX);
    }

    public boolean isValidBirthDate(LocalDateTime birthDate) {
        int years = Period.between(birthDate.toLocalDate(), LocalDateTime.now().toLocalDate()).getYears();
        return years >= 18;
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches(PHONE_NUMBER_REGEX);
    }

    @Override
    public void validate(User user) throws ValidatorException {
        String error = "";

        if (!isValidMail(user.getMail())) error += "Invalid email!\n";
        if (!isValidPassword(user.getPassword())) error += "Invalid password! Requirements: min. 8 characters,; 1 uppercase, 1 lowercase, 1 digit.\n";
        if (!isValidName(user.getFirstName())) error += "Invalid first name! Only characters allowed.\n";
        if (!isValidName(user.getLastName())) error += "Invalid last name! Only characters allowed.\n";
        if (!isValidBirthDate(user.getBirthDate())) error += "Invalid birth date! You must be at least 18 years old.\n";
        if (!isValidPhoneNumber(user.getPhoneNumber())) error += "Invalid phone number! \n";

        if (!error.equals(""))
            throw new ValidatorException(error);
    }
}
