package org.route;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    // LinkedHashMap keeps the products in the order they were added
    private final Map<String, Product> store = new LinkedHashMap<>();

    @PostConstruct
    void saveProducts() {
        add("PH-X5-128", "Smartphone X5 128GB", "Electronics", "16000.00", 12);
        add("ACC-CHG-20W", "Fast Charger 20W", "Electronics", "650.00", 40);
        add("ACC-BUDS-6", "Wireless Earbuds", "Electronics", "1100.00", 8);
        add("FSH-GAL-001", "Cotton Galabeya", "Fashion", "900.00", 25);
        add("HOM-PAN-28", "Non-stick Frying Pan 28cm", "Home & Kitchen", "1200.00", 3);
        add("HOM-SPC-RCK", "Wooden Spice Rack", "Home & Kitchen", "750.00", 10);
        add("BTY-SUN-50", "Sun Cream SPF50", "Beauty", "350.00", 0);
        add("BK-CAI-TRI", "The Cairo Trilogy", "Books", "450.00", 15);
    }

    private void add(String sku, String name, String category, String price, int stock) {
        save(Product.builder()
                .sku(sku)
                .name(name)
                .category(category)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .isActive(true)
                .build());
    }

    @PreDestroy
    void close() {
        System.out.println("Catalog closed");
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(store.get(sku));
    }

    @Override
    public void save(Product product) {
        store.put(product.getSku(), product);
    }
}
