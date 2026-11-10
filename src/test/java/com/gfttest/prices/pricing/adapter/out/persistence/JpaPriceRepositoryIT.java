package com.gfttest.prices.pricing.adapter.out.persistence;

import com.gfttest.prices.pricing.application.port.out.PriceRepository;
import com.gfttest.prices.pricing.domain.model.Money;
import com.gfttest.prices.pricing.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaPriceRepository.class)
class JpaPriceRepositoryIT {

    @Autowired
    private PriceRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void startFromAnEmptyTable() {
        jdbc.update("delete from prices");
    }

    @Test
    void findsEveryPriceOfTheBrandAndProductWithEveryField() {
        Price wholeSeason = price(1, 35455, 1, 0, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "35.50");
        Price june14Afternoon = price(1, 35455, 2, 1, "2020-06-14T15:00:00", "2020-06-14T18:30:00", "25.45");
        seed(wholeSeason);
        seed(june14Afternoon);
        seed(price(1, 99, 3, 1, "2020-06-15T00:00:00", "2020-06-15T11:00:00", "30.50"));
        seed(price(2, 35455, 4, 1, "2020-06-15T16:00:00", "2020-12-31T23:59:59", "38.95"));

        List<Price> found = repository.findByBrandAndProduct(1, 35455);

        assertThat(found).containsExactlyInAnyOrder(wholeSeason, june14Afternoon);
    }

    @Test
    void findsNothingForAnotherBrand() {
        seed(price(1, 35455, 1, 0, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "35.50"));

        List<Price> found = repository.findByBrandAndProduct(2, 35455);

        assertThat(found).isEmpty();
    }

    @Test
    void findsNothingForAnotherProduct() {
        seed(price(1, 35455, 1, 0, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "35.50"));

        List<Price> found = repository.findByBrandAndProduct(1, 99);

        assertThat(found).isEmpty();
    }

    private void seed(Price price) {
        jdbc.update("""
                        insert into prices (brand_id, product_id, price_list, priority, start_date, end_date, price, curr)
                        values (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                price.brandId(), price.productId(), price.priceList(), price.priority(),
                price.startDate(), price.endDate(), price.money().amount(), price.money().currency().getCurrencyCode());
    }

    private static Price price(long brandId, long productId, long priceList, int priority,
                               String start, String end, String amount) {
        return new Price(brandId, productId, priceList, priority, LocalDateTime.parse(start), LocalDateTime.parse(end),
                Money.of(new BigDecimal(amount), "EUR"));
    }
}
