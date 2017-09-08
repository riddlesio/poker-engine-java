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

import com.stevebrecher.HandEval;

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.riddles.javainterface.game.state.AbstractPlayerState;
import io.riddles.poker.game.move.PokerMove;
import io.riddles.poker.game.table.HandEvaluator;
import io.riddles.poker.game.table.card.Card;

/**
 * io.riddles.poker.game.state.PokerPlayerState - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerPlayerState extends AbstractPlayerState<PokerMove> {

    private ArrayList<Card> hand;
    private ArrayList<PokerMove> betRoundMoves;
    private int chips;
    private Double odds;
    private int bet;
    private boolean hasFailedInput;
    private boolean hasFolded;
    private int handStrength;
    private HandEval.HandCategory handCategory;
    private boolean isAllIn;

    public PokerPlayerState(int playerId, int chips) {
        super(playerId);
        this.chips = chips;
        this.bet = 0;
        this.hasFailedInput = false;
        this.handStrength = -1;
        this.handCategory = null;
        this.hand = new ArrayList<>();
        this.betRoundMoves = new ArrayList<>();
        this.isAllIn = false;
    }

    public PokerPlayerState(PokerPlayerState playerState) {
        super(playerState.getPlayerId());

        this.chips = playerState.chips;
        this.odds = playerState.odds;
        this.bet = playerState.bet;
        this.hasFailedInput = playerState.hasFailedInput;
        this.hasFolded = playerState.hasFolded;
        this.handStrength = playerState.handStrength;
        this.handCategory = playerState.handCategory;
        this.isAllIn = playerState.isAllIn;

        this.hand = playerState.hand.stream()
                .map(card -> new Card(card.getNumber()))
                .collect(Collectors.toCollection(ArrayList::new));
        this.betRoundMoves = playerState.betRoundMoves.stream()
                .map(PokerMove::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Resets the player state for a new round (hand)
     */
    public void resetHand() {
        this.hand = new ArrayList<>();
        this.betRoundMoves = new ArrayList<>();
        this.odds = null;
        this.bet = 0;
        this.hasFolded = false;
        this.handStrength = -1;
        this.handCategory = null;
        this.isAllIn = false;
    }

    /**
     * Initializes the player state for the next bet round.
     * Called on Flop, Turn, and River
     */
    public void startBetRound() {
        this.bet = 0;
        this.betRoundMoves = new ArrayList<>();
        this.odds = null;
        this.handStrength = -1;
        this.handCategory = null;
    }

    public void giveCard(Card card) {
        this.hand.add(card);
    }

    public void increaseBet(int bet) {
        if (bet <= 0) {
            throw new RuntimeException("Bet needs to be a positive number");
        }

        if (bet >= this.chips) {  // Player goes all-in
            this.isAllIn = true;
            this.bet = this.chips + this.bet;
            this.chips = 0;
        } else {
            this.bet += bet;
            this.chips -= bet;
        }
    }

    /**
     * Decreases the bet (to put in the pot) by given amount. If the player
     * hasn't bet enough, the bet will be set to 0.
     * @param amount Amount to decrease the bet with
     * @return The amount that the bet was actually decreased
     */
    public int decreaseBet(int amount) {
        if (amount < 0) {
            throw new RuntimeException("Amount needs to be a positive number");
        }

        if (amount > this.bet) {
            this.bet = 0;
            return this.bet;
        }

        this.bet -= amount;
        return amount;
    }

    public void increaseChips(int chips) {
        if (chips < 0) {
            throw new RuntimeException("Chips needs to be a positive number");
        }

        this.chips += chips;
    }

    public ArrayList<Card> getHand() {
        return this.hand;
    }

    public ArrayList<PokerMove> getBetRoundMoves() {
        return this.betRoundMoves;
    }

    public PokerMove getLastMove() {
        if (this.betRoundMoves.isEmpty()) {
            return null;
        }

        return this.betRoundMoves.get(this.betRoundMoves.size() - 1);
    }

    public String getHandString() {
        return this.hand.stream()
                .map(Card::toString)
                .collect(Collectors.joining(","));
    }

    public boolean isAllin() {
        return this.isAllIn;
    }

    public boolean isAlive() {
        return (this.chips > 0 || this.bet > 0 || isAllin()) && !this.hasFailedInput;
    }

    public boolean isActive() {
        return isAlive() && !this.hasFolded && !isAllin();
    }

    public boolean isInHand() {
        return isAlive() && !this.hasFolded;
    }

    @Override
    public void setMove(PokerMove move) {
        this.betRoundMoves.add(move);
    }

    public int getChips() {
        return this.chips;
    }

    public void setOdds(double odds) {
        this.odds = odds;
    }

    public Double getOdds() {
        return this.odds;
    }

    public void setHandStrength(int strength) {
        this.handStrength = strength;
        this.handCategory = HandEvaluator.handStrengthToCategory(strength);
    }

    public int getHandStrength() {
        return this.handStrength;
    }

    public HandEval.HandCategory getHandCategory() {
        return this.handCategory;
    }

    public int getBet() {
        return this.bet;
    }

    public void setFailedInput() {
        this.hasFailedInput = true;
    }

    public boolean hasActed() {
        return !this.betRoundMoves.isEmpty();
    }

    public void setFolded() {
        this.hasFolded = true;
    }
}
