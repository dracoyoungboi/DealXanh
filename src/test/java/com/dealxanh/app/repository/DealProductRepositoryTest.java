package com.dealxanh.app.repository;

import com.dealxanh.app.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration tests for DealProductRepository.findHomeActiveDealProducts().
 *
 * Uses the real test database (AutoConfigureTestDatabase.Replace.NONE) because
 * H2 is not in the project. Each test persists only what it needs.
 *
 * NOTE: Requires a running MySQL database with the 'test' Spring profile.
 * If the test database is unavailable, these tests will fail to load the context.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DealProductRepositoryTest {

    @Autowired
    private DealProductRepository dealProductRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findHomeActiveDealProducts: active deal with available product is returned")
    void activeDeal_AvailableProduct_IsReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Test Store");
        Deal activeDeal = persistDeal("ACTIVE", now.minusDays(1), now.plusDays(10), store, 1);
        Product product = persistProduct("Test Product", store);
        DealProduct dp = persistDealProduct(activeDeal, product, 100000.0, 80000.0, null, 0, 1);

        List<DealProduct> results = dealProductRepository.findHomeActiveDealProducts(now);

        assertThat(results).extracting(r -> r.getDealProductId()).contains(dp.getDealProductId());
    }

    @Test
    @DisplayName("findHomeActiveDealProducts: expired deal not returned")
    void expiredDeal_NotReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Expired Store");
        Deal expiredDeal = persistDeal("ACTIVE", now.minusDays(10), now.minusDays(1), store, 2);
        Product product = persistProduct("Expired Product", store);
        DealProduct dp = persistDealProduct(expiredDeal, product, 100000.0, 80000.0, null, 0, 1);

        List<DealProduct> results = dealProductRepository.findHomeActiveDealProducts(now);

        // The expired DealProduct should NOT appear in results
        assertThat(results).extracting(r -> r.getDealProductId()).doesNotContain(dp.getDealProductId());
    }

    @Test
    @DisplayName("findHomeActiveDealProducts: future deal not returned")
    void futureDeal_NotReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Future Store");
        Deal futureDeal = persistDeal("ACTIVE", now.plusDays(1), now.plusDays(10), store, 3);
        Product product = persistProduct("Future Product", store);
        DealProduct dp = persistDealProduct(futureDeal, product, 100000.0, 80000.0, null, 0, 1);

        List<DealProduct> results = dealProductRepository.findHomeActiveDealProducts(now);

        // The future DealProduct should NOT appear in results
        assertThat(results).extracting(r -> r.getDealProductId()).doesNotContain(dp.getDealProductId());
    }

    @Test
    @DisplayName("findHomeActiveDealProducts: inactive product not returned")
    void inactiveProduct_NotReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Inactive Store");
        Deal activeDeal = persistDeal("ACTIVE", now.minusDays(1), now.plusDays(10), store, 4);
        Product product = createProduct("Inactive Product", store);
        product.setActive(false);
        em.persist(product);
        DealProduct dp = persistDealProduct(activeDeal, product, 100000.0, 80000.0, null, 0, 1);

        List<DealProduct> results = dealProductRepository.findHomeActiveDealProducts(now);

        // The inactive product's DealProduct should NOT appear
        assertThat(results).extracting(r -> r.getDealProductId()).doesNotContain(dp.getDealProductId());
    }

    @Test
    @DisplayName("findHomeActiveDealProducts: out of stock product not returned")
    void outOfStockProduct_NotReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Stock Store");
        Deal activeDeal = persistDeal("ACTIVE", now.minusDays(1), now.plusDays(10), store, 5);
        Product product = createProduct("Stock Product", store);
        product.setStockQuantity(0);
        em.persist(product);
        DealProduct dp = persistDealProduct(activeDeal, product, 100000.0, 80000.0, null, 0, 1);

        List<DealProduct> results = dealProductRepository.findHomeActiveDealProducts(now);

        // The out-of-stock product's DealProduct should NOT appear
        assertThat(results).extracting(r -> r.getDealProductId()).doesNotContain(dp.getDealProductId());
    }

    @Test
    @DisplayName("findHomeActiveDealProducts: sold out (maxQuantity reached) not returned")
    void soldOutMaxQuantity_NotReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("MaxQty Store");
        Deal activeDeal = persistDeal("ACTIVE", now.minusDays(1), now.plusDays(10), store, 6);
        Product product = persistProduct("MaxQty Product", store);
        DealProduct dp = createDealProduct(activeDeal, product, 100000.0, 80000.0, 10, 10, 1);
        em.persist(dp);
        em.flush();

        List<DealProduct> results = dealProductRepository.findHomeActiveDealProducts(now);

        assertThat(results).extracting(r -> r.getDealProductId()).doesNotContain(dp.getDealProductId());
    }

    @Test
    @DisplayName("findHomeActiveDealProducts: null maxQuantity (unlimited) still returned")
    void unlimitedMaxQuantity_IsReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Unlimited Store");
        Deal activeDeal = persistDeal("ACTIVE", now.minusDays(1), now.plusDays(10), store, 7);
        Product product = persistProduct("Unlimited Product", store);
        DealProduct dp = persistDealProduct(activeDeal, product, 100000.0, 80000.0, null, 5, 1);

        List<DealProduct> results = dealProductRepository.findHomeActiveDealProducts(now);

        assertThat(results).extracting(r -> r.getDealProductId()).contains(dp.getDealProductId());
    }

    // ================================================================
    // Helpers
    // ================================================================

    private Store persistStore(String name) {
        Store store = new Store();
        store.setStoreName(name);
        store.setStatus("ACTIVE");
        em.persist(store);
        return store;
    }

    private Deal persistDeal(String status, LocalDateTime start, LocalDateTime end, Store store, int priority) {
        Deal deal = new Deal();
        deal.setDealName("Test Deal " + priority);
        deal.setDealCode("TEST" + priority + System.nanoTime());
        deal.setDealType("FLASH_SALE");
        deal.setDiscountType("PERCENT");
        deal.setDiscountValue(20.0);
        deal.setStatus(status);
        deal.setStartTime(start);
        deal.setEndTime(end);
        deal.setStore(store);
        deal.setPriority(priority);
        em.persist(deal);
        return deal;
    }

    private Product createProduct(String name, Store store) {
        Product product = new Product();
        product.setName(name);
        product.setActive(true);
        product.setDeleted(false);
        product.setStockQuantity(100);
        product.setApprovalStatus("APPROVED");
        product.setOriginalPrice(150000.0);
        product.setStore(store);
        return product;
    }

    private Product persistProduct(String name, Store store) {
        Product product = createProduct(name, store);
        em.persist(product);
        return product;
    }

    private DealProduct createDealProduct(Deal deal, Product product, double origPrice,
                                           double salePrice, Integer maxQty, int soldQty, int priority) {
        DealProduct dp = new DealProduct();
        dp.setDeal(deal);
        dp.setProduct(product);
        dp.setOriginalPrice(origPrice);
        dp.setSalePrice(salePrice);
        dp.setMaxQuantity(maxQty);
        dp.setSoldQuantity(soldQty);
        dp.setPriority(priority);
        return dp;
    }

    private DealProduct persistDealProduct(Deal deal, Product product, double origPrice,
                                            double salePrice, Integer maxQty, int soldQty, int priority) {
        DealProduct dp = createDealProduct(deal, product, origPrice, salePrice, maxQty, soldQty, priority);
        em.persist(dp);
        em.flush();
        return dp;
    }
}
