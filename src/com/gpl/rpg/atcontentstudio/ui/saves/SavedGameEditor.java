package com.gpl.rpg.atcontentstudio.ui.saves;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.saves.SavedGame;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.Editor;

import javax.swing.*;
import java.awt.*;

public class SavedGameEditor extends Editor {

    private static final long serialVersionUID = 6055910379650778737L;

    public SavedGameEditor(SavedGame save) {
        this.name = save.loadedSave.displayInfo;
        this.icon = new ImageIcon(DefaultIcons.getHeroIcon());
        this.target = save;
        setLayout(new BorderLayout());
        add(new JScrollPane(new com.gpl.rpg.andorstrainer.ui.SavedGameEditor(save.loadedSave, ATContentStudio.frame)), BorderLayout.CENTER);

    }

    @Override
    public void targetUpdated() {
        // TODO Auto-generated method stub

    }

}
