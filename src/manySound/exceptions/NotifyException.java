package manySound.exceptions;

public class NotifyException extends UserShownException {

    String message;

    public NotifyException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getHeader() {
        return "Notify";
    }
}
