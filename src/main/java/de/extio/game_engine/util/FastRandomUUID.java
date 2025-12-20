/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.util;

import java.util.Random;
import java.util.UUID;

import de.extio.game_engine.util.rng.ThreadLocalXorShift128Random;

/**
 * Optimized factory to create a random UUID
 */
public final class FastRandomUUID {
	
	/**
	 * Creates a random UUID that has been created fast without draining the operating system's pool of entropy but is not safe for security relevant use (but this is usually not our use case anyway)
	 */
	public static UUID create() {
		final Random cur = ThreadLocalXorShift128Random.current();
		return new UUID(cur.nextLong(), cur.nextLong());
	}
	
}
