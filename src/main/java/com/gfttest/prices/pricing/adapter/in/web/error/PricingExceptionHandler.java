package com.gfttest.prices.pricing.adapter.in.web.error;

import com.gfttest.prices.pricing.application.exception.InvalidQueryException;
import com.gfttest.prices.pricing.application.exception.PriceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class PricingExceptionHandler {

    @ExceptionHandler(InvalidQueryException.class)
    ProblemDetail invalidQuery(InvalidQueryException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid query");
        return problem;
    }

    @ExceptionHandler(PriceNotFoundException.class)
    ProblemDetail priceNotFound(PriceNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Price not found");
        return problem;
    }
}
