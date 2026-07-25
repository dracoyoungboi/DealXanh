package com.dealxanh.app.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Centralized Vietnamese Dong (VND) price formatter.
 * Produces output like "722.000đ" with proper thousands separators.
 * Accessible in Thymeleaf as {@code ${@priceFormatter.format(value)}}.
 */
@Component("priceFormatter")
public class PriceFormatter {

    private final DecimalFormat vndFormat;

    public PriceFormatter() {
        vndFormat = (DecimalFormat) NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
        vndFormat.setGroupingUsed(true);
    }

    /**
     * Format a number as VND currency with "đ" suffix.
     * Example: format(722000) → "722.000đ", format(null) → "0đ"
     */
    public String format(Number value) {
        if (value == null) return "0đ";
        return vndFormat.format(value.longValue()) + "đ";
    }

    /**
     * Format a number with thousands separators but WITHOUT the "đ" suffix.
     * Use for non-currency numbers like order counts.
     * Example: formatPlain(1234) → "1.234", formatPlain(null) → "0"
     */
    public String formatPlain(Number value) {
        if (value == null) return "0";
        return vndFormat.format(value.longValue());
    }
}
