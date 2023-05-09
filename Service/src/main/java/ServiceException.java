public class ServiceException extends Exception {
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }

    public ServiceException(String message) {
        this.message = message;
    }
}
