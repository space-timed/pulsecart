package com.pulsecart.inventory.repository;

import com.pulsecart.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

    Optional<InventoryItem> findByProductId(String productId);
    Optional<InventoryItem> findBySku(String sku);

    /**
     * Atomic Conditional Update ensuring zero overselling under high concurrency.
     * Decrements available_quantity and increments reserved_quantity only if available_quantity >= qty.
     * Returns 1 if successful, 0 if insufficient stock.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryItem i " +
           "SET i.availableQuantity = i.availableQuantity - :qty, " +
           "    i.reservedQuantity = i.reservedQuantity + :qty, " +
           "    i.version = i.version + 1, " +
           "    i.updatedAt = CURRENT_INSTANT " +
           "WHERE i.productId = :productId AND i.availableQuantity >= :qty")
    int reserveStockAtomic(@Param("productId") String productId, @Param("qty") int qty);

    /**
     * Compensating release of reserved stock back to available inventory.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryItem i " +
           "SET i.availableQuantity = i.availableQuantity + :qty, " +
           "    i.reservedQuantity = i.reservedQuantity - :qty, " +
           "    i.version = i.version + 1, " +
           "    i.updatedAt = CURRENT_INSTANT " +
           "WHERE i.productId = :productId AND i.reservedQuantity >= :qty")
    int releaseStockAtomic(@Param("productId") String productId, @Param("qty") int qty);

    /**
     * Final commitment: permanently deducts fulfilled goods from reserved inventory.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryItem i " +
           "SET i.reservedQuantity = i.reservedQuantity - :qty, " +
           "    i.totalQuantity = i.totalQuantity - :qty, " +
           "    i.version = i.version + 1, " +
           "    i.updatedAt = CURRENT_INSTANT " +
           "WHERE i.productId = :productId AND i.reservedQuantity >= :qty")
    int commitStockAtomic(@Param("productId") String productId, @Param("qty") int qty);
}
