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
import java.util.Collections;
import java.util.stream.Collectors;

import io.riddles.poker.engine.PokerEngine;

/**
 * io.riddles.poker.game.table.card.Deck - Created on 29-8-17
 *
 * Class representing a single deck of cards, which is shuffled in random order.
 * Cards can be drawn from the deck.
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Deck {

    private ArrayList<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();

        for (int i = 0; i < 52; i++) {
            this.cards.add(new Card(i));
        }

        Collections.shuffle(this.cards, PokerEngine.RANDOM);
    }

    public Deck(Deck deck) {
        this.cards = deck.cards.stream()
                .map(card -> new Card(card.getNumber()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Card nextCard() {
        if (this.cards.size() <= 0) {
            return null;
        }

        return this.cards.remove(this.cards.size() - 1);
    }

    public void shuffle() {
        Collections.shuffle(this.cards, PokerEngine.RANDOM);
    }
}
