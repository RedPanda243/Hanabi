package main;

import agents.Agent;
import agents.BasicAgent;
import agents.HumanAgent;
import game.Action;
import game.Card;
import game.IllegalActionException;
import game.State;

public class Main
{

	private Agent[] players;
	private State state;
	private java.util.Stack<Card> deck;
	private StringBuffer log;
	private boolean running;

	public Main()
	{
		players = null;
		state = null;
		deck = null;
		running = false;
	}

	public Main(Agent[] agents)
	{
		this();
		init(agents);
	}

	public void init(Agent[] agents) throws IllegalStateException
	{
		if (running)
			throw new IllegalStateException("Game running");
		players = agents;
		deck = Card.shuffledDeck();
/*		String[] s = new String[agents.length];
		for(int i=0; i<s.length; i++)s[i] = agents[i].toString();*/
		state = new State(agents, deck);
		log = new StringBuffer();
	}
/*
	public int play(boolean turnlog)
	{
		if (state == null)
			throw new IllegalStateException("Game not initialized");
		running = true;
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Action a;
		for (Agent agent:players)
			agent.init(state);
		try{
			while(!state.gameOver()){
				int p = state.getCurrentPlayer();
				log.append("Player ").append(p).append(" turn\n");
				log.append(state.toString());
				if (turnlog)
					printLog();
				a = players[p].doAction(state);
				log.append(a).append("\n\n");
				state = state.nextState(a,deck);
			}
			log.append(state).append("\n");
			running = false;
			return state.getScore();
		}
		catch(IllegalActionException e){
			running = false;
			e.printStackTrace();
			return -1;
		}
	}

	private void printLog()
	{
		System.out.print(log);
		log = new StringBuffer();
	}

	public static void main(String[] args){
//		Agent[] agents = {new agents.Agent(),new agents.Agent(), new agents.Agent(), new agents.Agent(), new agents.Agent()};
		Agent[] agents = {new HumanAgent(),new BasicAgent()};
		boolean turnlog = true;
		Main game= new Main(agents);
		int result = game.play(turnlog);
		game.log.append("The final score is "+result+".\n");
		game.log.append(Hanabi.critique(result));
		System.out.print(game.log);
	}

 */
}
