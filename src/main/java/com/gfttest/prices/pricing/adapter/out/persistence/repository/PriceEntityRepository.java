package com.gfttest.prices.pricing.adapter.out.persistence.repository;

import com.gfttest.prices.pricing.adapter.out.persistence.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceEntityRepository extends JpaRepository<PriceEntity, Long> {

    List<PriceEntity> findByBrandIdAndProductId(long brandId, long productId);
}
