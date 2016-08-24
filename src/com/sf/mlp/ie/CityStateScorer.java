package com.sf.mlp.ie;

import com.sf.mlp.ie.ds.BankRecordLabel;
import com.sf.mlp.ie.ds.LabeledBankRecord;
import com.sf.mlp.ie.ds.Range;
import com.sf.mlp.ie.ds.USCity;
import org.apache.commons.collections4.trie.PatriciaTrie;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Joms on 4/10/2016.
 * Identifies possible cities and states in the bank record.
 */

public class CityStateScorer {

    private static final PatriciaTrie<ArrayList<USCity>> cityTrie;
    private static final HashSet<String> stateHashSet;

    private String source;
    //The start index of source within bank record
    private int sourceStartIndex;
    private int[] cityScores;
    private double[] cityRatios;
    private int[] stateScores;

    protected static final int CITY_STARTING_OFFSET = 4;
    protected static final int CITY_LEN_THRESHOLD = 5;
    protected static final double CITY_RATIO_THRESHOLD = 0.7;
    protected static final int NO_OF_STATES = 100;
    protected static final double MAX_CONFIDENCE = 1.0;


    static {
        //TODO: See whether size can be initialized
        cityTrie = new PatriciaTrie<>();
        stateHashSet = new HashSet<>(NO_OF_STATES);
        loadCityTrieAndStateHashSet();
    }

    public CityStateScorer(String source, int sourceStartIndex) {
        //TODO: Move toUpperCase?
        this.source = source.toUpperCase();
        this.sourceStartIndex = sourceStartIndex;
        this.cityScores = new int[source.length()];
        this.cityRatios = new double[source.length()];
        this.stateScores = new int[source.length()];
    }

    public CityStateScorer(String source) {
        //TODO: Move toUpperCase?
        this.source = source.toUpperCase();
        this.sourceStartIndex = 0;
        this.cityScores = new int[source.length()];
        this.cityRatios = new double[source.length()];
        this.stateScores = new int[source.length()];
    }


