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

package io.riddles.poker;

import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.io.IOHandler;
import io.riddles.poker.engine.PokerEngine;
import io.riddles.poker.game.state.PokerState;

/**
 * io.riddles.poker.Poker - Created on 29-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Poker {

    public static void main(String[] args) throws Exception {
        PokerEngine engine = new PokerEngine(new PlayerProvider<>(), new IOHandler());

        PokerState firstState = engine.willRun();
        PokerState finalState = engine.run(firstState);

        engine.didRun(firstState, finalState);
    }
}
