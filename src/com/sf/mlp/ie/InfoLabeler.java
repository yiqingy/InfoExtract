package com.sf.mlp.ie;

import com.sf.mlp.ie.ds.BankRecordLabel;
import com.sf.mlp.ie.ds.LabeledBankRecord;
import com.sf.mlp.ie.ds.Range;

/**
 * Created by Joms on 4/16/2016.
 * Labels name, city and state.
 * Modified by Yiqing on 8/24/2016.
 * Check non-garbage string by parts to remain merchant name.
 */
public class InfoLabeler {

    private Range preprocessName(Range nameRange, LabeledBankRecord labeledBR) {
        String name = labeledBR.getBankRecordSubstring(nameRange);
        Range selectedName=null;
        int comIndex = name.indexOf(".COM");
        int orgIndex = name.indexOf(".ORG");
        int onlineIndex = comIndex>=0 ? comIndex : orgIndex;
        if (onlineIndex>=0) {
            int i = onlineIndex-1;
            while (i>=0) {
                //TODO: The bank record "MACY*S .COM #0129 MASON OH" fails.
                if (Character.isLetterOrDigit(name.charAt(i)) || Character.isWhitespace(name.charAt(i)) || name.charAt(i)=='-' ||
                        name.charAt(i)=='@')
                    i--;
                else break;
            }
            labeledBR.setChar(nameRange.getStart()+onlineIndex, ' ');
            return new Range(nameRange.getStart()+i+1, nameRange.getStart()+onlineIndex+4);
        }

        int slashIndex = name.indexOf("/");
        if (slashIndex>=0)
            return new Range(nameRange.getStart(), nameRange.getStart()+slashIndex);
        
        
        int starIndex = name.indexOf("*");
        if (starIndex>=0) {
            String[] candidateNames = name.split("\\*");
            if (candidateNames.length == 1) {
                labeledBR.setChar(nameRange.getStart()+starIndex, ' ');
                selectedName = new Range(nameRange.getStart(), nameRange.getEnd());
            }

            //TODO: Assumes there is no * at the beginning or end.
            //      Remove harcoded value.
            else if (candidateNames.length == 2) {
                if ((candidateNames[0].length() <= 3 && !candidateNames[0].equals("NYT") && !candidateNames[0].equals("ACT"))
                        || candidateNames[0].equals(candidateNames[1]))
                    selectedName =  new Range(nameRange.getStart()+starIndex+1, nameRange.getEnd());
                else {
                    labeledBR.setChar(nameRange.getStart()+starIndex, ' ');
                    selectedName = new Range(nameRange.getStart(), nameRange.getEnd());
                }
            }

            //TODO: Assumes there is no * at the beginning or end.
            else if (candidateNames.length > 2) {
                int resetPosition = candidateNames[0].length() + nameRange.getStart();
                boolean found = false;
                int i = 0;
                Range r = null;
                for (i=0; i<candidateNames.length-1; i++) {
                    r = new Range(resetPosition-candidateNames[i].length(), resetPosition);
                    if (candidateNames[i].length() > 3 && isName(labeledBR.getBankRecordSubstring(r))) {
                        selectedName = r;
                        break;
                    }
                    labeledBR.setChar(resetPosition, ' ');
                    resetPosition += (candidateNames[i+1].length() + 1);
                }
                r = new Range(resetPosition-candidateNames[i].length(), resetPosition);
                if (!found && candidateNames[i].length()>3 && isName(labeledBR.getBankRecordSubstring(r)))
                    selectedName = r;
            }
        }

        selectedName = selectedName==null ? nameRange : selectedName;
        return selectedName;
    }
    
    private Range cleanName(Range candidateName, LabeledBankRecord labeledBR){
        String br = labeledBR.getBankRecordSubstring(candidateName);
        
        //This condition is moved from isName to here.
        if (br==null || br.length() < 2) return null;

        br = br.trim().replaceAll("[* ]", " ").replaceAll(" +", " ");
        String[] nameToken = br.split(" ");
        boolean validNameFound = false;
        int startIndex = -1;
        
        for(String t:nameToken){
            if(isName(t)){
                if (!validNameFound){
                    validNameFound = true;
                    startIndex = br.indexOf(t);
                }
            }
            else if(validNameFound)
                return new Range(startIndex, br.indexOf(t));
        }
        if(validNameFound)
            return new Range(startIndex, candidateName.getEnd());
        return null;
    }
    
