package org.route;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Map<Integer, ShoppingCart> carts = new HashMap<>();
    private static int lastCartNumber = 0;

    public static void main(String[] args) {
        // try-with-resources closes the context on exit, so the @PreDestroy methods run
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            ProductService productService = context.getBean(ProductService.class);
            CheckoutService checkoutService = context.getBean(CheckoutService.class);
            Scanner in = new Scanner(System.in);

            while (true) {
                printMenu();
                String choice = ask(in, "Choose: ");
                if (choice == null || choice.equals("0")) {
                    break;
                }
                try {
                    switch (choice) {
                        case "1" -> showProducts(productService);
                        case "2" -> createCart(context, in);
                        case "3" -> addToCart(in);
                        case "4" -> showCart(in, productService);
                        case "5" -> checkOut(in, checkoutService);
                        default -> System.out.println("Please choose a number from the menu.");
                    }
                } catch (StoreException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            System.out.println("Goodbye!");
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("========== NILEMART ==========");
        System.out.println("1. Show products");
        System.out.println("2. Create a cart");
        System.out.println("3. Add a product to a cart");
        System.out.println("4. Show a cart");
        System.out.println("5. Check out a cart");
        System.out.println("0. Exit");
    }

    private static void showProducts(ProductService productService) {
        System.out.printf("%-14s %-28s %-16s %12s %6s%n", "SKU", "Name", "Category", "Price (EGP)", "Stock");
        for (Product p : productService.findAll()) {
            System.out.printf("%-14s %-28s %-16s %12s %6d%n", p.getSku(), p.getName(), p.getCategory(),
                    Money.format(p.getPrice()), p.getStockQuantity());
        }
    }

    private static void createCart(AnnotationConfigApplicationContext context, Scanner in) {
        String name = ask(in, "Customer full name: ");
        String phone = ask(in, "Phone: ");
        String email = ask(in, "Email (leave empty for none): ");
        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            throw new StoreException("Name and phone are required");
        }
        Customer customer = new Customer(name.trim(), phone.trim(),
                email == null || email.isBlank() ? null : email.trim());

        ShoppingCart cart = context.getBean(ShoppingCart.class);   // a new cart each time (prototype)
        cart.setCartNumber(++lastCartNumber);
        cart.setCustomer(customer);
        carts.put(cart.getCartNumber(), cart);
        System.out.println("Cart " + cart.getCartNumber() + " created for " + customer.fullName());
    }

    private static void addToCart(Scanner in) {
        ShoppingCart cart = findCart(in);
        String sku = ask(in, "Product SKU: ");
        int qty = askInt(in, "Quantity: ");
        cart.addItem(sku == null ? "" : sku.trim(), qty);
        System.out.println("Added to cart " + cart.getCartNumber());
    }

    private static void showCart(Scanner in, ProductService productService) {
        ShoppingCart cart = findCart(in);
        System.out.println("Cart " + cart.getCartNumber() + " - " + cart.getCustomer().fullName());
        if (cart.isEmpty()) {
            System.out.println("(empty)");
            return;
        }
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            Product p = productService.findBySku(line.getKey());
            System.out.printf("  %-28s x %-3d %12s EGP%n", p.getName(), line.getValue(),
                    Money.format(productService.getPrice(line.getKey(), line.getValue())));
        }
        System.out.println("  Subtotal: " + Money.format(cart.subtotal()) + " EGP");
    }

    private static void checkOut(Scanner in, CheckoutService checkoutService) {
        ShoppingCart cart = findCart(in);
        Governorate[] all = Governorate.values();
        for (int i = 0; i < all.length; i++) {
            System.out.printf("%2d. %-16s (%s)%n", i + 1, all[i].getDisplayName(), all[i].getZone());
        }
        int number = askInt(in, "Governorate number: ");
        if (number < 1 || number > all.length) {
            throw new StoreException("Please choose a governorate from the list");
        }
        Order order = checkoutService.placeOrder(cart, all[number - 1]);
        System.out.println("Order " + order.orderNumber() + " placed");
        System.out.println("  Subtotal: " + Money.format(order.subtotal()) + " EGP");
        System.out.println("  VAT:      " + Money.format(order.vat()) + " EGP");
        System.out.println("  Shipping: " + Money.format(order.shippingFee()) + " EGP");
        System.out.println("  Total:    " + Money.format(order.total()) + " EGP");
    }

    private static ShoppingCart findCart(Scanner in) {
        int number = askInt(in, "Cart number: ");
        ShoppingCart cart = carts.get(number);
        if (cart == null) {
            throw new StoreException("Cart " + number + " does not exist");
        }
        return cart;
    }

    /** Returns null when the input has ended (e.g. piped input), so the program can exit cleanly. */
    private static String ask(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private static int askInt(Scanner in, String prompt) {
        String text = ask(in, prompt);
        try {
            return Integer.parseInt(text == null ? "" : text);
        } catch (NumberFormatException e) {
            throw new StoreException("Please enter a valid number");
        }
    }
}
