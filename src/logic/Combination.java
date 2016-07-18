package logic;

/**
 * Created by VladVin on 17.07.2016.
 */
public class Combination {
    public enum Type { STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE,
        FLUSH, STRAIGHT, THREE_OF_A_KIND, TWO_PAIR, ONE_PAIR, HIGH_CARD }

    public Type type;
    public int priority;
}
