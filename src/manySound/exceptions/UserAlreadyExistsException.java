package manySound.exceptions;

public class UserAlreadyExistsException extends UserShownException {

    String login;

    public UserAlreadyExistsException(String login) {
        this.login = login;
    }

    @Override
    public String getMessage() {
        return "User '" + login + "' already exists!";
    }

    @Override
    public String getHeader() {
        return "User already exists";
    }
}
