import java.util.ArrayList;
import java.util.Stack;

/**
 * Class designed to simulate poker game flow and control rules related
 * to the game.
 * @see startGame(int aInitStackValue, int aMinBetValue).
 * 
 * Originally, game consists of the rounds and continues until only one player
 * remained with not empty stack (each player has a stack of values to bet).
 * Each round contains a list of actions.
 * Each round flow is the following:
 * 
 * 1. Small blind(each round new by clockwise) adds a half of the minimum bet
 * bet value to the bank.
 * 
 * 2. Big blind(leftmost from the big blind) adds minimum bet value to stack.
 * 
 * 3. Card stack mixes and each player receives 2 closed cards.
 * 
 * 5. Each player starting from leftmost to big blind make action:
 * Call - add to bank the same value last player bet.
 * Bet - add to bank value >= then minimum bet to force next players call or raise or fold.
 * Raise - add to bank value > then last bet value.
 * Check - asks to watch next card on the table and applicable when
 * no betting in progress.
 * Fold - rejects bet and excludes from the round.
 * 
 * 6. If there is more than one player remained when betting ends than
 * next cards(after 1st bet 3 cards, and than 1 by 1 to max 5) gets opened and betting restarts
 * from the dealer leftmost player(in our case just not a dealer player).
 */
public class GameEngine {
	public int initStackValue;
	public int minBetValue;
	
	/**
	 * Object to display game progress in console
	 */
	GameInterface gameInterface;
	
	/**
	 * Array of players taking part in the game (bot and user for now).
	 * @see Bot
	 * @see User
	 */
	Player[] players;
	
	/**
	 * Current card stack to open cards from.
	 * @see Card.
	 */
	Stack<Card> cardStack;
	
	/**
	 * Cards visible to players (maximum 5).
	 * @see Card.
	 */
	Card[] opennedCards;
	
	/**
	 * Actions made by the players in current round. This list would be reseted
	 * when round completes.
	 * @see Action.
	 */
	ArrayList<Action> madeActions;
	
	/**
	 * 	Current round bank formed from the players actions (bet, raise)
	 */
	int bankValue;
	
	/**
	 * Player's index in players list described above to make action now.
	 */
	int actionPlayerIndex;
	
	public void startGame(int aInitStackValue, int aMinBetValue){
		initStackValue = aInitStackValue;
		minBetValue = aMinBetValue;
	}
	
	private void startNewRound(){
		
	}
	
	private void mixAndSetupInitialRoundCards(){
		
	}
	
	private void openNextCard(){
		
	}
	
	private boolean nextAction(){
		return false;
	}
	
	private short validateReceivedAction(){
		return 0;
	}
	
	private void completeRound(){
		
	}
}
