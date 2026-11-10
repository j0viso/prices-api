package com.gfttest.prices.pricing.domain.model;

import com.gfttest.prices.pricing.domain.exception.InvalidPriceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) {
            throw new InvalidPriceException("amount is required");
        }
        if (currency == null) {
            throw new InvalidPriceException("currency is required");
        }
        if (amount.signum() < 0) {
            throw new InvalidPriceException("amount must not be negative: " + amount);
        }
        amount = scaled(amount, currency);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyOf(currencyCode));
    }

    private static Currency currencyOf(String code) {
        if (code == null) {
            throw new InvalidPriceException("currency is required");
        }
        try {
            return Currency.getInstance(code);
        } catch (IllegalArgumentException e) {
            throw new InvalidPriceException("unknown currency: " + code, e);
        }
    }

    private static BigDecimal scaled(BigDecimal amount, Currency currency) {
        try {
            return amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new InvalidPriceException("amount " + amount + " has more decimals than " + currency + " allows", e);
        }
    }
}
