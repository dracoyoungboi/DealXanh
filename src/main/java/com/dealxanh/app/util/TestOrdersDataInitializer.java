package com.dealxanh.app.util;

import com.dealxanh.app.entity.*;
import com.dealxanh.app.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Test Orders Data Initializer
 * Script tạo dữ liệu test cho trang Orders Admin
 *
 * Cách sử dụng:
 * 1. Uncomment @Component annotation để kích hoạt
 * 2. Chạy application
 * 3. Sau khi xong, comment lại @Component để không chạy lại lần sau
 *
 * Hoặc chạy trực tiếp từ beans:
 * testOrdersDataInitializer.run()
 */
//@Component  // Uncomment để chạy
public class TestOrdersDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== STARTING TEST ORDERS DATA INITIALIZER ===");

        try {
            // Kiểm tra xem data đã tồn tại chưa
            if (orderRepository.count() > 50) {
                System.out.println("Data đã tồn tại! Bỏ qua initialization.");
                return;
            }

            createTestData();
            System.out.println("=== TEST ORDERS DATA INITIALIZED SUCCESSFULLY ===");
        } catch (Exception e) {
            System.err.println("ERROR initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    public void createTestData() {
        // BƯỚC 1: TẠO ROLES
        Role roleAdmin = createRole("ROLE_ADMIN");
        Role roleUser = createRole("ROLE_USER");
        Role roleStoreOwner = createRole("ROLE_STORE_OWNER");

        // BƯỚC 2: TẠO USERS
        User owner1 = createUser("store_owner_1", "owner1@test.com", "Nguyễn Văn A", "0901111111", roleStoreOwner);
        User owner2 = createUser("store_owner_2", "owner2@test.com", "Trần Thị B", "0902222222", roleStoreOwner);
        User owner3 = createUser("store_owner_3", "owner3@test.com", "Lê Văn C", "0903333333", roleStoreOwner);

        User buyer1 = createUser("buyer1", "buyer1@test.com", "Phạm Minh Tuấn", "0911111111", roleUser);
        User buyer2 = createUser("buyer2", "buyer2@test.com", "Hoàng Thanh Lan", "0922222222", roleUser);
        User buyer3 = createUser("buyer3", "buyer3@test.com", "Đỗ Quốc Huy", "0933333333", roleUser);
        User buyer4 = createUser("buyer4", "buyer4@test.com", "Vũ Thị Mai", "0944444444", roleUser);

        // BƯỚC 3: TẠO STORES
        Store store1 = createStore("Bánh Mì Như Ý", owner1, "Quán Ăn", "Hà Nội", "Hoàn Kiếm", "/img/stores/banhmi.jpg");
        Store store2 = createStore("Cà Phê Thức Mình", owner2, "Quán Cà Phê", "Hồ Chí Minh", "Quận 1", "/img/stores/cafe.jpg");
        Store store3 = createStore("Phở Bò 3 Mươi", owner3, "Quán Ăn", "Hà Nội", "Ba Đình", "/img/stores/pho.jpg");

        // BƯỚC 4: TẠO PRODUCTS
        Product p1_1 = createProduct(store1, "Bánh Mì Thịt Nướng", "Bánh mì thịt nướng đặc biệt", 25000.0);
        Product p1_2 = createProduct(store1, "Bánh Mì Chả Lụa", "Bánh mì chả lụa truyền thống", 20000.0);
        Product p1_3 = createProduct(store1, "Bánh Mì Bi Sườn", "Bánh mì bi sườn bì tonnes", 30000.0);

        Product p2_1 = createProduct(store2, "Cà Phê Sữa Đá", "Cà phê sữa đá truyền thống", 25000.0);
        Product p2_2 = createProduct(store2, "Bạc Xỉu", "Bạc xỉu nhạt hơn cà phê sữa", 20000.0);
        Product p2_3 = createProduct(store2, "Cà Phê Muối", "Cà phê muối giảm cân", 35000.0);

        Product p3_1 = createProduct(store3, "Phở Bò Tái", "Phở bò tái chín từ từ", 45000.0);
        Product p3_2 = createProduct(store3, "Phở Bò Gàu", "Phở bò gàu đầy đặn", 55000.0);
        Product p3_3 = createProduct(store3, "Phở Bò Đặc Biệt", "Phở bò tất cả các phần", 65000.0);

        // BƯỚC 5: TẠO ORDERS

        // Store 1: 5 orders (3 completed, 1 ready, 1 pending)
        createOrder(buyer1, store1, 45000.0, 5000.0, "COMPLETED", "PAID", "MOMO",
            LocalDateTime.now().minusDays(3), "Không hành, ít rau", p1_1, 2);

        createOrder(buyer2, store1, 50000.0, 0.0, "COMPLETED", "PAID", "BANK_TRANSFER",
            LocalDateTime.now().minusDays(2), "Thêm ớt", p1_2, 1, p1_3, 1);

        createOrder(buyer3, store1, 70000.0, 10000.0, "READY_FOR_PICKUP", "PAID", "CASH",
            LocalDateTime.now().plusHours(2), "Gọi trước khi giao", p1_3, 1);

        createOrder(buyer4, store1, 20000.0, 0.0, "PENDING", "UNPAID", "CASH",
            LocalDateTime.now().plusHours(4), null, p1_2, 1);

        createOrder(buyer1, store1, 95000.0, 15000.0, "CONFIRMED", "PAID", "ZALOPAY",
            LocalDateTime.now().plusHours(6), null, p1_1, 2, p1_2, 1);

        // Store 2: 4 orders (2 completed, 1 pending, 1 cancelled)
        createOrder(buyer2, store2, 45000.0, 0.0, "COMPLETED", "PAID", "MOMO",
            LocalDateTime.now().minusDays(1), "Ít đá, nhiều sữa", p2_1, 1, p2_2, 1);

        createOrder(buyer3, store2, 20000.0, 0.0, "COMPLETED", "PAID", "CASH",
            LocalDateTime.now().minusHours(12), null, p2_2, 1);

        createOrder(buyer4, store2, 55000.0, 5000.0, "PENDING", "UNPAID", "BANK_TRANSFER",
            LocalDateTime.now().plusHours(3), null, p2_3, 1, p2_1, 1);

        createOrder(buyer1, store2, 25000.0, 0.0, "CANCELLED", "UNPAID", "CASH",
            LocalDateTime.now().plusHours(5), "Khách hủy vì đổi ý", p2_1, 1);

        // Store 3: 4 orders (1 completed, 1 ready, 1 pending, 1 confirmed)
        createOrder(buyer3, store3, 100000.0, 10000.0, "COMPLETED", "PAID", "MOMO",
            LocalDateTime.now().minusDays(4), "Nhiều quẩy, chan nước", p3_2, 1, p3_1, 1);

        createOrder(buyer4, store3, 45000.0, 0.0, "READY_FOR_PICKUP", "PAID", "CASH",
            LocalDateTime.now().plusHours(1), null, p3_1, 1);

        createOrder(buyer1, store3, 65000.0, 0.0, "PENDING", "UNPAID", "ZALOPAY",
            LocalDateTime.now().plusHours(5), null, p3_3, 1);

        createOrder(buyer2, store3, 110000.0, 20000.0, "CONFIRMED", "PAID", "BANK_TRANSFER",
            LocalDateTime.now().plusHours(7), null, p3_2, 1, p3_3, 1);

        // PRINT SUMMARY
        printSummary();
    }

    private Role createRole(String name) {
        return roleRepository.findByName(name)
            .orElseGet(() -> {
                Role role = new Role();
                role.setName(name);
                return roleRepository.save(role);
            });
    }

    private User createUser(String username, String email, String fullName, String phone, Role role) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> {
                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode("123456")); // Default password
                user.setFullName(fullName);
                user.setPhone(phone);
                user.setRole(role);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                return userRepository.save(user);
            });
    }

    private Store createStore(String name, User owner, String type, String city, String district, String logo) {
        return storeRepository.findByStoreName(name)
            .orElseGet(() -> {
                Store store = new Store();
                store.setStoreName(name);
                store.setOwner(owner);
                store.setBusinessType(type);
                store.setCity(city);
                store.setDistrict(district);
                store.setLogoUrl(logo);
                store.setStatus("ACTIVE");
                store.setCreatedAt(LocalDateTime.now());
                store.setUpdatedAt(LocalDateTime.now());
                return storeRepository.save(store);
            });
    }

    private Product createProduct(Store store, String name, String desc, Double price) {
        Product product = new Product();
        product.setStore(store);
        product.setProductName(name);
        product.setDescription(desc);
        product.setPrice(price);
        product.setOriginalPrice(price * 1.2);
        product.setStock(100);
        product.setStatus("ACTIVE");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    private void createOrder(User buyer, Store store, Double totalAmount, Double discount,
                              String status, String paymentStatus, String paymentMethod,
                              LocalDateTime pickupTime, String note,
                              Object... productsAndQuantities) {
        Order order = new Order();
        order.setUser(buyer);
        order.setStore(store);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discount != null ? discount : 0.0);
        order.setFinalAmount(totalAmount - (discount != null ? discount : 0.0));
        order.setStatus(status);
        order.setPaymentStatus(paymentStatus);
        order.setPaymentMethod(paymentMethod);
        order.setScheduledPickupTime(pickupTime);
        order.setNote(note);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setPickupQrCode("QR" + System.currentTimeMillis());

        Order savedOrder = orderRepository.save(order);

        // Add order items
        for (int i = 0; i < productsAndQuantities.length; i += 2) {
            Product product = (Product) productsAndQuantities[i];
            Integer quantity = (Integer) productsAndQuantities[i + 1];

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            orderItemRepository.save(item);
        }
    }

    private void printSummary() {
        System.out.println("\n=== TEST DATA SUMMARY ===");
        System.out.println("Users: " + userRepository.count());
        System.out.println("Stores: " + storeRepository.count());
        System.out.println("Products: " + productRepository.count());
        System.out.println("Orders: " + orderRepository.count());
        System.out.println("Order Items: " + orderItemRepository.count());

        System.out.println("\n=== ORDERS BY STORE ===");
        storeRepository.findAll().forEach(store -> {
            long orderCount = orderRepository.countByStoreStoreId(store.getStoreId());
            Double revenue = orderRepository.sumCompletedRevenueByStore(store.getStoreId());
            long pending = orderRepository.countPendingOrdersByStore(store.getStoreId());
            System.out.println(store.getStoreName() + ": " + orderCount + " orders, Revenue: " +
                (revenue != null ? revenue/1000000 : 0) + "M VND, Pending: " + pending);
        });
    }

    /**
     * Method để XÓA toàn bộ test data
     * Cẩn thận khi sử dụng!
     */
    @Transactional
    public void clearTestData() {
        System.out.println("=== CLEARING TEST DATA ===");

        // Clear by specific usernames/names
        userRepository.findByEmail("owner1@test.com").ifPresent(user -> {
            orderItemRepository.deleteAll(user.getOrders().stream()
                .flatMap(o -> o.getOrderItems().stream()).toList());
            orderRepository.deleteAll(user.getOrders());
            productRepository.deleteAll(user.getStores().stream()
                .flatMap(s -> s.getProducts().stream()).toList());
            storeRepository.deleteAll(user.getStores());
            userRepository.delete(user);
        });

        System.out.println("Test data cleared!");
    }
}
