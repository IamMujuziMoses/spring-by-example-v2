package com.springbyexample.v2.springcontextcaching;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContextManager;

/**
 * Verifies application context reuse and invalidation in the Spring TestContext Framework.
 *
 * @author Mujuzi Moses
 */
public class ContextCachingTest {

    @Test
    void reusesContextAcrossTestClasses() {
        ApplicationContext firstContext = loadContext(FirstContextTest.class);
        ApplicationContext secondContext = loadContext(SecondContextTest.class);

        assertSame(firstContext, secondContext);
    }

    @Test
    void createsSeparateContextForDifferentConfiguration() {
        ApplicationContext sharedContext = loadContext(FirstContextTest.class);
        ApplicationContext alternativeContext = loadContext(AlternativeContextTest.class);

        assertNotSame(sharedContext, alternativeContext);
    }

    @Test
    void reloadsContextAfterDirtiesContextInvalidation() throws Exception {
        TestContextManager dirtyContextManager = new TestContextManager(DirtyContextTest.class);
        dirtyContextManager.beforeTestClass();

        ApplicationContext originalContext = dirtyContextManager.getTestContext().getApplicationContext();
        dirtyContextManager.afterTestClass();

        ApplicationContext reloadedContext = loadContext(DirtyContextProbe.class);

        assertNotSame(originalContext, reloadedContext);
    }

    private ApplicationContext loadContext(Class<?> testClass) {
        return new TestContextManager(testClass).getTestContext().getApplicationContext();
    }

    @org.springframework.test.context.ContextConfiguration(classes = DirtyContextTest.DirtyConfiguration.class)
    static class DirtyContextProbe {
    }
}
