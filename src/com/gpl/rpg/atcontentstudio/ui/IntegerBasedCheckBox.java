package com.gpl.rpg.atcontentstudio.ui;

import javax.swing.*;

public class IntegerBasedCheckBox extends JCheckBox {

    private static final long serialVersionUID = 3941646360487399554L;

    static final Integer one = 1;

    public Integer getIntegerValue() {
        return isSelected() ? one : null;
    }

    public void setIntegerValue(Integer val) {
        setSelected(val != null && val.equals(one));
    }

}
