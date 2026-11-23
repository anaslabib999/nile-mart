package org.route;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductService productService;
    private final ShippingQuoteService shippingQuoteService;
    private final TaxCalculator taxCalculator;
    private final OrderRegistry orderRegistry;
    private final List<NotificationSender> notificationSenders;

    public Order placeOrder(ShoppingCart cart, Governorate governorate) {
        // 1. ORD-02: empty cart
        if (cart.isEmpty()) {
            throw new StoreException("The cart is empty");
        }
        if (cart.getCustomer() == null) {
            throw new StoreException("The cart has no customer");
        }
        if (governorate == null) {
            throw new StoreException("Please choose a governorate");
        }

        // 2. ORD-03: check the stock of every line again, before changing anything
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            Product product = productService.findBySku(line.getKey());
            if (product.getStockQuantity() < line.getValue()) {
                throw new StoreException("Not enough stock for " + product.getName()
                        + ": only " + product.getStockQuantity() + " available");
            }
        }

        // 3. totals
        BigDecimal subtotal = cart.subtotal();
        BigDecimal vat = taxCalculator.calculate(subtotal);
        BigDecimal shipping = shippingQuoteService.quote(governorate, subtotal);
        BigDecimal total = Money.round(subtotal.add(vat).add(shipping));

        // 4. ORD-06: reduce stock for all lines (all were just checked, so none can fail)
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            productService.reduceStock(line.getKey(), line.getValue());
        }

        // 5. save the order
        Order order = new Order(orderRegistry.nextOrderNumber(), cart.getCustomer(), governorate,
                new LinkedHashMap<>(cart.getLines()), subtotal, vat, shipping, total);
        orderRegistry.save(order);

        // 6. ORDER_CONFIRMED (NOT-04): every sender in the list
        String message = "Order " + order.orderNumber() + " confirmed. Total "
                + Money.format(total) + " EGP";
        for (NotificationSender sender : notificationSenders) {
            sender.send(order.customer(), message);
        }

        // 7. empty the cart
        cart.clear();
        return order;
    }
}
