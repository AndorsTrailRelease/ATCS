package com.gpl.rpg.atcontentstudio.io;

import java.io.StringWriter;

public class JsonPrettyWriter extends StringWriter {

    private int indentLevel = 0;
    private String indentText = "    ";

    public JsonPrettyWriter() {
        super();
    }

    public JsonPrettyWriter(String indent) {
        super();
        this.indentText = indent;
    }

    @Override
    public void write(int c) {
        if (((char) c) == '[' || ((char) c) == '{') {
            super.write(c);
            super.write('\n');
            indentLevel++;
            writeIndentation();
        } else if (((char) c) == ',') {
            super.write(c);
            super.write('\n');
            writeIndentation();
        } else if (((char) c) == ']' || ((char) c) == '}') {
            super.write('\n');
            indentLevel--;
            writeIndentation();
            super.write(c);
        } else {
            super.write(c);
        }

    }

    //Horrible hack to remove the horrible escaping of slashes in json-simple....
    @Override
    public void write(String str) {
        super.write(str.replaceAll("\\\\/", "/"));
    }

    private void writeIndentation() {
        for (int i = 0; i < indentLevel; i++) {
            super.write(indentText);
        }
    }

}
