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

import java.util.HashMap;
import java.util.Map;

/**
 * io.riddles.poker.game.table.card.Card - Created on 29-8-17
 *
 * Represents a playing card. Contains the Number and Suit and is created
 * by giving a number 0 - 52.
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Card {

    private static final Map<String, Card> CARD_MAP = new HashMap<>();

    static {
        for (int i = 0; i < 52; i++) {
            Card card = new Card(i);
            CARD_MAP.put(card.toString(), card);
        }
    }

    private CardHeight height;
    private CardSuit suit;
    private int number;
    private long code;  // Code used in HandEval

    public Card(int number) {
        this.number = number;
        this.height = CardHeight.numberToCardHeight(number);
        this.suit = CardSuit.numberToCardSuit(number);

        int suitShift = this.number / 13;
        int heightShift = this.number % 13;
        this.code = 1L << (16 * suitShift + heightShift);
    }

    public static Card fromString(String string) {
        return CARD_MAP.get(string);
    }

    public String toString() {
        return "" + this.height + this.suit;
    }

    public int getNumber() {
        return this.number;
    }

    public long getCode() {
        return this.code;
    }
}
