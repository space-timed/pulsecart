package com.pulsecart.returns.controller;

import com.pulsecart.returns.dto.ReturnsDtos.*;
import com.pulsecart.returns.service.ReturnsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnsController {

    private final ReturnsService returnsService;

    public ReturnsController(ReturnsService returnsService) {
        this.returnsService = returnsService;
    }

    @PostMapping
    public ResponseEntity<ReturnResponse> createReturnRequest(@Valid @RequestBody CreateReturnRequest request) {
        ReturnResponse response = returnsService.createReturnRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{rmaId}")
    public ResponseEntity<ReturnResponse> getReturnById(@PathVariable String rmaId) {
        return ResponseEntity.ok(returnsService.getReturnById(rmaId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ReturnResponse>> getReturnsByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(returnsService.getReturnsByOrderId(orderId));
    }

    @PostMapping("/{rmaId}/receive")
    public ResponseEntity<ReturnResponse> receiveReturn(
            @PathVariable String rmaId,
            @RequestBody(required = false) ProcessReturnReceiptRequest request
    ) {
        ProcessReturnReceiptRequest effectiveRequest = request != null ? request : new ProcessReturnReceiptRequest("RESTOCKABLE", "Normal return inspection");
        ReturnResponse response = returnsService.receiveReturn(rmaId, effectiveRequest);
        return ResponseEntity.ok(response);
    }
}
