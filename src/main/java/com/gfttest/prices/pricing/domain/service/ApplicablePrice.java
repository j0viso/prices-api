package com.gfttest.prices.pricing.domain.service;

import com.gfttest.prices.pricing.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class ApplicablePrice {

    private ApplicablePrice() {
    }

    public static Optional<Price> select(LocalDateTime date, Collection<Price> prices) {
        return prices.stream()
                .filter(price -> price.appliesAt(date))
                .max(Comparator.comparingInt(Price::priority));
    }
}
