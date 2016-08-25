package com.sf.mlp.ie.test;

import com.sf.mlp.ie.IEResult;
import com.sf.mlp.ie.InformationExtraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Joms on 6/3/2016.
 */
public class CsvTest {
    private String fileName;
    private int bankRecordFieldNumber;
    private int resolvedNameFieldNumber;
    private int txAddressFieldNumber;
    private int txCityFieldNumber;
    private int txStateFieldNumber;
    private boolean headerPresent;
    private String delimiter;
    private int noOfFields;

    public CsvTest(String fileName, int bankRecordFieldNumber, int resolvedNameFieldNumber, boolean headerPresent,
                   String delimiter, int noOfFields) {
        this.fileName = fileName;
        this.bankRecordFieldNumber = bankRecordFieldNumber;
        this.resolvedNameFieldNumber = resolvedNameFieldNumber;
        this.headerPresent = headerPresent;
        this.delimiter = delimiter;
        this.txAddressFieldNumber = 0;
        this.txCityFieldNumber = 0;
        this.txStateFieldNumber = 0;
        this.noOfFields = noOfFields;
    }

    public CsvTest(String fileName, int bankRecordFieldNumber, int resolvedNameFieldNumber, boolean headerPresent) {
        this.fileName = fileName;
        this.bankRecordFieldNumber = bankRecordFieldNumber;
        this.resolvedNameFieldNumber = resolvedNameFieldNumber;
        this.headerPresent = headerPresent;
        this.delimiter = ",";
        this.txAddressFieldNumber = 0;
        this.txCityFieldNumber = 0;
        this.txStateFieldNumber = 0;
        this.noOfFields = 0;
    }

    private String[] split(String line) {
        String[] row = line.split(delimiter, -1);
        if (row.length == this.noOfFields)
            return row;

        ArrayList<String> r = new ArrayList<>();
        int fieldEnd, fieldStart, counter, fieldsCounter=0;

        while (fieldsCounter<noOfFields) {
            if (line.startsWith("\"")) {
                fieldEnd = line.indexOf("\"" + delimiter);
                counter = fieldEnd + 2;
                fieldStart = 1;
            }
            else {
                fieldEnd = line.indexOf(delimiter);
                counter = fieldEnd + 1;
                fieldStart = 0;
            }

            if (fieldsCounter==noOfFields-1) {
                fieldEnd = line.length();
                counter = fieldEnd>0 ? fieldEnd - 1 : 0;
            }

            r.add(line.substring(fieldStart, fieldEnd));
            line = line.substring(counter);
            fieldsCounter++;
        }
        return r.toArray(new String[r.size()]);
    }

    public void readCsvInput() throws IOException{
        InformationExtraction ie;
        DupBankTxClassifier dup;
        File output = new File("validation_ie_new_0825.txt");
        FileWriter outputFile = new FileWriter(output);
        FileWriter caughtByRuleOutputFile = new FileWriter(new File("caughtByRule_0825.txt"));
        int nullCount = 0;
        int lineNo = 0;
        int caughtByRuleNo = 0;
        String inputLine = "";
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            if (headerPresent) {
                outputFile.write(br.readLine());
                outputFile.write("\tnew_ie_resolved_name\tnew_ie_resolved_city\tnew_ie_resolved_state\n");
            }
            while ((inputLine = br.readLine()) != null) {
                //if (inputLine.startsWith("2557289831"))
                //    System.out.println();
                //System.out.println(inputLine);
                String[] inputRow = inputLine.split("\t");
                //System.out.println(lineNo);
                String bankRecord = inputRow[bankRecordFieldNumber];
                String resolvedName = inputRow[resolvedNameFieldNumber];
                String address = inputRow[txAddressFieldNumber];
                String city = inputRow[txCityFieldNumber];
                String state = inputRow[txStateFieldNumber];
                
                //System.out.println(bankRecord + "|||||" + resolvedName);

                if (bankRecord.startsWith("\"") && bankRecord.endsWith("\"")) {
                    int start, end;
                    start = bankRecord.length()>1 ? 1:0;
                    end = bankRecord.length()<1 ? 0:bankRecord.length()-1;
                    bankRecord = bankRecord.substring(start, end);
                }

                if (resolvedName.startsWith("\"") && resolvedName.endsWith("\"")) {
                    int start, end;
                    start = resolvedName.length()>1 ? 1:0;
                    end = resolvedName.length()<1 ? 0:resolvedName.length()-1;
                    resolvedName = resolvedName.substring(start, end);
                }

                //System.out.println(bankRecord + "|||||" + resolvedName);
                
                /*dup = new DupBankTxClassifier(bankRecord, resolvedName);
                if (dup.isBankTx()) {
                    //System.out.println(inputLine + "," + "" + "," + "" + "," + "");
                    caughtByRuleOutputFile.write(inputLine + "\n");
                    caughtByRuleNo++;
                    continue;
                }*/
                //System.out.println(bankRecord);
                if (bankRecord==null || bankRecord.trim().equals("") || bankRecord.toUpperCase().equals("NULL")) {
                    if (resolvedName == null || resolvedName.trim().equals("") || resolvedName.toUpperCase().equals("NULL"))
                        bankRecord = "";
                    else
                        bankRecord = resolvedName;
                }
                if (bankRecord.equals("")) {
                    nullCount ++;
                    System.out.println(inputLine + "," + "" + "," + "" + "," + "");
                    continue;
                }
                //System.out.println(bankRecord);
                ie = new InformationExtraction(bankRecord, resolvedName, address, city, state);
                //System.out.println(bankRecord+","+resolvedName);
                IEResult res = ie.extract_info();
                
                outputFile.write(inputLine + "\t" + "\""+res.getName()+"\"" + "\t" + "\""+res.getCity()+"\"" + "\t" +
                        "\""+res.getState()+"\"\n");
                lineNo++;
            }
            System.out.println(lineNo);
            System.out.println(nullCount);
            System.out.println(caughtByRuleNo);
            outputFile.close();
            caughtByRuleOutputFile.close();
        }
        catch(IOException e) {
            System.out.println(inputLine);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*
        String fileName = "C:\\Users\\Joms\\Desktop\\FPs.csv";
        CsvTest ct = new CsvTest(fileName, 4, 5, false, ",");
        ct.readCsvInput();
        */

        //String fileName = "C:\\Users\\Joms\\Desktop\\04-27-16-blaster\\data\\fused\\fused_052716.csv";
        String fileName = "/Users/Yiqing/Desktop/ieImprove/validation_ie_0825.txt";
        //String fileName = "C:\\Users\\Joms\\Desktop\\fused_with_ie_052716.csv";
        CsvTest ct = new CsvTest(fileName, 0, 1, true, "\t", 12);
        try{
            ct.readCsvInput();
        }catch(IOException ioe){
            System.out.println("");
        }
    }
}
