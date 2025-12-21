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

import org.junit.jupiter.api.Test;

public class ZeroRandomTests {
	
	@Test
	public void testSpeedRunRandom() {
		final Random speedRunRandom = new ZeroRandom();
		
		System.out.println("nextDouble()");
		for (int i = 0; i < 10; i++) {
			System.out.println(speedRunRandom.nextDouble());
		}
		System.out.println("nextInt()");
		for (int i = 0; i < 10; i++) {
			System.out.println(speedRunRandom.nextInt());
		}
		System.out.println("nextInt(5)");
		for (int i = 0; i < 10; i++) {
			System.out.println(speedRunRandom.nextInt(5));
		}
		System.out.println("nextLong()");
		for (int i = 0; i < 10; i++) {
			System.out.println(speedRunRandom.nextLong());
		}
		System.out.println("nextBoolean()");
		for (int i = 0; i < 10; i++) {
			System.out.println(speedRunRandom.nextBoolean());
		}
		
		System.out.println();
		System.out.println("Performance:");
		final long start = System.currentTimeMillis();
		long a = 5;
		for (int i = 0; i < 1000000000; i++) {
			a = a + speedRunRandom.nextLong();
		}
		System.out.println(a + " " + (System.currentTimeMillis() - start));
	}
	
}
