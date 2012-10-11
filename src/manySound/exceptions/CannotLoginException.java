package manySound.exceptions;

public class CannotLoginException extends UserShownException {

    String user;

    public CannotLoginException(String login) {
        user = login;
    }

    @Override
    public String getMessage() {
        return "Login as user '" + user + "' failed.";
    }

    @Override
    public String getHeader() {
        return "Login failed.";
    }
}
