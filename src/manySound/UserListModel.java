package manySound;

import javax.swing.*;
import java.util.ArrayList;

public class UserListModel extends AbstractListModel<String> {

    ArrayList<String> userList;

    UserListModel() {
        userList = new ArrayList<>();
        userList.add("Fetching users...");
    }

    void update(ArrayList<String> list) {
        userList = list;
        fireContentsChanged(this, 0, getSize());
    }

    @Override
    public int getSize() {
        return userList.size();
    }

    @Override
    public String getElementAt(int index) {
        return userList.get(index);
    }

}
