package api.client;

import api.game.Action;
import api.game.State;
import api.game.Turn;

public abstract class AbstractAgent
{
	public abstract Action chooseAction();

	public abstract void notifyState(State state);

	public abstract void notifyTurn(Turn turn);
}
