package agents;

import game.Action;
import hanabAI.IllegalActionException;
import game.State;

import java.net.Socket;

public class RemoteAgent extends AbstractAgent
{
	public RemoteAgent(Socket s)
	{

	}

	@Override
	public Action chooseAction(State s) throws IllegalActionException {
		return null;
	}
}
