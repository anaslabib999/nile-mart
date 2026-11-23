package org.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TaxCalculator {

    private final BigDecimal vatRate;

    public TaxCalculator(@Value("${store.vat-rate}") BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    /** GEN-03: VAT is a percentage of the subtotal, rounded HALF_UP to 2 decimals. */
    public BigDecimal calculate(BigDecimal subtotal) {
        return Money.round(subtotal.multiply(vatRate));
    }
}
