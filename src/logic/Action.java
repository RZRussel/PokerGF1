package logic;

public class Action {
	public static enum Type { BET, RAISE, CALL, CHECK, FAULT }
	
	public Type type;
	public int value;
}
