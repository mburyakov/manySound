package manySound.exceptions;

public class SomethingAlreadyExistsException extends UserShownException {

    String name;

    public SomethingAlreadyExistsException(String name) {
        this.name = name;
    }

    public String getType() {
        return "Something";
    }

    @Override
    public String getMessage() {
        return getType() +" '" + name + "' already exists!";
    }

    @Override
    public String getHeader() {
        return getType() + " already exists";
    }

}
