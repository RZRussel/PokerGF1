package logic;

public class Action {
	public enum Type { BET, RAISE, ALL_IN, CALL, CHECK, FOLD }
	
	public Type type;
	public int value;
}
