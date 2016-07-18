package logic;

public class Bot extends User {
	public Action makeAction(Action lastUserAction, Card[] cards, int bank){
		Action action = new Action();

		if(lastUserAction.type == Action.Type.CHECK){
			action.type = Action.Type.CHECK;
		}else{
			action.type = Action.Type.CALL;
		}

		return action;
	}
}
