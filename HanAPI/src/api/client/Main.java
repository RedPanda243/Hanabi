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

	private static void start(String... args)
	{
		try {
			String host = "0";
			int port = 9494;
			playerName = "Player";

			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-a")) {
					i++;
					host = args[i];
				} else if (args[i].equals("-p")) {
					i++;
					port = Integer.parseInt(args[i]);
				} else if (args[i].equals("-n")) {
					i++;
					playerName = args[i];
				}
			}

			Socket socket = new Socket(host, port);
			PrintStream out = new PrintStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(playerName);
			out.flush();
			playerName = in.readLine();

			new Game(in);

			State last = new State(in);
			//		HandCardsProbability prob = new HandCardsProbability(playerName, last);

			while (!last.gameOver()) {
//			System.out.println(last);
				agent.notifyState(last);
				if (last.getCurrentPlayer().equals(playerName)) {
					Action a = agent.chooseAction();
					out.print(a.toString(0));
					out.flush();
					//		System.err.println(a.toString(0));
				}
				agent.notifyTurn(new Turn(in));
				last = new State(in);
			}
		}
		catch(Exception e)
		{agent.log(e);}
//		System.out.println(last);
//		System.out.println("Score: "+last.getScore());

	}

	public static void main(String... args)
	{
		running = true;
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		start(args);
	}
}
