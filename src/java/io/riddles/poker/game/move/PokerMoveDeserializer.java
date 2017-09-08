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
import io.riddles.javainterface.serialize.Deserializer;

/**
 * io.riddles.poker.game.move.PokerMoveDeserializer - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerMoveDeserializer implements Deserializer<PokerMove> {

    @Override
    public PokerMove traverse(String string) {
        try {
            return visitMove(string);
        } catch (InvalidInputException ex) {
            return new PokerMove(ex);
        } catch (Exception ex) {
            return new PokerMove(new InvalidInputException("Failed to parse action."));
        }
    }

    private PokerMove visitMove(String input) throws InvalidInputException {
        String[] split = input.split("_");

        MoveType moveType = visitMoveType(split[0]);

        if (split.length < 2 && moveType != MoveType.RAISE) {
            return new PokerMove(moveType);
        }

        int amount = visitAmount(split[1]);
        return new PokerMove(moveType, amount);
    }

    private MoveType visitMoveType(String input) throws InvalidInputException {
        MoveType moveType = MoveType.fromString(input);

        if (moveType == null) {
            throw new InvalidInputException(String.format("Unknown move type '%s'.", input));
        }

        return moveType;
    }

    private int visitAmount(String input) throws InvalidInputException {
        try {
            int amount = Integer.parseInt(input);

            if (amount <= 0) {
                throw new InvalidInputException("Amount can't be smaller than or equal to 0.");
            }

            return amount;
        } catch (Exception ex) {
            throw new InvalidInputException(String.format("Can't parse amount '%s'.", input));
        }
    }
}
