package com.sf.mlp.ie;

/**
 * Created by Joms on 6/2/2016.
 * Special Transaction Filter to avoid FPs
 */

public class SpecialTransactionClassifier {
    private final String bankRecord;

    public SpecialTransactionClassifier(String bankRecord) {
        this.bankRecord = bankRecord.toUpperCase();
    }

    public boolean isSpecialTransaction() {
        if (bankRecord.contains("BROKERAGE CREDIT") ||
                bankRecord.contains("BROKERAGE DEBIT") ||
                bankRecord.contains("THANK") ||
                bankRecord.contains("CHECKOUT") ||
                bankRecord.contains("ADJUSTMENT") ||
                bankRecord.contains("FOREIGN") ||
                bankRecord.contains("STATEMENT") ||
                bankRecord.contains("SURCHARGE") ||
                bankRecord.contains("INTEREST") ||
                bankRecord.contains("OVERDRAFT") ||
                bankRecord.contains("DEPOSIT") ||
                bankRecord.contains("ATM") ||
                bankRecord.contains("TELLER") ||
                bankRecord.contains("CHECKING") ||
                bankRecord.contains("ECHECK") ||
                bankRecord.contains("CASH WITHDRAWAL") ||
                bankRecord.contains("SQC*") ||
                //Lenient Checks
                bankRecord.contains(" CHARGE") ||
                bankRecord.contains("REWARD") ||
                bankRecord.contains("CONFIRMATION") ||
                bankRecord.contains("EMAIL") ||
                bankRecord.contains("CARDMEMBER") ||
                bankRecord.contains("ANNUAL") ||
                bankRecord.contains("BENEFIT"))
            return true;

        if (bankRecord.contains("INTERNET") && (
                bankRecord.contains("XFER") || bankRecord.contains("PMT") ||
                bankRecord.contains("TFR") || bankRecord.contains("FEE") ||
                bankRecord.contains("TRANSFER") || bankRecord.contains("WITHDRAWAL") ||
                bankRecord.contains("PAYMENT") || bankRecord.contains("ACCESS")))
            return true;

        if (bankRecord.contains("COUNTER") && (
                bankRecord.contains("CHECK") || bankRecord.contains("CREDIT") ||
                bankRecord.contains("WITHDRAWAL")))
            return true;

        if (bankRecord.contains("REDEMPTION") && (
                bankRecord.contains("CREDIT") || bankRecord.contains("POINTS") ||
                bankRecord.contains("BONUS") || bankRecord.contains("CASH")))
            return true;

        if (bankRecord.contains("BONUS") && (
                bankRecord.contains("CATEGORY SPEND") || bankRecord.contains("PURCHASE") ||
                bankRecord.contains("REFERRAL")))
            return true;

        if (bankRecord.contains("LATE") && bankRecord.contains("FEE"))
            return true;

        if (bankRecord.contains("TRANSACTION") &&
                !bankRecord.contains("WITHDRWL") && !bankRecord.contains("CHECKCARD") &&
                !bankRecord.endsWith("PENDING TRANSACTION"))
            return true;

        if (bankRecord.contains("PHONE") && (
                bankRecord.contains("FEE") || bankRecord.contains("TRANSF")))
            return true;

        if ((bankRecord.contains("ACCOUNT") || bankRecord.contains("MONTHLY")) && bankRecord.contains("FEE"))
            return true;

        if (bankRecord.contains("VISA MONEY TRANSFER")) return true;

        return false;
    }
}
