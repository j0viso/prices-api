package com.gfttest.prices.pricing.domain.model;

import com.gfttest.prices.pricing.domain.exception.InvalidPriceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {

    private static final LocalDateTime START = LocalDateTime.of(2020, 6, 14, 15, 0);
    private static final LocalDateTime END = LocalDateTime.of(2020, 6, 14, 18, 30);

    @Test
    void rejectsAMissingStartDate() {
        assertThatThrownBy(() -> new Price(1, 35455, 2, 1, null, END, eur("25.45")))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("startDate is required");
    }

    @Test
    void rejectsAMissingEndDate() {
        assertThatThrownBy(() -> new Price(1, 35455, 2, 1, START, null, eur("25.45")))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("endDate is required");
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        assertThatThrownBy(() -> new Price(1, 35455, 2, 1, END, START, eur("25.45")))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("endDate 2020-06-14T15:00 is before startDate 2020-06-14T18:30");
    }

    @Test
    void acceptsARangeOfASingleInstant() {
        Price price = new Price(1, 35455, 2, 1, START, START, eur("25.45"));

        assertThat(price.startDate()).isEqualTo(START);
        assertThat(price.endDate()).isEqualTo(START);
    }

    @Test
    void rejectsMissingMoney() {
        assertThatThrownBy(() -> new Price(1, 35455, 2, 1, START, END, null))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage("money is required");
    }

    @ParameterizedTest
    @CsvSource({
            "2020-06-14T14:59:59, false",
            "2020-06-14T15:00:00, true",
            "2020-06-14T16:00:00, true",
            "2020-06-14T18:30:00, true",
            "2020-06-14T18:30:01, false"
    })
    void appliesBetweenItsDatesBothIncluded(LocalDateTime at, boolean applies) {
        Price price = new Price(1, 35455, 2, 1, START, END, eur("25.45"));

        assertThat(price.appliesAt(at)).isEqualTo(applies);
    }

    private static Money eur(String amount) {
        return Money.of(new BigDecimal(amount), "EUR");
    }
}
