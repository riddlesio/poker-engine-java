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

package io.riddles.poker.game.table.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * io.riddles.poker.game.table.card.CardHeight - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public enum CardHeight {
    DEUCE("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("T"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A");

    private static final List<CardHeight> HEIGHT_LIST = new ArrayList<>();
    private String shorthand;

    static {
        HEIGHT_LIST.addAll(Arrays.asList(values()));
    }

    CardHeight(String shorthand) {
        this.shorthand = shorthand;
    }

    public static CardHeight numberToCardHeight(int number) {
        return HEIGHT_LIST.get(number % 13);
    }

    @Override
    public String toString() {
        return this.shorthand;
    }
}