    private boolean isName(String candidateName) {
        /*
         * Checks whether the input string is mix of digits and characters
         */
        //if (candidateName==null || candidateName.length() < 2) return false;

        //TODO: Example that fails:
        //FAIRWAY C 766 6TH AVE NEW YORK NY
        //PURCHASE ON 02/10 AT FAIRWAY C 766 6TH AVE NEW YORK NY
        int characterCount = 0, digitCount = 0, transitions = 0;
        boolean isPreviousDigit = false, isDigit;
        if (Character.isDigit(candidateName.charAt(0))) {
            digitCount++;
            isPreviousDigit = true;
        }
        else if (Character.isLetter(candidateName.charAt(0)))characterCount++;

        for (int i=1; i<candidateName.length(); i++) {
            if (Character.isDigit(candidateName.charAt(i))) {
                digitCount++;
                isDigit = true;
            }
            else
                isDigit = false;

            if (Character.isLetter(candidateName.charAt(i))) characterCount++;

            if (isPreviousDigit != isDigit) transitions++;
            isPreviousDigit = isDigit;

            if (transitions>2)
                return false;
        }

        //Condition removed
        //if (characterCount==0 && digitCount==0) return false;
        if ((characterCount>0 && digitCount>characterCount+2) ||
                (characterCount>2 && digitCount>2 && digitCount>=characterCount))
            return false;

        return true;
    }

    private Range findName(int searchTillGpNo, LabeledBankRecord labeledBR) {
        //Will be called only with valid group numbers.
        //TODO: Optimize
        for (int i=0; i<=searchTillGpNo; i++) {
            Range nameRange = labeledBR.getGroupByIndex(i);
            if (nameRange!=null && isName(labeledBR.getBankRecordSubstring(nameRange)))
                return nameRange;
        }
        return null;
    }

    private boolean isConfidentInCity(Range candidateName, Range candidateCity, int noOfValidGroups, LabeledBankRecord labeledBR) {
        //Will be called only if city is not null and name is not null.
        //TODO: Check - If noOfValidGroups == 1, candidateCity and candidateName always overlaps.(subset)
        String br = labeledBR.getBankRecord();
        if (noOfValidGroups==1) {
            //TODO: Maybe make this check less stringent by checking whether the candidateCity is not at the end of candidateName - return false.
            String candidate = labeledBR.getBankRecordSubstring(candidateName.getStart(), candidateCity.getStart());
            String[] tokens = candidate.split(" ");
            //TODO: Check whether character before cityStart !isLetterOrDigit and !isWhiteSpace
            if ((tokens.length < 3 && candidate.length()<15) ||
                    (candidateCity.getStart()>3 && br.substring(candidateCity.getStart()-4, candidateCity.getStart()).equals(" OF ")) ||
                    (candidateCity.getStart()>1 && br.substring(candidateCity.getStart()-2, candidateCity.getStart()).equals("OF")) ||
                    candidate.length() - candidateCity.getLength() < 6)
                return false;
            return true;
        }

        //noOfValidGroups > 1
        //TODO: Change this(the overlap logic) since getting city is more stringent now in CityStateScorer
        if (candidateName.overlap(candidateCity) || candidateCity.getStart()<=candidateName.getStart()) return false;

        //Assumptions: 1) City does not span over multiple groups. 2) Name, city overlap already tested.
        int cityGroupNo = labeledBR.findGroupNo(candidateCity.getStart());
        Range cityGroup = labeledBR.getGroupByIndex(cityGroupNo);
        int unknownAlphabetCount = 0;

        //TODO:
        if (cityGroup == null) return false;
        for (int i=cityGroup.getStart(); i<candidateCity.getStart(); i++)
            if (Character.isLetter(br.charAt(i))) unknownAlphabetCount++;
        //TODO: Check for isState Label is unnecessary, since this function is called only if state is not present.
        for (int i=candidateCity.getEnd(); i<cityGroup.getEnd(); i++) {
            if (labeledBR.matchLabel(i, BankRecordLabel.STATE))
                break;
            if (Character.isLetter(br.charAt(i))) unknownAlphabetCount++;
        }
        if (unknownAlphabetCount>1) return false;

        return true;
    }

    public boolean isOnlyAlphabetsOrWhitespace(int start, int end, LabeledBankRecord labeledBR) {
        String br = labeledBR.getBankRecord();
        if (!Character.isWhitespace(br.charAt(start)) || !Character.isWhitespace(br.charAt(end-1)))
            return false;

        for (int i=start+1; i<end-1; i++)
            if ((!Character.isLetter(br.charAt(i))) && (!Character.isWhitespace(br.charAt(i)))) return false;
        return true;
    }