    protected static void loadCityTrieAndStateHashSet() {
        String cityLine;
        try(BufferedReader br = new BufferedReader(new FileReader("/Users/Yiqing/Desktop/InfoExtract/src/com/sf/mlp/ie/data/places.csv"))) {
            while ((cityLine = br.readLine()) != null) {
                String[] cityRow = cityLine.split(",");
                //city, county, state
                USCity city = new USCity(cityRow[0], cityRow[cityRow.length-2], cityRow[cityRow.length-1]);
                //one city key may have multiple values
                cityTrie.computeIfAbsent(cityRow[0].toUpperCase(), k -> new ArrayList<>()).add(city);
                stateHashSet.add(cityRow[cityRow.length-1].toUpperCase());
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadCityTrie() {
        //if checksum
        cityTrie.clear(); //?
        loadCityTrieAndStateHashSet();
    }

    private int findMinLength(SortedMap<String, ArrayList<USCity>> matchedCities) {
        int minLength = 0;
        if (matchedCities.size() != 0) {
            minLength = matchedCities.firstKey().length();
            for (String city : matchedCities.keySet()) {
                if (city.length() < minLength)
                    minLength = city.length();
            }
        }
        return minLength;
    }

    private ArrayList<String> findMaxRatioCities(SortedMap<String, ArrayList<USCity>> matchedCities, int minLength) {
        ArrayList<String> maxRatioCities = new ArrayList<>();
        if (matchedCities.size() != 0 && minLength > 0) {
            for (String city : matchedCities.keySet()) {
                if (city.length() == minLength)
                    maxRatioCities.add(city);
            }
        }
        return maxRatioCities;
    }

    private void helperPrefixTruncation(String s, int startIndex) {
        /*
         * Starting from index 0 and initial size of 4, query the
         *      trie for matching cities.
         * Continue increasing size of query string by 1 until no matches are obtained.
         * For the string input to this function identified by the starting
         *      index of the string input in source, the maximum length of the string
         *      with a matching key is it's score.
         * Ratio is obtained by dividing that by the minimum length city in the matched set.
         */
        int i = CITY_STARTING_OFFSET;
        ArrayList<Integer> scoresTemp = new ArrayList<>();
        ArrayList<Double> ratiosTemp = new ArrayList<>();
        int minLength;
        SortedMap<String, ArrayList<USCity>> matchedCities;

        while (i <= s.length()) {
            matchedCities = cityTrie.prefixMap(s.substring(0, i));
            minLength = findMinLength(matchedCities);
            if (minLength > 0) {
                scoresTemp.add(i);
                ratiosTemp.add((double)i/minLength);
            }
            else
                break;
            i++;
        }

        if (ratiosTemp.size() > 0) {
            double maxRatio = ratiosTemp.get(ratiosTemp.size()-1);
            int maxRatioIndex = ratiosTemp.size()-1;
            for (int j=ratiosTemp.size()-1; j>=0; j--) {
                if (ratiosTemp.get(j)> maxRatio) {
                    maxRatio = ratiosTemp.get(j);
                    maxRatioIndex = j;
                }
            }
            cityScores[startIndex] = scoresTemp.get(maxRatioIndex);
            cityRatios[startIndex] = maxRatio;
        }
        else {
            cityScores[startIndex] = 0;
            cityRatios[startIndex] = 0;
        }

        /*
        System.out.println("\nHelper Scores");
        for (Integer s1: cityScores) {
            System.out.print(s1.intValue());
            System.out.print(" ");
        }
        System.out.println("\nHelper Ratios");
        for (Double s1: cityRatios) {
            System.out.print(s1.doubleValue());
            System.out.print(" ");
        }
        */
    }

    private void computeCityScoresAndRatios() {
        //Try to match substrings to the cityTrie, starting from every index
        int skipCounter = 0;

        while (skipCounter<source.length() && skipCounter<3) {
            cityScores[skipCounter] = 0;
            cityRatios[skipCounter] = 0;
            skipCounter++;
        }

        for (int i=3; i<source.length(); i++)
            helperPrefixTruncation(source.substring(i), i);

        int i = 0, j=0;
        /*
         * 1. Take score at index 1.
         * 2. Search if there is a higher score from index 1 to (1+score)th index.
         * 3. If no - set all indices from 1 to (1+score)th index to score.
         * 4. Else - set all indices from 1 to jth index where a higher score was found to 0.
         * 5. Repeat 1 to 4 from (i+score)th index if 3 else from jth index if 4.
         */
        while (i < cityScores.length) {
            if (cityScores[i] > 2) {

                for(j=i+1; j<i+cityScores[i]; j++) {
                    if (cityScores[j] > cityScores[i]) {
                        for(int k=i; k<j; k++) {
                            cityScores[k] = 0;
                            cityRatios[k] = 0;
                        }
                        break;
                    }
                    //else
                    cityScores[j] = cityScores[i];
                    cityRatios[j] = cityRatios[i];
                }
                i = j;
            }
            else
                i++;
        }
        assert source.length() == cityScores.length;
        /*
        System.out.println("\nScores");
        for (Integer s1: cityScores) {
            System.out.print(s1.intValue());
            System.out.print(" ");
        }
        System.out.println("\nRatios");
        for (Double s1: cityRatios) {
            System.out.print(s1.doubleValue());
            System.out.print(" ");
        }
        */
    }

    private void computeStateScores() {
        /*
         * Sets all indices that are beginnings of a state to 1 and others to 0.
         */
        for (int i=0; i<source.length()-1; i++) {
            String candidateState = source.substring(i, i+2);
            if (stateHashSet.contains(candidateState))
                stateScores[i] = 1;
            else
                stateScores[i] = 0;
        }
    }

    private boolean[] findAreElementsCity() {
        //TODO:Raise exception if cityScores and cityRatios are not set.
        boolean[] areElementsCity = new boolean[source.length()];
        for (int i=0; i<source.length(); i++)
            areElementsCity[i] = cityScores[i] > CITY_LEN_THRESHOLD && cityRatios[i] > CITY_RATIO_THRESHOLD;

        return areElementsCity;
    }

    private List<Range> findCandidateCities() {
        //Returns list of start and end indices of candidate cities
        List<Range> candidateCities = new ArrayList<>();
        int start, end;
        int i=0, cityEnd;

        while (i<cityRatios.length-1) {
            if (cityRatios[i] == 0) {
                i++;
                continue;
            }
            start = i;
            cityEnd = i+cityScores[i];
            while (i<cityRatios.length-1 && i<cityEnd-1 && cityRatios[i]==cityRatios[i+1] && cityScores[i]==cityScores[i+1])
                i++;
            end = i+1;
            candidateCities.add(new Range(start, end));
            i++;
        }
        return candidateCities;
    }

    private List<Integer> findCandidateStates() {
        List<Integer> candidateStates = new ArrayList<>();
        for (int i=0; i<stateScores.length; i++) {
            if (stateScores[i] == 1)
                candidateStates.add(i);
        }
        return candidateStates;
    }

    private boolean isPrevious(boolean[] areElementsCity, int stateIndex, LabeledBankRecord labeledBR) {
        //TODO: Refactor so that there is no space between garbage and unknown.
        /*
         * Checks if previous index is state or
         * previous or previous-1 is state and in between characters are garbage or space.
         */
        String br = labeledBR.getBankRecord();
        return (areElementsCity[stateIndex-1] ||
                (stateIndex>1 && areElementsCity[stateIndex-2] && (labeledBR.matchLabel(stateIndex-1, BankRecordLabel.GARBAGE) || br.charAt(stateIndex-1)==' ')) ||
                (stateIndex>2 && areElementsCity[stateIndex-3] && ((labeledBR.matchLabel(stateIndex-1, BankRecordLabel.GARBAGE) || br.charAt(stateIndex-1)==' ') &&
                        (labeledBR.matchLabel(stateIndex-2, BankRecordLabel.GARBAGE) || br.charAt(stateIndex-2)==' '))));
    }

    public Range selectState(LabeledBankRecord labeledBR) {
        /*
         * Given a substring of the bank record, this function returns the index of the state selected from the
         * substring w.r.t the bank record along with a confidence.
         */

        String br = labeledBR.getBankRecord();
        computeCityScoresAndRatios();
        computeStateScores();
        boolean[] areElementsCity = findAreElementsCity();
        List<Integer> candidateStateIndices = findCandidateStates();

        List<Integer> filteredStateIndices = new ArrayList<>();
        for (int stateIndex : candidateStateIndices) {
            int brStateIndex = stateIndex + sourceStartIndex;
            if (brStateIndex>0 && (labeledBR.matchLabel(brStateIndex-1, BankRecordLabel.GARBAGE) ||
                    (isPrevious(areElementsCity, brStateIndex, labeledBR) && !areElementsCity[brStateIndex]) ||
                    (br.charAt(brStateIndex-1)==' '))) {
                if (brStateIndex+2 < br.length()) {
                    if (labeledBR.matchLabel(brStateIndex+2, BankRecordLabel.GARBAGE) ||
                            !Character.isLetter(br.charAt(brStateIndex+2)) ||
                            (brStateIndex+4 <= br.length() && br.substring(brStateIndex+2, brStateIndex+4).equals("US")))
                        filteredStateIndices.add(brStateIndex);
                }
                else filteredStateIndices.add(brStateIndex);
            }
        }

        int selectedStateIndex;
        if (filteredStateIndices.size() > 0) {
            selectedStateIndex = Collections.max(filteredStateIndices);
            if (selectedStateIndex > (br.length()/2)-1)
                return new Range(selectedStateIndex, selectedStateIndex+2, MAX_CONFIDENCE);
        }
        return null;
    }

    private int alphabetCount(String str, int start, int end) {
        int count = 0;
        for (int i=start; i<end; i++)
            if (Character.isLetter(str.charAt(i))) count++;

        return count;
    }

    public boolean verify_city_against_state(Range city, Range state, String br) {
        /*
         * Make sure that the selected city is in the selected state.
         * If the state is null, city is not predicted.
         * TODO: A change in logic might be needed since most transactions outside the US do not have a state.
         */

        //If state is null, the city is selected only if it as the end of the bank record.
        if (state == null)
            //TODO: There would still be some FPs - "LA CITY PARKING METER"
            return city.getEnd()==br.length() || br.substring(city.getEnd(), br.length()).equals(" US");

        String stateStr = br.substring(state.getStart(), state.getEnd());
        SortedMap<String, ArrayList<USCity>> matchedCities;
        matchedCities = cityTrie.prefixMap(br.substring(city.getStart(), city.getEnd()));
        int minLength = findMinLength(matchedCities);
        ArrayList<String> maxRatioCities = findMaxRatioCities(matchedCities, minLength);
        for (String cityStr:maxRatioCities) {
            for (USCity usCity : cityTrie.get(cityStr)) {
                if (usCity.getState().equals(stateStr))
                    return true;
            }
        }
        return false;
    }

    public Range selectCity(LabeledBankRecord labeledBR, Range selectedState) {
        /*
         * Given a substring of the bank record, this function returns the index of the city selected from the
         * substring w.r.t the bank record along with a confidence.
         */

        String br = labeledBR.getBankRecord();
        //TODO
        //computeCityScoresAndRatios();
        List<Range> candidateCities = findCandidateCities();
        Range selectedCity = null;

        for (Range candidateCity : candidateCities) {
            int start = candidateCity.getStart();
            int end = candidateCity.getEnd();
            if ((start>0 && (Character.isWhitespace(br.charAt(start-1)) || labeledBR.matchLabel(start-1, BankRecordLabel.GARBAGE))) &&
                    (end==br.length() || Character.isWhitespace(br.charAt(end)) || labeledBR.matchLabel(end, BankRecordLabel.GARBAGE) ||
                            labeledBR.matchLabel(end, BankRecordLabel.STATE) || Character.isWhitespace(br.charAt(end-1))) &&
                    (selectedState!=null && alphabetCount(br, sourceStartIndex+end, selectedState.getStart())<2) &&
                    cityRatios[start]==1)
                selectedCity = candidateCity;

            if (selectedState!=null && selectedCity!=null && selectedState.isValid(br) &&
                    end+3+sourceStartIndex>selectedCity.getStart() && cityRatios[start]>0.8) {
                selectedCity = candidateCity;
            }
        }

        if (selectedCity == null && candidateCities!=null && !candidateCities.isEmpty()) {
            Range candidateCity = candidateCities.get(candidateCities.size()-1);
            int start = candidateCity.getStart();
            if (cityRatios[start]>0.8 && cityScores[start]>5 && start>3) selectedCity=candidateCity;
        }

        if (selectedCity != null) {
            selectedCity.addOffset(sourceStartIndex);
            if (!verify_city_against_state(selectedCity, selectedState, br))
                selectedCity = null;
        }

        return selectedCity;
    }
}
