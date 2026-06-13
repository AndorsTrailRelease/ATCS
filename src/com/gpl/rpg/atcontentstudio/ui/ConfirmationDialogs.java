package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;

import javax.swing.*;
import java.awt.*;

public final class ConfirmationDialogs {

    private ConfirmationDialogs() {
    }

    // Project delete dialogs
    public static boolean confirmProjectDelete() {
        return confirmProjectDelete(ATContentStudio.frame);
    }

    public static boolean confirmProjectDelete(Component parent) {
        return JOptionPane.showConfirmDialog(
                parent,
                "Are you sure you wish to delete this project ?\nAll files created for it will be deleted too...",
                "Delete this project ?",
                JOptionPane.OK_CANCEL_OPTION
        ) == JOptionPane.OK_OPTION;
    }

    /**
     * @param elementCount - Number of elements that will be deleted (e.g., treeview bulk select)
     * @return true if the user confirmed the deletion
     */
    public static boolean confirmDelete(int elementCount) {
        return confirmDelete(ATContentStudio.frame, elementCount);
    }

    /**
     *
     * @param parent - Parent component of the dialogue (influences placement)
     * @param elementCount
     * @return true if the user confirmed the deletion
     */
    public static boolean confirmDelete(Component parent, int elementCount) {
        int confirm = JOptionPane.showOptionDialog(
                parent,
                "Are you sure you want to delete " + elementCount + " selected elements?\n\nAny changes or new content in these elements will be lost.",
                "Confirm delete",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Cancel", "Delete"},
                "Cancel"
        );
        return confirm == 1;
    }

    /**
     * @param element - GDE to be deleted
     * @return  true if the user confirmed the deletion
     */
    public static boolean confirmDelete(GameDataElement element) {
        return confirmDelete(ATContentStudio.frame, element);
    }

    /**
     * @param parent - Parent component of the dialogue (influences placement)
     * @param element - GDE to be deleted
     * @return true if the user confirmed the deletion
     */
    public static boolean confirmDelete(Component parent, GameDataElement element) {
        String message;
        String title;
        String[] options;
        if (element.getDataType() == GameSource.Type.altered) {
            message = "Are you sure you want to revert '" + element.getDesc() + "' to the original version?\n\nAny changes you have made will be lost.";
            title = "Confirm revert";
            options = new String[]{"Cancel", "Revert"};
        } else {
            message = "Are you sure you want to delete '" + element.getDesc() + "'?";
            title = "Confirm delete";
            options = new String[]{"Cancel", "Delete"};
        }

        int confirm = JOptionPane.showOptionDialog(
                parent,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                "Cancel"
        );
        return confirm == 1;
    }
}



