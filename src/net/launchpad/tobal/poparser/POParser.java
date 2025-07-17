/**
 *
 * @author Balázs Tóth (tobal17@gmail.com)
 *
 * Based on the work of István Nyitrai
 * 
 * Modified by Kevin POCHAT for ATCS
 */
package net.launchpad.tobal.poparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Vector;

public class POParser
{
    private POEntry[] entries;
    private POEntry header;
//    private File file;
    private POEntry.StringType parserMode;

    /**
     * Creates a POParser object. Use getPOFile() method,
     * to access parsed data.
     * @param file, File object of the PO file
     */
    public POParser()
    {
        parserMode = null;
    }

    public POFile parseFile(File file)
    {
        return parse(file);
    }
    
    public POFile parseStream(BufferedReader br) throws IOException {
    	return parse(br);
    }

    private String unQuote(String string)
    {
        String str = new String();
        if(string.startsWith("\""))
        {
            str = string.substring(1);
            string = str;
        }
        if(string.endsWith("\""))
        {
            str = string.substring(0, string.length()-1);
        }
        return str;
    }

    private POFile parse(File file)
    {
    	POFile result = null;
        try
        {
            BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
            result = parse(br);
            br.close();
        }
        catch (java.io.FileNotFoundException e)
        {
            System.out.println(e.toString());
        }
        catch (java.io.IOException e) {
            System.out.println(e.toString());
        }

        return result;
    }
    
    private POFile parse(BufferedReader br) throws IOException {
    	Vector<String> rawentry = new Vector<String>(1, 1);
        Vector<Vector<String>> rawentries = new Vector<Vector<String>>();
        String line;
        int id = 0;
        while((line = br.readLine()) != null)
        {
            if(!line.equals(""))
            {
                if(!line.startsWith("#~")) // ignore
                {
                    rawentry.add(line);
                }
            }
            else
            {
                if(rawentry.size() > 0)
                {
                    rawentry.add(0, String.valueOf(id));
                    id++;
                    rawentries.add(new Vector<String>(rawentry));
                    rawentry = new Vector<String>(1, 1);
                }
            }
        }

        if(!rawentry.equals(rawentries.lastElement()) && rawentry.size() > 0)
        {
            rawentry.add(0, String.valueOf(id));
            rawentries.add(new Vector<String>(rawentry));
        }
        this.header = parseHeader(rawentries);
        this.entries = parseEntries(rawentries);
        
        return new POFile(entries, header, null);
    }

    private POEntry parseHeader(Vector<Vector<String>> vectors)
    {
        POEntry tempheader = new POEntry();

        // is this header?
        Vector<String> rawentry = vectors.get(0);
        if(new Integer(rawentry.get(0)) == 0 && rawentry.contains("msgid \"\""))
        {
            for(int i = 1; i < rawentry.size(); i++)
            {
                String str = rawentry.get(i);
                tempheader.addLine(POEntry.StringType.HEADER, str);
            }
            return tempheader;
        }
        else
        {
            return null;
        }
    }

    private POEntry[] parseEntries(Vector<Vector<String>> vectors)
    {
        String line;
        boolean thereIsHeader = false;

        // is this header
        Vector<String> rawentry = vectors.get(0);
        if(new Integer(rawentry.get(0)) == 0 && rawentry.contains("msgid \"\""))
        {
            thereIsHeader = true;
        }

        int size;
        if(thereIsHeader)
        {
            size = vectors.size()-1;
        }
        else
        {
            size = vectors.size();
        }

        POEntry[] tempentries = new POEntry[size];

        for(int i = 0; i < size; i++)
        {
            POEntry entry = new POEntry();

            if(thereIsHeader)
                rawentry = vectors.get(i+1);
            else
                rawentry = vectors.get(i);
            
            rawentry.remove(0);
            for(int j = 0; j < rawentry.size(); j++)
            {
                line = rawentry.get(j);
                POEntry.StringType strType = null;
                int subStrIndex = 0;
                if(line.startsWith("#"))
                {
                    parserMode = null;
                    if(line.startsWith("# "))
                    {
                        strType = POEntry.StringType.TRLCMNT;
                        if (line.startsWith("#  "))
                        {
                            subStrIndex = 3;
                        }
                        else
                        {
                            subStrIndex = 2;
                        }
                    }
                    if(line.startsWith("#."))
                    {
                        strType = POEntry.StringType.EXTCMNT;
                        subStrIndex = 3;
                    }
                    if(line.startsWith("#:"))
                    {
                        strType = POEntry.StringType.REFERENCE;
                        subStrIndex = 3;
                    }
                    if(line.startsWith("#,"))
                    {
                        strType = POEntry.StringType.FLAG;
                        subStrIndex = 3;
                    }
                    if(line.startsWith("#|"))
                    {
                        // TODO: can these comments be multi line? if no,
                        // drop support for it
                        if(line.startsWith("#| msgctxt "))
                        {
                            strType = POEntry.StringType.PREVCTXT;
                            parserMode = strType;
                            subStrIndex = 11;
                        }
                        if(line.startsWith("#| msgid "))
                        {
                            strType = POEntry.StringType.PREVUNTRSTRSING;
                            parserMode = strType;
                            subStrIndex = 9;
                        }
                        if(line.startsWith("#| msgid_plural "))
                        {
                            strType = POEntry.StringType.PREVUNTRSTRPLUR;
                            parserMode = strType;
                            subStrIndex = 16;
                        }
                    }
                    String str = line.substring(subStrIndex);
                    entry.addLine(strType, str);
                }
                else if(line.startsWith("msg"))
                {
                    parserMode = null;
                    if(line.startsWith("msgctxt "))
                    {
                        strType = POEntry.StringType.MSGCTXT;
                        parserMode = strType;
                        subStrIndex = 8;
                    }
                    if(line.startsWith("msgid "))
                    {
                        strType = POEntry.StringType.MSGID;
                        parserMode = strType;
                        subStrIndex = 6;
                    }
                    if(line.startsWith("msgstr "))
                    {
                        strType = POEntry.StringType.MSGSTR;
                        parserMode = strType;
                        subStrIndex = 7;
                    }
                    // TODO: is unquoting nessessary?
                    String str = unQuote(line.substring(subStrIndex));
                    entry.addLine(strType, str);
                }
                else
                {
                    if(parserMode != null)
                    {
                        entry.addLine(parserMode, unQuote(line));
                    }
                }
            }
            tempentries[i] = entry;
        }

        return tempentries;
    }
}
