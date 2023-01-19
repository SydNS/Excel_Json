package com.example.xcel_loader.repository;

import com.example.xcel_loader.model.NameSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NameSearchRepository extends JpaRepository<NameSearch, UUID>, JpaSpecificationExecutor<NameSearch> {
    boolean existsByName(String name);
    Optional<NameSearch> findByNoOrderByCreatedAtDesc(String no);

    Optional<NameSearch> findByNoOrIdOrderByCreatedAtDesc(String no, UUID id);

    List<NameSearch> findByName(String exactMatch);
}
