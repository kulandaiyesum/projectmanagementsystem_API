package com.mk.service;

import com.mk.model.PlanType;
import com.mk.model.Subscription;
import com.mk.model.User;

public interface SubscriptionService {
    Subscription createSubscription(User user);
    Subscription getUsersSubscription(Long userId) throws Exception;
    Subscription upgradeSubscription(Long userId, PlanType planType) throws Exception;
    boolean isValid(Subscription subscription);
}
