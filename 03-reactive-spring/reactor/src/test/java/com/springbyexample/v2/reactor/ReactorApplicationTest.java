package com.springbyexample.v2.reactor;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class ReactorApplicationTest {

    @Test
    void just_shouldCreatePublisherWithValues() {
        Flux<String> names = Flux.just("John", "Jane", "Peter");

        StepVerifier.create(names).expectNext("John", "Jane", "Peter").verifyComplete();
    }

    @Test
    void map_shouldTransformValues() {
        Flux<String> names = Flux.just("John", "Jane", "Peter");
        Flux<String> uppercaseNames = names.map(String::toUpperCase);

        StepVerifier.create(uppercaseNames).expectNext("JOHN", "JANE", "PETER").verifyComplete();
    }

    @Test
    void filter_shouldKeepMatchingValues() {
        Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5);
        Flux<Integer> evenNumbers = numbers.filter(number -> number % 2 == 0);

        StepVerifier.create(evenNumbers).expectNext(2, 4).verifyComplete();
    }

    @Test
    void collectList_shouldConvertReactiveSequenceToList() {
        Flux<String> names = Flux.just("John", "Jane", "Peter");

        Mono<List<String>> result = names.map(String::toUpperCase).collectList();

        StepVerifier.create(result).assertNext(namesList -> assertThat(namesList)
                        .containsExactly("JOHN", "JANE", "PETER")).verifyComplete();
    }

    @Test
    void pipeline_shouldComposeMultipleOperators() {
        Flux<Integer> numbers = Flux.range(1, 10);
        Flux<Integer> result = numbers.filter(number -> number % 2 == 0).map(number -> number * 10);

        StepVerifier.create(result).expectNext(20, 40, 60, 80, 100).verifyComplete();
    }

    @Test
    void reactivePipeline_shouldNotExecuteUntilSubscribed() {
        boolean[] executed = {false};

        Flux<String> names = Flux.defer(() -> { executed[0] = true; return Flux.just("John", "Jane"); });

        assertThat(executed[0]).isFalse();

        StepVerifier.create(names).expectNext("John", "Jane").verifyComplete();

        assertThat(executed[0]).isTrue();
    }
}
