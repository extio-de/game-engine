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

/**
 * Thread local version of XorShift128Random. This is the preferred way of using this generator.
 */
public final class ThreadLocalXorShift128Random {
	
	private final static ThreadLocal<Random> INSTANCE = ThreadLocal.withInitial(XorShift128Random::new);
	
	public static Random current() {
		return INSTANCE.get();
	}
	
	public static int nextInt(final int startInclusive, final int endExclusive) {
		if (startInclusive == endExclusive) {
			return startInclusive;
		}
		
		return startInclusive + current().nextInt(endExclusive - startInclusive);
	}
}
