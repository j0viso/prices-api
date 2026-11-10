package com.gfttest.prices.pricing.application.usecase;

import com.gfttest.prices.pricing.application.exception.PriceNotFoundException;
import com.gfttest.prices.pricing.application.model.PriceView;
import com.gfttest.prices.pricing.application.port.in.GetApplicablePrice;
import com.gfttest.prices.pricing.application.port.out.PriceRepository;
import com.gfttest.prices.pricing.application.query.GetApplicablePriceQuery;
import com.gfttest.prices.pricing.domain.model.Price;
import com.gfttest.prices.pricing.domain.service.ApplicablePrice;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class GetApplicablePriceUseCase implements GetApplicablePrice {

    private final PriceRepository prices;

    public GetApplicablePriceUseCase(PriceRepository prices) {
        this.prices = prices;
    }

    @Override
    public PriceView find(GetApplicablePriceQuery query) {
        List<Price> candidates = prices.findByBrandAndProduct(query.brandId(), query.productId());
        return ApplicablePrice.select(query.applicationDate(), candidates)
                .map(GetApplicablePriceUseCase::toView)
                .orElseThrow(() -> new PriceNotFoundException("no price for product " + query.productId()
                        + " of brand " + query.brandId()
                        + " at " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(query.applicationDate())));
    }

    private static PriceView toView(Price price) {
        return new PriceView(
                price.brandId(),
                price.productId(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.money().amount(),
                price.money().currency().getCurrencyCode());
    }
}
