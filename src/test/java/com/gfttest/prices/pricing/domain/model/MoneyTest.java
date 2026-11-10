package com.gfttest.prices.pricing.domain.model;

import com.gfttest.prices.pricing.domain.exception.InvalidPriceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void rejectsAMissingAmount() {
        assertThatThrownBy(() -> new Money(null, EUR))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("amount is required");
    }

    @Test
    void rejectsAMissingCurrency() {
        assertThatThrownBy(() -> new Money(new BigDecimal("35.50"), null))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("currency is required");
    }

    @Test
    void rejectsANegativeAmount() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-0.01"), EUR))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("amount must not be negative: -0.01");
    }

    @Test
    void acceptsZero() {
        Money money = new Money(BigDecimal.ZERO, EUR);

        assertThat(money.amount()).isEqualTo(new BigDecimal("0.00"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"35.5", "35.50", "35.5000"})
    void scalesTheAmountToItsCurrency(String amount) {
        Money money = new Money(new BigDecimal(amount), EUR);

        assertThat(money.amount()).isEqualTo(new BigDecimal("35.50"));
    }

    @Test
    void rejectsAnAmountWithMoreDecimalsThanItsCurrencyAllows() {
        assertThatThrownBy(() -> new Money(new BigDecimal("35.505"), EUR))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("amount 35.505 has more decimals than EUR allows")
                .hasCauseInstanceOf(ArithmeticException.class);
    }

    @Test
    void buildsFromAnIsoCurrencyCode() {
        Money money = Money.of(new BigDecimal("25.45"), "EUR");

        assertThat(money.amount()).isEqualTo(new BigDecimal("25.45"));
        assertThat(money.currency()).isEqualTo(EUR);
    }

    @Test
    void rejectsAnUnknownCurrencyCode() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("25.45"), "EUROS"))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("unknown currency: EUROS")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAMissingCurrencyCode() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("25.45"), null))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("currency is required");
    }
}
