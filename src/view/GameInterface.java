package view;

import logic.Action;
import logic.Card;
import logic.Player;
import java.util.Scanner;

public class GameInterface {
	private static final String[] strSuit = {"\u2665", "\u2666", "\u2663", "\u2660"};
	private static final String[] strName = {"2 ", "3 ", "4 ", "5 ", 
	       "6 ", "7 ", "8 ", "9 ", "10", "J ", "Q ", "K ", "A "};
	// represent card as string       
	private String toString(Card c) {
		return strSuit[c.suite.ordinal()] + strName[c.value.ordinal()];
	}
	// show cards on the table
	private String ShowTable(Card[] openCards) {
		String cards = "";
		for(int i = 0; i < 5; ++i) {
			cards += (i < openCards.length) ? toString(openCards[i]) : "XX ";
			cards += " ";
		}
		return "                           -= " + cards + " =-"; 
	}
	// show current bunk
	private String ShowBank(int bank) {
		return String.format("                                  Bank: %d ", bank);
	}
	// represent current cards and points of the players
	private String ShowPlayerState(Player[] pl, boolean showBot) {
		return String.format(
		"                       You:                       Bot: \n" +
		"Cards:            %11s                %11s \n" +
		"Stack:                %4d                       %4d\n",
		toString(pl[0].cards[0]) + " " + toString(pl[0].cards[1]),
		showBot ?  toString(pl[1].cards[0]) + " " + toString(pl[1].cards[1]) : "XX  XX ",
		pl[0].stack, pl[1].stack);
	}
    // refresh information about state of players
	public void redraw(Player[] players, Card[] openedCards, Action lastAction, int bank, boolean showsBotCards){
		System.out.println(ShowTable(openedCards));
        System.out.println(ShowBank(bank));
        System.out.println(ShowPlayerState(players, showsBotCards));

	}
	// ask user action
	public Action requestActionFromUser(Action botAction){
        Scanner sn = new Scanner(System.in);
        Action ac = new Action();

        System.out.print("Bot turn is " + botAction.type.toString());
        if(botAction.type == Action.Type.RAISE)
            System.out.println(" on " + botAction.value + ".");
        else
            System.out.println(".");

        while(true) {
            System.out.print("You turn('c' - check, 'f' - fold, '=' - call, '+<value>' - bet/raise): ");
            String ans = sn.nextLine().trim().toLowerCase();
            String tok[] = ans.split(" ");

            if(tok.length > 0) {
                if(tok[0].equals("c")) {
                    ac.value = 0;
                    ac.type = Action.Type.CHECK;
                    return ac;
                }
                else if(tok[0].equals("f")) {
                    ac.value = 0;
                    ac.type = Action.Type.FOLD;
                    return ac;
                }
                else if(tok[0].equals("a")) {
                    ac.value = 0;
                    ac.type = Action.Type.ALL_IN;
                    return ac;
                }
                else if(tok[0].equals("=")) {
                    ac.value = botAction.value;
                    ac.type = Action.Type.CALL;
                    return ac;
                }
                else if(Character.isDigit(tok[0].charAt(0))) {
                    int getBet = Integer.valueOf(tok[0]).intValue();
                    if(getBet < 100 || getBet % 100 > 0) {
                        System.out.println("Must be divisible by 100");
                        continue;
                    }
                    ac.value = getBet;
                    ac.type = Action.Type.BET;
                    return ac;
                }
                else if(tok[0].charAt(0) == '+') {
                    String firstTok = (tok.length > 1) ? tok[0] + tok[1] : tok[0];
                    int getVal = Integer.valueOf(firstTok.substring(1,firstTok.length())).intValue() + botAction.value;
                    if(getVal < 100 || getVal % 100 > 0) {
                        System.out.println("Must be divisible by 100");
                        continue;
                    }
                    ac.value = getVal;
                    ac.type = Action.Type.RAISE;
                    return ac;
                }
                else if(tok[0].equals("?") || tok[0].equals("h") || tok[0].equals("help")) {
                    System.out.println("Some help...");
                    continue;
                }
            }

            System.out.println("Unexpected command!");
        }
	}
	// show message
	public void notifyUserWithMessage(String message){
		System.out.println(message);
	}

    public void Test() {

        // redraw

        Player players[] = new Player[] {new Player(), new Player()};
        players[0].cards = new Card[] {new Card(), new Card(), new Card()};
        players[0].stack = 500;
        for(int i = 0; i < 2; ++i) {
            players[0].cards[i].suite = Card.Suite.DIAMOND;
            players[0].cards[i].value = Card.Value.C4;
        }
        players[1].cards = new Card[] {new Card(), new Card(), new Card()};
        players[1].stack = 700;
        for(int i = 0; i < 2; ++i) {
            players[1].cards[i].suite = Card.Suite.DIAMOND;
            players[1].cards[i].value = Card.Value.JACK;
        }

        Card[] opened = new Card[] {new Card(), new Card()};
        opened[0].suite = Card.Suite.SPADE;
        opened[0].value = Card.Value.ACE;
        opened[1].suite = Card.Suite.DIAMOND;
        opened[1].value = Card.Value.C7;

        redraw(players, opened, null, 400, false);

        // requestAction

        Action ac = new Action();
        ac.type = Action.Type.RAISE;
        ac.value = 100;

        Action ans = requestActionFromUser(ac);

        System.out.println(ans.type.toString());
        System.out.println(ans.value);
    }
}
