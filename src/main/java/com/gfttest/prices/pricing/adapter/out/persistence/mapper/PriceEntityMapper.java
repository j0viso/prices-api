package com.gfttest.prices.pricing.adapter.out.persistence.mapper;

import com.gfttest.prices.pricing.adapter.out.persistence.entity.PriceEntity;
import com.gfttest.prices.pricing.domain.model.Money;
import com.gfttest.prices.pricing.domain.model.Price;

public final class PriceEntityMapper {

    private PriceEntityMapper() {
    }

    public static Price toPrice(PriceEntity entity) {
        return new Price(
                entity.getBrandId(),
                entity.getProductId(),
                entity.getPriceList(),
                entity.getPriority(),
                entity.getStartDate(),
                entity.getEndDate(),
                Money.of(entity.getPrice(), entity.getCurr()));
    }
}
