package com.loyalty.service;

import com.loyalty.config.LoyaltyProperties;
import com.loyalty.dto.PurchaseRequest;
import com.loyalty.dto.PurchaseResponse;
import com.loyalty.entity.*;
import com.loyalty.exception.BusinessRuleException;
import com.loyalty.exception.InsufficientPointsException;
import com.loyalty.exception.ResourceNotFoundException;
import com.loyalty.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final CustomerRepository customerRepo;
    private final TierRepository tierRepo;
    private final PurchaseRepository purchaseRepo;
    private final PointsLedgerRepository ledgerRepo;
    private final LoyaltyProperties props;

    public PurchaseService(CustomerRepository customerRepo, TierRepository tierRepo, PurchaseRepository purchaseRepo, PointsLedgerRepository ledgerRepo, LoyaltyProperties props) {
        this.customerRepo = customerRepo;
        this.tierRepo = tierRepo;
        this.purchaseRepo = purchaseRepo;
        this.ledgerRepo = ledgerRepo;
        this.props = props;
    }

    /**
     * Features 1-4 in ONE atomic transaction:
     * validate -> tier discount -> redeem -> earn -> ledger -> tier upgrade.
     * Any exception rolls back everything.
     */
    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest req) {
        // Row lock on the customer: concurrent purchases are serialised.
        Customer c = customerRepo.findByIdForUpdate(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + req.customerId()));

        long redeem = req.pointsToRedeem() == null ? 0L : req.pointsToRedeem();

        // RULE 2 (checked BEFORE saving): cannot redeem more than the available balance
        if (redeem > c.getAvailablePoints()) {
            throw new InsufficientPointsException("Cannot redeem " + redeem + " points; available balance is "
                    + c.getAvailablePoints());
        }

        Tier tierBefore = c.getTier();
        BigDecimal amount = req.amount().setScale(2, RoundingMode.HALF_UP);

        // Feature 3: tier discount (based on the tier held BEFORE this purchase)
        BigDecimal tierDiscount = amount.multiply(tierBefore.getDiscountPercent())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal afterTier = amount.subtract(tierDiscount);

        // Feature 4: redemption value
        BigDecimal redemptionValue = props.rupeesPerPoint().multiply(BigDecimal.valueOf(redeem))
                .setScale(2, RoundingMode.HALF_UP);
        if (redemptionValue.compareTo(afterTier) > 0) {
            throw new BusinessRuleException("Redemption value (" + redemptionValue
                    + ") exceeds the payable amount (" + afterTier + ")");
        }
        BigDecimal finalAmount = afterTier.subtract(redemptionValue);

        // Feature 1: points earned on what the customer actually pays
        long earned = finalAmount.multiply(props.pointsPerRupee())
                .setScale(0, RoundingMode.DOWN).longValue();

        Purchase p = new Purchase();
        p.setCustomer(c);
        p.setAmount(amount);
        p.setTierDiscountPercent(tierBefore.getDiscountPercent());
        p.setTierDiscountAmount(tierDiscount);
        p.setPointsRedeemed(redeem);
        p.setRedemptionDiscount(redemptionValue);
        p.setFinalAmount(finalAmount);
        p.setPointsEarned(earned);
        purchaseRepo.save(p);

        // Ledger (append-only) + balances
        long balance = c.getAvailablePoints();
        if (redeem > 0) {
            balance -= redeem;
            addLedger(c, p, LedgerType.REDEEM, -redeem, balance, "Redeemed on purchase #" + p.getId());
        }
        if (earned > 0) {
            balance += earned;
            addLedger(c, p, LedgerType.EARN, earned, balance, "Earned on purchase #" + p.getId());
        }
        c.setAvailablePoints(balance);
        c.setLifetimePoints(c.getLifetimePoints() + earned); // redemption never reduces lifetime points

        // Feature 2 / RULE 1: automatic upgrade the moment a threshold is crossed; never downgrade
        tierRepo.findFirstByMinPointsLessThanEqualOrderByMinPointsDesc(c.getLifetimePoints())
                .filter(t -> t.getMinPoints() > c.getTier().getMinPoints())
                .ifPresent(c::setTier);
        customerRepo.save(c);

        return new PurchaseResponse(p.getId(), c.getId(), amount,
                tierBefore.getDiscountPercent(), tierDiscount,
                redeem, redemptionValue, finalAmount, earned,
                tierBefore.getName(), c.getTier().getName(), !tierBefore.getId().equals(c.getTier().getId()),
                c.getAvailablePoints(), c.getLifetimePoints(), p.getPurchaseDate());
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> historyForCustomer(Long customerId) {
        Customer c = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        return purchaseRepo.findByCustomerIdOrderByPurchaseDateDesc(customerId).stream()
                .map(p -> new PurchaseResponse(p.getId(), customerId, p.getAmount(),
                        p.getTierDiscountPercent(), p.getTierDiscountAmount(),
                        p.getPointsRedeemed(), p.getRedemptionDiscount(), p.getFinalAmount(), p.getPointsEarned(),
                        null, c.getTier().getName(), false,
                        c.getAvailablePoints(), c.getLifetimePoints(), p.getPurchaseDate()))
                .toList();
    }

    private void addLedger(Customer c, Purchase p, LedgerType type, long points, long balanceAfter, String desc) {
        PointsLedger l = new PointsLedger();
        l.setCustomer(c);
        l.setPurchase(p);
        l.setEntryType(type);
        l.setPoints(points);
        l.setBalanceAfter(balanceAfter);
        l.setDescription(desc);
        ledgerRepo.save(l);
    }
}
