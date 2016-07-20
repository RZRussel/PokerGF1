package logic;

import java.util.Random;
import java.util.ArrayList;

public class Bot extends User {
    private int bet_curr;

    public Action makeAction(Action lastUserAction, ArrayList<Card> cards, int bank, int minBetValue, boolean didLastMistake) {
        // public enum Type { STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE,
        //  FLUSH, STRAIGHT, THREE_OF_A_KIND, TWO_PAIR, ONE_PAIR, HIGH_CARD }

        Random rnd = new Random();
        Action action = new Action();
        if (didLastMistake) {
            // bot always agree for now
            if (lastUserAction.type == Action.Type.CHECK || lastUserAction.type == Action.Type.CALL) {
                action.type = Action.Type.CHECK;
            } else {
                action.type = Action.Type.CALL;
            }
            return action;

        }
        double[] cardsHandProbability = {0.999985, 0.999745, 0.998345, 0.996345, 0.992445, 0.971445, 0.923445, 0.503445, 0.390533};
        if (cards.size() == 0) bet_curr = 0;

        ArrayList<Card> allCards = new ArrayList<>(cards);
        allCards.add(this.cards[0]);
        allCards.add(this.cards[1]);

        Combination comb = CombinatorsPredictor.chooseBestCombination(allCards); //from Vlad

        double p = cardsHandProbability[comb.type.ordinal()]; // вероятность
        double win = bank * p;
        if (win < bet_curr)
            action.type = Action.Type.FOLD;
        if (bet_curr + minBetValue / 2 > win && win >= bet_curr) {
            if (rnd.nextBoolean()) {
                action.type = Action.Type.CALL;
                action.value = lastUserAction.value;
                bet_curr += action.value;
            } else action.type = Action.Type.CHECK;
        }
        if (win >= bet_curr + minBetValue / 2) {
            action.type = Action.Type.RAISE;
            double diff = win - bet_curr - minBetValue / 2;
            int z = (int) diff;
            if (z < minBetValue) {
                z = minBetValue;
            } else
                z -= z % minBetValue;

            bet_curr += z;
            action.value = z;

        }
        return action;
    }
}
