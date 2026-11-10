package com.gfttest.prices.pricing.application.port.in;

import com.gfttest.prices.pricing.application.model.PriceView;
import com.gfttest.prices.pricing.application.query.GetApplicablePriceQuery;

public interface GetApplicablePrice {

    PriceView find(GetApplicablePriceQuery query);
}
