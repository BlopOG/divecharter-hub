package com.divecharter.hub.repositories;

import com.divecharter.hub.models.DiveSite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiveSiteRepository extends JpaRepository<DiveSite, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}