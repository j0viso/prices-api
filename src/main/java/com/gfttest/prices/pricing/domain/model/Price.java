package com.gfttest.prices.pricing.domain.model;

import com.gfttest.prices.pricing.domain.exception.InvalidPriceException;

import java.time.LocalDateTime;

public record Price(
        long brandId,
        long productId,
        long priceList,
        int priority,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Money money) {

    public Price {
        if (startDate == null) {
            throw new InvalidPriceException("startDate is required");
        }
        if (endDate == null) {
            throw new InvalidPriceException("endDate is required");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidPriceException("endDate " + endDate + " is before startDate " + startDate);
        }
        if (money == null) {
            throw new InvalidPriceException("money is required");
        }
    }

    public boolean appliesAt(LocalDateTime date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
