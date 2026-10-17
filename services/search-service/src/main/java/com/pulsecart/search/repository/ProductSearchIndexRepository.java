package com.pulsecart.search.repository;

import com.pulsecart.search.domain.ProductSearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchIndexRepository extends JpaRepository<ProductSearchIndex, String>, JpaSpecificationExecutor<ProductSearchIndex> {
    List<ProductSearchIndex> findByCategoryNameIgnoreCase(String categoryName);
}
