package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VladVin on 17.07.2016.
 */
public class CombinatorsPredictor {
    private static final int HANDS_COUNT = 5;
    public static Combination chooseBestCombination(final List<Card> cards) {
        Combination bestCombination = null;
        List<List<Integer>> indexCombinations = generateCombinations(cards.size(), HANDS_COUNT);
        for (List<Integer> indexComb : indexCombinations) {
            List<Card> cardsComb = new ArrayList<>();
            for (int index : indexComb) {
                Card card = cards.get(index);
                cardsComb.add(card);
            }
            cardsComb = sortCards(cardsComb);
            Combination comb;
            comb = checkStraightFlush(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
                continue;
            }
            comb = checkTwoPair(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
                continue;
            }
            comb = checkOnePair(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
                continue;
            }
            comb = checkHighCard(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
            }
        }
        return bestCombination;
    }

    public static Combination predictBestCombination(final Card card1, final Card card2) {
        return null;
    }

    public static int compareTwoCombinations(final Combination combination1, final Combination combination2) {
        if (combination1.type.ordinal() < combination2.type.ordinal()) {
            // 1st combination better than 2nd one
            return 1;
        } else if (combination1.type.ordinal() > combination2.type.ordinal()) {
            // 2nd combination better than 1st one
            return -1;
        } else {
            if (combination1.priority > combination2.priority) {
                // 1st combination better than 2nd one
                return 1;
            } else if (combination1.priority < combination2.priority) {
                // 2nd combination better than 1st one
                return -1;
            } else {
                // 1st combination is equal 2nd one
                return 0;
            }
        }
    }

    private static List<List<Integer>> generateCombinations(int n, int k) {
        List<List<Integer>> combinations = new ArrayList<>();
        for (int i = 0; i < (int)Math.pow(2.0, n); i++) {
            int sumOfOnes = 0;
            List<Integer> combination = new ArrayList<>();
            int m = i;
            for (int j = 0; j < n; j++) {
                if (m % 2 == 1) {
                    combination.add(j);
                    sumOfOnes++;
                }
                m = m >> 1;
            }
            if (sumOfOnes == k) {
                combinations.add(combination);
            }
        }
        return combinations;
    }

    private static List<Card> sortCards(final List<Card> cards) {
        cards.sort((o1, o2) -> (o1.value.ordinal()) - (o2.value.ordinal()));
        return cards;
    }

    private static Combination findBestCombination(final Combination oldBestComb, final Combination newComb) {
        if (oldBestComb == null) {
            return newComb;
        }
        if (newComb.type.ordinal() < oldBestComb.type.ordinal()) {
            return newComb;
        } else if (newComb.type.ordinal() == oldBestComb.type.ordinal()) {
            if (newComb.priority > oldBestComb.priority) {
                return newComb;
            }
        }
        return oldBestComb;
    }

    private static Combination checkStraightFlush(final List<Card> cardsComb) {
        boolean hardOrdered = true;
        boolean steelWheel = false;
        for (int i = 1; i < cardsComb.size(); i++) {
            if (cardsComb.get(i).value.ordinal() - cardsComb.get(i--).value.ordinal() != 1) {
                if ((i == cardsComb.size() - 1) && cardsComb.get(i).value == Card.Value.ACE) {
                    steelWheel = true;
                }
                if (!steelWheel) {
                    hardOrdered = false;
                }
                break;
            }
        }
        if (hardOrdered) {  // Straight flush
            Combination comb = new Combination();
            comb.type = Combination.Type.STRAIGHT_FLUSH;
            if (!steelWheel) {
                comb.priority = cardsComb.get(cardsComb.size() - 1).value.ordinal();
            } else {
                comb.priority = cardsComb.get(cardsComb.size() - 2).value.ordinal();
            }
            return comb;
        }
        return null;
    }

    private static Combination checkTwoPair(final List<Card> cardComb) {
        int pairsCount = 0;
        int priority1 = 0, priority2 = 0;
        int kicker = 0;
        // Assume that XXXYY has been considered earlier
        for (int i = cardComb.size() - 1; i >= 1; i--) {
            if (cardComb.get(i).value == cardComb.get(i - 1).value) {
                if (pairsCount == 0) {
                    priority1 = cardComb.get(i).value.ordinal();
                    pairsCount++;
                } else if (pairsCount == 1) {
                    priority2 = cardComb.get(i).value.ordinal();
                    pairsCount++;
                }
            }
        }
        if (pairsCount == 2) {
            for (Card card : cardComb) {
                if (card.value.ordinal() != priority1 && card.value.ordinal() != priority2) {
                    kicker = card.value.ordinal();
                    break;
                }
            }
            Combination comb = new Combination();
            comb.type = Combination.Type.TWO_PAIR;
            comb.priority = priority1 * 10000 + priority2 * 100 + kicker;
            return comb;
        }
        return null;
    }

    private static Combination checkOnePair(final List<Card> cardComb) {
        int priority = -1;
        int kicker = 0;
        for (int i = cardComb.size() - 1; i >= 1; i--) {
            if (cardComb.get(i).value == cardComb.get(i - 1).value) {
                priority = cardComb.get(i).value.ordinal();
                if (i + 1 <= cardComb.size() - 1) {
                    kicker = cardComb.get(i + 1).value.ordinal();
                } else if (i - 2 >= 0) {
                    kicker = cardComb.get(i - 2).value.ordinal();
                }
                break;
            }
        }
        if (priority >= 0) {
            Combination comb = new Combination();
            comb.type = Combination.Type.ONE_PAIR;
            comb.priority = priority * 100 + kicker;
            return comb;
        }
        return null;
    }

    private static Combination checkHighCard(final List<Card> cardComb) {
        Combination comb = new Combination();
        comb.type = Combination.Type.HIGH_CARD;
        comb.priority = cardComb.get(cardComb.size() - 1).value.ordinal();
        return comb;
    }
}