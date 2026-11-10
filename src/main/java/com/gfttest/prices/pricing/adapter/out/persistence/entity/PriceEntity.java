package com.gfttest.prices.pricing.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceEntity {

    @Id
    private Long id;

    private long brandId;

    private long productId;

    private long priceList;

    private int priority;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.CHAR)
    private String curr;
}
