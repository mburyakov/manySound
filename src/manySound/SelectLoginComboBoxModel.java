package manySound;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class SelectLoginComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

    UserListModel userListModel;
    Object selectedItem;

    SelectLoginComboBoxModel(UserListModel userListModel) {
        this.userListModel = userListModel;
        userListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                fireIntervalAdded(e.getSource(), e.getIndex0(), e.getIndex1());
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                fireIntervalRemoved(e.getSource(), e.getIndex0(), e.getIndex1());
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                fireContentsChanged(e.getSource(), e.getIndex0(), e.getIndex1());
            }
        });
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedItem = anItem;
        fireContentsChanged(this, 0, getSize());
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return userListModel.getSize();
    }

    @Override
    public String getElementAt(int index) {
        return userListModel.getElementAt(index);
    }
}
