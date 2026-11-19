package com.campusbite.loyalty;

import org.springframework.stereotype.Service;

@Service
public class LoyaltyService {
    private final LoyaltyAccountRepository repository;
    public LoyaltyService(LoyaltyAccountRepository repository){this.repository=repository;}

    public int currentDiscountRate(String email) {
        return repository.findByCustomerEmailIgnoreCase(email)
                .map(LoyaltyAccount::getAvailableCouponPercent).orElse(0);
    }
    public void redeemCoupon(String email) {
        repository.findByCustomerEmailIgnoreCase(email).ifPresent(account -> {
            account.redeemCoupon();
            repository.save(account);
        });
    }
    public LoyaltySummary summary(String email) {
        LoyaltyAccount a=repository.findByCustomerEmailIgnoreCase(email).orElse(null);
        int stamps=a==null?0:a.getStamps();
        int coupon=a==null?0:a.getAvailableCouponPercent();
        Integer nextAt=stamps<5?5:stamps<10?10:stamps<15?15:null;
        Integer nextDiscount=stamps<5?5:stamps<10?8:stamps<15?12:null;
        int progress=stamps%5;
        if(stamps>0 && progress==0) progress=5;
        int remaining=nextAt==null?0:nextAt-stamps;
        String message=coupon>0
          ? coupon+"% coupon ready to use on your next order!"
          : nextAt==null ? "You have unlocked all Sunflower Club rewards!"
          : "Complete "+remaining+" more delivered order(s) over ₹100 to unlock "+nextDiscount+"% off.";
        return new LoyaltySummary(stamps,progress,remaining,coupon,nextAt,nextDiscount,message);
    }
    public void awardStamp(String email,String orderId) {
        LoyaltyAccount a=repository.findByCustomerEmailIgnoreCase(email)
                .orElseGet(()->new LoyaltyAccount(email.toLowerCase()));
        a.addStampForOrder(orderId);
        repository.save(a);
    }
}
