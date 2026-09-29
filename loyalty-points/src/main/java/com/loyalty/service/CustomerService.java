package com.loyalty.service;

import com.loyalty.dto.CustomerRequest;
import com.loyalty.dto.CustomerResponse;
import com.loyalty.dto.LedgerResponse;
import com.loyalty.dto.PageResponse;
import com.loyalty.entity.Customer;
import com.loyalty.entity.PointsLedger;
import com.loyalty.entity.Tier;
import com.loyalty.exception.BusinessRuleException;
import com.loyalty.exception.DuplicateResourceException;
import com.loyalty.exception.ResourceNotFoundException;
import com.loyalty.repository.CustomerRepository;
import com.loyalty.repository.PointsLedgerRepository;
import com.loyalty.repository.TierRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepo;
    private final TierRepository tierRepo;
    private final PointsLedgerRepository ledgerRepo;

    public CustomerService(CustomerRepository customerRepo, TierRepository tierRepo, PointsLedgerRepository ledgerRepo) {
        this.customerRepo = customerRepo;
        this.tierRepo = tierRepo;
        this.ledgerRepo = ledgerRepo;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest r) {
        String email = r.email().trim().toLowerCase();
        if (customerRepo.existsByEmail(email)) {
            throw new DuplicateResourceException("Customer with email already exists: " + email);
        }
        Tier entryTier = tierRepo.findFirstByOrderByMinPointsAsc()
                .orElseThrow(() -> new BusinessRuleException("No tiers configured. Create a tier first."));
        Customer c = new Customer();
        c.setName(r.name().trim());
        c.setEmail(email);
        c.setPhone(r.phone());
        c.setTier(entryTier);
        return toResponse(customerRepo.save(c));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list() {
        return customerRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest r) {
        Customer c = find(id);
        String email = r.email().trim().toLowerCase();
        if (!email.equals(c.getEmail()) && customerRepo.existsByEmail(email)) {
            throw new DuplicateResourceException("Customer with email already exists: " + email);
        }
        c.setName(r.name().trim());
        c.setEmail(email);
        c.setPhone(r.phone());
        return toResponse(customerRepo.save(c));
    }
    @Transactional
    public void delete(Long id) {
        Customer c = find(id);
        customerRepo.delete(c);
    }
    /** Feature 5: points history (paged, newest first). */
    @Transactional(readOnly = true)
    public PageResponse<LedgerResponse> history(Long customerId, Pageable pageable) {
        find(customerId); // 404 if customer does not exist
        Page<PointsLedger> page = ledgerRepo.findByCustomerId(customerId, pageable);
        List<LedgerResponse> content = page.getContent().stream()
                .map(l -> new LedgerResponse(l.getId(), l.getEntryType(), l.getPoints(), l.getBalanceAfter(),
                        l.getPurchase() == null ? null : l.getPurchase().getId(),
                        l.getDescription(), l.getCreatedAt()))
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    private Customer find(Long id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private CustomerResponse toResponse(Customer c) {
        Optional<Tier> next = tierRepo.findFirstByMinPointsGreaterThanOrderByMinPointsAsc(c.getLifetimePoints());
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(),
                c.getTier().getName(), c.getTier().getDiscountPercent(),
                c.getLifetimePoints(), c.getAvailablePoints(),
                next.map(Tier::getName).orElse(null),
                next.map(t -> t.getMinPoints() - c.getLifetimePoints()).orElse(null));
    }
}
