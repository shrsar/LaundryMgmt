package com.laundrymgmt.modern.repository;

import com.laundrymgmt.modern.model.LaundryService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaundryServiceRepository extends JpaRepository<LaundryService, Long> {

    Optional<LaundryService> findByServiceCodeIgnoreCase(String serviceCode);

    boolean existsByServiceCodeIgnoreCase(String serviceCode);

    List<LaundryService> findAllByOrderByClothTypeAscServiceTypeAsc();
}
