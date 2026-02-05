package com.example.flashsale.repository;

import com.example.flashsale.entity.InventorySyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventorySyncLogRepository extends JpaRepository<InventorySyncLog, Long> {
    boolean existsBySyncTypeAndReferenceId(String syncType, String referenceId);
}
