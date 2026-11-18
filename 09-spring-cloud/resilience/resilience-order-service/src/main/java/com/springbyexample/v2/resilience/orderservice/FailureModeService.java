package com.springbyexample.v2.resilience.orderservice;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

/**
 * @author Mujuzi Moses
 */
@Service
public class FailureModeService {

    private final AtomicBoolean failureEnabled = new AtomicBoolean();

    private final AtomicInteger requestCount = new AtomicInteger();

    public boolean shouldFail() {
        requestCount.incrementAndGet();
        return failureEnabled.get();
    }

    public void setFailureEnabled(boolean enabled) {
        failureEnabled.set(enabled);
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public void reset() {
        failureEnabled.set(false);
        requestCount.set(0);
    }

}
