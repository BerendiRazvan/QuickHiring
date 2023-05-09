package validators;


public interface Validator<T> {
    void validate(T elem) throws ValidatorException;
}