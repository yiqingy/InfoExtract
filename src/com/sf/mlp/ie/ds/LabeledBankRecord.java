package com.sf.mlp.ie.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joms on 4/12/2016.
 */

//This can be used for both bank record and resolved name
//TODO: Exception Handling
public class LabeledBankRecord {
    private String bankRecord;
    private List<BankRecordLabel> labels;
    private static final String GARBAGE_CHARACTER = "|";
    /*
    * Stores the internal state of the BankRecord. False by default. If condenseGarbage and stripGarbage
    * are called, this is set to True. If setLabels is called, this is set to False.
    * All other functions that yield number of groups and strings by group no should be called only if
    * this state is True. This should not be modified outside the class.
    */
    private boolean garbageCondensed;
    private boolean garbageStripped;

    public LabeledBankRecord(String bankRecord) {
        //System.out.println(bankRecord);
        this.bankRecord = bankRecord.toUpperCase();
        this.labels = new ArrayList<>(Collections.nCopies(bankRecord.length(), BankRecordLabel.UNKNOWN));
        this.garbageCondensed = false;
        this.garbageStripped = false;
    }

    public String getBankRecord() {
        return bankRecord;
    }

    public String getBankRecordSubstring(int start, int end) {
        if (start<=end && start>=0 && end<=bankRecord.length())
            return bankRecord.substring(start, end);
        return null;
    }

    public String getBankRecordSubstring(Range r) {
        if (r!=null)
            return getBankRecordSubstring(r.getStart(), r.getEnd());
        return null;
    }

    //TODO: Check if there are labels other than garbage.
    public boolean isValid() {return labels.size()>0;}

    public String getInfoByLabel(BankRecordLabel label) {
        /*
         * Returns the first group of simultaneously occurring characters of the specified label.
         */
        StringBuilder info = new StringBuilder("");
        boolean foundFirst = false;
        for (int i=0; i< labels.size(); i++) {
            if (labels.get(i) == label && foundFirst)
                info.append(bankRecord.charAt(i));
            else if (labels.get(i) != label && foundFirst)
                return info.toString();
            else if (labels.get(i) == label && !foundFirst) {
                foundFirst = true;
                info.append(bankRecord.charAt(i));
            }
        }
        return info.toString();
    }

    public BankRecordLabel getLabelAtIndex(int index) {
        return this.labels.get(index);
    }

    public int getFirstOccurrenceOfLabel(BankRecordLabel inputLabel) {
        for (int i=0; i<labels.size(); i++)
            if (labels.get(i) == inputLabel)
                return i;
        return -1;
    }

    public int getFirstOccurrenceOfLabel(int startIndex, BankRecordLabel inputLabel) {
        for (int i=startIndex; i<labels.size(); i++)
            if (labels.get(i) == inputLabel)
                return i;
        return -1;
    }


    public int findNoOfValidGroups() {
        /*
         * A group is defined as the number of non-garbage character groups separated by the garbage character.
         * Returns number of non-unique groups separated by GARBAGE labels.
         * This should be called only after condenseGarbage and stripGarbage are called.
         */
        int garbageCount = 0;
        if (!garbageCondensed) condenseGarbage();
        if (!garbageStripped) stripGarbage();

        for (BankRecordLabel label : labels)
            if (label == BankRecordLabel.GARBAGE) garbageCount++;

        return garbageCount+1;

        /* Alternate version without calling condense and strip garbage
        garbageCount = 0;
        if (labels.get(0) != BankRecordLabel.GARBAGE) garbageCount++;
        for(int i=1; i<labels.size(); i++)
            if (labels.get(i) != BankRecordLabel.GARBAGE && labels.get(i-1) == BankRecordLabel.GARBAGE)
                garbageCount++;
        return garbageCount;
        */
    }

    public ArrayList<String> getAllValidGroups() {
        /*
         * Returns list of all valid groups in order.
         */
        return new ArrayList<>();
    }

    public int findGroupNo(int index) {
        /*
         * Given an index within the bank_record, finds the index of the group to which that index belongs.
         * gpNo starts from 0.
         */
        if (!garbageCondensed) condenseGarbage();
        if (!garbageStripped) stripGarbage();

        if (index >= labels.size() || labels.get(index) == BankRecordLabel.GARBAGE) {
            //TODO: Raise exception?
            return -1;
        }
        int gpNo = 0;
        for (int i=0; i<labels.size(); i++) {
            if (i == index)
                return gpNo;
            if (labels.get(i) == BankRecordLabel.GARBAGE)
                gpNo++;
        }
        return gpNo;
    }

