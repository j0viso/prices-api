package com.gfttest.prices.pricing.application.query;

import com.gfttest.prices.pricing.application.exception.InvalidQueryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetApplicablePriceQueryTest {

    private static final LocalDateTime JUNE_14_AT_10 = LocalDateTime.of(2020, 6, 14, 10, 0);

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void rejectsANonPositiveBrandId(long brandId) {
        assertThatThrownBy(() -> new GetApplicablePriceQuery(brandId, 35455, JUNE_14_AT_10))
                .isInstanceOf(InvalidQueryException.class)
                .hasMessage("brandId must be positive");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void rejectsANonPositiveProductId(long productId) {
        assertThatThrownBy(() -> new GetApplicablePriceQuery(1, productId, JUNE_14_AT_10))
                .isInstanceOf(InvalidQueryException.class)
                .hasMessage("productId must be positive");
    }

    @Test
    void rejectsAMissingApplicationDate() {
        assertThatThrownBy(() -> new GetApplicablePriceQuery(1, 35455, null))
                .isInstanceOf(InvalidQueryException.class)
                .hasMessage("applicationDate is required");
    }

    @Test
    void keepsPositiveIdsAndTheDate() {
        GetApplicablePriceQuery query = new GetApplicablePriceQuery(1, 35455, JUNE_14_AT_10);

        assertThat(query.brandId()).isEqualTo(1);
        assertThat(query.productId()).isEqualTo(35455);
        assertThat(query.applicationDate()).isEqualTo(JUNE_14_AT_10);
    }
}
