package de.extio.game_engine.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = EventsAutoConfiguration.class)
class EventServiceTest {

	@Autowired
	private EventService eventService;

	private ConcurrentLinkedQueue<Event> receivedEvents;
	private AtomicInteger invocationCount;

	@BeforeEach
	void setUp() {
		this.receivedEvents = new ConcurrentLinkedQueue<>();
		this.invocationCount = new AtomicInteger(0);
	}

	@Test
	void testZeroConsumers() {
		final var event = new TestEvent("test");
		this.eventService.fire(event);

		await().pollDelay(Duration.ofMillis(50))
				.atMost(Duration.ofMillis(200))
				.untilAsserted(() -> assertThat(this.receivedEvents).isEmpty());
	}

	@Test
	void testSingleConsumer() throws InterruptedException {
		final var latch = new CountDownLatch(1);
		this.eventService.register(TestEvent.class, "consumer1", event -> {
			this.receivedEvents.add(event);
			this.invocationCount.incrementAndGet();
			latch.countDown();
		});

		final var event = new TestEvent("single");
		this.eventService.fire(event);

		assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(this.receivedEvents).hasSize(1);
		assertThat(this.receivedEvents.peek()).isEqualTo(event);
		assertThat(this.invocationCount.get()).isEqualTo(1);
	}

	@Test
	void testMultipleConsumers() throws InterruptedException {
		final var latch = new CountDownLatch(3);

		this.eventService.register(TestEvent.class, "consumer1", event -> {
			this.receivedEvents.add(event);
			this.invocationCount.incrementAndGet();
			latch.countDown();
		});

		this.eventService.register(TestEvent.class, "consumer2", event -> {
			this.receivedEvents.add(event);
			this.invocationCount.incrementAndGet();
			latch.countDown();
		});

		this.eventService.register(TestEvent.class, "consumer3", event -> {
			this.receivedEvents.add(event);
			this.invocationCount.incrementAndGet();
			latch.countDown();
		});

		final var event = new TestEvent("multiple");
		this.eventService.fire(event);

		assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(this.receivedEvents).hasSize(3);
		assertThat(this.invocationCount.get()).isEqualTo(3);
		assertThat(this.receivedEvents).allMatch(e -> e.equals(event));
	}

	@Test
	void testConsumerUnregistration() throws InterruptedException {
		final var latch = new CountDownLatch(1);

		this.eventService.register(TestEvent.class, "consumer1", event -> {
			this.receivedEvents.add(event);
			latch.countDown();
		});

		this.eventService.unregister(TestEvent.class, "consumer1");

		final var event = new TestEvent("unregistered");
		this.eventService.fire(event);

		await().pollDelay(Duration.ofMillis(50))
				.atMost(Duration.ofMillis(200))
				.untilAsserted(() -> assertThat(this.receivedEvents).isEmpty());
	}

	@Test
	void testUnregisterAll() throws InterruptedException {
		this.eventService.register(TestEvent.class, "consumer1", event -> this.receivedEvents.add(event));
		this.eventService.register(OtherTestEvent.class, "consumer1", event -> this.receivedEvents.add(event));

		this.eventService.unregisterAll("consumer1");

		this.eventService.fire(new TestEvent("test"));
		this.eventService.fire(new OtherTestEvent("other"));

		await().pollDelay(Duration.ofMillis(50))
				.atMost(Duration.ofMillis(200))
				.untilAsserted(() -> assertThat(this.receivedEvents).isEmpty());
	}

