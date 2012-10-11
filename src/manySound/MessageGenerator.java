package manySound;

public class MessageGenerator {

    public static String loggedOn(UserSession user) {
        if (user==null) {
            return "You are not logged on.";
        } else {
            return "You are currently logged on as '" + user.getUserName() + "'.";
        }
    }
}
