package manySound;

import manySound.exceptions.MeetingNotFoundException;
import manySound.exceptions.UnknownSQLException;

public class Instrument {

    public Meeting getMeeting() {
        return meeting;
    }

    public String getName() throws UnknownSQLException, MeetingNotFoundException {
        if (name == null) {
            DatabaseChanger.InstrumentProps props = DatabaseChanger.getInstance().getInstrumentProps(id, meeting.getUserSession().getUserName());
        }
        return name;
    }

    private int id;
    private Meeting meeting;
    private String name;

    public Instrument(int id, Meeting meeting) {
        new Instrument(id, meeting, null);
    }

    public Instrument(int id, Meeting meeting, String name) {
        this.id = id;
        this.meeting = meeting;
        this.name = name;
    }

}