/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.base.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PairUtil {

    /**
     *
     * @param array
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(Iterable<Pair<K, V>> array) {
        if (array != null) {
            return StreamSupport.stream(array.spliterator(), false)
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        }
        return new HashMap<>();
    }

    /**
     *
     * @param array
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(Pair<K, V> ... array) {
        if (ArrayUtils.isNotEmpty(array)) {
            return toMap(Arrays.asList(array));
        }
        return new HashMap<>();
    }

}

