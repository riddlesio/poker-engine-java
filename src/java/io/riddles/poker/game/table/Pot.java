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

import java.util.ArrayList;

/**
 * io.riddles.poker.game.table.Pot - Created on 29-8-17
 *
 * Class Pot is used for keeping track of the pot size, both from the main pot and
 * the possible side pots, and the players that are involved in the side pots.
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Pot {

    private int chips;
    private ArrayList<Integer> participantIds;
    private Pot sidePot;

    public Pot(ArrayList<Integer> participantIds) {
        this.chips = 0;
        this.participantIds = participantIds;
        this.sidePot = null;
    }

    public Pot(Pot pot) {
        this.chips = pot.chips;
        this.participantIds = new ArrayList<>(pot.participantIds);
        this.sidePot = pot.sidePot != null ? new Pot(pot.sidePot) : null;
    }

    public Pot getActivePot() {
        Pot pot = this;

        while (pot.sidePot != null) {
            pot = pot.sidePot;
        }

        return pot;
    }

    public Pot createSidePot(ArrayList<Integer> participantIds) {
        this.sidePot = new Pot(participantIds);
        return this.sidePot;
    }

    public ArrayList<Integer> getParticipantIds() {
        return this.participantIds;
    }

    public void increaseChips(int amount) {
        if (amount < 0) {
            throw new RuntimeException("Amount needs to be a positive number");
        }

        this.chips += amount;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    public int getChips() {
        return this.chips;
    }

    /**
     * Gets all the chips from the pot given player is eligible for
     * @param playerId Player to get chips for
     * @param sharedWith Ids of other players the pot should be shared with
     * @param empty Actually empty the pots in the process
     * @return The amount of chips the player is eligible for
     */
    public int getTotalPotForParcipant(int playerId, ArrayList<Integer> sharedWith, boolean empty) {
        Pot pot = this;
        int totalPot = 0;

        // We know that if a participant isn't in the sidepot anymore
        // he won't be in another sidepot
        while (pot != null && pot.participantIds.contains(playerId)) {

            // Divide the pot between players if shared
            Pot finalPot = pot;
            int divides = (int) sharedWith.stream()
                    .filter(id -> finalPot.participantIds.contains(id))
                    .count();

            int chips = pot.chips / (divides + 1);
            totalPot += chips;

            if (empty) {
                pot.chips -= chips;
            }

            pot = pot.sidePot;
        }

        return totalPot;
    }

    public Pot getSidePot() {
        return this.sidePot;
    }
}
