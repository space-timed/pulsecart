package com.pulsecart.search.controller;

import com.pulsecart.search.dto.SearchDtos.*;
import com.pulsecart.search.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false, defaultValue = "relevance") String sort
    ) {
        SearchResponse response = searchService.search(q, category, minPrice, maxPrice, inStock, sort);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/index")
    public ResponseEntity<SearchResultDto> indexProduct(@RequestBody IndexProductRequest request) {
        SearchResultDto dto = searchService.indexProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/rebuild")
    public ResponseEntity<Map<String, Object>> rebuildIndex() {
        int indexedCount = searchService.rebuildIndex();
        return ResponseEntity.ok(Map.of(
                "status", "COMPLETED",
                "message", "Search read projection rebuilt successfully",
                "indexedCount", indexedCount
        ));
    }
}
