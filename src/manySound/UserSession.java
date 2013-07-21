package manySound;

import manySound.exceptions.UnknownSQLException;
import manySound.exceptions.UserShownException;

import java.util.ArrayList;
import java.util.Vector;

public class UserSession {

    public static final Vector<String> meetingRowNames;

    static {
        meetingRowNames = new Vector<>();
        meetingRowNames.add("id");
        meetingRowNames.add("Name");
        meetingRowNames.add("Description");
    }

    private String userName;

    private UserSession(String login) {
        userName = login;
    }

    public static UserSession tryLogin(String login, String password) {
        try {
            if (DatabaseChanger.getInstance().tryLogin(login, password)) {
                return new UserSession(login);
            } else {
                return null;
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            DatabaseChanger.die(e);
            return null;
        } catch (UserShownException e) {
            e.showMessage();
            return null;
        }
    }

    public String getUserName() {
        return userName;
    }

    public Vector<Vector<String>> getMeetingsList() throws UnknownSQLException {
        return DatabaseChanger.getInstance().fetchMeetings(userName);
    }
}
