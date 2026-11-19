package com.campusbite.loyalty;

public record LoyaltySummary(
                int stamps,
                int stampsTowardNextReward,
                int stampsUntilNextReward,
                int currentDiscountPercent,
                Integer nextRewardAt,
                Integer nextRewardDiscountPercent,
                String message) {
}