    public Range getGroupByIndex(int index) {
        /*
         * Returns the nth group of same labels. GARBAGE labels occurring together are not considered as a group.
         * Those are considered as separators for groups.
         */
        if (!garbageCondensed) condenseGarbage();
        if (!garbageStripped) stripGarbage();

        if (index<0) return null;
        int pos = 0, start=-1, end=labels.size();
        boolean foundIndex = false;

        for (int i=0; i<labels.size(); i++) {
            if (labels.get(i) == BankRecordLabel.GARBAGE) pos += 1;
            else if (index == pos && !foundIndex) {
                start = i;
                foundIndex = true;
            }
            else if (index != pos && foundIndex) {
                end = i-1;
                break;
            }
        }
        if (start>=0 && end>start) return new Range(start, end);
        return null;
    }

    public ArrayList<String> getGroupByLabel(BankRecordLabel label) {
        /*
         * Returns group(s) of the specified label in order of occurrence.
         */
        return new ArrayList<>();
    }

    public boolean verifyLabels(){
        /**
         * ?
         */
        return true;
    }

    public void setLabels(int start, int end, BankRecordLabel label) {
        //TODO: Function should return a boolean which specifies whether the setLabel was successful.
        //Check if start and end are valid.
        for (int i=start; i<end; i++)
            labels.set(i, label);
        if (label == BankRecordLabel.GARBAGE) {
            garbageCondensed = false;
            garbageStripped = false;
        }
    }

    public void setLabels(String s, BankRecordLabel label) {
        int start = bankRecord.indexOf(s.toUpperCase());
        if (start>=0) {
            int end = start + s.length();
            setLabels(start, end, label);
        }
    }


    public void stripGarbage() {
        /*
         * Remove all consecutive garbage and special unknown characters from both ends of the bankRecord
         */
        int i=0;
        while (i<labels.size() &&
                (labels.get(i) == BankRecordLabel.GARBAGE ||
                (labels.get(i) == BankRecordLabel.UNKNOWN && !(Character.isLetterOrDigit(bankRecord.charAt(i))))))
            i++;
        int start = i;
        i = bankRecord.length()-1;
        while (i>=0 &&
                (labels.get(i) == BankRecordLabel.GARBAGE ||
                (labels.get(i) == BankRecordLabel.UNKNOWN && !(Character.isLetterOrDigit(bankRecord.charAt(i))))))
            i--;
        int end = i;

        if (start<labels.size() && end>=start) {
            //if (start>0) {
            //    startGarbage = "|";
            //    start -= 1;
            //}
            //if (end<labels.size()-1) {
            //    endGarbage = "|";
            //    end += 1;
            //}
            bankRecord = bankRecord.substring(start, end + 1);
            labels = labels.subList(start, end + 1);
        }
        else {
            bankRecord = "";
            labels.clear();
        }

        garbageStripped = true;
    }

    public void condenseGarbage() {
        /*
         * Replaces all consecutive garbage and space characters with a single garbage character.
         */
        StringBuilder tempBankRecord = new StringBuilder();
        List<BankRecordLabel> tempLabels = new ArrayList<>(bankRecord.length());

        boolean garbageFlag;
        if (labels.get(0) == BankRecordLabel.GARBAGE) {
            tempBankRecord.append(GARBAGE_CHARACTER);
            tempLabels.add(BankRecordLabel.GARBAGE);
            garbageFlag = true;
        }
        else {
            tempBankRecord.append(bankRecord.charAt(0));
            tempLabels.add(labels.get(0));
            garbageFlag = false;
        }
        //TODO: Make sure this is bug free.
        for (int i=1; i<bankRecord.length(); i++) {
            if (garbageFlag && (labels.get(i) == BankRecordLabel.GARBAGE || bankRecord.charAt(i) == ' '))
                continue;
            else if (!garbageFlag && labels.get(i) == BankRecordLabel.GARBAGE) {
                tempBankRecord.append(GARBAGE_CHARACTER);
                tempLabels.add(BankRecordLabel.GARBAGE);
                garbageFlag = true;
            }
            else if (labels.get(i) != BankRecordLabel.GARBAGE) {
                tempBankRecord.append(bankRecord.charAt(i));
                tempLabels.add(labels.get(i));
                garbageFlag = false;
            }

            /*
            if ((labels.get(i) == BankRecordLabel.GARBAGE && labels.get(i-1) == BankRecordLabel.GARBAGE)     ||
                    (bankRecord.charAt(i) == ' ' && labels.get(i-1) == BankRecordLabel.GARBAGE))
                continue;
            else if (labels.get(i) == BankRecordLabel.GARBAGE) {
                tempBankRecord.append(GARBAGE_CHARACTER);
                tempLabels.add(BankRecordLabel.GARBAGE);
            }
            else {
                tempBankRecord.append(bankRecord.charAt(i));
                tempLabels.add(labels.get(i));
            }
            */
        }

        bankRecord = tempBankRecord.toString();
        labels = tempLabels;

        garbageCondensed = true;
    }

