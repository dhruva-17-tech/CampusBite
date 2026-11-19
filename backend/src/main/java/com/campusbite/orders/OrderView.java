package com.campusbite.orders;

public record OrderView(CampusOrder order, long queuePosition, int estimatedWaitMinutes) {
}
