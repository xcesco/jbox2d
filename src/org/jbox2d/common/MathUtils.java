/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 * 
 * JBox2D homepage: http://jbox2d.sourceforge.net/
 * Box2D homepage: http://www.box2d.org
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package org.jbox2d.common;

/**
 * A few math methods that don't fit very well anywhere else. djm: added ToOut
 * method
 */
public class MathUtils {

	public static final boolean FAST_MATH = true;

	public static final float abs(final float x) {
		if (FAST_MATH) {
			return x > 0 ? x : -x;
		}
		else {
			return Math.abs(x);
		}
	}

	/**
	 * This method is a *lot* faster than using (int)Math.floor(x).
	 * 
	 * @param x
	 *            value to be floored
	 * @return
	 */
	public static final int floor(final float x) {
		if (FAST_MATH) {
			return x > 0 ? (int) x : (int) x - 1;
		}
		else {
			return (int) Math.floor(x);
		}
	}

	// Max/min rewritten here because for some reason Math.max/min
	// can run absurdly slow for such simple functions...
	// TODO: profile, see if this just seems to be the case or is actually
	// causing issues...
	public final static float max(final float a, final float b) {
		return a > b ? a : b;
	}

	public final static float min(final float a, final float b) {
		return a < b ? a : b;
	}

	public final static float map(final float val, final float fromMin, final float fromMax,
			final float toMin, final float toMax) {
		final float mult = (val - fromMin) / (fromMax - fromMin);
		final float res = toMin + mult * (toMax - toMin);
		return res;
	}

	/** Returns the closest value to 'a' that is in between 'low' and 'high' */
	public final static float clamp(final float a, final float low, final float high) {
		return MathUtils.max(low, MathUtils.min(a, high));
	}

	/* djm optimized */
	public final static Vec2 clamp(final Vec2 a, final Vec2 low, final Vec2 high) {
		final Vec2 min = new Vec2();
		Vec2.minToOut(a, high, min);
		Vec2.maxToOut(low, min, min);
		return min;
	}

	/* djm created */
	public final static void clampToOut(final Vec2 a, final Vec2 low, final Vec2 high,
			final Vec2 dest) {
		Vec2.minToOut(a, high, dest);
		Vec2.maxToOut(low, dest, dest);
	}

	/**
	 * Next Largest Power of 2: Given a binary integer value x, the next largest
	 * power of 2 can be computed by a SWAR algorithm that recursively "folds"
	 * the upper bits into the lower bits. This process yields a bit vector with
	 * the same most significant 1 as x, but all 1's below it. Adding 1 to that
	 * value yields the next largest power of 2.
	 */
	public final static int nextPowerOfTwo(int x) {
		x |= x >> 1;
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		x |= x >> 16;
		return x + 1;
	}

	public final static boolean isPowerOfTwo(final int x) {
		return x > 0 && (x & x - 1) == 0;
	}

	// UNTESTED
	public static final float atan2(final float y, final float x) {
		if (FAST_MATH) {
			// float coeff_1 = PI/4;
			// float coeff_2 = 3*coeff_1;
			final float abs_y = abs(y) + .0000000001f; // kludge to prevent 0/0
			// condition
			float angle, r;
			if (x >= 0) {
				r = (x - abs_y) / (x + abs_y);
				// angle = coeff_1 - coeff_1 * r;
				angle = 0.1963f * r * r * r - 0.9817f * r + Settings.pi / 4;
			}
			else {
				r = (x + abs_y) / (abs_y - x);
				// angle = coeff_2 - coeff_1 * r;
				angle = 0.1963f * r * r * r - 0.9817f * r + 3 * Settings.pi / 4;
			}
			if (y < 0) {
				return -angle; // negate if in quad III or IV
			}
			else {
				return angle;
			}
		}
		else {
			return (float) Math.atan2(y, x);
		}
	}

	/**
	 * Computes a fast approximation to <code>MathUtils.pow(a, b)</code>.
	 * Adapted from <url>http://www.dctsystems.co.uk/Software/power.html</url>.
	 * 
	 * @param a
	 *            a positive number
	 * @param b
	 *            a number
	 * @return a^b
	 */
	// UNTESTED
	public static final float pow(final float a, float b) {
		// adapted from: http://www.dctsystems.co.uk/Software/power.html
		if (FAST_MATH) {
			float x = Float.floatToRawIntBits(a);
			x *= 1.0f / (1 << 23);
			x = x - 127;
			float y = x - MathUtils.floor(x);
			b *= x + (y - y * y) * 0.346607f;
			y = b - MathUtils.floor(b);
			y = (y - y * y) * 0.33971f;
			return Float.intBitsToFloat((int) ((b + 127 - y) * (1 << 23)));
		}
		else {
			return (float) Math.pow(a, b);
		}
	}

	public static final float sqrt(float x) {
		if (FAST_MATH) {
			x = invSqrt(x);

			if (x != 0.0f) {
				return 1.0f / x;
			}
			else {
				return 0;
			}
		}
		else {
			return (float) Math.sqrt(x);
		}
	}

	public final static float invSqrt(float x) {
		final float xhalf = 0.5f * x;
		int i = Float.floatToRawIntBits(x);
		i = 0x5f3759df - (i >> 1);
		x = Float.intBitsToFloat(i);
		x *= (1.5f - xhalf * x * x);
		// REPEAT FOR ACCURACY (make sure at least 2 are here, too inaccurate
		// otherwise)
		x *= (1.5f - xhalf * x * x);
		x *= (1.5f - xhalf * x * x);
		x *= (1.5f - xhalf * x * x);
		return x;
	}
}
