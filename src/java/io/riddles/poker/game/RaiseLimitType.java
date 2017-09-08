/*
 *  Copyright 2017 riddles.io (developers@riddles.io)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 *      For the full copyright and license information, please view the LICENSE
 *      file that was distributed with this source code.
 */

package io.riddles.poker.game;

import java.util.HashMap;
import java.util.Map;

/**
 * io.riddles.poker.game.RaiseLimitType - Created on 30-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public enum RaiseLimitType {
    NOLIMIT,
    POTLIMIT;

    private static final Map<String, RaiseLimitType> TYPE_MAP = new HashMap<>();

    static {
        for (RaiseLimitType raiseLimitType : values()) {
            TYPE_MAP.put(raiseLimitType.toString(), raiseLimitType);
        }
    }

    public static RaiseLimitType fromString(String string) {
        RaiseLimitType type = TYPE_MAP.get(string.toLowerCase());

        if (type == null) {
            throw new RuntimeException(
                    String.format("Raise limit type '%s' not recognized", string));
        }

        return type;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
