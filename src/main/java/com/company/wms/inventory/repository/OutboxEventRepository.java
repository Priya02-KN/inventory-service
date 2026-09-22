package com.company.wms.inventory.repository;
import com.company.wms.inventory.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByPublishedFalse();
}

