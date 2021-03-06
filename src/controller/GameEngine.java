package controller;

import logic.*;
import view.GameInterface;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import static logic.Action.*;

/**
 * Class designed to simulate poker game flow and control rules related
 * to the game.
 * 
 * Originally, game consists of the rounds and continues until only one player
 * remained with not empty stack (each player has a stack of values to bet).
 * Each round contains a list of actions.
 * Each round flow is the following:
 * 
 * 1. Small blind(each round new by clockwise) adds a half of the minimum bet
 * value to the bank.
 * 
 * 2. Big blind(leftmost from the small blind) adds minimum bet value to stack.
 * 
 * 3. Cardstack get mixed and each player receives 2 closed cards.
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
 * from the dealer leftmost player(in our case first time from the small blind and than from the big one).
 */
public class GameEngine {
    static final short GEERROR_INVALID_ACTIONS_ORDER = 1;
    static final short GEERROR_NOT_ENOUGH_IN_STACK = 2;
    static final short GEERROR_NOT_MATCH_OPPONENT_STACK = 3;

    /**
     * Minimum value any player can bet or raise on his turn
     */
	private int minBetValue;
	
	/**
	 * Object to display game progress in console
	 */
	private GameInterface gameInterface;

    /**
     * Random generator to mix cards
     */
    private Random cardStackGenerator;
	
	/**
	 * Array of players taking part in the game (bot and user for now).
	 * @see Bot
	 * @see User
	 */
    private Player[] players;
	
	/**
	 * Current card stack to open cards from.
	 * @see Card
	 */
    private Stack<Card> cardStack;
	
	/**
	 * Cards visible to players (maximum 5).
	 * @see Card
	 */
    private ArrayList<Card> opennedCards;
	
	/**
	 * Last action made by the opponent
	 * @see Action
	 */
    private Action lastPlayerAction;
	
	/**
	 * 	Current round bank formed from the players actions (bet, raise)
	 */
    private int bankValue;
	
	/**
	 * logic.Player's index in players list described above to make action now.
	 */
    private int actionPlayerIndex;

	/**
	 * 	Index of the player which is a small blind in current round
     */
    private int smallBlindPlayerIndex;

    /**
     *  Index of the player bet/raise last time
     */
    private int lastBetPlayerIndex;

    /**
     * Players' value which were given during recent beting
     */
    private int[] playersBetValues;

    /**
     * Minimum value player must give to stay in round
     */
    private int betValue;

    private int roundResult;

	public void startGame(int aInitStackValue, int aMinBetValue){
	    // initialize default values
		minBetValue = aMinBetValue;

        // connect game interface
        gameInterface = new GameInterface();

        // initialize players
        players = new Player[2];
        players[0] = new User();
        players[0].stack = aInitStackValue;
        players[1] = new Bot();
        players[1].stack = aInitStackValue;

        // setup bet values store
        playersBetValues = new int[2];

        // prepare card stores
        cardStackGenerator = new Random();
        cardStack = new Stack<Card>();
        opennedCards = new ArrayList<Card>();

        while(startNewRound()){}

        gameInterface.notifyUserWithMessage("Game completed!");
	}
	
	private boolean startNewRound(){
	    // small blind forwarded by clockwise
		smallBlindPlayerIndex = (smallBlindPlayerIndex + 1)%2;

        // small blind makes first action
        actionPlayerIndex = smallBlindPlayerIndex;

        // reset last bet player index
        lastBetPlayerIndex = -1;

        // set small and big blinds
        bankValue = 3*minBetValue/2;
        players[smallBlindPlayerIndex].stack -= minBetValue/2;
        players[(smallBlindPlayerIndex + 1)%2].stack -= minBetValue;

        // setup default beting state
        betValue = minBetValue;
        playersBetValues[smallBlindPlayerIndex] = minBetValue/2;
        playersBetValues[(smallBlindPlayerIndex + 1)%2] = minBetValue;

        // setup first action of big blind's bet
        lastPlayerAction = new Action();
        lastPlayerAction.type = Action.Type.BET;
        lastPlayerAction.value = minBetValue;

        // mix cards and supply players
        mixAndSetupInitialRoundCards();

        do{
            Card[] aOpenedCards = opennedCards.size() > 0 ? opennedCards.toArray(new Card[opennedCards.size()]) : new Card[0];
            gameInterface.redraw(players, aOpenedCards, lastPlayerAction, bankValue, false);
        }while(nextAction());

        // complete current round
        completeRound();

        // return flag if next round possible
        return players[0].stack > 0 && players[1].stack > 0;
    }
	
