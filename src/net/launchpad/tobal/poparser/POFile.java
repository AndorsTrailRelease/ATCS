/**
 *
 * @author Balázs Tóth (tobal17@gmail.com)
 * 
 * Modified by Kevin POCHAT for ATCS
 */
package net.launchpad.tobal.poparser;

import java.io.File;
import java.util.Vector;

public class POFile
{
    private POEntry[] entries;
    private POEntry header;
    private File file;

    POFile(POEntry[] entries, POEntry header, File file)
    {
        this.entries = entries;
        this.header = header;
        this.file = file;
    }

    /**
     * Returns with the name of the po file
     * @return name of po file
     */
    public String getFileName()
    {
        return file == null ? null : file.getAbsolutePath();
    }

    /**
     * Returns with the POEntry object array
     * @return POEntry array
     */
    public POEntry[] getEntryArray()
    {
        return entries;
    }

    /**
     * Gets the POEntry object specified by the index
     * @param index, index of the entry
     * @return one POEntry object
     */
    public POEntry getEntry(int index)
    {
        return entries[index];
    }

    /**
     * Returns how many entries are there in the po file
     * @return count of entries
     */
    public int getEntryLength()
    {
        return entries.length;
    }
    
    public POEntry getHeader() {
    	return header;
    }

    /**
     * Checks if the specified flag is set in the entry,
     * given by the entry index.
     * @param flag, string representing the flag
     * @param entryIndex, index of the entry to examine
     * @return true, if the flag is set, false otherwise
     */
    public boolean checkFlag(String flag, int entryIndex)
    {
        boolean status = false;
        Vector<String> strings;
        strings = entries[entryIndex].getStringsByType(POEntry.StringType.FLAG);
        if (strings != null)
        {
            for(int i = 0; i < strings.size(); i++)
            {
                if (strings.get(i).contains(flag))
                {
                    status = true;
                }
            }
        }
        return status;
    }

    /**
     * Returns with all the strings of the given type, from
     * the specified entry.
     * @param entryIndex
     * @param type
     * @return String array of specified type
     */
    public String[] getStringsFromEntryByType(int entryIndex, POEntry.StringType type)
    {
        Vector<String> vector = entries[entryIndex].getStringsByType(type);
        String[] str = new String[vector.size()];
        for(int i = 0; i < str.length; i++)
        {
            str[i] = vector.get(i);
        }
        return str;
    }

    /**
     * For debug purposes
     */
    public void printFile()
    {
        for(int i = 0; i < entries.length; i++)
        {
            POLine[] lines = entries[i].getLines();
            for(int j = 0; j < lines.length; j++)
            {
                Vector<String> strings = lines[j].getStrings();
                for(int k = 0; k < strings.size(); k++)
                {
                    System.out.println(strings.get(k));
                }
            }
        }
    }

    /**
     * For debug purposes
     */
    public void printHeader()
    {
        POLine[] lines = header.getLines();
        for(int j = 0; j < lines.length; j++)
        {
            Vector<String> strings = lines[j].getStrings();
            for(int k = 0; k < strings.size(); k++)
            {
                System.out.println(strings.get(k));
            }
        }
    }
}
