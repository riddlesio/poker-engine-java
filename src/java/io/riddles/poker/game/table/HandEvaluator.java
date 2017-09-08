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

import com.stevebrecher.HandEval;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import io.riddles.poker.engine.PokerEngine;
import io.riddles.poker.game.table.card.Card;
import io.riddles.poker.game.table.card.Deck;

/**
 * io.riddles.poker.game.table.HandEvaluator - Created on 31-8-17
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class HandEvaluator {

    private static final int ODDS_RUNS = 1000;  // Amount of simulations for odds calculation
    private static final int ODDS_DECIMALS = 1;  // Decimal places in the odds

    /**
     * Calculates the strength of a hand with the cards on the table.
     * 5 cards on the table are assumed.
     * @param hand Hand of cards
     * @param table 5 table cards
     * @return The valuation of the given hand
     */
    public static int getHandStrength(ArrayList<Card> hand, ArrayList<Card> table) {
        if (table.size() != 5) {
            throw new RuntimeException("Table needs to contain exactly 5 cards");
        }

        switch (PokerEngine.POKER_TYPE) {
            case TEXASHOLDEM:
                return getTexasHoldEmHandStrength(hand, table);
            case OMAHA:
                return getOmahaHandStrength(hand, table);
            default:
                throw new RuntimeException("Can't evaluate poker type " + PokerEngine.POKER_TYPE);
        }
    }

    /**
     * Convert strength number to category enum
     * @param strength Hand strength
     * @return Category
     */
    public static HandEval.HandCategory handStrengthToCategory(int strength) {
        return HandEval.HandCategory.values()[strength >> HandEval.VALUE_SHIFT];
    }

    /**
     * Gets the odds for each hand to win on the table by running simulations
     * @param hands Hands for each bot, indexed by the bot ID
     * @param table The current table, with any number of cards
     * @param deck The current deck
     * @return A list of odds for each bot to win the hand, indexed by bot ID
     */
    public static ArrayList<Double> getHandOdds(ArrayList<ArrayList<Card>> hands,
                                                ArrayList<Card> table, Deck deck, int playerCount) {
        double[] winsPerBot = new double[playerCount];

        // If table has 5 cards, we don't have to do all the simulations
        if (table.size() == 5) {
            ArrayList<Integer> winnerIds = getHandWinnerIds(hands, table);
            for (Integer winnerId : winnerIds) {
                winsPerBot[winnerId] = ODDS_RUNS / ((double) winnerIds.size());
            }
        } else {

            // Run simulations by drawing random cards from the deck until table is full
            // and do this ODDS_RUNS times
            for (int i = 0; i < ODDS_RUNS; i++) {
                Deck simulationDeck = new Deck(deck);
                simulationDeck.shuffle();
                ArrayList<Card> simulationTable = new ArrayList<>(table);

                while (simulationTable.size() < 5) {
                    simulationTable.add(simulationDeck.nextCard());
                }

                ArrayList<Integer> winnerIds = getHandWinnerIds(hands, simulationTable);
                for (Integer winnerId : winnerIds) {
                    winsPerBot[winnerId] += 1 / ((double) winnerIds.size());
                }
            }
        }

        // Calculate the odds for each bot rounded to ODDS_DECIMALS
        double[] oddsArray = new double[playerCount];
        for (int i = 0; i < winsPerBot.length; i++) {
            double odds = (winsPerBot[i] / ((double) ODDS_RUNS)) * 100.0;
            int scale = (int) Math.pow(10, ODDS_DECIMALS);
            double roundedOdds = (double) Math.round(odds * scale) / scale;

            oddsArray[i] = roundedOdds;
        }

        return DoubleStream.of(oddsArray).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    private static int getTexasHoldEmHandStrength(ArrayList<Card> hand, ArrayList<Card> table) {
        if (hand.size() != 2) {
            throw new RuntimeException("Hand needs to contain exactly 2 cards");
        }

        long handCode = Stream.concat(hand.stream(), table.stream())
                .mapToLong(Card::getCode)
                .sum();

        return HandEval.hand7Eval(handCode);
    }

    private static int getOmahaHandStrength(ArrayList<Card> hand, ArrayList<Card> table) {
        if (hand.size() != 4) {
            throw new RuntimeException("Hand needs to contain exactly 4 cards");
        }

        int strength = 0;

        for (int i = 0; i < hand.size() - 1; i++) {
            for (int j = i + 1; j < hand.size(); j++) {
                for (int k = 0; k < table.size() - 2; k++) {
                    for (int l = k + 1; l < table.size() - 1; l++) {
                        for (int m = l + 1; m < table.size(); m++) {
                            long handCode = hand.get(i).getCode()
                                    + hand.get(j).getCode()
                                    + table.get(k).getCode()
                                    + table.get(l).getCode()
                                    + table.get(m).getCode();

                            strength = Math.max(strength, HandEval.hand5Eval(handCode));
                        }
                    }
                }
            }
        }

        return strength;
    }

    private static ArrayList<Integer> getHandWinnerIds(ArrayList<ArrayList<Card>> hands,
                                                       ArrayList<Card> table) {
        int maxStrength = -1;
        ArrayList<Integer> winnerIds = new ArrayList<>();

        for (ArrayList<Card> hand : hands) {
            if (hand == null || hand.isEmpty()) continue;

            int strength = getHandStrength(hand, table);

            if (strength > maxStrength) {
                maxStrength = strength;
                winnerIds.clear();
                winnerIds.add(hands.indexOf(hand));
            } else if (strength == maxStrength) {
                winnerIds.add(hands.indexOf(hand));
            }
        }

        return winnerIds;
    }
}
