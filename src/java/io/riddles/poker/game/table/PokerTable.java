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
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.riddles.javainterface.exception.InvalidMoveException;
import io.riddles.poker.engine.PokerEngine;
import io.riddles.poker.game.RaiseLimitType;
import io.riddles.poker.game.move.MoveType;
import io.riddles.poker.game.move.PokerMove;
import io.riddles.poker.game.state.PokerPlayerState;
import io.riddles.poker.game.state.PokerState;
import io.riddles.poker.game.table.card.Card;
import io.riddles.poker.game.table.card.Deck;

/**
 * io.riddles.poker.game.table.Table - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerTable {

    private final int[] BLINDLEVELHEIGHTS = {  // the size of the big blind in the consecutive blind levels
        10, 20, 30, 40, 50, 60, 80,
        100, 120, 160, 200, 240, 300, 400, 500, 600, 800,
        1000, 1200, 1600, 2000, 2400, 3000, 4000, 5000, 6000, 8000,
        10000, 12000, 16000, 20000, 24000, 30000, 40000, 50000, 60000, 80000,
        100000, 120000, 160000, 200000, 240000, 300000, 400000, 500000, 600000, 800000,
        1000000
    };

    private PokerState state;  // back reference to state this table belongs to

    private Deck deck;
    private ArrayList<Card> tableCards;
    private Pot pot;

    private int playerCount;
    private int buttonId;
    private int smallBlindId;  // may be < 0 if no small blind is paid
    private int bigBlindId;
    private int blindHeight;

    private int lastFullRaise;  // Keeps track of the last full raise for bet re-opening

    // For initial state only
    public PokerTable(PokerState state) {
        this.state = state;

        this.playerCount = state.getPlayerStates().size();
        this.buttonId = -1;

        this.blindHeight = PokerEngine.configuration.getInt("initialBigBlind");
    }

    public PokerTable(PokerTable table, PokerState state) {
        this.state = state;

        this.deck = new Deck(table.deck);
        this.pot = new Pot(table.pot);
        this.tableCards = table.tableCards.stream()
                .map(card -> new Card(card.getNumber()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.playerCount = table.playerCount;
        this.buttonId = table.buttonId;
        this.smallBlindId = table.smallBlindId;
        this.bigBlindId = table.bigBlindId;
        this.blindHeight = table.blindHeight;
    }

    // Prepares the table for a new hand
    public void resetTable() {
        clearTable();
        increaseBlindHeight();
        setNextButtonAndBlinds();
        this.lastFullRaise = 0;

        this.state.getPlayerStates().forEach(PokerPlayerState::resetHand);
    }

    public void startBetRound() {
        this.lastFullRaise = 0;
        this.state.getPlayerStates().forEach(PokerPlayerState::startBetRound);
    }

    public void payBlinds() {
        PokerPlayerState smallBlindPlayer = this.state.getPlayerStateById(this.smallBlindId);
        PokerPlayerState bigBlindPlayer = this.state.getPlayerStateById(this.bigBlindId);

        smallBlindPlayer.increaseBet(getSmallBlind());
        bigBlindPlayer.increaseBet(getBigBlind());
    }

    public void dealCards() {
        int cardCount = 0;

        switch (PokerEngine.POKER_TYPE) {
            case TEXASHOLDEM:
                cardCount = 2;
                break;
            case OMAHA:
                cardCount = 4;
                break;
        }

        for (PokerPlayerState activePlayer : this.state.getAlivePlayers()) {
            for (int i = 0; i < cardCount; i++) {
                activePlayer.giveCard(this.deck.nextCard());
            }
        }
    }

    /**
     * Processes the given playerState's move in the game, after it's validated.
     * @param playerState PlayerState
     */
    public void processMove(PokerPlayerState playerState) {
        validateMove(playerState);

        PokerMove move = playerState.getLastMove();
        int callAmount = getCallAmountForPlayer(playerState);

        switch (move.getMoveType()) {
            case FOLD:
                playerState.setFolded();
                break;
            case CHECK:
                // Nothing happens
                break;
            case CALL:
                playerState.increaseBet(callAmount);
                break;
            case RAISE:
                int raiseAmount = move.getAmount();

                playerState.increaseBet(callAmount + raiseAmount);

                if ((callAmount == 0 && raiseAmount >= this.blindHeight * 2)
                        || (callAmount > 0 && raiseAmount >= this.lastFullRaise)) {
                    this.lastFullRaise = raiseAmount;
                }

                break;
        }
    }

    public boolean hasHandEnded() {
        // Hand ends when there is only one player who hasn't folded yet
        return this.state.getHandPlayers().size() <= 1;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean hasBetRoundEnded(boolean showdown) {
        if (hasHandEnded()) {
            return true;
        }

        ArrayList<PokerPlayerState> activePlayers = this.state.getActivePlayers();

        // Betting round ends when no active players (everybody all-in or folded)
        // or when we are in showdown (i.e. everybody all-in except maybe one player)
        if (activePlayers.isEmpty() || showdown) {
            return true;
        }

        // Betting round can't end when there are active players that haven't acted yet
        if (!activePlayers.stream().allMatch(PokerPlayerState::hasActed)) {
            return false;
        }

        // Betting round ends when all active players' bets are equal
        if (activePlayers.stream().allMatch(ap -> ap.getBet() == activePlayers.get(0).getBet())) {
            return true;
        }

        return false;
    }

    /**
     * The amount that is currently in the pot for given player. There might actually
     * be more in the pot, but this is the maximum amount he can win if no other players
     * add anything to it.
     * @param playerState Player to calculate pot for
     * @return The current pot for given player
     */
    public int getPotForPlayer(PokerPlayerState playerState) {
        int maxBet = playerState.getBet() + playerState.getChips();

        // Get the bets of the current bet round
        int potForPlayer = 0;
        for (PokerPlayerState otherPlayerState : this.state.getAlivePlayers()) {
            if (otherPlayerState.getBet() < maxBet) {
                potForPlayer += otherPlayerState.getBet();
            } else {
                potForPlayer += maxBet;
            }
        }

        // Get the chips in the pot already from previous bet rounds
        potForPlayer += this.pot.getTotalPotForParcipant(
                playerState.getPlayerId(), new ArrayList<>(), false);

        return potForPlayer;
    }

    public int getCallAmountForPlayer(PokerPlayerState playerState) {
        PokerPlayerState previousPlayer = getPreviousAlivePlayer(playerState.getPlayerId());

        return Math.max(0, previousPlayer.getBet() - playerState.getBet());
    }

    public PokerPlayerState getBetRoundStartingPlayer() {
        return this.state.getBetRound() == BetRound.PREFLOP
                ? getNextAlivePlayer(this.bigBlindId)
                : getNextAlivePlayer(this.buttonId);
    }

    public PokerPlayerState getNextActivePlayer(int id) {
        return getTypePlayerInDirection(id, "active", 1);
    }

    /**
     * Draws the given amount of cards from the deck and adds them
     * to the table
     * @param amount Amount of cards to add to the table
     */
    public void addTableCards(int amount) {
        for (int i = 0; i < amount; i++) {
            this.tableCards.add(this.deck.nextCard());
        }
    }

    /**
     * Adds the all the player bets to the correct pots
     */
    public void putBetsInPot() {
        Pot activePot = this.pot.getActivePot();

        ArrayList<PokerPlayerState> sortedPlayerStates = activePot.getParticipantIds().stream()
                .map(id -> this.state.getPlayerStateById(id))
                .filter(ps -> ps.getBet() > 0)
                .sorted(Comparator.comparingInt(PokerPlayerState::getBet))
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < sortedPlayerStates.size(); i++){
            PokerPlayerState playerState = sortedPlayerStates.get(i);

            if (playerState.getBet() <= 0) continue;

            // Create a new sidepot if we still have open bets on the table
            // after the first iteration
            if (i > 0) {
                ArrayList<Integer> participantIds = new ArrayList<>();
                for (int j = i; j < sortedPlayerStates.size(); j++) {
                    participantIds.add(sortedPlayerStates.get(j).getPlayerId());
                }

                activePot = activePot.createSidePot(participantIds);
            }

            // Move (part of) the bets to the current active pot
            Pot finalActivePot = activePot;
            int bet = playerState.getBet();
            activePot.getParticipantIds().stream()
                    .map(id -> this.state.getPlayerStateById(id))
                    .forEach(ps -> {
                        int amount = ps.decreaseBet(bet);
                        finalActivePot.increaseChips(amount);
                    });
        }
    }

    public void setHandStrengths() {
        if (this.tableCards.size() != 5) return;

        // Only set the strenghts on a showdown
        for (PokerPlayerState playerState : this.state.getHandPlayers()) {
            int strength = HandEvaluator.getHandStrength(
                    playerState.getHand(), this.tableCards);
            playerState.setHandStrength(strength);
        }
    }

    public ArrayList<Integer> getPotPayout() {
        int[] winningPerBot = new int[this.playerCount];

        ArrayList<PokerPlayerState> sortedHandPlayers = this.state.getHandPlayers().stream()
                .sorted(Comparator.comparingInt(PokerPlayerState::getHandStrength).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        if (sortedHandPlayers.size() == 1) {  // We ended on a fold, so get all bets on the table
            PokerPlayerState handPlayer = sortedHandPlayers.get(0);
            for (PokerPlayerState playerState : this.state.getAlivePlayers()) {
                handPlayer.increaseChips(playerState.getBet());
            }
        }

        // Increase the chips for each player, according to whats in the pot and
        // how strong their hand is
        for (int i = 0; i < sortedHandPlayers.size(); i++) {
            PokerPlayerState playerState = sortedHandPlayers.get(i);

            // Get what players the pot is shared with. Only looking ahead, this is
            // important because the pot is emptied when getting it
            ArrayList<Integer> sharedWith = new ArrayList<>();
            for (int j = i + 1; j < sortedHandPlayers.size(); j++) {
                PokerPlayerState otherPlayerState = sortedHandPlayers.get(j);

                if (playerState.getHandStrength() != otherPlayerState.getHandStrength()) break;

                sharedWith.add(otherPlayerState.getPlayerId());
            }

            int chips =  this.pot.getTotalPotForParcipant(
                    playerState.getPlayerId(), sharedWith, true);
            winningPerBot[playerState.getPlayerId()] += chips;
            playerState.increaseChips(chips);
        }

        return IntStream.of(winningPerBot).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public void setHandOdds() {
        ArrayList<ArrayList<Card>> hands = new ArrayList<>();

        for (PokerPlayerState playerState : this.state.getAlivePlayers()) {
            hands.add(playerState.getPlayerId(), playerState.getHand());
        }

        ArrayList<Double> odds = HandEvaluator.getHandOdds(
                hands, this.tableCards, this.deck, this.playerCount);

        for (int i = 0; i < odds.size(); i++) {
            this.state.getPlayerStateById(i).setOdds(odds.get(i));
        }
    }

    public ArrayList<Card> getTableCards() {
        return this.tableCards;
    }

    public Pot getPot() {
        return this.pot;
    }

    public void setState(PokerState state) {
        this.state = state;
    }

    public void setSmallBlindId(int id) {
        this.smallBlindId = id;
    }

    public int getSmallBlindId() {
        return this.smallBlindId;
    }

    public void setBigBlindId(int id) {
        this.bigBlindId = id;
    }

    public int getBigBlindId() {
        return this.bigBlindId;
    }

    public int getBigBlind() {
        return this.blindHeight;
    }

    public int getSmallBlind() {
        return this.blindHeight / 2;
    }

    public void setButtonId(int id) {
        this.buttonId = id;
    }

    public void setPlayerCount(int count) {
        this.playerCount = count;
    }

    public int getButtonId() {
        return this.buttonId;
    }

    public String getTableCardsString() {
        return this.tableCards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(","));
    }

    public void setNextButtonAndBlinds() {
        if (this.buttonId < 0) {
           setInitialButtonAndBlinds();
           return;
        }

        // Big blind is next active player after the previous big blind
        PokerPlayerState nextBigBlindPlayer = getNextAlivePlayer(this.bigBlindId);
        this.bigBlindId = nextBigBlindPlayer.getPlayerId();

        // Small blind is the player before the big blind
        int nextSmallBlindId = this.bigBlindId;
        while (true) {
            nextSmallBlindId = Math.floorMod(nextSmallBlindId - 1, this.playerCount);

            // Dead blind rule: the small blind can't be the previous small blind player, so
            // it will be skipped instead.
            if (nextSmallBlindId == this.smallBlindId) {
                this.smallBlindId = -1;
                break;
            }

            if (this.state.getPlayerStateById(nextSmallBlindId).isAlive()) {
                this.smallBlindId = nextSmallBlindId;
                break;
            }
        }

        if (this.playerCount == 2) {
            this.buttonId = this.smallBlindId;
        } else {
            // Button player is the player before the small blind
            PokerPlayerState nextButtonPlayer = getPreviousAlivePlayer(this.smallBlindId);
            this.buttonId = nextButtonPlayer.getPlayerId();
        }
    }

    private PokerPlayerState getNextAlivePlayer(int id) {
        return getTypePlayerInDirection(id, "alive", 1);
    }

    private PokerPlayerState getPreviousAlivePlayer(int id) {
        return getTypePlayerInDirection(id, "alive", -1);
    }

    private void increaseBlindHeight() {
        if (this.state.getRoundNumber() <= 1) return;

        int handsPerBlindLevel = PokerEngine.configuration.getInt("handsPerBlindLevel");

        // Only increase on handsPerBlindLevel
        if (Math.floorMod(this.state.getRoundNumber() - 1, handsPerBlindLevel) != 0) return;

        // Increase to next level in list
        for (int blindLevel : BLINDLEVELHEIGHTS) {
            if (blindLevel > this.blindHeight) {
                this.blindHeight = blindLevel;
                return;
            }
        }
    }

    /**
     * Checks if the given move is correct for the current game situation, and
     * if not, transforms the move to the logical alternative and sets an exception.
     *
     * See: https://www.pagat.com/poker/rules/betting.html
     *
     * @param playerState PlayerState with an unprocessed move
     */
    private void validateMove(PokerPlayerState playerState) {
        PokerMove move = playerState.getLastMove();
        int callAmount = getCallAmountForPlayer(playerState);

        switch (move.getMoveType()) {
            case FOLD:
                // Folding is always possible
                break;
            case CHECK:
                if (callAmount > 0) {
                    move.setMoveType(MoveType.FOLD);
                    move.setException(new InvalidMoveException(
                            "Checking not possible when a bet has already been made."));
                }
                break;
            case CALL:
                if (callAmount == 0) {
                    move.setMoveType(MoveType.CHECK);
                    move.setException(new InvalidMoveException("There is no bet to call."));
                }
                break;
            case RAISE:
                int amount = move.getAmount();
                int chips = playerState.getChips();

                if (playerState.getBetRoundMoves().size() > 1) {  // not first move
                    if (callAmount < this.blindHeight * 2 || callAmount < this.lastFullRaise) {
                        move.setMoveType(MoveType.CALL);
                        move.setException(new InvalidMoveException(
                                "Betting is not re-opened for your bot."));
                        break;
                    }
                }

                ArrayList<PokerPlayerState> otherHandPlayers = this.state.getHandPlayers().stream()
                        .filter(ps -> ps.getPlayerId() != playerState.getPlayerId())
                        .collect(Collectors.toCollection(ArrayList::new));
                if (otherHandPlayers.stream().allMatch(PokerPlayerState::isAllin)) {
                    move.setMoveType(MoveType.CALL);
                    move.setException(new InvalidMoveException("All players are already all-in."));
                    break;
                }

                if (chips < callAmount) {
                    move.setMoveType(MoveType.CALL);
                    move.setException(new InvalidMoveException("Not enough chips to raise."));
                    break;
                }

                if (amount < chips) {
                    if ((callAmount == 0 && amount < this.blindHeight * 2)
                            || (callAmount > 0 && amount < this.lastFullRaise)) {
                        move.setMoveType(MoveType.CALL);
                        move.setException(new InvalidMoveException("Raise amount below minimum."));
                        break;
                    }
                } else {
                    move.setAmount(chips - callAmount);
                }

                if (PokerEngine.RAISE_LIMIT_TYPE == RaiseLimitType.POTLIMIT) {
                    int potChips = this.pot.getActivePot().getChips();

                    if (amount > potChips) {
                        move.setAmount(potChips + callAmount);
                        move.setException(new InvalidMoveException("Raise amount above pot."));
                        break;
                    }
                }

                break;
        }
    }

    /**
     * Gets the next alive, active or hand player after given player id
     * @param id Player id
     * @param direction before or after given id
     * @return The next PlayerState that is active
     */
    private PokerPlayerState getTypePlayerInDirection(int id, String type, int direction) {
        int startId = Math.floorMod(id + direction, this.playerCount);

        for (int i = startId; i != id; i = Math.floorMod(i + direction, this.playerCount)) {
            PokerPlayerState playerState = this.state.getPlayerStateById(i);

            if (type.equals("alive") && playerState.isAlive()) {
                return playerState;
            }

            if (type.equals("hand") && playerState.isInHand()) {
                return playerState;
            }

            if (type.equals("active") && playerState.isActive()) {
                return playerState;
            }
        }

        return null;
    }

    private void clearTable() {
        this.deck = new Deck();
        this.tableCards = new ArrayList<>();

        ArrayList<Integer> participantIds = this.state.getAlivePlayers().stream()
                .map(PokerPlayerState::getPlayerId)
                .collect(Collectors.toCollection(ArrayList::new));

        this.pot = new Pot(participantIds);
    }

    private void setInitialButtonAndBlinds() {
        this.buttonId = 0;

        if (this.playerCount <= 2) {
            this.smallBlindId = 0;
            this.bigBlindId = 1;
        } else {
            this.smallBlindId = 1;
            this.bigBlindId = 2;
        }
    }
}