	private void mixAndSetupInitialRoundCards(){
        // generate all cards
		ArrayList<Card> cards = new ArrayList<Card>();
        for (Card.Suite suite : Card.Suite.values()){
            for(Card.Value value : Card.Value.values()){
                Card card = new Card();
                card.suite = suite;
                card.value = value;
                cards.add(card);
            }
        }

        // randomize random sequence of indexes to move cards to stack
        while (cards.size() > 0){
            int randomIndex = cardStackGenerator.nextInt(cards.size());
            cardStack.push(cards.get(randomIndex));
            cards.remove(randomIndex);
        }

        // supply players with initial cards
        for (int i = 0; i < 2; i++){
            for (Player player: players) {
                player.cards[i] = cardStack.pop();
            }
        }
	}
	
	private void openNextCards(){
		if(opennedCards.size() == 0){
		    // open flop cards
		    for(int i = 0; i < 3; i++){
		        opennedCards.add(cardStack.pop());
            }
        }else if(opennedCards.size() < 5){
            // open turn and river
            opennedCards.add(cardStack.pop());
        }
	}
	
	private boolean nextAction(){
        Player actionPlayer = players[actionPlayerIndex];

        if(lastBetPlayerIndex == actionPlayerIndex){
            // betting completed so open card or complete round
            completeBet();

            if(opennedCards.size() < 5){
                // open cards
                openNextCards();

                // start next actions from small blind
                actionPlayerIndex = (smallBlindPlayerIndex + 1)%2;
                lastPlayerAction = new Action();
                lastPlayerAction.type = Action.Type.CHECK;
                lastPlayerAction.value = 0;
                return true;
            }else{
                // make decision about who won the round
                calculateRoundResult();
                return false;
            }
        }

        // complete previous bet on big blind during preflop but allow him to start new one
        if(opennedCards.size() == 0 && lastBetPlayerIndex == -1 && actionPlayerIndex == (smallBlindPlayerIndex + 1)%2){
            completeBet();
        }

        // use counter to notify about error messages after first not valid one
        short validationResult = 0;
        if(actionPlayer.getClass() == User.class){
            // request action from user until valid one not be detected
            Action userAction = null;
            do{
                if(validationResult > 0){
                    displayErrorMessageForCode(lastPlayerAction, validationResult);
                }
                userAction = gameInterface.requestActionFromUser(lastPlayerAction);

                validationResult = validateReceivedAction(userAction);
            }while(validationResult != 0);
            lastPlayerAction = userAction;
        }else{
            Action botAction = null;
            do{
                botAction = ((Bot)actionPlayer).makeAction(lastPlayerAction, opennedCards, bankValue, minBetValue, (validationResult != 0));
                validationResult = validateReceivedAction(botAction);
            }while(validationResult != 0);
            lastPlayerAction = botAction;
        }

        // convert all in to bet or raise or call
        if(lastPlayerAction.type == Action.Type.ALL_IN) lastPlayerAction = convertCurrentPlayerAllInAction();

        // check preflop completion condition
        if((lastPlayerAction.type == Action.Type.CHECK) && (lastBetPlayerIndex == -1) &&
                (actionPlayerIndex == (smallBlindPlayerIndex + 1)%2) && (opennedCards.size() == 0)){
            completeBet();
            openNextCards();
            actionPlayerIndex = (smallBlindPlayerIndex + 1)%2;
            return true;
        }else if((lastPlayerAction.type == Action.Type.CHECK) && (lastBetPlayerIndex == -1) &&
                (actionPlayerIndex == smallBlindPlayerIndex) && (opennedCards.size() > 0)){
            completeBet();
            if(opennedCards.size() < 5){
                openNextCards();
                actionPlayerIndex = (smallBlindPlayerIndex + 1)%2;
                return true;
            }else{
                // make decision about who won the round
                calculateRoundResult();
                return false;
            }
        }

        if(lastPlayerAction.type == Action.Type.BET){
            // update bet information
            betValue = lastPlayerAction.value;
            playersBetValues[actionPlayerIndex] = lastPlayerAction.value;

            // put bet to the bank
            bankValue += betValue;

            // update player's stack
            players[actionPlayerIndex].stack -= lastPlayerAction.value;

            // update current bet index
            lastBetPlayerIndex = actionPlayerIndex;
        }else if(lastPlayerAction.type == Action.Type.RAISE){
            // update bet information
            int actualSubtractvalue = betValue - playersBetValues[actionPlayerIndex] + lastPlayerAction.value;
            betValue += lastPlayerAction.value;
            playersBetValues[actionPlayerIndex] += actualSubtractvalue;

            // put raise to bank
            bankValue += actualSubtractvalue;

            // update player's stack
            players[actionPlayerIndex].stack -= actualSubtractvalue;

            // update current bet index
            lastBetPlayerIndex = actionPlayerIndex;
        }else if(lastPlayerAction.type == Type.CALL){
            // update bet info
            int callValue = betValue - playersBetValues[actionPlayerIndex];
            playersBetValues[actionPlayerIndex] += callValue;

            // put call to bank
            bankValue += callValue;

            // update player's stack
            players[actionPlayerIndex].stack -= callValue;
        }else if(lastPlayerAction.type == Action.Type.FOLD){
            // player fault so no futher actions required
            calculateRoundResult();
            return false;
        }

        // move to next player to act
        actionPlayerIndex = (actionPlayerIndex + 1)%2;

		return true;
	}
	
