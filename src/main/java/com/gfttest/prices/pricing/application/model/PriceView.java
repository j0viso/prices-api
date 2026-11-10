package com.gfttest.prices.pricing.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceView(
        long brandId,
        long productId,
        long priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency) {
}
