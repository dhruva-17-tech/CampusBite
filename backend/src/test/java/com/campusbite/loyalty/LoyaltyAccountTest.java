package com.campusbite.loyalty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoyaltyAccountTest {
    @Test
    void redeemingFiveStampCouponResetsBalanceAndAllowsAnotherRewardCycle() {
        LoyaltyAccount account = new LoyaltyAccount("customer@example.com");
        addStamps(account, 1, 5);

        assertEquals(5, account.getStamps());
        assertEquals(5, account.getAvailableCouponPercent());
        assertEquals(5, account.redeemCoupon());
        assertEquals(0, account.getStamps());
        assertEquals(0, account.getAvailableCouponPercent());

        addStamps(account, 6, 10);

        assertEquals(5, account.getStamps());
        assertEquals(5, account.getAvailableCouponPercent());
    }

    @Test
    void unusedCouponDoesNotResetStampsAndHigherMilestoneIsReached() {
        LoyaltyAccount account = new LoyaltyAccount("customer@example.com");
        addStamps(account, 1, 10);

        assertEquals(10, account.getStamps());
        assertEquals(8, account.getAvailableCouponPercent());
    }

    private void addStamps(LoyaltyAccount account, int first, int last) {
        for (int order = first; order <= last; order++) {
            account.addStampForOrder("order-" + order);
        }
    }
}