    public InfoLabelerResult labelBankRecord(LabeledBankRecord labeledBR) {
        String br = labeledBR.getBankRecord();
        CityStateScorer cityStateScorer = new CityStateScorer(br);
        //TODO: computeCityScoresAndRatios is now called in selectState. Make selectCity and selectState independent.
        Range selectedState = cityStateScorer.selectState(labeledBR);
        if (selectedState != null)
            labeledBR.setLabels(selectedState.getStart(), selectedState.getEnd(), BankRecordLabel.STATE);
        Range candidateCity = cityStateScorer.selectCity(labeledBR, selectedState);

        /*
         * City is not always right. Therefore, city is labelled after finding name to be confident enough.
         * The algorithm below aims to find a name, city and state with confidence. (Scoring removed for now).
         * Scoring could be incorporated in the future when MLP can handle it.
         */

        Range candidateName;
        int nameStart = labeledBR.getFirstOccurrenceOfLabel(BankRecordLabel.UNKNOWN);
        if (nameStart < 0) {
            return null;  //nothing
        }
        int nameEnd = labeledBR.getFirstOccurrenceOfLabel(nameStart, BankRecordLabel.GARBAGE);
        nameEnd = nameEnd>0 ? nameEnd:br.length();
        candidateName = new Range(nameStart, nameEnd);

        int noOfValidGroups = labeledBR.findNoOfValidGroups();
        //Override noOfValidGroups to 1, if both city and state occur in same group as name.
        if (candidateCity!=null && selectedState!=null &&
                candidateName.contains(candidateCity) && candidateName.contains(selectedState))
            noOfValidGroups = 1;

        //Processing if only one group - Start:
        if (noOfValidGroups==1) {
            if (selectedState!=null && candidateCity!=null) {
                if ((selectedState.getStart()>candidateCity.getEnd()+4 &&
                        //isOnlyAlphabetsOrWhitespace(candidateCity.getEnd(), selectedState.getStart(), labeledBR)) {
                        isName(labeledBR.getBankRecordSubstring(candidateCity.getEnd(), selectedState.getStart()))) ||
                        (br.indexOf(" OF ")+4==candidateCity.getStart() || br.indexOf("OF")+2==candidateCity.getStart())) {
                    candidateName = new Range(nameStart, selectedState.getStart());
                    //TODO: Is this required. Conflicting examples(Fixed):
                    //SOUTH GREENWICH CONV. MA
                    //DEBIT PURCHASE Jul 30 10:29 1177 PECK'S MARKET OF CAL CALLICOON NY 15212
                    //However, in second case city extraction had gone wrong extracting market.
                    candidateCity = null;
                }
                else
                    candidateName = new Range(nameStart, candidateCity.getStart());
            }
            else if (selectedState != null)
                candidateName = new Range(nameStart, selectedState.getStart());
            else if (candidateCity != null) {
                //TODO: Make sure that there is overlap in this case - this check is not required inside the function isConfidentInCity.
                if (isConfidentInCity(candidateName, candidateCity, noOfValidGroups, labeledBR))
                    candidateName = new Range(nameStart, candidateCity.getStart());
                else {
                    candidateName = new Range(nameStart, nameEnd);
                    candidateCity = null;
                }
            }
        }
        //Processing if only one group - End.


        //Processing if more than one group - Start:
        else if (noOfValidGroups>1) {
            boolean lowCityConfidence = false;
            int searchTillGpNo = noOfValidGroups;
            if (selectedState!=null && candidateCity!=null)
                searchTillGpNo = labeledBR.findGroupNo(candidateCity.getStart());
            else if (selectedState != null)
                searchTillGpNo = labeledBR.findGroupNo(selectedState.getStart());
            else if (candidateCity != null)
                lowCityConfidence = true;

            candidateName = findName(searchTillGpNo, labeledBR);
            if ((candidateName==null) || (lowCityConfidence && !isConfidentInCity(candidateName, candidateCity, noOfValidGroups, labeledBR)))
                candidateCity = null;
        }
        //Processing if more than one group - End.


        Range selectedCity = candidateCity;
        if (selectedCity!=null)
            labeledBR.setLabels(selectedCity.getStart(), selectedCity.getEnd(), BankRecordLabel.CITY);
        //Name processing - Start:
        //TODO: Check
        
        Range cleanedCandidateName;
        
        if (candidateName!=null&&(cleanedCandidateName=cleanName(candidateName, labeledBR))!=null) {
            
            Range selectedName = preprocessName(cleanedCandidateName, labeledBR);
            return new InfoLabelerResult(selectedName, selectedCity, selectedState);
        }
        else
            return null; //nothing
        //Name processing - End.

        /*
        double cityConfidenceScore = 0;
        if (candidateCity!=null && labeledBR.getLabelAtIndex(candidateCity.getEnd()-1) == BankRecordLabel.UNKNOWN)
            cityConfidenceScore = 0.1;
        else cityConfidenceScore = 1;


        nameStart = nameStart>=0 ? nameStart:0;

        if (candidateCity != null)
            nameEnd = nameEnd>nameStart ? nameEnd:candidateCity.getStart();
        else if (selectedState != null)
            nameEnd = nameEnd>nameStart ? nameEnd:selectedState.getStart();
        else
            nameEnd = nameEnd>nameStart ? nameEnd:labeledBR.getBankRecord().length();

        if (candidateCity!=null && candidateCity.getStart()>3 &&
                (cityConfidenceScore == 1 || br.substring(candidateCity.getStart()-4, candidateCity.getStart()).equals(" OF ")))
            candidateName = new Range(nameStart, nameEnd);
        else
            candidateName = new Range(nameStart, nameEnd);

        labeledBR.setLabels(candidateName.getStart(), candidateName.getEnd(), BankRecordLabel.NAME);

        ArrayList<Range> results = new ArrayList<>();
        results.add(candidateName);
        results.add(candidateCity);
        results.add(selectedState);
        return results;
        */
    }
}

