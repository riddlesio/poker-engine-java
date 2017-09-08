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

import spock.lang.Specification

/**
 * io.riddles.poker.game.table.PotSpec - Created on 6-9-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
class PotSpec extends Specification {

    def "test getTotalPotForParcipant 2 players"() {
        setup:
        ArrayList<Integer> participants1 = new ArrayList<>()
        participants1.add(0)
        participants1.add(1)

        ArrayList<Integer> participants2 = new ArrayList<>()
        participants2.add(0)
        Pot pot = new Pot(participants1)
        pot.createSidePot(participants2)

        when:
        pot.setChips(1000)
        pot.getSidePot().setChips(500)
        int amount1 = pot.getTotalPotForParcipant(0, new ArrayList<>(), true)

        then:
        amount1 == 1500
        pot.getChips() == 0
        pot.getSidePot().getChips() == 0

        when:
        pot.setChips(1000)
        pot.getSidePot().setChips(500)
        ArrayList<Integer> sharedWith1 = new ArrayList<>()
        sharedWith1.add(1)
        int amount2 = pot.getTotalPotForParcipant(0, sharedWith1, true)
        int amount3 = pot.getTotalPotForParcipant(1, new ArrayList<>(), true)

        then:
        amount2 == 1000
        amount3 == 500
        pot.getChips() == 0
        pot.getSidePot().getChips() == 0

        when:
        pot.setChips(1000)
        pot.getSidePot().setChips(500)
        int amount4 = pot.getTotalPotForParcipant(1, new ArrayList<>(), true)
        int amount5 = pot.getTotalPotForParcipant(0, new ArrayList<>(), true)

        then:
        amount4 == 1000
        amount5 == 500
        pot.getChips() == 0
        pot.getSidePot().getChips() == 0

        when:
        pot.setChips(1000)
        pot.getSidePot().setChips(500)
        ArrayList<Integer> sharedWith2 = new ArrayList<>()
        sharedWith2.add(0)
        int amount6 = pot.getTotalPotForParcipant(1, sharedWith2, true)
        int amount7 = pot.getTotalPotForParcipant(0, new ArrayList<>(), true)

        then:
        amount6 == 500
        amount7 == 1000
        pot.getChips() == 0
        pot.getSidePot().getChips() == 0
    }
}
