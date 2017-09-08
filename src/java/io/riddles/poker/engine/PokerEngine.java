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

package io.riddles.poker.engine;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import io.riddles.javainterface.configuration.Configuration;
import io.riddles.javainterface.engine.AbstractEngine;
import io.riddles.javainterface.engine.GameLoopInterface;
import io.riddles.javainterface.engine.SimpleGameLoop;
import io.riddles.javainterface.exception.TerminalException;
import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.io.IOInterface;
import io.riddles.poker.game.PokerSerializer;
import io.riddles.poker.game.PokerType;
import io.riddles.poker.game.RaiseLimitType;
import io.riddles.poker.game.player.PokerPlayer;
import io.riddles.poker.game.processor.PokerProcessor;
import io.riddles.poker.game.state.PokerPlayerState;
import io.riddles.poker.game.state.PokerState;

/**
 * io.riddles.poker.engine.GeneralsEngine - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class PokerEngine extends AbstractEngine<PokerProcessor, PokerPlayer, PokerState> {

    public static PokerType POKER_TYPE;
    public static RaiseLimitType RAISE_LIMIT_TYPE;
    public static SecureRandom RANDOM;

    public PokerEngine(PlayerProvider<PokerPlayer> playerProvider, IOInterface ioHandler) throws TerminalException {
        super(playerProvider, ioHandler);
    }

    @Override
    protected Configuration getDefaultConfiguration() {
        Configuration configuration = new Configuration();

        configuration.put("initialStack", 2000);
        configuration.put("initialBigBlind", 60);  // starting big blind or small bet limit
        configuration.put("handsPerBlindLevel", 10);
        configuration.put("raiseLimitType", "noLimit"); // no raise limit
        configuration.put("pokerType", "TexasHoldEm");
        configuration.put("seed", UUID.randomUUID().toString());

        return configuration;
    }

    @Override
    protected PokerProcessor createProcessor() {
        return new PokerProcessor(this.playerProvider);
    }

    @Override
    protected GameLoopInterface createGameLoop() {
        return new SimpleGameLoop();
    }

    @Override
    protected PokerPlayer createPlayer(int id) {
        return new PokerPlayer(id);
    }

    @Override
    protected void sendSettingsToPlayer(PokerPlayer player) {
        player.sendSetting("initial_stack", configuration.getInt("initialStack"));
        player.sendSetting("initial_big_blind", configuration.getInt("initialBigBlind"));
        player.sendSetting("hands_per_blind_level", configuration.getInt("handsPerBlindLevel"));
    }

    @Override
    protected PokerState getInitialState() {
        setRandomSeed();

        POKER_TYPE = PokerType.fromString(configuration.getString("pokerType"));
        RAISE_LIMIT_TYPE = RaiseLimitType.fromString(configuration.getString("raiseLimitType"));

        int initialStack = configuration.getInt("initialStack");

        ArrayList<PokerPlayerState> playerStates = this.playerProvider.getPlayers().stream()
                .map(player -> new PokerPlayerState(player.getId(), initialStack))
                .collect(Collectors.toCollection(ArrayList::new));

        PokerState initialState = new PokerState(playerStates);
        initialState.getTable().resetTable();

        return initialState;
    }

    @Override
    protected String getPlayedGame(PokerState initialState) {
        PokerSerializer serializer = new PokerSerializer();
        return serializer.traverseToString(this.processor, initialState);
    }

    private void setRandomSeed() {
        try {
            RANDOM = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.severe("Not able to use SHA1PRNG, using default algorithm");
            RANDOM = new SecureRandom();
        }
        String seed = configuration.getString("seed");
        LOGGER.info("RANDOM SEED IS: " + seed);
        RANDOM.setSeed(seed.getBytes());
    }
}
