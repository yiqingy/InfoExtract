package com.sf.mlp.ie;

import com.sf.mlp.ie.ds.BankRecordLabel;
import com.sf.mlp.ie.ds.LabeledBankRecord;

/**
 * Created by Joms on 4/8/2016.
 * Main Class for Information Extraction.
 */

public class InformationExtraction {

    private String bank_record;
    private String resolved_name;
    private String tx_address;
    private String tx_city;
    private String tx_state;

    public InformationExtraction(String bank_record, String resolved_name,
                                 String tx_address, String tx_city, String tx_state) {
        this.bank_record = bank_record;
        this.resolved_name = resolved_name;
        this.tx_address = tx_address;
        this.tx_city = tx_city;
        this.tx_state = tx_state;
    }


    private int noOfValidCharacters(String extracted_name) {
        int validCharacterCount = 0;
        for (int i=0; i<extracted_name.length(); i++)
            if (Character.isLetterOrDigit(extracted_name.charAt(i))) validCharacterCount++;

        return validCharacterCount;
    }


    private boolean isResultGood(String extracted_name) {
        /*
         * This function keeps the lookup in check. Makes sure that the lookup does not make FPs
         * by avoiding predicting small names, cities and other hardcoded values.
         */
        //TODO: Remove hardcoded values

        extracted_name = extracted_name.trim();
        //Checking if extracted online merchant names are proper
        if (extracted_name.endsWith(" COM"))
            extracted_name = extracted_name.replace(" COM", "");
        return true;
        /*
        int validCharCount = noOfValidCharacters(extracted_name);
        if (this.tx_city!=null && extracted_name.toUpperCase().equals(tx_city.toUpperCase())
                || extracted_name.toUpperCase().equals("NORWALK"))
            return false;
        if (extracted_name.toUpperCase().equals("CHASE")) return false;
        if (this.tx_address!=null && !(this.tx_address.trim().equals("")) && validCharCount>2) return true;
        String[] nameTokens = extracted_name.split(" ");
        if (nameTokens.length>1 && extracted_name.length()>4) return true;
        if (nameTokens.length==1 && this.tx_city!=null && !(this.tx_city.trim().equals("")) &&
                validCharCount>3) return true;
        if (validCharCount>4) return true;
        return false;*/
    }


    public IEResult extract_info() {
        /*
         * Entry point for information extraction. Let there be light!
         */
        IEResult emptyResult = new IEResult();

        //SpecialTransactionClassifier cl = new SpecialTransactionClassifier(this.bank_record);
        //if (cl.isSpecialTransaction()) return emptyResult;

        LabeledBankRecord labeledBR = new LabeledBankRecord(this.bank_record);
        TemplateMatcher tm = new TemplateMatcher();
        boolean flag = tm.matchTemplatesAndClean(labeledBR);
        if (flag) {
            String name = labeledBR.getInfoByLabel(BankRecordLabel.NAME);
            String city = labeledBR.getInfoByLabel(BankRecordLabel.CITY);
            String state = labeledBR.getInfoByLabel(BankRecordLabel.STATE);

            //if (isResultGood(name))
            return new IEResult(name, city, state);
            //else
              //  return emptyResult;
        }

        GarbageLabeler gl = new GarbageLabeler();
        gl.labelBankRecord(labeledBR);
        if (!labeledBR.isValid()) {
            return emptyResult;
        }
        InfoLabeler csl = new InfoLabeler();
        InfoLabelerResult infoLabelerResult = csl.labelBankRecord(labeledBR);

        if (infoLabelerResult==null) return emptyResult;

        String name = labeledBR.getBankRecordSubstring(infoLabelerResult.getName());
        String city = labeledBR.getBankRecordSubstring(infoLabelerResult.getCity());
        String state = labeledBR.getBankRecordSubstring(infoLabelerResult.getState());

        if (isResultGood(name))
            return new IEResult(name, city, state);
        else
            return emptyResult;
    }
}
