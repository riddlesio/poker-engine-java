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

package io.riddles.poker.game.table;

/**
 * io.riddles.poker.game.table.adsf - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public enum BetRound {
    START,  // the state where no cards have been dealt
    PREFLOP,
    FLOP,
    TURN,
    RIVER;

    public boolean hasNext() {
        return this != RIVER;
    }

    public BetRound getNext() {
        switch (this) {
            case START:
                return PREFLOP;
            case PREFLOP:
                return FLOP;
            case FLOP:
                return TURN;
            case TURN:
                return RIVER;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