    /*
    public void condenseGarbage() {
        StringBuilder tempBankRecord = new StringBuilder();
        List<BankRecordLabel> tempLabels = new ArrayList<>(bankRecord.length());

        if (labels.get(0) == BankRecordLabel.GARBAGE ||
                (labels.get(0) == BankRecordLabel.UNKNOWN && bankRecord.charAt(0) == ' ')) {
            tempBankRecord.append(GARBAGE_CHARACTER);
            tempLabels.add(BankRecordLabel.GARBAGE);
        }
        else {
            tempBankRecord.append(bankRecord.charAt(0));
            tempLabels.add(labels.get(0));
        }


        for (int i=1; i<bankRecord.length(); i++) {
            if ((labels.get(i) == BankRecordLabel.GARBAGE ||
                    (labels.get(i) == BankRecordLabel.UNKNOWN && bankRecord.charAt(i) == ' '))
                    &&
                    (labels.get(i-1) == BankRecordLabel.GARBAGE ||
                    (labels.get(i-1) == BankRecordLabel.UNKNOWN && bankRecord.charAt(i-1) == ' ')))
                continue;
            else if (labels.get(i) == BankRecordLabel.GARBAGE) {
                tempBankRecord.append(GARBAGE_CHARACTER);
                tempLabels.add(BankRecordLabel.GARBAGE);
            }
            else if (labels.get(i) == BankRecordLabel.UNKNOWN && bankRecord.charAt(i) == ' ' &&
                     labels.get(i+1) == BankRecordLabel.UNKNOWN && bankRecord.charAt(i+1) == ' ' &&
                     i<bankRecord.length()-1) {
                tempBankRecord.append(GARBAGE_CHARACTER);
                tempLabels.add(BankRecordLabel.GARBAGE);
            }
            else {
                tempBankRecord.append(bankRecord.charAt(i));
                tempLabels.add(labels.get(i));
            }
        }

        bankRecord = tempBankRecord.toString();
        labels = tempLabels;

        garbageCondensed = true;
    }
    */


    public void condenseGarbage(int start, int end) {
        //TODO: Verify that only the specified label is present from start to end
        for (int i = start; i < end; i++) {
            if (i == end - 1) {
                labels.set(i, BankRecordLabel.GARBAGE);
                break;
            }
            labels.remove(i);
        }
        bankRecord = bankRecord.substring(0, start) + GARBAGE_CHARACTER +
                bankRecord.substring(end, bankRecord.length());

        garbageCondensed = true;
    }

    public boolean matchLabel(int index, BankRecordLabel label) {
        return labels.get(index) == label;
    }


    /*
     * TODO:
     * This function should not be used until the Range DS for different labels are added to this class.
     * If one label range is found and then an entry is deleted, it messes up everything. This problem is already
     * possible theoretically because of condense and strip garbage functions. However, ranges are not found before
     * these functions are called.
     */
    public void deleteEntryAt(int index) {
        if (index<0 || index>labels.size()) return;

        labels.remove(index);
        bankRecord = bankRecord.substring(0, index) + bankRecord.substring(index+1);
    }

    public void setChar(int index, char c) {
        if (index<0 || index>labels.size()) return;

        bankRecord = bankRecord.substring(0, index) + c + bankRecord.substring(index+1);
    }

}
