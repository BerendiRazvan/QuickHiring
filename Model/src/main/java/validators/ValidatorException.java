package validators;

public class ValidatorException extends Exception{
    private final String message;

    @Override
    public String getMessage() {return message;}

    public ValidatorException(String message) {this.message = message;}
}