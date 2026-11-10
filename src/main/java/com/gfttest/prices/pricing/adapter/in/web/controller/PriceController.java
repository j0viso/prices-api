package com.gfttest.prices.pricing.adapter.in.web.controller;

import com.gfttest.prices.pricing.adapter.in.web.api.PricingApi;
import com.gfttest.prices.pricing.adapter.in.web.dto.PriceResponse;
import com.gfttest.prices.pricing.application.model.PriceView;
import com.gfttest.prices.pricing.application.port.in.GetApplicablePrice;
import com.gfttest.prices.pricing.application.query.GetApplicablePriceQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
class PriceController implements PricingApi {

    private final GetApplicablePrice getApplicablePrice;

    PriceController(GetApplicablePrice getApplicablePrice) {
        this.getApplicablePrice = getApplicablePrice;
    }

    @Override
    public ResponseEntity<PriceResponse> getApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        PriceView view = getApplicablePrice.find(new GetApplicablePriceQuery(brandId, productId, applicationDate));
        return ResponseEntity.ok(toResponse(view));
    }

    private static PriceResponse toResponse(PriceView view) {
        return new PriceResponse(
                view.brandId(),
                view.productId(),
                view.priceList(),
                view.startDate(),
                view.endDate(),
                view.price(),
                view.currency());
    }
}
