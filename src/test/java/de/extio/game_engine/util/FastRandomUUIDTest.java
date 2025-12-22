package de.extio.game_engine.util;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class FastRandomUUIDTest {
	
	@Test
	void testPerformance() {
		final int iterations = 10_000_000;
		
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			FastRandomUUID.create();
		}
		final long fastRandomUuidDuration = System.nanoTime() - start;
		
		start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			UUID.randomUUID();
		}
		final long systemRandomUuidDuration = System.nanoTime() - start;
		
		System.out.println("=== UUID Generation Performance ===");
		System.out.printf("FastRandomUUID.create():   %,d ns for %,d UUIDs (%.2f ns/UUID)%n", 
			fastRandomUuidDuration, iterations, (double) fastRandomUuidDuration / iterations);
		System.out.printf("UUID.randomUUID():         %,d ns for %,d UUIDs (%.2f ns/UUID)%n", 
			systemRandomUuidDuration, iterations, (double) systemRandomUuidDuration / iterations);
		System.out.printf("Speedup: %.2fx faster%n", 
			(double) systemRandomUuidDuration / fastRandomUuidDuration);
	}

}
