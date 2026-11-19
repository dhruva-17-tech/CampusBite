package com.campusbite.loyalty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "loyalty_accounts")
public class LoyaltyAccount {
    @Id private String id;
    @Indexed(unique = true) private String customerEmail;
    private int stamps;
    private int availableCouponPercent;
    private List<Integer> couponMilestonesIssued = new ArrayList<>();
    private List<String> rewardedOrderIds = new ArrayList<>();
    private Instant updatedAt;

    public LoyaltyAccount() {}
    public LoyaltyAccount(String customerEmail) {
        this.customerEmail = customerEmail.toLowerCase();
        this.updatedAt = Instant.now();
    }
    public String getId(){return id;}
    public String getCustomerEmail(){return customerEmail;}
    public int getStamps(){return stamps;}
    public int getAvailableCouponPercent(){return availableCouponPercent;}
    public List<String> getRewardedOrderIds(){return rewardedOrderIds;}
    public Instant getUpdatedAt(){return updatedAt;}

    public void addStampForOrder(String orderId) {
        if (rewardedOrderIds == null) rewardedOrderIds = new ArrayList<>();
        if (couponMilestonesIssued == null) couponMilestonesIssued = new ArrayList<>();
        if (!rewardedOrderIds.contains(orderId)) {
            rewardedOrderIds.add(orderId);
            stamps++;
            if (stamps == 5 || stamps == 10 || stamps == 15) {
                int rate = stamps == 5 ? 5 : stamps == 10 ? 8 : 12;
                if (!couponMilestonesIssued.contains(stamps)) {
                    availableCouponPercent = rate;
                    couponMilestonesIssued.add(stamps);
                }
            }
            updatedAt = Instant.now();
        }
    }
    public int redeemCoupon() {
        int rate = availableCouponPercent;
        if (rate == 0) return 0;
        availableCouponPercent = 0;
        stamps = 0;
        if (couponMilestonesIssued == null) couponMilestonesIssued = new ArrayList<>();
        couponMilestonesIssued.clear();
        updatedAt = Instant.now();
        return rate;
    }
}