	@Test
	void testConsumerReregistration() throws InterruptedException {
		final var firstLatch = new CountDownLatch(1);
		final var secondLatch = new CountDownLatch(1);

		this.eventService.register(TestEvent.class, "consumer1", event -> {
			this.receivedEvents.add(event);
			firstLatch.countDown();
		});

		this.eventService.register(TestEvent.class, "consumer1", event -> {
			this.receivedEvents.add(event);
			this.invocationCount.incrementAndGet();
			secondLatch.countDown();
		});

		final var event = new TestEvent("reregistered");
		this.eventService.fire(event);

		assertThat(secondLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(this.receivedEvents).hasSize(1);
		assertThat(this.invocationCount.get()).isEqualTo(1);
		assertThat(firstLatch.getCount()).isEqualTo(1);
	}

	@Test
	void testSingleConsumerWithException() throws InterruptedException {
		final var latch = new CountDownLatch(1);

		this.eventService.register(TestEvent.class, "failingConsumer", event -> {
			latch.countDown();
			throw new RuntimeException("Consumer failed");
		});

		final var event = new TestEvent("exception");
		this.eventService.fire(event);

		assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void testMultipleConsumersWithPartialFailure() throws InterruptedException {
		final var latch = new CountDownLatch(2);

		this.eventService.register(TestEvent.class, "successConsumer1", event -> {
			this.receivedEvents.add(event);
			latch.countDown();
		});

		this.eventService.register(TestEvent.class, "failingConsumer", event -> {
			throw new RuntimeException("Consumer failed");
		});

		this.eventService.register(TestEvent.class, "successConsumer2", event -> {
			this.receivedEvents.add(event);
			latch.countDown();
		});

		final var event = new TestEvent("partial-failure");
		this.eventService.fire(event);

		assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(this.receivedEvents).hasSize(2);
	}

	@Test
	void testMultipleEvents() throws InterruptedException {
		final var latch = new CountDownLatch(3);

		this.eventService.register(TestEvent.class, "consumer1", event -> {
			this.receivedEvents.add(event);
			latch.countDown();
		});

		final var event1 = new TestEvent("event1");
		final var event2 = new TestEvent("event2");
		final var event3 = new TestEvent("event3");

		this.eventService.fire(event1);
		this.eventService.fire(event2);
		this.eventService.fire(event3);

		assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(this.receivedEvents).containsExactly(event1, event2, event3);
	}

	@Test
	void testDifferentEventTypes() throws InterruptedException {
		final var testEventLatch = new CountDownLatch(1);
		final var otherEventLatch = new CountDownLatch(1);

		this.eventService.register(TestEvent.class, "testConsumer", event -> {
			this.receivedEvents.add(event);
			testEventLatch.countDown();
		});

		this.eventService.register(OtherTestEvent.class, "otherConsumer", event -> {
			this.receivedEvents.add(event);
			otherEventLatch.countDown();
		});

		final var testEvent = new TestEvent("test");
		final var otherEvent = new OtherTestEvent("other");

		this.eventService.fire(testEvent);
		this.eventService.fire(otherEvent);

		assertThat(testEventLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(otherEventLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(this.receivedEvents).containsExactlyInAnyOrder(testEvent, otherEvent);
	}

	@Test
	void testConcurrentEventSubmission() throws InterruptedException {
		final var latch = new CountDownLatch(100);

		this.eventService.register(TestEvent.class, "consumer1", event -> {
			this.invocationCount.incrementAndGet();
			latch.countDown();
		});

		for (int i = 0; i < 100; i++) {
			final var event = new TestEvent("event-" + i);
			this.eventService.fire(event);
		}

		assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(this.invocationCount.get()).isEqualTo(100);
	}

	@Test
	void testSlowConsumer() throws InterruptedException {
		final var latch = new CountDownLatch(1);

		this.eventService.register(TestEvent.class, "slowConsumer", event -> {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			this.receivedEvents.add(event);
			latch.countDown();
		});

		final var event = new TestEvent("slow");
		this.eventService.fire(event);

		assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(this.receivedEvents).hasSize(1);
	}

	@Test
	void testMultipleSlowConsumersExecuteInParallel() throws InterruptedException {
		final var startLatch = new CountDownLatch(3);
		final var finishLatch = new CountDownLatch(3);
		final long sleepDuration = 200;

		for (int i = 1; i <= 3; i++) {
			this.eventService.register(TestEvent.class, "slowConsumer" + i, event -> {
				startLatch.countDown();
				try {
					Thread.sleep(sleepDuration);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				finishLatch.countDown();
			});
		}

		final long startTime = System.currentTimeMillis();
		this.eventService.fire(new TestEvent("parallel-test"));

		assertThat(startLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(finishLatch.await(1, TimeUnit.SECONDS)).isTrue();

		final long duration = System.currentTimeMillis() - startTime;
		assertThat(duration).isLessThan(sleepDuration * 2);
	}

	private record TestEvent(String data) implements Event {
	}

	private record OtherTestEvent(String data) implements Event {
	}
}
