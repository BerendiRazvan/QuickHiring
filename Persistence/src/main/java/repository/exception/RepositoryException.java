package repository.exception;


public class RepositoryException extends Exception {
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }

    public RepositoryException(String message) {
        this.message = message;
    }
}