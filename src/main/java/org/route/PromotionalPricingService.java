package org.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Percentage discount taken from store.discount-amount (20 means 20% off). */
@Component
public class PromotionalPricingService implements PricingService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BigDecimal discountAmount;

    public PromotionalPricingService(@Value("${store.discount-amount}") BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal getPrice(Product product, int qty) {
        BigDecimal gross = product.getPrice().multiply(BigDecimal.valueOf(qty));
        return gross.multiply(HUNDRED.subtract(discountAmount))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
