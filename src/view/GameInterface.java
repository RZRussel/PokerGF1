package view;

import logic.Action;
import logic.Card;
import logic.Player;

public class GameInterface {
	public void redraw(Player[] players, Card[] openedCards, Action lastAction, int bank){
		
	}
	
	public Action requestActionFromUser(Action botAction){
		return null;
	}
	
	public void notifyUserWithMessage(String message){
		
	}
}
