package com.springbyexample.v2.backpressure;

import org.jspecify.annotations.NonNull;
import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author Mujuzi Moses
 */
public class BackPressureApplication {

    public static void main(String[] args) {
        Flux.range(1, 10).log().subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    protected void hookOnSubscribe(@NonNull Subscription subscription) {
                        System.out.println("Requesting first 2 elements");
                        request(2);
                    }

                    @Override
                    protected void hookOnNext(@NonNull Integer value) {
                        System.out.println("Received: " + value);

                        if (value == 2) {
                            System.out.println("Requesting next 3 elements");
                            request(3);
                        } else if (value == 5) {
                            System.out.println("Requesting remaining elements");
                            request(5);
                        }
                    }
                });
    }
}
