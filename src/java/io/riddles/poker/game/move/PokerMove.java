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

package io.riddles.poker.game.move;

import io.riddles.javainterface.exception.InvalidInputException;
import io.riddles.javainterface.game.move.AbstractMove;

/**
 * io.riddles.poker.game.move.PokerMove - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerMove extends AbstractMove {

    private MoveType moveType;
    private Integer amount;

    public PokerMove(MoveType moveType, int amount) {
        this.moveType = moveType;
        this.amount = amount;
    }

    public PokerMove(MoveType moveType) {
        this.moveType = moveType;
    }

    public PokerMove(PokerMove move) {
        this.moveType = move.moveType;
        this.amount = move.amount;
    }

    public PokerMove(InvalidInputException exception) {
        super(exception);
    }

    @Override
    public String toString() {
        if (this.moveType == MoveType.RAISE) {
            return String.format("%s_%s", this.moveType, this.amount);
        }

        return this.moveType.toString();
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public MoveType getMoveType() {
        return this.moveType;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Integer getAmount() {
        return this.amount;
    }
}
