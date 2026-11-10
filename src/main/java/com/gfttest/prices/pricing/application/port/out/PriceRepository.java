package com.gfttest.prices.pricing.application.port.out;

import com.gfttest.prices.pricing.domain.model.Price;

import java.util.List;

public interface PriceRepository {

    List<Price> findByBrandAndProduct(long brandId, long productId);
}
