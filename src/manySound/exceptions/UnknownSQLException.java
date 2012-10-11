package manySound.exceptions;

import java.sql.SQLException;

public class UnknownSQLException extends UserShownException {

    SQLException sqlException;

    public UnknownSQLException(SQLException e) {
        sqlException = e;
    }

    @Override
    public String getMessage() {
        return "SQL return error " + sqlException.getErrorCode() + ": " + sqlException.getMessage();
    }

    @Override
    public String getHeader() {
        return "SQL returned error";
    }
}
