package com.gfttest.prices.pricing.config;

import com.gfttest.prices.pricing.application.port.in.GetApplicablePrice;
import com.gfttest.prices.pricing.application.port.out.PriceRepository;
import com.gfttest.prices.pricing.application.usecase.GetApplicablePriceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PricingConfig {

    @Bean
    GetApplicablePrice getApplicablePrice(PriceRepository prices) {
        return new GetApplicablePriceUseCase(prices);
    }
}
