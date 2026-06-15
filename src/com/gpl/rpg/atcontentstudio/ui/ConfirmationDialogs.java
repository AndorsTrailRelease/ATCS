package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Workspace;

import java.awt.Component;

import javax.swing.JOptionPane;

public final class ConfirmationDialogs {

    private ConfirmationDialogs() {
    }

    // Project delete dialogs
    public static boolean confirmProjectDelete() {
        return confirmProjectDelete(ATContentStudio.frame);
    }

    public static boolean confirmProjectDelete(Component parent) {
        int confirm = JOptionPane.showOptionDialog(
                parent,
                "Are you sure you wish to delete this project?\n\nAll files created for it will be deleted too...",
                "Delete this project ?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Cancel", "Delete"},
                "Cancel"
        );
            return confirm == 1;

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
     * @param elementCount - Number of elements that will be deleted (e.g., treeview bulk select)
     * @return true if the user confirmed the deletion
     */
    public static boolean confirmDelete(Component parent, int elementCount) {
        int confirm = JOptionPane.showOptionDialog(
                parent,
                "Are you sure you want to delete %d selected elements?\n\nAny changes or new content in these elements will be lost.".formatted(elementCount),
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
            message = "Are you sure you want to revert '%s' to the original version?\n\nAny changes you have made will be lost.".formatted(element.getDesc());
            title = "Confirm revert";
            options = new String[]{"Cancel", "Revert"};
        } else {
            message = "Are you sure you want to delete '%s' ?\n\nAny new content in this element will be lost.".formatted(element.getDesc());
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

    /**
     * @param actionLabel - label for the action to be done (e.g., "restart" or "exit")
     * @return - true if the user confirmed/approved the action
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean confirmExitOrRestart(String actionLabel) {
        return confirmExitOrRestart(ATContentStudio.frame, actionLabel);
    }

    /**
     * @param parent - Parent component of the dialogue (influences placement)
     * @param actionLabel - label for the action to be done (e.g., "restart" or "exit")
     * @return - true if the user confirmed/approved the action
     */
    public static boolean confirmExitOrRestart(Component parent, String actionLabel) {
        if (Workspace.activeWorkspace == null || !Workspace.activeWorkspace.needsSaving()) {
            return true;
        }

        String normalizedAction = actionLabel == null ? "proceed" : actionLabel.trim();
        if (normalizedAction.isEmpty()) {
            normalizedAction = "proceed";
        }
        String capitalizedAction = Character.toUpperCase(normalizedAction.charAt(0)) + normalizedAction.substring(1);

        int answer = JOptionPane.showConfirmDialog(
                parent,
                "There are unsaved changes in your workspace.\n%sing ATCS will discard these changes.\nDo you really want to %s?".formatted(capitalizedAction, normalizedAction),
                "Unsaved changes. Confirm %s.".formatted(normalizedAction),
                JOptionPane.YES_NO_OPTION
        );
        return answer == JOptionPane.YES_OPTION;
    }
}



