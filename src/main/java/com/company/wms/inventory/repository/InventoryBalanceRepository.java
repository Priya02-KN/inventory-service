package com.company.wms.inventory.repository;
import com.company.wms.inventory.domain.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryBalanceRepository
        extends JpaRepository<InventoryBalance, Long> {

    List<InventoryBalance> findBySkuId(String skuId);

    Optional<InventoryBalance> findBySkuIdAndWarehouseIdAndBinId(
            String skuId,
            String warehouseId,
            String binId
    );
}