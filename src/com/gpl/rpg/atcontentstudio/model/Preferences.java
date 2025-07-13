package com.gpl.rpg.atcontentstudio.model;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Preferences implements Serializable {

    private static final long serialVersionUID = 2455802658424031276L;

    public Dimension windowSize = null;
    public Map<String, Integer> splittersPositions = new HashMap<String, Integer>();

    public Preferences() {

    }

}
