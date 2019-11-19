package api.client;

import api.game.Action;
import api.game.Game;
import api.game.State;
import api.game.Turn;
import sjson.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Main
{
	public static BufferedReader keyboard;
	public static String playerName;
	private static AbstractAgent agent;
	private static boolean running = false;

	public static AbstractAgent getAgent()
	{
		return agent;
	}

	public static void setAgent(AbstractAgent a)
	{
		if (running)
			throw new IllegalStateException("Agent is running");
		agent = a;
	}

	private static void start(String... args) throws IOException, JSONException
	{
		if (args.length == 0)
		{
			System.out.println("Inserisci indirizzo remoto");
			String s = keyboard.readLine();
			if (s.contains(":"))
			{
				String[] split = s.split(":");
				start(split[0],split[1]);
			}
			else
				start(s);
		}
		else if (args.length == 1)
		{
			System.out.println("Inserisci porta remota"); //TODO e se ti passo ip:port? Un solo parametro ma ha già la porta!
			start(args[0],keyboard.readLine());
		}
		else if (args.length == 2)
		{
			System.out.println("Inserisci nome giocatore");
			start(args[0],args[1],keyboard.readLine());
		}
/*		else if (args.length == 3)
		{
			System.out.println("Inserisci tipo giocatore");
			start(args[0],args[1],args[2],keyboard.readLine());
		}*/
		else
		{
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			playerName = args[2];
			Socket socket = new Socket(host, port);
			PrintStream out = new PrintStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(playerName);
			out.flush();
			playerName = in.readLine();

			new Game(in);
/*
			AbstractAgent agent = null;

			if (args[3].equalsIgnoreCase("human")) //TODO controlla solo gli agent automatici
				agent = new HumanAgent();
			else if (args[3].equalsIgnoreCase("strategy1"))
				agent = new Strategy1Agent();
			else
			{
				start(args[0],args[1],args[2]);
			}
*/
			State last = new State(in);
			//		HandCardsProbability prob = new HandCardsProbability(playerName, last);

			while(!last.gameOver())
			{
	//			System.out.println(last);
				agent.notifyState(last);
				if (last.getCurrentPlayer().equals(playerName))
				{
					Action a = agent.chooseAction();
					out.print(a.toString(0));
					out.flush();
					//		System.err.println(a.toString(0));
				}
				else
					agent.notifyTurn(new Turn(in));
				last = new State(in);
			}
	//		System.out.println(last);
	//		System.out.println("Score: "+last.getScore());
		}
	}

	public static void main(String... args) throws IOException, JSONException
	{
		running = true;
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		start(args);
	}
}
