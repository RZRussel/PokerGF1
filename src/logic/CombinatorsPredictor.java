package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.abs;

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
            comb = checkFourOfAKind(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
                continue;
            }
            comb = checkFullHouse(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
                continue;
            }
            comb = checkFlush(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
                continue;
            }
            comb = checkStraight(cardsComb);
            if (comb != null) {
                bestCombination = findBestCombination(bestCombination, comb);
                continue;
            }
            comb = checkThreeOfAKind(cardsComb);
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

//    public static Combination predictBestCombination(final Card card1, final Card card2) {
//        Combination comb = new Combination();
//        return comb;
//    }

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
            if (cardsComb.get(i).value.ordinal() - cardsComb.get(i - 1).value.ordinal() != 1 ||
                    cardsComb.get(i).suite != cardsComb.get(i - 1).suite) {
                if ((i == cardsComb.size() - 1) &&
                        cardsComb.get(i).value == Card.Value.ACE &&
                        cardsComb.get(0).value == Card.Value.C2 &&
                        cardsComb.get(i).suite == cardsComb.get(i - 1).suite) {
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

    private static Combination checkFourOfAKind(final List<Card> cardComb) {
        int startIdx = cardComb.size() - 1;
        if (cardComb.get(cardComb.size() - 1).value.ordinal() !=
                cardComb.get(cardComb.size() - 2).value.ordinal()) {
            startIdx--;
        }
        int priority = cardComb.get(startIdx).value.ordinal();
        boolean isHand = true;
        for (int i = startIdx; i >= 1; i--) {
            if (cardComb.get(i).value.ordinal() != cardComb.get(i - 1).value.ordinal()) {
                isHand = false;
                break;
            }
        }
        if (isHand) {
            Combination comb = new Combination();
            comb.type = Combination.Type.FOUR_OF_A_KIND;
            comb.priority = priority;
            return comb;
        }
        return null;
    }

    private static Combination checkFullHouse(final List<Card> cardsComb) {
        HashMap<Card.Value, Integer> map = new HashMap<>();
        for (Card card : cardsComb) {
            int count = (map.containsKey(card.value)) ? card.value.ordinal() : 0;
            map.put(card.value, count + 1);
        }
        if (map.size() == 2) {
            Iterator<Card.Value> it = map.keySet().iterator();
            Card.Value value1 = it.next();
            Card.Value value2 = it.next();
            boolean isFullHouse = false;
            int priority1 = -1;
            int priority2 = -1;
            if (map.get(value1) == 3 && map.get(value2) == 2) {
                priority1 = value1.ordinal();
                priority2 = value2.ordinal();
                isFullHouse = true;
            } else if (map.get(value1) == 2 && map.get(value2) == 3) {
                priority1 = value2.ordinal();
                priority2 = value1.ordinal();
                isFullHouse = true;
            }
            if (isFullHouse) {
                int priority = 100 * priority1 + priority2;
                Combination comb = new Combination();
                comb.type = Combination.Type.FULL_HOUSE;
                comb.priority = priority;
                return comb;
            }
        }
        return null;
    }

    private static Combination checkFlush(final List<Card> cardsComb) {
        Card.Suite suite = cardsComb.get(0).suite;
        boolean isFlush = true;
        for (Card card : cardsComb) {
            if (card.suite != suite) {
                isFlush = false;
                break;
            }
        }
        if (isFlush) {
            Combination comb = new Combination();
            comb.type = Combination.Type.FLUSH;
            comb.priority = cardsComb.get(cardsComb.size() - 1).value.ordinal();
            return comb;
        }
        return null;
    }

    private static Combination checkStraight(final List<Card> cardsComb) {
        boolean hardOrdered = true;
        boolean steelWheel = false;
        for (int i = 1; i < cardsComb.size(); i++) {
            if (cardsComb.get(i).value.ordinal() - cardsComb.get(i - 1).value.ordinal() != 1) {
                if ((i == cardsComb.size() - 1) &&
                        cardsComb.get(i).value == Card.Value.ACE &&
                        cardsComb.get(0).value == Card.Value.C2) {
                    steelWheel = true;
                }
                if (!steelWheel) {
                    hardOrdered = false;
                }
                break;
            }
        }
        if (hardOrdered) {  // Straight
            Combination comb = new Combination();
            comb.type = Combination.Type.STRAIGHT;
            if (!steelWheel) {
                comb.priority = cardsComb.get(cardsComb.size() - 1).value.ordinal();
            } else {
                comb.priority = cardsComb.get(cardsComb.size() - 2).value.ordinal();
            }
            return comb;
        }
        return null;
    }

    private static Combination checkThreeOfAKind(final List<Card> cardsComb) {
        int maxValueCardIdx = -1;
        for (int i = cardsComb.size() - 1; i >= cardsComb.size() - 3; i--) {
            boolean isEqual = true;
            for (int j = i - 1; j >= i - 2; j--) {
                if (cardsComb.get(j).value != cardsComb.get(i).value) {
                    isEqual = false;
                    maxValueCardIdx = i;
                    break;
                }
            }
            if (isEqual) {
                break;
            }
        }
        if (maxValueCardIdx != -1) {
            Combination comb = new Combination();
            comb.type = Combination.Type.THREE_OF_A_KIND;
            comb.priority = cardsComb.get(maxValueCardIdx).value.ordinal();
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
