package com.loyalty.controller;

import com.loyalty.dto.TierRequest;
import com.loyalty.dto.TierResponse;
import com.loyalty.service.TierService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tiers")
public class TierController {
    private final TierService service;

    public TierController(TierService service) {
        this.service = service;
    }

    @GetMapping
    public List<TierResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    public TierResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<TierResponse> create(@Valid @RequestBody TierRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PutMapping("/{id}")
    public TierResponse update(@PathVariable Long id, @Valid @RequestBody TierRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
