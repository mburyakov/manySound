package manySound.exceptions;

public class UserAlreadyExistsException extends SomethingAlreadyExistsException {

    final static String type = "User";

    public UserAlreadyExistsException(String login) {
        super(login);
    }

    @Override
    public String getType() {
        return "User";
    }

}
