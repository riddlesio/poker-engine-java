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

import org.json.JSONArray;
import org.json.JSONObject;

import io.riddles.javainterface.game.AbstractGameSerializer;
import io.riddles.poker.game.processor.PokerProcessor;
import io.riddles.poker.game.state.PokerState;
import io.riddles.poker.game.state.PokerStateSerializer;

/**
 * io.riddles.poker.game.GeneralsSerializer - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerSerializer extends AbstractGameSerializer<PokerProcessor, PokerState> {

    @Override
    public String traverseToString(PokerProcessor processor, PokerState initialState) {
        PokerStateSerializer stateSerializer = new PokerStateSerializer();
        JSONObject game = new JSONObject();

        game = addDefaultJSON(initialState, game, processor);

        JSONArray states = new JSONArray();
//        states.put(stateSerializer.traverseToJson(initialState));

        PokerState state = initialState;
        while (state.hasNextState()) {
            state = (PokerState) state.getNextState();
            states.put(stateSerializer.traverseToJson(state));
        }

        game.put("states", states);

        return game.toString();
    }
}
