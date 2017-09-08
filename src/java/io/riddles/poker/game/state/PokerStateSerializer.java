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

package io.riddles.poker.game.state;

import org.json.JSONArray;
import org.json.JSONObject;

import io.riddles.javainterface.serialize.Serializer;
import io.riddles.poker.game.table.Pot;

/**
 * io.riddles.poker.game.state.PokerStateSerializer - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerStateSerializer implements Serializer<PokerState> {

    @Override
    public String traverseToString(PokerState state) {
        return visitState(state).toString();
    }

    @Override
    public JSONObject traverseToJson(PokerState state) {
        return visitState(state);
    }

    private JSONObject visitState(PokerState state) {
        JSONObject stateObj = new JSONObject();

        stateObj.put("round", state.getRoundNumber());
        stateObj.put("table", visitTable(state));
        stateObj.put("pot", visitPot(state));
        stateObj.put("players", visitPlayers(state));

        return stateObj;
    }

    private JSONArray visitTable(PokerState state) {
        JSONArray table = new JSONArray();

        state.getTable().getTableCards().forEach(card -> table.put(card.toString()));

        return table;
    }

    private JSONArray visitPot(PokerState state) {
        JSONArray potArray = new JSONArray();

        Pot pot = state.getTable().getPot();

        while (pot != null) {
            JSONObject potObj = new JSONObject();

            JSONArray participants = new JSONArray();
            pot.getParticipantIds().forEach(participants::put);

            potObj.put("chips", pot.getChips());
//            potObj.put("participants", participants);  // not really used in visualizer at the moment

            potArray.put(potObj);

            pot = pot.getSidePot();
        }

        return potArray;
    }

    private JSONArray visitPlayers(PokerState state) {
        PokerPlayerStateSerializer playerStateSerializer = new PokerPlayerStateSerializer();

        JSONArray players = new JSONArray();

        state.getPlayerStates().forEach(
                playerState -> players.put(playerStateSerializer.traverseToJson(playerState))
        );

        return players;
    }
}
