package de.extio.game_engine.event;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

public class EventExecutor implements DisposableBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventExecutor.class);
	
	private final ExecutorService executor = Executors.newFixedThreadPool(4); //Executors.newVirtualThreadPerTaskExecutor();
	
	private final BlockingQueue<QueuedEvent> eventQueue = new LinkedBlockingQueue<>();
	
	private final Thread processorThread;
	
	private final EventHandlerRegistry registry;
	
	public EventExecutor(final EventHandlerRegistry registry) {
		this.registry = registry;
		this.processorThread = Thread.ofPlatform()
				.daemon(true)
				.name("Events-Processor")
				.start(() -> {
					final var tasks = new ArrayList<Callable<Void>>();
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

	@Override
	public void destroy() throws Exception {
		this.processorThread.interrupt();
		this.executor.shutdownNow();
	}
	
	public void submit(final Event event) {
		this.eventQueue.offer(new QueuedEvent(event.getClass(), event));
	}
	
	private void processEvent(final List<Callable<Void>> tasks, final Class<? extends Event> eventClass, final Event event) throws InterruptedException {
		tasks.clear();
		final var consumers = this.registry.getHandlers(eventClass);
		if (consumers != null) {
			switch (consumers.size()) {
				case 0:
					return;
				
				case 1:
					try {
						@SuppressWarnings("unchecked") final Consumer<Event> consumer = (Consumer<Event>) consumers.getFirst().consumer();
						consumer.accept(event);
					}
					catch (final NoSuchElementException e) {
						return;
					}
					catch (final Exception e) {
						LOGGER.error("Event consumer failed", e);
					}
					return;
				
				default:
					for (final var consumer : consumers) {
						tasks.add(() -> {
							@SuppressWarnings("unchecked") final Consumer<Event> eventConsumer = (Consumer<Event>) consumer.consumer();
							eventConsumer.accept(event);
							return null;
						});
					}
					try {
						for (final var future : executor.invokeAll(tasks)) {
							try {
								future.get();
							}
							catch (final Exception e) {
								LOGGER.error("Event consumer failed", e.getCause() == null ? e : e.getCause());
							}
						}
					}
					catch (final InterruptedException e) {
						throw e;
					}
			}
		}
	}
	
	private record QueuedEvent(Class<? extends Event> eventClass, Event event) {
	}
}
