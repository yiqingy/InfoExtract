package com.sf.mlp.ie;

import com.sf.mlp.ie.ds.BankRecordLabel;
import com.sf.mlp.ie.ds.LabeledBankRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joms on 4/11/2016.
 */
public class GarbageLabeler {
    private static final List<Pattern> garbageRegexPatterns;
    private static final List<String> garbageStrings;

    static {
        //TODO: Initialize size for better performance
        garbageRegexPatterns = new ArrayList<>();
        garbageStrings = new ArrayList<>();
        loadGarbageRegexPatterns();
        loadGarbageStrings();
    }

    //Use a utility function from a different class for the below two functions.
    private static void loadGarbageRegexPatterns() {
        String regexLine;
        try(BufferedReader br = new BufferedReader(new FileReader
                ("/Users/Yiqing/Desktop/InfoExtract/src/com/sf/mlp/ie/data/garbageRegex.csv"))) {
            while ((regexLine = br.readLine()) != null) {
                String[] regexRow = regexLine.split(",,,,");
                Pattern p = Pattern.compile(regexRow[0]);
                garbageRegexPatterns.add(p);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadGarbageStrings() {
        String garbageLine;
        try(BufferedReader br = new BufferedReader(new FileReader
                ("/Users/Yiqing/Desktop/InfoExtract/src/com/sf/mlp/ie/data/garbageString.csv"))) {
            while ((garbageLine = br.readLine()) != null) {
                String[] garbageRow = garbageLine.split(",");
                garbageStrings.add(garbageRow[0]);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadAll() {
        //if checksum
        garbageRegexPatterns.clear();
        loadGarbageRegexPatterns();
        garbageStrings.clear();
        loadGarbageStrings();
    }



    public void matchGarbageRegexes(LabeledBankRecord labeledBR) {
        String br = labeledBR.getBankRecord();
        for (Pattern p: garbageRegexPatterns) {
            Matcher m = p.matcher(br);
            //TODO: Does this work?
            while (m.find())
                labeledBR.setLabels(m.start(), m.end(), BankRecordLabel.GARBAGE);
        }
    }

    public void matchGarbageStrings(LabeledBankRecord labeledBR) {
        for (String s: garbageStrings) {
            labeledBR.setLabels(s, BankRecordLabel.GARBAGE);
        }
    }

    public void labelBankRecord(LabeledBankRecord labeledBR) {
        matchGarbageRegexes(labeledBR);
        matchGarbageStrings(labeledBR);
        //System.out.println("Labeled Bank Record: "+ labeledBR.getBankRecord());
        //System.out.println("Labels: "+labeledBR.getLabels());
        labeledBR.condenseGarbage();
        labeledBR.stripGarbage();
        //System.out.println("Labeled Bank Record: "+ labeledBR.getBankRecord());
        //System.out.println("Labels: "+labeledBR.getLabels());
    }

}
