package com.gfttest.prices.pricing.domain.service;

import com.gfttest.prices.pricing.domain.model.Money;
import com.gfttest.prices.pricing.domain.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicablePriceTest {

    private static final LocalDateTime JUNE_14_AT_16 = LocalDateTime.of(2020, 6, 14, 16, 0);

    private final Price wholeSeason = price(1, 0, "2020-06-14T00:00:00", "2020-12-31T23:59:59");
    private final Price june14Afternoon = price(2, 1, "2020-06-14T15:00:00", "2020-06-14T18:30:00");
    private final Price june15Morning = price(3, 1, "2020-06-15T00:00:00", "2020-06-15T11:00:00");

    @Test
    void findsNothingAmongNoPrices() {
        Optional<Price> found = ApplicablePrice.select(JUNE_14_AT_16, List.of());

        assertThat(found).isEmpty();
    }

    @Test
    void findsNothingWhenNoPriceCoversTheDate() {
        Optional<Price> found = ApplicablePrice.select(JUNE_14_AT_16, List.of(june15Morning));

        assertThat(found).isEmpty();
    }

    @Test
    void prefersTheHighestPriorityWhateverTheOrder() {
        Optional<Price> lowestPriorityFirst = ApplicablePrice.select(JUNE_14_AT_16, List.of(wholeSeason, june14Afternoon));
        Optional<Price> highestPriorityFirst = ApplicablePrice.select(JUNE_14_AT_16, List.of(june14Afternoon, wholeSeason));

        assertThat(lowestPriorityFirst).contains(june14Afternoon);
        assertThat(highestPriorityFirst).contains(june14Afternoon);
    }

    @Test
    void ignoresAHigherPriorityPriceOutsideItsDates() {
        Optional<Price> found = ApplicablePrice.select(JUNE_14_AT_16, List.of(wholeSeason, june15Morning));

        assertThat(found).contains(wholeSeason);
    }

    private static Price price(long priceList, int priority, String start, String end) {
        return new Price(1, 35455, priceList, priority, LocalDateTime.parse(start), LocalDateTime.parse(end),
                Money.of(new BigDecimal("10.00"), "EUR"));
    }
}
