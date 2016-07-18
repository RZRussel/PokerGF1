package logic;

public class Card {
	public enum Suite { HEART, DIAMOND, CLUBS, SPADE }
	public enum Value { C2, C3, C4, C5, C6, C7, C8, C9, C10, JACK, QUEEN, KING, ACE }
	
	public Suite suite;
	public Value value;
}
