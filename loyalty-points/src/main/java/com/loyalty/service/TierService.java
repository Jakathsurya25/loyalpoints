package com.loyalty.service;

import com.loyalty.dto.TierRequest;
import com.loyalty.dto.TierResponse;
import com.loyalty.entity.Tier;
import com.loyalty.exception.BusinessRuleException;
import com.loyalty.exception.DuplicateResourceException;
import com.loyalty.exception.ResourceNotFoundException;
import com.loyalty.repository.CustomerRepository;
import com.loyalty.repository.TierRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TierService {
    private final TierRepository tierRepo;
    private final CustomerRepository customerRepo;

    public TierService(TierRepository tierRepo, CustomerRepository customerRepo) {
        this.tierRepo = tierRepo;
        this.customerRepo = customerRepo;
    }

    @Transactional(readOnly = true)
    public List<TierResponse> list() {
        return tierRepo.findAllByOrderByMinPointsAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TierResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public TierResponse create(TierRequest r) {
        tierRepo.findByName(r.name().trim().toUpperCase()).ifPresent(t -> {
            throw new DuplicateResourceException("Tier name already exists: " + t.getName());
        });
        tierRepo.findByMinPoints(r.minPoints()).ifPresent(t -> {
            throw new DuplicateResourceException("Another tier already starts at " + r.minPoints() + " points");
        });
        Tier t = new Tier();
        apply(t, r);
        return toResponse(tierRepo.save(t));
    }

    @Transactional
    public TierResponse update(Long id, TierRequest r) {
        Tier t = find(id);
        tierRepo.findByName(r.name().trim().toUpperCase()).filter(o -> !o.getId().equals(id)).ifPresent(o -> {
            throw new DuplicateResourceException("Tier name already exists: " + o.getName());
        });
        tierRepo.findByMinPoints(r.minPoints()).filter(o -> !o.getId().equals(id)).ifPresent(o -> {
            throw new DuplicateResourceException("Another tier already starts at " + r.minPoints() + " points");
        });
        apply(t, r);
        return toResponse(tierRepo.save(t));
    }

    @Transactional
    public void delete(Long id) {
        Tier t = find(id);
        if (customerRepo.existsByTierId(id)) {
            throw new BusinessRuleException("Cannot delete tier '" + t.getName() + "': customers are on this tier");
        }
        tierRepo.delete(t);
    }

    private Tier find(Long id) {
        return tierRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + id));
    }

    private void apply(Tier t, TierRequest r) {
        t.setName(r.name().trim().toUpperCase());
        t.setMinPoints(r.minPoints());
        t.setDiscountPercent(r.discountPercent());
    }

    private TierResponse toResponse(Tier t) {
        return new TierResponse(t.getId(), t.getName(), t.getMinPoints(), t.getDiscountPercent());
    }
}
