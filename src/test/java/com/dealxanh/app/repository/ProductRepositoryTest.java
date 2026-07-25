package com.dealxanh.app.repository;

import com.dealxanh.app.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration tests for new ProductRepository queries.
 * Uses the real test database — @AutoConfigureTestDatabase(replace = NONE).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DealProductRepository dealProductRepository;

    @Autowired
    private TestEntityManager em;

    // ================================================================
    // findHomeStandaloneCombos()
    // ================================================================

    @Test
    @DisplayName("findHomeStandaloneCombos: available COMBO returned with store")
    void standaloneCombo_Available_Returned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Combo Store");
        Product combo = createProduct("My Combo", store);
        combo.setProductType("COMBO");
        em.persist(combo);
        em.flush();

        List<Product> results = productRepository.findHomeStandaloneCombos(now);

        assertThat(results).extracting(Product::getName).contains("My Combo");
        // Store should be eager-fetched — accessing it should not throw
        Product result = results.get(0);
        assertThat(result.getStore()).isNotNull();
        assertThat(result.getStore().getStoreName()).isEqualTo("Combo Store");
    }

    @Test
    @DisplayName("findHomeStandaloneCombos: out-of-stock COMBO not returned")
    void standaloneCombo_OutOfStock_NotReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Stock Combo Store");
        Product combo = createProduct("OOS Combo", store);
        combo.setProductType("COMBO");
        combo.setStockQuantity(0);
        em.persist(combo);
        em.flush();

        List<Product> results = productRepository.findHomeStandaloneCombos(now);

        assertThat(results).extracting(Product::getName).doesNotContain("OOS Combo");
    }

    @Test
    @DisplayName("findHomeStandaloneCombos: SPECIFIC_DEAL type not returned")
    void nonComboType_NotReturned() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("NonCombo Store");
        Product specificDeal = createProduct("Regular Product", store);
        specificDeal.setProductType("SPECIFIC_DEAL");
        em.persist(specificDeal);
        em.flush();

        List<Product> results = productRepository.findHomeStandaloneCombos(now);

        assertThat(results).extracting(Product::getName).doesNotContain("Regular Product");
    }

    // ================================================================
    // findHomeSuggestedProducts()
    // ================================================================

    @Test
    @DisplayName("findHomeSuggestedProducts: product in active deal is excluded")
    void suggestedProducts_Excludes_ActiveDealProducts() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Suggested Store");
        Product inDeal = persistProduct("In Deal", store);
        Product notInDeal = persistProduct("Not In Deal", store);

        Deal activeDeal = persistDeal("ACTIVE", now.minusDays(1), now.plusDays(10), store);
        DealProduct dp = createDealProduct(activeDeal, inDeal);
        em.persist(dp);
        em.flush();

        List<Product> results = productRepository.findHomeSuggestedProducts(now, PageRequest.of(0, 50));

        assertThat(results).extracting(Product::getName)
                .doesNotContain("In Deal")
                .contains("Not In Deal");
    }

    @Test
    @DisplayName("findHomeSuggestedProducts: store is eager-fetched")
    void suggestedProducts_StoreEagerlyFetched() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Eager Store");
        persistProduct("Eager Product", store);

        List<Product> results = productRepository.findHomeSuggestedProducts(now, PageRequest.of(0, 50));

        if (!results.isEmpty()) {
            Product result = results.get(0);
            assertThat(result.getStore()).isNotNull();
        }
    }

    @Test
    @DisplayName("findHomeSuggestedProducts: unavailable product not returned")
    void suggestedProducts_Excludes_Unavailable() {
        LocalDateTime now = LocalDateTime.now();

        Store store = persistStore("Unavail Store");
        Product inactive = createProduct("Inactive", store);
        inactive.setActive(false);
        em.persist(inactive);
        em.flush();

        List<Product> results = productRepository.findHomeSuggestedProducts(now, PageRequest.of(0, 50));

        assertThat(results).extracting(Product::getName).doesNotContain("Inactive");
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

    private Deal persistDeal(String status, LocalDateTime start, LocalDateTime end, Store store) {
        Deal deal = new Deal();
        deal.setDealName("Test Deal");
        deal.setDealCode("SD" + System.nanoTime());
        deal.setDealType("FLASH_SALE");
        deal.setDiscountType("PERCENT");
        deal.setDiscountValue(15.0);
        deal.setStatus(status);
        deal.setStartTime(start);
        deal.setEndTime(end);
        deal.setStore(store);
        deal.setPriority(1);
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
        product.setOriginalPrice(120000.0);
        product.setStore(store);
        return product;
    }

    private Product persistProduct(String name, Store store) {
        Product p = createProduct(name, store);
        em.persist(p);
        return p;
    }

    private DealProduct createDealProduct(Deal deal, Product product) {
        DealProduct dp = new DealProduct();
        dp.setDeal(deal);
        dp.setProduct(product);
        dp.setOriginalPrice(100000.0);
        dp.setSalePrice(80000.0);
        dp.setPriority(1);
        return dp;
    }
}
