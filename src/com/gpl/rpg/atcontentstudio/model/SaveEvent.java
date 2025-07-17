package com.gpl.rpg.atcontentstudio.model;

public class SaveEvent {

    public enum Type {
        moveToAltered,
        moveToCreated,
        alsoSave
    }

    public Type type;
    public GameDataElement target;

    public boolean error = false;
    public String errorText;

    public SaveEvent(SaveEvent.Type type, GameDataElement target) {
        this.type = type;
        this.target = target;
    }

    public SaveEvent(SaveEvent.Type type, GameDataElement target, boolean error, String errorText) {
        this.type = type;
        this.target = target;
        this.error = error;
        this.errorText = errorText;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SaveEvent)) return false;
        else return (((SaveEvent) obj).type == this.type) && (((SaveEvent) obj).target == this.target);
    }

}
