import com.example.A;
import com.example.DI.ApplicationContext;
import com.example.MySingletonComponent;
import com.example.SpanishGreetingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ApplicationContextThreadSafetyTest {
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() throws Exception {
        applicationContext = new ApplicationContext("com.example");
    }

    @Test
    void testThreadSafety() throws Exception {
        final int threadCount = 10;
        // Latch to make sure all threads start at the same time
        var latch = new CountDownLatch(threadCount);
        // ExecutorService to manage threads
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // AtomicReference to store the singleton bean
        AtomicReference<A> singletonReference = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.countDown();  // Decrement latch, signaling this thread is ready
                    latch.await();       // Wait for all threads to be ready
                    // Get singleton bean from ApplicationContext
                    A singleton = applicationContext.getBean(A.class);

                    // Compare and set the singletonReference only if it is not set yet
                    singletonReference.compareAndSet(null, singleton);
                    // Assert that all retrieved singleton beans are the same instance
                    Assertions.assertSame(singletonReference.get(), singleton);
                } catch (Exception e) {
                    Assertions.fail("Exception occurred: " + e.getMessage());
                }
            });
        }
        // Shutdown the executor service
        executorService.shutdown();
    }

    @Test
    void testConcurrentAccessToSharedApplicationContext() throws InterruptedException {
        final int threadCount = 10;
        // Latch to ensure all threads start at the same time
        CountDownLatch latch = new CountDownLatch(threadCount);
        // ExecutorService to manage threads
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // Runnable task to be executed by each thread
        Runnable task = () -> {
            try {
                latch.countDown();  // Decrement latch, signaling this thread is ready
                latch.await();       // Wait for all threads to be ready
                // Get a singleton bean from ApplicationContext
                MySingletonComponent singleton = applicationContext.getBean(MySingletonComponent.class);

                // Perform some operation on the singleton bean
                singleton.incrementCounter();

                // Get a prototype bean from ApplicationContext
                SpanishGreetingService prototype = applicationContext.getBean(SpanishGreetingService.class);

                // Perform some operation on the prototype bean
                prototype.greet("parsa");
            } catch (Exception e) {
                Assertions.fail("Exception occurred: " + e.getMessage());
            }
        };

        // Submit all tasks to the executor service
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(task);
        }

        // Shutdown the executor service and wait for all tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        // Verify that the singleton bean's state is as expected after all threads have run
        MySingletonComponent singleton = applicationContext.getBean(MySingletonComponent.class);
        Assertions.assertEquals(threadCount, singleton.getCounter());
    }
}
