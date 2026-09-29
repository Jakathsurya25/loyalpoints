package com.loyalty.controller;

import com.loyalty.dto.PurchaseRequest;
import com.loyalty.dto.PurchaseResponse;
import com.loyalty.service.PurchaseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {
    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    /** Records a purchase: applies tier discount, optional redemption, awards points, upgrades tier. */
    @PostMapping
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody PurchaseRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPurchase(r));
    }

    @GetMapping("/customer/{customerId}")
    public List<PurchaseResponse> byCustomer(@PathVariable Long customerId) {
        return service.historyForCustomer(customerId);
    }
}
