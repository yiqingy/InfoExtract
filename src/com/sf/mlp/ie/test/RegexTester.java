package com.sf.mlp.ie.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joms on 4/18/2016.
 */
public class RegexTester {
    public static void main(String[] args) {
        //String regex = "PURCHASE\\s+AUTHORIZED ON\\s+\\d\\d/\\d\\d\\s+(.*?)\\s{2,}((.*?)(\\s{2,}))?(.*?)\\s{2,}(.*?)\\s{2,}(.*?)";
        //String regex = "PURCHASE\\s+AUTHORIZED ON\\s+\\d\\d/\\d\\d\\s+.*?\\s+";
        String regex = "(.*?)(DES:(.*?)INDN:.*)";
        //\s\{2, \}(.*?)
        Pattern p = Pattern.compile(regex);
        //String testStr = "PURCHASE                                AUTHORIZED ON   12/22 TJ TJ MAXX                NORWALK       CT  P00465356620073696 CARD 6217";
        //String testStr = "PURCHASE                                AUTHORIZED ON   04/23 MACY'S    3301 VETERAN    METARIE       LA  P00000000453675843 CARD 7098";
        //String testStr = "PURCHASE                                AUTHORIZED ON   12/22 TJ TJ MAXX                NORWALK       CT  P00465356620073696 CARD 6217";
        String testStr = "SMARTFINANCE LLC DES:PAYROLL ID:XXXXX500033518X INDN:EIMBINDER, RICHARD A";
        Matcher m = p.matcher(testStr);
        if (m.matches()) {
            System.out.println("Success");
            System.out.println(m.group(0));
            System.out.println(m.group(1));
            System.out.println(m.group(2));
            //System.out.println(m.group(3));
            //System.out.println(m.group(4));
            //System.out.println(m.group(5));
            //System.out.println(m.group(6));
            //System.out.println(m.group(7));
        }
        else
            System.out.println("Failure");

    }
}

