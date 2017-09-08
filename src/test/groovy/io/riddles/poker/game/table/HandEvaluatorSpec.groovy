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

import com.stevebrecher.HandEval
import io.riddles.poker.engine.PokerEngine
import io.riddles.poker.game.PokerType
import io.riddles.poker.game.table.card.Card
import spock.lang.Specification

/**
 * io.riddles.poker.game.table.HandEvaluatorSpec - Created on 6-9-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
class HandEvaluatorSpec extends Specification {

    def "test Texas Hold'em hands"() {
        setup:
        PokerEngine.POKER_TYPE = PokerType.TEXASHOLDEM

        when:
        ArrayList<Card> hand1 = new ArrayList<>()
        ArrayList<Card> hand2 = new ArrayList<>();
        ArrayList<Card> table1 = new ArrayList<>()
        hand1.add(new Card(26))
        hand1.add(new Card(39))
        hand2.add(new Card(5))
        hand2.add(new Card(31))
        table1.add(new Card(13))
        table1.add(new Card(2))
        table1.add(new Card(12))
        table1.add(new Card(36))
        table1.add(new Card(45))
        int strength1 = HandEvaluator.getHandStrength(hand1, table1)
        int strength2 = HandEvaluator.getHandStrength(hand2, table1)
        HandEval.HandCategory category1 = HandEvaluator.handStrengthToCategory(strength1)
        HandEval.HandCategory category2 = HandEvaluator.handStrengthToCategory(strength2)

        then:
        category1 == HandEval.HandCategory.PAIR
        category2 == HandEval.HandCategory.PAIR
        strength2 > strength1

        when:
        ArrayList<Card> hand3 = new ArrayList<>()
        ArrayList<Card> hand4 = new ArrayList<>();
        ArrayList<Card> table2 = new ArrayList<>()
        hand3.add(new Card(12))
        hand3.add(new Card(11))
        hand4.add(new Card(7))
        hand4.add(new Card(6))
        table2.add(new Card(10))
        table2.add(new Card(9))
        table2.add(new Card(8))
        table2.add(new Card(30))
        table2.add(new Card(24))
        int strength3 = HandEvaluator.getHandStrength(hand3, table2)
        int strength4 = HandEvaluator.getHandStrength(hand4, table2)
        HandEval.HandCategory category3 = HandEvaluator.handStrengthToCategory(strength3)
        HandEval.HandCategory category4 = HandEvaluator.handStrengthToCategory(strength4)

        then:
        category3 == HandEval.HandCategory.STRAIGHT_FLUSH
        category4 == HandEval.HandCategory.STRAIGHT_FLUSH
        strength3 > strength4
    }
}
