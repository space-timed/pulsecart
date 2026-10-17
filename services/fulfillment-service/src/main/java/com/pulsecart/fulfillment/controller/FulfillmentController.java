package com.pulsecart.fulfillment.controller;

import com.pulsecart.fulfillment.dto.FulfillmentDtos.*;
import com.pulsecart.fulfillment.service.FulfillmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fulfillments")
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    public FulfillmentController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @PostMapping("/process")
    public ResponseEntity<FulfillmentResponse> processFulfillment(@Valid @RequestBody ProcessFulfillmentRequest request) {
        FulfillmentResponse response = fulfillmentService.processFulfillment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FulfillmentResponse> getFulfillment(@PathVariable String id) {
        return fulfillmentService.getFulfillment(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<FulfillmentResponse> getFulfillmentByOrderId(@PathVariable String orderId) {
        return fulfillmentService.getFulfillmentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
