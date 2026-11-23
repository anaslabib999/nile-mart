package org.route;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderRegistry {

    private final List<Order> orders = new ArrayList<>();
    private LocalDate sequenceDate;
    private int sequence;

    /** GEN-07: NM-YYYYMMDD-NNNNN. The counter restarts every day. */
    public synchronized String nextOrderNumber() {
        LocalDate today = LocalDate.now();
        if (!today.equals(sequenceDate)) {
            sequenceDate = today;
            sequence = 0;
        }
        sequence++;
        return "NM-" + today.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%05d", sequence);
    }

    public synchronized void save(Order order) {
        orders.add(order);
    }

    public synchronized List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    @PreDestroy
    void endOfDay() {
        BigDecimal revenue = BigDecimal.ZERO;
        for (Order order : orders) {
            revenue = revenue.add(order.total());
        }
        System.out.println("End of day: " + orders.size() + " orders, revenue "
                + Money.format(revenue) + " EGP");
    }
}
