package org.recap.matchingalgorithm;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.recap.ScsbConstants.MATCHING_COUNTER_OPEN;
import static org.recap.ScsbConstants.MATCHING_COUNTER_SHARED;
import static org.recap.ScsbConstants.MATCHING_COUNTER_UPDATED_OPEN;
import static org.recap.ScsbConstants.MATCHING_COUNTER_UPDATED_SHARED;

public class MatchingCounterUT {

    @Test
    public void testMatchingCounter(){
        Map<String,Integer> cgdCounterMap=new HashMap<>();
        cgdCounterMap.put(MATCHING_COUNTER_SHARED,1);
        cgdCounterMap.put(MATCHING_COUNTER_OPEN,1);
        cgdCounterMap.put(MATCHING_COUNTER_UPDATED_SHARED,0);
        cgdCounterMap.put(MATCHING_COUNTER_UPDATED_OPEN,0);
        List<String> institutions= Arrays.asList("PUL","CUL","NYPL","HL");
        Map<String, Map<String, Integer>> institutionCounterMap=new HashMap<>();
        for (String institution : institutions) {
            institutionCounterMap.put(institution,cgdCounterMap);
        }
        updateCGDCounter("PUL",institutionCounterMap,true);
    }

    public Map<String, Integer> updateCGDCounter(String institution,Map<String, Map<String, Integer>> institutionCounterMap,boolean isOpen){
        Map<String, Integer> institutionCgdCounter = institutionCounterMap.get(institution);
        if(isOpen){
            institutionCgdCounter.put(MATCHING_COUNTER_SHARED,institutionCgdCounter.get(MATCHING_COUNTER_SHARED)-1);
            institutionCgdCounter.put(MATCHING_COUNTER_OPEN,institutionCgdCounter.get(MATCHING_COUNTER_OPEN)+1);
            institutionCgdCounter.put(MATCHING_COUNTER_UPDATED_OPEN,institutionCgdCounter.get(MATCHING_COUNTER_UPDATED_OPEN)+1);
        }
        return institutionCgdCounter;
    }}
