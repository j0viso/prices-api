package com.gfttest.prices.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:get-applicable-price;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class GetApplicablePriceIT {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @CsvSource({
            "2020-06-14T10:00:00, 1, 2020-06-14T00:00:00, 2020-12-31T23:59:59, 35.50",
            "2020-06-14T16:00:00, 2, 2020-06-14T15:00:00, 2020-06-14T18:30:00, 25.45",
            "2020-06-14T21:00:00, 1, 2020-06-14T00:00:00, 2020-12-31T23:59:59, 35.50",
            "2020-06-15T10:00:00, 3, 2020-06-15T00:00:00, 2020-06-15T11:00:00, 30.50",
            "2020-06-16T21:00:00, 4, 2020-06-15T16:00:00, 2020-12-31T23:59:59, 38.95"
    })
    void answersThePriceInForceAtEachDateOfTheExercise(String at, long priceList, String start, String end, String price)
            throws Exception {
        mvc.perform(get("/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", at))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "brandId": 1,
                          "productId": 35455,
                          "priceList": %d,
                          "startDate": "%s",
                          "endDate": "%s",
                          "price": %s,
                          "currency": "EUR"
                        }
                        """.formatted(priceList, start, end, price), true));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 35455, 2020-06-13T10:00:00",
            "1, 1,     2020-06-14T10:00:00",
            "2, 35455, 2020-06-14T10:00:00"
    })
    void isNotFoundWhenNoPriceOfTheBrandAndProductCoversTheDate(String brandId, String productId, String at)
            throws Exception {
        mvc.perform(get("/prices")
                        .param("brandId", brandId)
                        .param("productId", productId)
                        .param("applicationDate", at))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Price not found"))
                .andExpect(jsonPath("$.detail").value("no price for product " + productId + " of brand " + brandId + " at " + at));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 35455, brandId must be positive",
            "1, -1,    productId must be positive"
    })
    void refusesANonPositiveIdWithAProblemDetail(String brandId, String productId, String detail) throws Exception {
        mvc.perform(get("/prices")
                        .param("brandId", brandId)
                        .param("productId", productId)
                        .param("applicationDate", "2020-06-14T16:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid query"))
                .andExpect(jsonPath("$.detail").value(detail));
    }

    @Test
    void refusesAMissingParameterWithAProblemDetail() throws Exception {
        mvc.perform(get("/prices")
                        .param("brandId", "1")
                        .param("productId", "35455"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString("applicationDate")));
    }

    @Test
    void refusesANonNumericIdWithAProblemDetail() throws Exception {
        mvc.perform(get("/prices")
                        .param("brandId", "1")
                        .param("productId", "abc")
                        .param("applicationDate", "2020-06-14T16:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString("productId")));
    }

    @Test
    void refusesAMalformedDateWithAProblemDetail() throws Exception {
        mvc.perform(get("/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "14/06/2020 16:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString("applicationDate")));
    }
}
