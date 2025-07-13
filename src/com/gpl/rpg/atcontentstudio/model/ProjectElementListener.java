package com.gpl.rpg.atcontentstudio.model;

public interface ProjectElementListener {

    public void elementAdded(GameDataElement added, int index);

    public void elementRemoved(GameDataElement removed, int index);

    public Class<? extends GameDataElement> getDataType();

}