	private short validateReceivedAction(Action recievedAction){
	    // convert all in action to bet or raise or check to simplify logic
	    if(recievedAction.type == Action.Type.ALL_IN){
	        // reject all in if stack is empty
	        if(players[actionPlayerIndex].stack == 0){
	            return GEERROR_NOT_ENOUGH_IN_STACK;
            }

	        recievedAction = convertCurrentPlayerAllInAction();
        }

	    switch(lastPlayerAction.type){
            case CHECK:{
                // accept: fold, check, bet, all in
                if(recievedAction.type == Action.Type.CALL || recievedAction.type == Type.RAISE){
                    return GEERROR_INVALID_ACTIONS_ORDER;
                }

                // player must have enough value to raise
                if (recievedAction.type == Action.Type.BET && players[actionPlayerIndex].stack < recievedAction.value){
                    return GEERROR_NOT_ENOUGH_IN_STACK;
                }

                // can't bet more than opponent have
                int nextPlayerIndex = (actionPlayerIndex + 1)%2;
                if(recievedAction.type == Action.Type.BET && recievedAction.value > players[nextPlayerIndex].stack){
                    return GEERROR_NOT_MATCH_OPPONENT_STACK;
                }

                break;
            }
            case BET:{
                // accept: fold, call, raise, all in
                if(recievedAction.type == Action.Type.CHECK || recievedAction.type == Action.Type.BET){
                    return GEERROR_INVALID_ACTIONS_ORDER;
                }

                if(recievedAction.type == Action.Type.RAISE){
                    int subtractValue = betValue - playersBetValues[actionPlayerIndex] + recievedAction.value;

                    // player must have enough value to raise
                    if (players[actionPlayerIndex].stack < subtractValue){
                        return GEERROR_NOT_ENOUGH_IN_STACK;
                    }

                    // opponent must have enough value
                    int nextPlayerIndex = (actionPlayerIndex + 1)%2;
                    if(players[nextPlayerIndex].stack < recievedAction.value){
                        return GEERROR_NOT_MATCH_OPPONENT_STACK;
                    }
                }

                break;
            }
            case RAISE:{
                // accept: fold, call, raise, all in
                if(recievedAction.type == Action.Type.CHECK || recievedAction.type == Action.Type.BET){
                    return GEERROR_INVALID_ACTIONS_ORDER;
                }

                if(recievedAction.type == Action.Type.RAISE){
                    int subtractValue = betValue - playersBetValues[actionPlayerIndex] + recievedAction.value;

                    // player must have enough value to raise
                    if (players[actionPlayerIndex].stack < subtractValue){
                        return GEERROR_NOT_ENOUGH_IN_STACK;
                    }

                    // opponent must have enough value
                    int nextPlayerIndex = (actionPlayerIndex + 1)%2;
                    if(players[nextPlayerIndex].stack < recievedAction.value){
                        return GEERROR_NOT_MATCH_OPPONENT_STACK;
                    }
                }

                break;
            }
            case CALL:{
                // accept bet only in preflop from the big blind
                if(recievedAction.type == Action.Type.BET && opennedCards.size() == 0 &&
                        actionPlayerIndex == (smallBlindPlayerIndex + 1)%2){
                    // player must have enough value to raise
                    if (players[actionPlayerIndex].stack < recievedAction.value){
                        return GEERROR_INVALID_ACTIONS_ORDER;
                    }
                }else if(recievedAction.type != Action.Type.CHECK && recievedAction.type != Action.Type.FOLD){
                    return GEERROR_INVALID_ACTIONS_ORDER;
                }
                break;
            }
        }

        return 0;
	}

