package org.route;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** The normal calculator: the fee of the governorate's zone (SHIP-01). */
@Component
@Primary
public class ZoneShippingCalculator implements ShippingCalculator {

    @Override
    public BigDecimal calculate(Governorate g) {
        return g.getZone().getFee();
    }
}
