package agents;

import api.game.Action;
import api.game.State;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAgent
{
	List<State> history;

	public AbstractAgent()
	{
		history = new ArrayList<>();
	}

	public void addHistory(State state)
	{
		history.add(state);
	}

	public abstract Action chooseAction(State current);
}
