package com.loban.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormat {

    private MoneyFormat() {}

    public static String fcfa(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(amount) + " FCFA";
    }
}
