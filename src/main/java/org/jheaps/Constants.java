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
package org.jheaps;

/**
 * Global library configuration flags which affect generated code at compile
 * time.
 *
 * @author Dimitrios Michail
 */
public class Constants {

    /**
     * Library name
     */
    public static final String NAME = "JHeaps";

    /**
     * Global debug flag which affects compiled code
     */
    public static final boolean DEBUG = false;

    /**
     * Global level one debug flag which affects compiled code.
     */
    public static final boolean DEBUG_LEVEL1 = false;

    /**
     * Global level two debug flag which affects compiled code
     */
    public static final boolean DEBUG_LEVEL2 = false;

    /**
     * Global benchmarking flag. This flag enables sanity checks when the code
     * is not for benchmarking performance. When benchmarking we assume that the
     * user provided the correct input.
     */
    public static final boolean NOT_BENCHMARK = true;

    private Constants() {
    }

}
