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

package io.riddles.poker.game.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.game.processor.SimpleProcessor;
import io.riddles.poker.game.move.ActionType;
import io.riddles.poker.game.move.MoveType;
import io.riddles.poker.game.move.PokerMove;
import io.riddles.poker.game.move.PokerMoveDeserializer;
import io.riddles.poker.game.player.PokerPlayer;
import io.riddles.poker.game.state.PokerPlayerState;
import io.riddles.poker.game.state.PokerState;
import io.riddles.poker.game.table.BetRound;
import io.riddles.poker.game.table.PokerTable;

/**
 * io.riddles.poker.game.processor.PokerProcessor - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerProcessor extends SimpleProcessor<PokerState, PokerPlayer> {

    private PokerMoveDeserializer moveDeserializer;

    // TODO: broadcast when a player is out of chips (not needed for heads-up)

    public PokerProcessor(PlayerProvider<PokerPlayer> playerProvider) {
        super(playerProvider);
        this.moveDeserializer = new PokerMoveDeserializer();
    }

    @Override
    public boolean hasGameEnded(PokerState state) {
        return getActuallyAlivePlayers(state).size() <= 1;
    }

    @Override
    public Integer getWinnerId(PokerState state) {
        ArrayList<PokerPlayerState> alivePlayers = getActuallyAlivePlayers(state);

        if (alivePlayers.isEmpty() || alivePlayers.size() > 1) {
            return null;
        }

        return alivePlayers.get(0).getPlayerId();
    }

    @Override
    public double getScore(PokerState state) {
        return state.getRoundNumber();
    }

    @Override
    public PokerState createNextState(PokerState inputState, int roundNumber) {
        PokerState nextState = inputState.createNextState(roundNumber, BetRound.START);

        // Store payout at the start of the next round
        ArrayList<Integer> winnings = nextState.getTable().getPotPayout();
        broadCastWinnings(winnings, nextState, false);

        nextState.getTable().resetTable();

        sendInitialUpdates(nextState);

        while (!nextState.getTable().hasHandEnded() && nextState.getBetRound().hasNext()) {
            nextState = createBetRoundStates(nextState);
        }

        PokerState finalHandState = createFinalHandState(nextState);

        if (!hasGameEnded(finalHandState)) {
            return finalHandState;
        }

        return createFinalState(finalHandState);
    }

    /**
     * Creates all the states of a single bet round and returns the last one
     * @param state Current state
     * @return The last state of the current bet round
     */
    private PokerState createBetRoundStates(PokerState state) {
        PokerState nextState = state.createNextState(
                state.getRoundNumber(), state.getBetRound().getNext());

        performBetRoundActions(nextState);

        int playerId = nextState.getTable().getBetRoundStartingPlayer().getPlayerId();

        // We're in showdown mode if there's 0 or 1 active player at the start of the betround
        boolean showdown = state.getActivePlayers().size() <= 1;

        while (!nextState.getTable().hasBetRoundEnded(showdown)) {
            nextState = createBetRoundState(nextState, playerId);

            PokerPlayerState nextActivePlayer = nextState.getTable().getNextActivePlayer(playerId);
            if (nextActivePlayer != null) {  // if null, the bet round will end
                playerId = nextActivePlayer.getPlayerId();
            }
        }

        return nextState;
    }

    /**
     * Create a single state within a bet round from current state
     * @param state Current state
     * @return The next state in the betting round
     */
    private PokerState createBetRoundState(PokerState state, int playerId) {
        PokerState nextState = state.createNextState(state.getRoundNumber(), state.getBetRound());

        PokerPlayer player = getPlayer(playerId);
        PokerPlayerState playerState = nextState.getPlayerStateById(playerId);
        PokerTable table = nextState.getTable();

        sendMoveUpdatesToPlayer(playerState, nextState);
        PokerMove move = getPlayerMove(player);
        setPlayerMove(playerState, move);

        table.processMove(playerState);
        broadCastPlayerMove(playerState, nextState);

        if (move.isInvalid()) {
            player.sendWarning(move.getException().getMessage());
        }

        return nextState;
    }

    /**
     * Creates the final state with the showdown, if there is one
     * @param state Current state
     * @return The current state if no show down, else a new state with the showdown.
     */
    private PokerState createFinalHandState(PokerState state) {
        if (state.getTable().hasHandEnded()) {
            return state;  // No new state without showdown
        }

        PokerState nextState = state.createNextState(state.getRoundNumber(), state.getBetRound());
        nextState.getTable().setHandStrengths();
        nextState.getTable().putBetsInPot();

        broadCastShowdown(nextState);

        return nextState;
    }

    /**
     * Creates the final state for the whole game
     * @param state Current state
     * @return The final state in the game, with hand strengths and pot paid out
     */
    private PokerState createFinalState(PokerState state) {
        PokerState finalState = state.createNextState(state.getRoundNumber(), state.getBetRound());

        ArrayList<Integer> winnings = finalState.getTable().getPotPayout();
        broadCastWinnings(winnings, finalState, true);

        return finalState;
    }

    /**
     * Updates that are sent to all players at the start of a hand
     * @param state Current state
     */
    private void sendInitialUpdates(PokerState state) {
        PokerTable table = state.getTable();
        PokerPlayer buttonPlayer = this.playerProvider.getPlayerById(table.getButtonId());

        for (PokerPlayerState playerState : state.getAlivePlayers()) {
            PokerPlayer player = getPlayer(playerState.getPlayerId());

            player.sendUpdate("round", state.getRoundNumber());
            player.sendUpdate("small_blind", table.getSmallBlind());
            player.sendUpdate("big_blind", table.getBigBlind());
            player.sendUpdate("on_button", buttonPlayer.getName());

            for (PokerPlayerState targetPlayerState : state.getPlayerStates()) {
                PokerPlayer target = getPlayer(targetPlayerState.getPlayerId());
                player.sendUpdate("chips", target, targetPlayerState.getChips());
            }
        }
    }

    /**
     * Performs the specific actions that needs to happen for each bet round.
     * @param state Current state
     */
    private void performBetRoundActions(PokerState state) {
        PokerTable table = state.getTable();

        table.putBetsInPot();
        table.startBetRound();

        switch (state.getBetRound()) {
            case PREFLOP:
                table.payBlinds();
                table.dealCards();
                break;
            case FLOP:
                table.addTableCards(3);
                break;
            case TURN:
            case RIVER:
                table.addTableCards(1);
                break;
        }

        table.setHandOdds();
        sendBetRoundUpdates(state);
    }

    /**
     * Updates that are sent to all players at the start of the PreFlop
     * @param state Current state
     */
    private void sendBetRoundUpdates(PokerState state) {
        PokerTable table = state.getTable();

        for (PokerPlayerState playerState : state.getAlivePlayers()) {
            PokerPlayer player = getPlayer(playerState.getPlayerId());

            player.sendUpdate("bet_round", state.getBetRound().toString());

            if (state.getBetRound() == BetRound.PREFLOP) {
                player.sendUpdate("hand", player, playerState.getHandString());
            } else {
                player.sendUpdate("table", table.getTableCardsString());
            }
        }
    }

    /**
     * Updates that are sent to given player each time a move is requested
     * @param playerState PlayerState
     * @param state Current state
     */
    private void sendMoveUpdatesToPlayer(PokerPlayerState playerState, PokerState state) {
        PokerTable table = state.getTable();
        PokerPlayer player = getPlayer(playerState.getPlayerId());

        for (PokerPlayerState targetPlayerState : state.getAlivePlayers()) {
            PokerPlayer targetPlayer = getPlayer(targetPlayerState.getPlayerId());
            player.sendUpdate("bet", targetPlayer, targetPlayerState.getBet());
        }

        player.sendUpdate("pot", player, table.getPotForPlayer(playerState));
        player.sendUpdate("amount_to_call", player, table.getCallAmountForPlayer(playerState));
    }

    /**
     * Sends the move performed to all other alive players
     * @param playerState PlayerState that just performed a move
     */
    private void broadCastPlayerMove(PokerPlayerState playerState, PokerState state) {
        PokerPlayer player = getPlayer(playerState.getPlayerId());

        for (PokerPlayerState otherPlayerState : state.getAlivePlayers()) {
            if (otherPlayerState.getPlayerId() == playerState.getPlayerId()) continue;

            PokerPlayer otherPlayer = getPlayer(otherPlayerState.getPlayerId());
            otherPlayer.sendUpdate("move", player, playerState.getLastMove().toString());
        }
    }

    private void broadCastShowdown(PokerState state) {
        ArrayList<PokerPlayerState> showdownPlayers = getActuallyAlivePlayers(state).stream()
                .filter(ps -> !ps.hasFolden())
                .collect(Collectors.toCollection(ArrayList::new));

        for (PokerPlayerState playerState : state.getPlayerStates()) {
            PokerPlayer player = getPlayer(playerState.getPlayerId());

            for (PokerPlayerState targetPlayerState : showdownPlayers) {
                if (playerState.getPlayerId() == targetPlayerState.getPlayerId()) continue;

                PokerPlayer targetPlayer = getPlayer(targetPlayerState.getPlayerId());

                player.sendUpdate("hand", targetPlayer, targetPlayerState.getHandString());
            }
        }
    }

    /**
     * Sends all the winnings to every alive player
     * @param winnings List of all the winnings, indexed by ID
     * @param state Current state
     */
    private void broadCastWinnings(ArrayList<Integer> winnings, PokerState state, boolean allPlayers) {
        for (Integer winning : winnings) {
            if (winning == null || winning <= 0) continue;

            int winningId = winnings.indexOf(winning);
            PokerPlayer winningPlayer = getPlayer(winningId);

            ArrayList<PokerPlayerState> playerStates = allPlayers
                    ? state.getPlayerStates()
                    : state.getAlivePlayers();

            for (PokerPlayerState playerState : playerStates) {
                PokerPlayer player = getPlayer(playerState.getPlayerId());

                player.sendUpdate("wins", winningPlayer, winning);
            }
        }
    }

    private PokerMove getPlayerMove(PokerPlayer player) {
        String response = player.requestMove(ActionType.MOVE);
        return this.moveDeserializer.traverse(response);
    }

    private void setPlayerMove(PokerPlayerState playerState, PokerMove move) {
        playerState.setMove(move);

        if (move.isInvalid()) {  // On wrong input, player is disqualified
            playerState.setFailedInput();
            move.setMoveType(MoveType.FOLD);
        }
    }

    // Gets the players that still have chips, or not, but will get them from the pot
    private ArrayList<PokerPlayerState> getActuallyAlivePlayers(PokerState state) {
        ArrayList<PokerPlayerState> alivePlayers = state.getAlivePlayers();
        int maxHandStrength = alivePlayers.stream()
                .map(PokerPlayerState::getHandStrength)
                .max(Comparator.comparingInt(s -> s))
                .orElse(-1);

        return alivePlayers.stream()
                .filter(hp -> hp.getChips() > 0 || hp.getHandStrength() >= maxHandStrength)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PokerPlayer getPlayer(int id) {
        return this.playerProvider.getPlayerById(id);
    }
}
