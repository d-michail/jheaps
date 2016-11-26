/*
 * (C) Copyright 2014-2016, by Dimitrios Michail
 *
 * JHeaps Library
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jheaps.monotone;

import java.math.BigInteger;

/**
 * Utilities for unsigned computation
 * 
 * @author Dimitrios Michail
 */
class UnsignedUtils {

    UnsignedUtils() {
    }

    private static final long UNSIGNED_MASK = 0x7fffffffffffffffL;

    static double unsignedLongToDouble(long x) {
        double d = (double) (x & UNSIGNED_MASK);
        if (x < 0) {
            d += 0x1.0p63;
        }
        return d;
    }

    static BigInteger unsignedLongToBigInt(long x) {
        BigInteger asBigInt = BigInteger.valueOf(x & UNSIGNED_MASK);
        if (x < 0) {
            asBigInt = asBigInt.setBit(Long.SIZE - 1);
        }
        return asBigInt;
    }

}
