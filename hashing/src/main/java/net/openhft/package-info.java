/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * API for hashing sequential data and zero-allocation, pretty fast implementations
 * of non-cryptographic hash functions.
 *
 * <p>Currently implemented (in alphabetical order):
 * <ul>
 *     <li>{@code long}-valued functions: see {@link net.openhft.hash.HashFunction}
 *     <ul>
 *         <li>
 *         {@linkplain net.openhft.hash.Hash#xx() xxHash without seed}.
 *         {@linkplain net.openhft.hash.Hash#xx3() xxHash without seed}.
 *         </li>
 *     </ul>
 *     </li>
 * </ul>
 */
package net.openhft;
