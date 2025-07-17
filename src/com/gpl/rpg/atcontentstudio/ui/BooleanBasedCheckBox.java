package com.gpl.rpg.atcontentstudio.ui;

import javax.swing.*;

public class BooleanBasedCheckBox extends JCheckBox {

    private static final long serialVersionUID = 3941646360487399554L;

    public Boolean getBooleanValue() {
        return isSelected() ? Boolean.TRUE : null;
    }

    public void setBooleanValue(Boolean val) {
        setSelected(val != null && val);
    }

}
