package com.springbyexample.v2.flux;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class FluxApplicationTest {

    @Test
    void just_shouldEmitMultipleValues() {
        // Flux.just() creates a publisher that emits the provided values in the same order and then completes.
        Flux<String> names = Flux.just("John", "Jane", "Peter");

        StepVerifier.create(names).expectNext("John", "Jane", "Peter").verifyComplete();
    }

    @Test
    void range_shouldEmitSequenceOfIntegers() {
        // Flux.range() generates a sequence of consecutive integers.
        // The first argument is the starting value and the second is the number of values.
        Flux<Integer> numbers = Flux.range(1, 5);

        StepVerifier.create(numbers).expectNext(1, 2, 3, 4, 5).verifyComplete();
    }

    @Test
    void fromIterable_shouldCreateFluxFromCollection() {
        // Flux.fromIterable() adapts an existing Iterable into a reactive publisher. Each element in the collection
        // becomes an emitted value.
        List<String> names = List.of("John", "Jane", "Peter");

        Flux<String> result = Flux.fromIterable(names);

        StepVerifier.create(result).expectNext("John", "Jane", "Peter").verifyComplete();
    }

    @Test
    void map_shouldTransformEachValue() {
        // map() transforms each value emitted by the source Flux. It does not change the number of values in the sequence.
        Flux<String> names = Flux.just("John", "Jane", "Peter");

        Flux<String> uppercaseNames = names.map(String::toUpperCase);

        StepVerifier.create(uppercaseNames).expectNext("JOHN", "JANE", "PETER").verifyComplete();
    }

    @Test
    void filter_shouldKeepMatchingValues() {
        // filter() allows values to continue through the sequence only when the supplied condition evaluates to true.
        Flux<Integer> numbers = Flux.range(1, 6);

        Flux<Integer> evenNumbers = numbers.filter(number -> number % 2 == 0);

        StepVerifier.create(evenNumbers).expectNext(2, 4, 6).verifyComplete();
    }

    @Test
    void take_shouldLimitNumberOfValues() {
        // take() limits how many values are allowed through the Flux.
        //
        // Once the requested number of values has been emitted, the resulting Flux completes.
        Flux<Integer> numbers = Flux.range(1, 10);

        Flux<Integer> firstThree = numbers.take(3);

        StepVerifier.create(firstThree).expectNext(1, 2, 3).verifyComplete();
    }

    @Test
    void empty_shouldCompleteWithoutValues() {
        // Flux.empty() represents a publisher that emits no values and immediately completes successfully.
        Flux<String> result = Flux.empty();

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void collectList_shouldCollectValuesIntoList() {
        // collectList() waits for the Flux to complete and collectsall emitted values into a List.
        //
        // This converts the multi-value reactive sequence into a single List result, which is useful when a collection
        // is required.
        Flux<String> names = Flux.just("John", "Jane", "Peter");

        List<String> result = names.map(String::toUpperCase).collectList().block();

        assertThat(result).containsExactly("JOHN", "JANE", "PETER");
    }

    @Test
    void defer_shouldDelayExecutionUntilSubscription() {
        // Flux.defer() delays creation of the source Flux until a subscriber actually subscribes.
        boolean[] executed = { false };

        Flux<String> names = Flux.defer(() -> {
            executed[0] = true;

            return Flux.just("John", "Jane");
        });

        // Creating the Flux does not execute the supplier.
        assertThat(executed[0]).isFalse();

        // StepVerifier subscribes to the Flux, causing the supplier to execute.
        StepVerifier.create(names).expectNext("John", "Jane").verifyComplete();

        assertThat(executed[0]).isTrue();
    }
}
