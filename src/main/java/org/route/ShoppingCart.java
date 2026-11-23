package org.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Prototype scope: every call to getBean(ShoppingCart.class) returns a new, separate cart. */
@Component
@Scope("prototype")
public class ShoppingCart {

    private final ProductService productService;
    private final int maxQuantityPerProduct;

    private final Map<String, Integer> lines = new LinkedHashMap<>();
    private int cartNumber;
    private Customer customer;

    public ShoppingCart(ProductService productService,
                        @Value("${store.max-quantity-per-product}") int maxQuantityPerProduct) {
        this.productService = productService;
        this.maxQuantityPerProduct = maxQuantityPerProduct;
    }

    public int getCartNumber() {
        return cartNumber;
    }

    public void setCartNumber(int cartNumber) {
        this.cartNumber = cartNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /** SKU -> quantity (read-only view). */
    public Map<String, Integer> getLines() {
        return Collections.unmodifiableMap(lines);
    }

    /** CART-01 to CART-03. */
    public void addItem(String sku, int qty) {
        if (qty <= 0) {
            throw new StoreException("Quantity must be at least 1");
        }
        Product product = productService.findBySku(sku);
        if (!product.isActive()) {
            throw new StoreException(product.getName() + " isn't available");
        }
        if (product.getStockQuantity() == 0) {                                   // CART-01
            throw new StoreException(product.getName() + " is out of stock");
        }
        int newQty = lines.getOrDefault(sku, 0) + qty;                           // CART-02
        if (newQty > maxQuantityPerProduct) {                                    // CART-03
            throw new StoreException("You can't order more than " + maxQuantityPerProduct
                    + " of " + product.getName());
        }
        if (newQty > product.getStockQuantity()) {                               // CART-03
            throw new StoreException("Only " + product.getStockQuantity()
                    + " available for " + product.getName());
        }
        lines.put(sku, newQty);
    }

    public BigDecimal subtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> line : lines.entrySet()) {
            sum = sum.add(productService.getPrice(line.getKey(), line.getValue()));
        }
        return Money.round(sum);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }
}
