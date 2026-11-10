package com.gfttest.prices.pricing.adapter.out.persistence;

import com.gfttest.prices.pricing.adapter.out.persistence.mapper.PriceEntityMapper;
import com.gfttest.prices.pricing.adapter.out.persistence.repository.PriceEntityRepository;
import com.gfttest.prices.pricing.application.port.out.PriceRepository;
import com.gfttest.prices.pricing.domain.model.Price;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaPriceRepository implements PriceRepository {

    private final PriceEntityRepository entities;

    JpaPriceRepository(PriceEntityRepository entities) {
        this.entities = entities;
    }

    @Override
    public List<Price> findByBrandAndProduct(long brandId, long productId) {
        return entities.findByBrandIdAndProductId(brandId, productId).stream()
                .map(PriceEntityMapper::toPrice)
                .toList();
    }
}
