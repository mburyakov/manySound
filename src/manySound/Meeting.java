package manySound;

import manySound.exceptions.UnknownSQLException;
import manySound.exceptions.UserShownException;

import java.util.Vector;

public class Meeting {
    public UserSession getUserSession() {
        return userSession;
    }

    public int getMeetingId() {
        return meetingId;
    }

    private UserSession userSession;
    private int meetingId;

    public String getDescription() {
        return description;
    }

    private String description;
    private String name;

    /*public String getOwner() {
        return owner;
    }*/

    private Boolean isOwner;

    public boolean isOwner() {
        return isOwner;
    }

    public String getName() {
        return name;
    }

    Meeting(int id, UserSession us) throws UserShownException {
        userSession = us;
        meetingId = id;
        DatabaseChanger.MeetingProps props = DatabaseChanger.getInstance().getMeetingProps(id, us.getUserName());
        name = props.name;
        description = props.description;
        isOwner = props.isOwner;
    }

    DatabaseChanger.InstrumentList getInstrumentList() throws UnknownSQLException {
        return DatabaseChanger.getInstance().fetchInstruments(meetingId, userSession.getUserName());
    }

    Vector<String> getRecipientList(int instrument) throws UnknownSQLException {
        return DatabaseChanger.getInstance().fetchRecipients(meetingId, userSession.getUserName(), instrument);
    }

}