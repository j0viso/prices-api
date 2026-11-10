package com.gfttest.prices.pricing.application.usecase;

import com.gfttest.prices.pricing.application.exception.PriceNotFoundException;
import com.gfttest.prices.pricing.application.model.PriceView;
import com.gfttest.prices.pricing.application.port.out.PriceRepository;
import com.gfttest.prices.pricing.application.query.GetApplicablePriceQuery;
import com.gfttest.prices.pricing.domain.model.Money;
import com.gfttest.prices.pricing.domain.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetApplicablePriceUseCaseTest {

    private static final LocalDateTime JUNE_14_AT_16 = LocalDateTime.of(2020, 6, 14, 16, 0);

    private final PriceRepository prices = mock(PriceRepository.class);
    private final GetApplicablePriceUseCase getApplicablePrice = new GetApplicablePriceUseCase(prices);

    @Test
    void answersTheHighestPriorityPriceCoveringTheDateWithEveryField() {
        when(prices.findByBrandAndProduct(1, 35455)).thenReturn(List.of(
                price(1, 35455, 1, 0, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "35.50"),
                price(1, 35455, 2, 1, "2020-06-14T15:00:00", "2020-06-14T18:30:00", "25.45")));

        PriceView found = getApplicablePrice.find(new GetApplicablePriceQuery(1, 35455, JUNE_14_AT_16));

        assertThat(found).isEqualTo(new PriceView(1, 35455, 2,
                LocalDateTime.parse("2020-06-14T15:00:00"), LocalDateTime.parse("2020-06-14T18:30:00"),
                new BigDecimal("25.45"), "EUR"));
    }

    @Test
    void throwsNotFoundWhenNoPriceOfTheBrandAndProductCoversTheDate() {
        when(prices.findByBrandAndProduct(1, 35455)).thenReturn(List.of(
                price(1, 35455, 1, 0, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "35.50")));

        assertThatThrownBy(() -> getApplicablePrice.find(
                new GetApplicablePriceQuery(1, 35455, LocalDateTime.parse("2020-06-13T10:00:00"))))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessage("no price for product 35455 of brand 1 at 2020-06-13T10:00:00");
    }

    @Test
    void throwsNotFoundWhenTheBrandAndProductHaveNoPrice() {
        when(prices.findByBrandAndProduct(2, 35455)).thenReturn(List.of());

        assertThatThrownBy(() -> getApplicablePrice.find(new GetApplicablePriceQuery(2, 35455, JUNE_14_AT_16)))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessage("no price for product 35455 of brand 2 at 2020-06-14T16:00:00");
    }

    private static Price price(long brandId, long productId, long priceList, int priority,
                               String start, String end, String amount) {
        return new Price(brandId, productId, priceList, priority, LocalDateTime.parse(start), LocalDateTime.parse(end),
                Money.of(new BigDecimal(amount), "EUR"));
    }
}