	private  void calculateRoundResult(){
	    if(lastPlayerAction.type == Action.Type.FOLD){
            Player player = players[actionPlayerIndex];
            roundResult = player.getClass() == Bot.class ? 1 : -1;
        }else{
            // make decision about who won the round
            ArrayList<Card> userHand = (ArrayList<Card>) opennedCards.clone();
            for (int i = 0; i < 2; i++){
                userHand.add(players[0].cards[i]);
            }

            ArrayList<Card> botHand = (ArrayList<Card>) opennedCards.clone();
            for (int i = 0; i < 2; i++){
                botHand.add(players[1].cards[i]);
            }

            // calculate best cards combination for user
            Combination userCombination = CombinatorsPredictor.chooseBestCombination(userHand);

            // calculate best cards combination for bot
            Combination botCombination = CombinatorsPredictor.chooseBestCombination(botHand);

            if(userCombination != null && botCombination != null){
                // find out who has better combination
                roundResult = CombinatorsPredictor.compareTwoCombinations(userCombination, botCombination);

                // force interface redraw to open bot's cards
                Card[] aOpenedCards = opennedCards.size() > 0 ? opennedCards.toArray(new Card[opennedCards.size()]) : new Card[0];
                gameInterface.redraw(players, aOpenedCards, lastPlayerAction, bankValue, true);
                gameInterface.showRoundCombinations(userCombination, botCombination);
            }else{
                // something went wrong, so force draw
                roundResult = 0;
            }
        }
    }

	private void completeBet(){
        playersBetValues[0] = 0;
        playersBetValues[1] = 0;
        betValue = 0;
        lastBetPlayerIndex = -1;
    }
	
	private void completeRound(){
	    // add bank value to won user's stack or equal portion to each of the users in tie case
        if(roundResult == 1){
            players[0].stack += bankValue;
            gameInterface.notifyUserWithMessage("You won in the game round!");
        }else if(roundResult == -1){
            players[1].stack += bankValue;
            gameInterface.notifyUserWithMessage("You lost in the game round!");
        }else{
            players[0].stack += bankValue/2;
            players[1].stack += bankValue/2;
            gameInterface.notifyUserWithMessage("Draw!");
        }

        // reset bank value
        bankValue = 0;

        // clear players' cards
        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 2; j++) {
                players[i].cards[j] = null;
            }
        }

        // clear stack and opened cards
        cardStack.clear();
        opennedCards.clear();

        // clear last player actions
        lastPlayerAction = null;
	}

    private Action convertCurrentPlayerAllInAction(){
        // convert all in to bet or raise or call
        Action newAction;
        newAction = null;

        if(betValue == 0){
            newAction = new Action();
            newAction.type = Action.Type.BET;
            newAction.value = players[actionPlayerIndex].stack;
        }else if(playersBetValues[actionPlayerIndex] + players[actionPlayerIndex].stack == betValue){
            newAction = new Action();
            newAction.type = Action.Type.CALL;
        }else if(playersBetValues[actionPlayerIndex] + players[actionPlayerIndex].stack > betValue){
            newAction  = new Action();
            newAction.type = Action.Type.RAISE;
            newAction.value = playersBetValues[actionPlayerIndex] + players[actionPlayerIndex].stack - betValue;
        }

        return newAction;
    }

    private void displayErrorMessageForCode(Action lastBotAction, short errorCode){
        switch(errorCode){
            case GEERROR_INVALID_ACTIONS_ORDER:
                String message = "Your action must match bot's last action.";
                switch (lastBotAction.type){
                    case BET: case RAISE:
                        message = message + " Please, choose one of: '=' (call), '+'<value> (raise), 'a' (all in), 'f' (fold)";
                        break;
                    case CALL:
                        message = message + " Please, choose one of: 'c' (check), '=' (call), <value> (bet), 'a' (all in), 'f' (fold)";
                        break;
                }

                gameInterface.notifyUserWithMessage(message);
                break;
            case GEERROR_NOT_ENOUGH_IN_STACK:
                gameInterface.notifyUserWithMessage("You don't have enough points in stack to make this action. Please, try again...");
                break;
            case GEERROR_NOT_MATCH_OPPONENT_STACK:
                gameInterface.notifyUserWithMessage("Bot will not have enough points to stack to call you. Please, revise passed value to match bot's stack...");
                break;
        }
    }
}