package agents;

import hanabAI.Action;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class BasicAgent extends AbstractAgent
{

	@Override
	public Action chooseAction(State s) throws IllegalActionException
	{
		getHints(s);
		Action a = playKnown(s);
		if(a==null) a = discardKnown(s);
		if(a==null) a = hintPlayable(s);
		if(a==null) a = playGuess(s);
		if(a==null) a = discardGuess(s);
		if(a==null) a = hintRandom(s);
		return a;
	}
}
