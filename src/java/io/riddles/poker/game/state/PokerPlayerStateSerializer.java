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

/**
 * io.riddles.poker.game.state.PokerPlayerStateSerializer - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerPlayerStateSerializer implements Serializer<PokerPlayerState> {

    @Override
    public String traverseToString(PokerPlayerState playerState) {
        return visitPlayerState(playerState).toString();
    }

    @Override
    public JSONObject traverseToJson(PokerPlayerState playerState) {
        return visitPlayerState(playerState);
    }

    private JSONObject visitPlayerState(PokerPlayerState playerState) {
        JSONObject playerStateObj = new JSONObject();

        playerStateObj.put("id", playerState.getPlayerId());
        playerStateObj.put("chips", playerState.getChips());
        playerStateObj.put("bet", playerState.getBet());
        playerStateObj.put("hand", visitHand(playerState));

        if (playerState.getLastMove() != null) {
            playerStateObj.put("move", playerState.getLastMove().toString());
        }

        if (playerState.getOdds() != null) {
            playerStateObj.put("odds", playerState.getOdds());
        }

        return playerStateObj;
    }

    private JSONObject visitHand(PokerPlayerState playerState) {
        JSONObject hand = new JSONObject();

        JSONArray cards = new JSONArray();
        playerState.getHand().forEach(card -> cards.put(card.toString()));

        hand.put("cards", cards);

        if (playerState.getHandCategory() != null) {
            hand.put("value", playerState.getHandCategory());
        }

        return hand;
    }
}
