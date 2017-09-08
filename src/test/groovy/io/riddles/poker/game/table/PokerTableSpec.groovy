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

package io.riddles.poker.game.table

import io.riddles.javainterface.configuration.Configuration
import io.riddles.poker.engine.PokerEngine
import io.riddles.poker.game.state.PokerPlayerState
import io.riddles.poker.game.state.PokerState
import spock.lang.Specification

/**
 * io.riddles.poker.game.table.PokerTableSpec - Created on 4-9-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
class PokerTableSpec extends Specification {

    def "test next button and blinds 2 players"() {
        setup:
        PokerState state = Stub(PokerState.class)
        PokerPlayerState player0 = new PokerPlayerState(0, 2000)
        PokerPlayerState player1 = new PokerPlayerState(1, 2000)
        PokerEngine.configuration = new Configuration()
        PokerEngine.configuration.put("initialBigBlind", 20)

        PokerTable table = new PokerTable(state)

        state.getPlayerStateById(0) >> player0
        state.getPlayerStateById(1) >> player1

        when:
        table.setPlayerCount(2)
        table.setButtonId(0)
        table.setSmallBlindId(0)
        table.setBigBlindId(1)
        table.setNextButtonAndBlinds()

        then:
        table.getButtonId() == 1
        table.getSmallBlindId() == 1
        table.getBigBlindId() == 0

        when:
        table.setPlayerCount(2)
        table.setButtonId(1)
        table.setSmallBlindId(1)
        table.setBigBlindId(0)
        table.setNextButtonAndBlinds()

        then:
        table.getButtonId() == 0
        table.getSmallBlindId() == 0
        table.getBigBlindId() == 1
    }

    def "test next button and blinds 4 players"() {
        setup:
        PokerState state = Stub(PokerState.class)
        PokerPlayerState player0 = new PokerPlayerState(0, 2000)
        PokerPlayerState player1 = new PokerPlayerState(1, 2000)
        PokerPlayerState player2 = new PokerPlayerState(2, 2000)
        PokerPlayerState player3 = new PokerPlayerState(3, 2000)
        PokerEngine.configuration = new Configuration()
        PokerEngine.configuration.put("initialBigBlind", 20)

        PokerTable table = new PokerTable(state)

        state.getPlayerStateById(0) >> player0
        state.getPlayerStateById(1) >> player1
        state.getPlayerStateById(2) >> player2
        state.getPlayerStateById(3) >> player3

        when:
        table.setPlayerCount(4)
        table.setButtonId(0)
        table.setSmallBlindId(1)
        table.setBigBlindId(2)
        table.setNextButtonAndBlinds()

        then:
        table.getButtonId() == 1
        table.getSmallBlindId() == 2
        table.getBigBlindId() == 3

        when:
        table.setPlayerCount(4)
        table.setButtonId(2)
        table.setSmallBlindId(3)
        table.setBigBlindId(0)
        table.setNextButtonAndBlinds()

        then:
        table.getButtonId() == 3
        table.getSmallBlindId() == 0
        table.getBigBlindId() == 1
    }
}
