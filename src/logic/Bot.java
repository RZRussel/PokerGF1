package logic;

public class Bot extends User {
	public Action makeAction(Action lastUserAction, Card[] cards, int bank){
		Action action = new Action();

		// bot always agree for now
		if(lastUserAction.type == Action.Type.CHECK || lastUserAction.type == Action.Type.CALL){
			action.type = Action.Type.CHECK;
		}else{
			action.type = Action.Type.CALL;
		}

		return action;
	}
}
