package com.sf.mlp.ie.test;

import java.util.prefs.BackingStoreException;

/**
 * Created by Joms on 6/21/2016.
 */
public class DupBankTxClassifier {
    private final String bankRecord;
    private final String resolvedName;

    public DupBankTxClassifier(String bankRecord, String resolvedName) {
        this.bankRecord = bankRecord==null ? "":bankRecord.trim().toUpperCase();
        this.resolvedName = resolvedName==null ? "":resolvedName.trim().toUpperCase();
    }

    public boolean isBankTx() {
        if (resolvedName.startsWith("AMERICAN EXPRESS") && !bankRecord.contains("SERVE")) return true;

        if (bankRecord.contains("KEEP THE CHANGE") || bankRecord.contains("KEEPTHECHANGE")) return true;

        if ((bankRecord.contains(" SALARY ") || bankRecord.contains(" PAYROLL ") || resolvedName.contains(" SALARY ") ||
                resolvedName.contains(" PAYROLL ")) && (!bankRecord.contains(" PENSION ") || !resolvedName.contains(" PENSION ")))
            return true;

        if (bankRecord.contains("CHASE QUICKPAY ELECTRONIC TRANSFER") || resolvedName.contains("CHASE QUICKPAY ELECTRONIC TRANSFER"))
            return true;

        if (bankRecord.contains("SSA") &&  bankRecord.contains("TREAS")) return true;

        if ((bankRecord.contains("SALARY") || bankRecord.contains("PAYROLL") || resolvedName.contains("SALARY") ||
                resolvedName.contains("PAYROLL")) && (!bankRecord.contains("PENSION") || !resolvedName.contains("PENSION")))
            return true;

        if (bankRecord.startsWith("IRS") || bankRecord.contains(" IRS ")) return true;

        if ((bankRecord.contains("WITHDRAW") || bankRecord.contains("WITHDRWL") || bankRecord.contains("CASH WITHDRA")) &&
                (!bankRecord.contains("TRANSFER") && !bankRecord.contains("FEE") && !bankRecord.contains("CHARGE")))
            return true;

        if ((bankRecord.contains("ATM W/D") || bankRecord.contains("ATM DEBIT")) &&
                (!bankRecord.contains("TRANSFER") && !bankRecord.contains("FEE") && !bankRecord.contains("CHARGE")))
            return true;

        if (bankRecord.startsWith("AMERICAN EXPRESS TVL") || bankRecord.startsWith("AMERICAN EXPRESS TRA"))
            return true;

        //Missing rule

        if (bankRecord.contains("AUTOPAY") || bankRecord.contains("THANK YOU"))
            return true;

        if ((bankRecord.contains("PAYMENT") || bankRecord.contains("PYMT")) &&
                !bankRecord.contains("BENEFIT") && !bankRecord.contains("FEE") && !bankRecord.contains(".COM"))
            return true;

        if (bankRecord.contains("VENMO") || resolvedName.contains("VENMO")) return true;

        //Missing Rule

        if (bankRecord.contains("BARCLAYCARD") && !bankRecord.contains("TRANSFER")) return true;

        if ((bankRecord.contains("PAY") || bankRecord.contains("MASTERCARD")) &&
                !bankRecord.contains("QUICKPAY") && bankRecord.contains(" CHASE"))
            return true;

        if ((bankRecord.contains("PAYMENT") || bankRecord.contains("PYMT") || bankRecord.contains("MASTERCARD")) &&
                (bankRecord.contains("CITIBANK") || bankRecord.contains("CITI")))
            return true;

        //incomplete rule
        if ((bankRecord.contains("AMERICAN EXPRESS") || bankRecord.contains("AMEX")))
            return true;

        if ((bankRecord.contains("PAYMENT") || bankRecord.contains("PMT")) &&
                (bankRecord.contains("BANK OF AMERICA") || bankRecord.contains("BKAMERICA"))) return true;

        if (bankRecord.contains("CREDIT") && bankRecord.contains("USAA")) return true;

        if (bankRecord.contains("WF CREDIT")) return true;

        if (bankRecord.contains("XOBI")) return true;

        if ((bankRecord.contains("PAYMENT") || bankRecord.contains("PMT")) &&
                bankRecord.contains("CAPITAL ONE") && !bankRecord.contains("FEE"))
            return true;

        if (bankRecord.contains("PAY") && bankRecord.contains("COMENITY")) return true;

        if (bankRecord.contains("PAYMENT") && bankRecord.contains("DISCOVER") &&
                !bankRecord.contains("FEE")) return  true;

        if (bankRecord.contains("FEE") || bankRecord.contains("FEES")) {
            if ((bankRecord.contains("TRANSFER ") || bankRecord.contains("TFR ") || bankRecord.contains("TF ")) &&
                    !bankRecord.contains("AVOID"))
                return true;

            if (bankRecord.contains("ATM ") && !bankRecord.contains("AVOID")) return true;

            if ((bankRecord.contains("BANK ") || bankRecord.contains(" TRANSACTION ") || bankRecord.contains(" TRANSACTIONS ")) &&
                    !bankRecord.contains("AVOID")) return true;

            if ((bankRecord.contains(" WITHDRWL ") || bankRecord.contains(" WITHDRAWAL ") || bankRecord.contains(" WITHDRAWALS ")) &&
                    !bankRecord.contains("AVOID")) return true;

            if (bankRecord.contains("WIRE ") && !bankRecord.contains("AVOID")) return true;

            if (bankRecord.contains("ANNUAL ") || bankRecord.contains("MEMBERSHIP ") || bankRecord.contains("LATE ") ||
                    bankRecord.contains(" FOREIGN TRANSACTION ")) return true;

            if ((bankRecord.contains("MONTHLY MAINTENANCE") || bankRecord.contains("MONTHLY ACCOUNT") ||
                    bankRecord.contains("MONTHLY SERVICE")) &&
                    !bankRecord.contains("AVOID")) return true;
        }

        //Missing Rule

        if(bankRecord.startsWith("WIRE TYPE:WIRE ") || ((bankRecord.contains("WIRE ") || bankRecord.contains("WIRED ")) &&
                (bankRecord.contains("TRANSFER") || bankRecord.contains("FUNDS") || bankRecord.contains("INCOMING") ||
                bankRecord.contains("OUTGOING"))) &&
                !bankRecord.contains("FEE") && !bankRecord.contains("CHARGE") && bankRecord.contains("REFUND"))
            return true;

        //Incomplete Rule
        if (bankRecord.contains("REWARDS") || (bankRecord.contains("CASH") && bankRecord.contains("REWARD")) ||
                bankRecord.contains("REWARD ")) return true;

        if (bankRecord.contains("REFUND") && (bankRecord.contains("FEE ") || bankRecord.contains("CHARGE ") ||
                bankRecord.contains("ATM ") || bankRecord.contains("INTEREST ") || bankRecord.contains("WIRE TRANSFER") ||
                bankRecord.contains("REFUND PROCESS") || bankRecord.contains("BALANCE")))
            return true;

        if ((bankRecord.contains("REBATE")) && (bankRecord.contains("FEES ") || bankRecord.contains("CHARGE ") ||
            bankRecord.contains("ATM ") || bankRecord.contains("ACCT BAL ")))
            return true;

        if ((bankRecord.contains("TRANSFER TO ") || bankRecord.contains("TRANSFER FROM ")) &&
                !bankRecord.contains("KEEP THE CHANGE") && !bankRecord.contains("MASTERCARD") && !bankRecord.contains("VISA"))
            return  true;

        if (bankRecord.contains(" BTFR-FR ") || bankRecord.contains(" BTFR-TO ") || bankRecord.contains(" BTFR TO" ) ||
                bankRecord.contains(" BTFR FRM "))
            return true;

        //Incomplete Rule
        if (bankRecord.contains("SCHEDULED TRANSFER") || bankRecord.startsWith("TRANSFER ") || bankRecord.startsWith("TRANSFER DEPOSIT ") ||
                bankRecord.startsWith("E TFR "))
            return true;

        //Incomplete Rule
        if (bankRecord.contains("EXT TRNSFR ") || bankRecord.contains("FNDTRNSFR") || bankRecord.contains("TRANSFER OF FUNDS"))
            return true;

        //Incomplete Rule
        if (bankRecord.startsWith("AUTO TRANSFER ") || bankRecord.startsWith("ATM ") || bankRecord.contains(" TRANSFERTO ") ||
                bankRecord.contains(" TRANSFERFROM ") || bankRecord.contains("TRANSFER IN BRANCH") ||
                bankRecord.startsWith("A2A TRANSFER CREDIT "))
            return true;

        if ((bankRecord.startsWith("ONLINE MONEY TRANSFER") || bankRecord.startsWith("ONLINE TRANSFER ") ||
                bankRecord.startsWith("ONLINE TRANSFER REF") || bankRecord.contains(" NA TRANSFER")) &&
                !bankRecord.contains(" CARD "))
            return true;

        return false;

    }
}
