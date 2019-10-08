package agents;

import api.game.Action;
import api.game.State;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAgent
{
	public abstract void addHistory(State state);

	public abstract Action chooseAction(State current);
}
