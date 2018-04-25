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

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.riddles.javainterface.game.state.AbstractState;
import io.riddles.poker.game.table.BetRound;
import io.riddles.poker.game.table.PokerTable;

/**
 * io.riddles.poker.game.state.PokerState - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerState extends AbstractState<PokerPlayerState> {

    private PokerTable table;
    private BetRound betRound;

    // For initial state only
    public PokerState(ArrayList<PokerPlayerState> playerStates) {
        super(null, playerStates, 1);
        this.table = new PokerTable(this);
        this.betRound = null;
    }

    public PokerState(PokerState previousState, ArrayList<PokerPlayerState> playerStates,
                      int roundNumber, BetRound betRound) {
        super(previousState, playerStates, roundNumber);
        this.table = new PokerTable(previousState.table, this);
        this.betRound = betRound;
    }

    public PokerState createNextState(int roundNumber, BetRound betRound) {
        // Create new player states from current player states
        ArrayList<PokerPlayerState> playerStates = new ArrayList<>();
        for (PokerPlayerState playerState : this.playerStates) {
            playerStates.add(new PokerPlayerState(playerState));
        }

        // Create new state from current state
        return new PokerState(this, playerStates, roundNumber, betRound);
    }

    public ArrayList<PokerPlayerState> getAlivePlayers() {
        return this.playerStates.stream()
                .filter(PokerPlayerState::isAlive)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PokerPlayerState> getHandPlayers() {
        return this.playerStates.stream()
                .filter(PokerPlayerState::isInHand)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PokerPlayerState> getActivePlayers() {
        return this.playerStates.stream()
                .filter(PokerPlayerState::isActive)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PokerPlayerState> getBettingPlayers() {
        return this.playerStates.stream()
                .filter(PokerPlayerState::isBetting)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public PokerTable getTable() {
        return this.table;
    }

    public void setBetRound(BetRound betRound) {
        this.betRound = betRound;
    }

    public BetRound getBetRound() {
        return this.betRound;
    }
}
