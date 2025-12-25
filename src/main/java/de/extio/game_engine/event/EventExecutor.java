package de.extio.game_engine.event;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.FailedException;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.StructuredTaskScope.Subtask.State;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventExecutor.class);
	
	private final BlockingQueue<QueuedEvent> eventQueue = new LinkedBlockingQueue<>();
	
	private final Thread processorThread;
	
	private final EventHandlerRegistry registry;
	
	public EventExecutor(final EventHandlerRegistry registry) {
		this.registry = registry;
		this.processorThread = Thread.ofPlatform()
				.daemon(true)
				.name("Events-Processor")
				.start(() -> {
					final var tasks = new ArrayList<Subtask<Object>>();
					while (!Thread.currentThread().isInterrupted()) {
						try {
							final QueuedEvent queuedEvent = this.eventQueue.take();
							processEvent(tasks, queuedEvent.eventClass(), queuedEvent.event());
						}
						catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
					}
				});
	}
	
	public void submit(final Event event) {
		this.eventQueue.offer(new QueuedEvent(event.getClass(), event));
	}
	
	private void processEvent(final List<Subtask<Object>> tasks, final Class<? extends Event> eventClass, final Event event) throws InterruptedException {
		final var consumers = this.registry.getHandlers(eventClass);
		if (consumers != null) {
			switch (consumers.size()) {
				case 0:
					return;
				
				case 1:
					try {
						((Consumer<Event>) consumers.getFirst().consumer()).accept(event);
					}
					catch (final NoSuchElementException e) {
						return;
					}
					catch (final Exception e) {
						LOGGER.error("Event consumer failed", e);
					}
					return;
				
				default:
					try (var scope = StructuredTaskScope.open(Joiner.awaitAll())) {
						tasks.clear();
						for (final var consumer : consumers) {
							tasks.add(scope.fork(() -> {
								((Consumer<Event>) consumer.consumer()).accept(event);
							}));
						}
						
						scope.join();
						
						for (final var task : tasks) {
							if (task.state() == State.FAILED) {
								LOGGER.error("Event consumer failed", task.exception());
							}
						}
					}
					catch (final InterruptedException e) {
						throw e;
					}
					catch (final FailedException e) {
						LOGGER.error("One or more event consumers failed", e);
					}
					catch (final Exception e) {
						LOGGER.error("Unexpected error while processing event", e);
					}
			}
		}
	}
	
	private record QueuedEvent(Class<? extends Event> eventClass, Event event) {
	}
}
