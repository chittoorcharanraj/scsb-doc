package org.recap.matchingalgorithm;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MatchScoreUtil {

    /**
     * Matching Algorithm Match Scores
     *
     * Match Scores are decimal numbers calculated based on bits in the
     * order : ISBN,OCLC,LCCN,ISSN,Title Bit
     * Ex : Match in  ISBN and OCLC is 11000 and equivalent match score is 24
     */
    public static final Integer OCLC_ISBN_SCORE =24;
    public static final Integer OCLC_ISSN_SCORE =10;
    public static final Integer OCLC_LCCN_SCORE =12;
    public static final Integer ISBN_ISSN_SCORE =18;
    public static final Integer ISBN_LCCN_SCORE =20;
    public static final Integer ISSN_LCCN_SCORE =6;
    public static final Integer OCLC_SCORE = 8;
    public static final Integer ISSN_SCORE = 10;
    public static final Integer LCCN_SCORE = 12;
    public static final Integer ISBN_SCORE = 16;

    public static int convertBinaryToDecimal(String binaryString) {
        return Integer.parseInt(binaryString, 2);
    }

    public static String convertDecimalToBinary(Integer decimal){
        String binaryString = Integer.toBinaryString(decimal);
        int length = binaryString.length();
        while(length<5){
            binaryString='0'+binaryString;
            length++;
        }
        return binaryString;
    }

    public static String calculateMatchScore(String m1, String m2) {
        char[] matchScore=new char[5];
        for(int i=0;i<m1.length();i++){
            for(int j=i;j<m2.length();j++){
                if(m1.charAt(i) == '1' || m2.charAt(j) == '1'){
                    matchScore[i]='1';
                }
                else{
                    matchScore[i]='0';
                }
                break;
            }
        }
        return String.valueOf(matchScore);
    }

    public static Integer getMatchScoreForSingleMatchAndTitle(Integer singleMatchScore) {
        StringBuilder stringBuilder = new StringBuilder(convertDecimalToBinary(singleMatchScore));
        stringBuilder.setCharAt(4,'1');
        return convertBinaryToDecimal(stringBuilder.toString());
    }
}
