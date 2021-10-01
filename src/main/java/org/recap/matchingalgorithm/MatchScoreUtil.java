package org.recap.matchingalgorithm;

import lombok.experimental.UtilityClass;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;

@UtilityClass
public class MatchScoreUtil {

    /**
     * Matching Algorithm Match Scores
     *
     * Match Scores are decimal numbers calculated based on bits in the
     * order : ISBN,OCLC,LCCN,ISSN,Title Bit
     * Ex : Match in  ISBN and OCLC is 11000 and equivalent match score is 24
     */
    public static final Integer ISSN_LCCN_SCORE =6;
    public static final Integer OCLC_ISSN_SCORE =10;
    public static final Integer OCLC_LCCN_SCORE =12;
    public static final Integer ISBN_ISSN_SCORE =18;
    public static final Integer ISBN_LCCN_SCORE =20;
    public static final Integer OCLC_ISBN_SCORE =24;
    public static final Integer OCLC_TITLE_SCORE =9;
    public static final Integer ISBN_TITLE_SCORE =17;
    public static final Integer ISSN_TITLE_SCORE =3;
    public static final Integer LCCN_TITLE_SCORE =5;

    public static final Integer ISSN_SCORE = 2;
    public static final Integer LCCN_SCORE = 4;
    public static final Integer OCLC_SCORE = 8;
    public static final Integer ISBN_SCORE = 16;
    public static final Integer TITLE_SCORE = 1;

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

    public static Integer calculateMatchScore(Integer m1, Integer m2) {
        String matchScoreBinary1 = convertDecimalToBinary(m1);
        String matchScoreBinary2 = convertDecimalToBinary(m2);
        String binaryScore = MatchScoreUtil.calculateMatchScore(matchScoreBinary1, matchScoreBinary2);
        return convertBinaryToDecimal(binaryScore);
    }

    public static Integer getMatchScoreForSingleMatchAndTitle(Integer singleMatchScore) {
        StringBuilder stringBuilder = new StringBuilder(convertDecimalToBinary(singleMatchScore));
        stringBuilder.setCharAt(4,'1');
        return convertBinaryToDecimal(stringBuilder.toString());
    }

    public static Integer getMatchScoreForMatchPoint(String matchPoint){
        switch (matchPoint){
            case ScsbCommonConstants.MATCH_POINT_FIELD_OCLC: return OCLC_SCORE;
            case ScsbCommonConstants.MATCH_POINT_FIELD_ISBN: return ISBN_SCORE;
            case ScsbCommonConstants.MATCH_POINT_FIELD_LCCN: return LCCN_SCORE;
            case ScsbCommonConstants.MATCH_POINT_FIELD_ISSN: return ISSN_SCORE;
            case ScsbConstants.TITLE: return TITLE_SCORE;
            default:return 0;
        }
    }

}
