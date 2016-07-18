package view;

import controller.GameEngine;

public class PokerTest {
	public static void main(String[] args){
		GameEngine gameEngine = new GameEngine();
		gameEngine.startGame(5000, 100);
	}
}
