package com.sf.mlp.ie;

import com.sf.mlp.ie.ds.BankRecordLabel;
import com.sf.mlp.ie.ds.BankRecordTemplate;
import com.sf.mlp.ie.ds.LabeledBankRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joms on 5/4/2016.
 * Module that uses regex to match a bank record and extract name, city and state using groups.
 */
public class TemplateMatcher {
    private static final List<BankRecordTemplate> bankRecordTemplates;

    static {
        //TODO: Initialize size for better performance
        bankRecordTemplates = new ArrayList<>();
        loadBankRecordTemplates();
    }

    private static void loadBankRecordTemplates() {
        String templateLine;
        try(BufferedReader br = new BufferedReader(new FileReader
                ("/Users/Yiqing/Desktop/InfoExtract/src/com/sf/mlp/ie/data/templates.csv"))) {
            while ((templateLine = br.readLine()) != null) {
                String[] templateRow = templateLine.split("\t");
                String[] garbageGpNosStr = Arrays.copyOfRange(templateRow, 6, templateRow.length);
                int[] garbageGpNosInt = new int[garbageGpNosStr.length];
                for (int i=0; i<garbageGpNosStr.length; i++)
                    garbageGpNosInt[i] = Integer.parseInt(garbageGpNosStr[i]);

                BankRecordTemplate template = new BankRecordTemplate(templateRow[0],
                        templateRow[1].charAt(0), Integer.parseInt(templateRow[2]),
                        Integer.parseInt(templateRow[3]), Integer.parseInt(templateRow[4]),
                        Integer.parseInt(templateRow[5]), garbageGpNosInt);
                bankRecordTemplates.add(template);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadAll() {
        //if checksum
        bankRecordTemplates.clear();
        loadBankRecordTemplates();
    }

    //Use a utility function from a different class.
    public boolean matchTemplatesAndClean(LabeledBankRecord labeledBR) {
        /*
         * Matches the bank record against templates and labels the bank record.
         * Condenses consecutive garbage characters to one and strips garbage characters.
         */
        String br = labeledBR.getBankRecord();
        for (BankRecordTemplate brt:bankRecordTemplates) {
            Pattern p = brt.getRegexPattern();
            Matcher m  = p.matcher(br);
            //TODO: Try to use group 0 instead of matches
            if (m.matches()) {
                LinkedHashMap<BankRecordLabel, int[]> labelGpNoMap = brt.getLabelGpNoMap();
                for (BankRecordLabel brl:labelGpNoMap.keySet())
                    if (labelGpNoMap.get(brl)[0] > 0)
                        for (int gpNo:labelGpNoMap.get(brl))
                            labeledBR.setLabels(m.start(gpNo), m.end(gpNo), brl);

                if (brt.isResolved()) return true;

                int[] garbageGpNos = labelGpNoMap.get(BankRecordLabel.GARBAGE);
                if (garbageGpNos[0] > 0) {
                    for (int i = 0; i < garbageGpNos.length; i++)
                        labeledBR.condenseGarbage(m.start(i), m.end(i));
                }
                break;
            }
        }
        //labeledBR.stripGarbage();

        return false;
    }

}
