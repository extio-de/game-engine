/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.util.rng;

import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class XorShift128RandomTests {
	
	@Test
	public void testXorShift128Random() {
		final Random random = new XorShift128Random(42);
		
		System.out.println("nextDouble()");
		for (int i = 0; i < 10; i++) {
			System.out.println(random.nextDouble());
		}
		System.out.println("nextInt()");
		for (int i = 0; i < 10; i++) {
			System.out.println(random.nextInt());
		}
		System.out.println("nextInt(5)");
		for (int i = 0; i < 10; i++) {
			System.out.println(random.nextInt(5));
		}
		System.out.println("nextLong()");
		for (int i = 0; i < 10; i++) {
			System.out.println(random.nextLong());
		}
		System.out.println("nextBoolean()");
		for (int i = 0; i < 10; i++) {
			System.out.println(random.nextBoolean());
		}
		
		System.out.println();
		System.out.println("Performance longs:");
		long start = System.currentTimeMillis();
		long a = 5;
		for (int i = 0; i < 1000000000; i++) {
			a = a + random.nextLong();
		}
		System.out.println(a + " " + (System.currentTimeMillis() - start));
		
		System.out.println();
		System.out.println("Performance ints:");
		start = System.currentTimeMillis();
		int b = 5;
		for (int i = 0; i < 1000000000; i++) {
			b = b + random.nextInt();
		}
		System.out.println(b + " " + (System.currentTimeMillis() - start));
	}
	
	@Test
	public void testXorShift128RandomSeed0() {
		final Random random = new XorShift128Random(0);
		
		System.out.println("nextDouble()");
		for (int i = 0; i < 10; i++) {
			System.out.println(random.nextDouble());
		}
	}
	
	@Test
	public void testXorShift128RandomIntRange() {
		final Random random = new XorShift128Random(777);
		
		for (int i = 1; i < 10; i++) {
			for (int ii = 0; ii < 10; ii++) {
				System.out.println(i + " " + ii + " random.nextInt(i): " + random.nextInt(i));
			}
		}
	}
	
	@Test
	@Disabled
	public void testXorShift128RandomDoubleRange() {
		final Random random = new XorShift128Random();
		
		final int[] buckets = new int[1000];
		
		for (int i = 0; i < 1000000; i++) {
			final double n = random.nextDouble();
			final int bucket = (int) Math.floor(n / 0.001);
			buckets[bucket]++;
		}
		
		for (int i = 0; i < 1000; i++) {
			System.out.println(String.format("%03d", i) + " " + buckets[i]);
		}
	}
	
}
