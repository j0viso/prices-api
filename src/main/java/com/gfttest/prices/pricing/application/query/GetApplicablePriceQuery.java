package com.gfttest.prices.pricing.application.query;

import com.gfttest.prices.pricing.application.exception.InvalidQueryException;

import java.time.LocalDateTime;

public record GetApplicablePriceQuery(long brandId, long productId, LocalDateTime applicationDate) {

    public GetApplicablePriceQuery {
        if (brandId <= 0) {
            throw new InvalidQueryException("brandId must be positive");
        }
        if (productId <= 0) {
            throw new InvalidQueryException("productId must be positive");
        }
        if (applicationDate == null) {
            throw new InvalidQueryException("applicationDate is required");
        }
    }
}
