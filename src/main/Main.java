package main;

import agents.AbstractAgent;
import agents.BasicAgent;
import agents.HumanAgent;
import hanabAI.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

	public int play(boolean wait)
	{
		if (state == null)
			throw new IllegalStateException("Game not initialized");
		running = true;
//		log.append(state).append("\n");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try{
			while(!state.gameOver()){
				log.append(state.toString());
				int p = state.getNextPlayer();
				State localState = state.hideHand(p);
				state = state.nextState(players[p].doAction(localState),deck);
				if (wait) {
					log.append("\nPress ENTER to see next turn...\n");
					System.out.print(log);
					log = new StringBuffer();
					try {
						br.readLine();
					}catch(IOException e){}
				}
			}
			running = false;
			return state.getScore();
		}
		catch(IllegalActionException e){
			running = false;
			e.printStackTrace();
			return -1;
		}
	}

	public static void main(String[] args){
//		Agent[] agents = {new agents.AbstractAgent(),new agents.AbstractAgent(), new agents.AbstractAgent(), new agents.AbstractAgent(), new agents.AbstractAgent()};
		Agent[] agents = {new BasicAgent(),new BasicAgent()};
		Main game= new Main(agents);
		int result = game.play(true);
		game.log.append("The final score is "+result+".\n");
		game.log.append(Hanabi.critique(result));
		System.out.print(game.log);
	}
}
