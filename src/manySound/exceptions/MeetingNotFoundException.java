package manySound.exceptions;

public class MeetingNotFoundException extends UserShownException {

    int meeting;

    public MeetingNotFoundException(int meeting) {
        this.meeting = meeting;
    }

    @Override
    public String getMessage() {
        return "Meeting not found: \"" + meeting + "\"";
    }

    @Override
    public String getHeader() {
        return "Meeting not found";
    }
}
