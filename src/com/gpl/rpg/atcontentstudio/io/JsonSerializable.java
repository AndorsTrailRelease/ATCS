package com.gpl.rpg.atcontentstudio.io;

import java.util.Map;

public interface JsonSerializable {
    Map toMap();
    void fromMap(Map map);
}
