package com.sf.mlp.ie.ds;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Created by Joms on 4/11/2016.
 */
public class BankRecordTemplate {
    /**
     * All this is read from a file or database. This class does not ensure that
     * the group numbers will throw an IndexOutOfBounds exception.
     */
    private final String template; //a regex
    private final Pattern regexPattern; //compiled regex Pattern
    private final char resolved;
    private final char resolvedCharacter = 'R';
    private final LinkedHashMap<BankRecordLabel, int[]> labelGpNoMap;

    public BankRecordTemplate(String template, char resolved, int merchantNameGpNo, int cityGpNo,
                              int stateGpNo, int unknownGpNo, int[] garbageGpNos) {
        this.template = template;
        this.regexPattern = Pattern.compile(this.template);
        this.resolved = resolved;
        this.labelGpNoMap = new LinkedHashMap<>(BankRecordLabel.values().length);
        this.labelGpNoMap.put(BankRecordLabel.NAME, new int[] {merchantNameGpNo});
        this.labelGpNoMap.put(BankRecordLabel.CITY, new int[] {cityGpNo});
        this.labelGpNoMap.put(BankRecordLabel.STATE, new int[] {stateGpNo});
        this.labelGpNoMap.put(BankRecordLabel.UNKNOWN, new int[] {unknownGpNo});
        this.labelGpNoMap.put(BankRecordLabel.GARBAGE, garbageGpNos);
    }

    public boolean isResolved() {
        return (Character.toUpperCase(resolved) == resolvedCharacter);
    }

    public Pattern getRegexPattern() {
        return regexPattern;
    }

    public LinkedHashMap<BankRecordLabel, int[]> getLabelGpNoMap() {
        return labelGpNoMap;
    }

    public int[] getMerchantNameGpNos() {
        return labelGpNoMap.get(BankRecordLabel.NAME);
    }

    public int[] getCityGpNos() {
        return labelGpNoMap.get(BankRecordLabel.CITY);
    }

    public int[] getStateGpNos() {
        return labelGpNoMap.get(BankRecordLabel.STATE);
    }

    public int[] getUnknownGpNos() {
        return labelGpNoMap.get(BankRecordLabel.UNKNOWN);
    }

    public int[] getGarbageGpNos() {
        return labelGpNoMap.get(BankRecordLabel.GARBAGE);
    }
}

