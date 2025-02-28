package com.myseotoolbox.gcpcommons;

public class SubscriptionNonExistentException extends Exception {
    public SubscriptionNonExistentException(String subscriptionName) {
        super("Subscription does not exist '" + subscriptionName + "'");
    }
}
