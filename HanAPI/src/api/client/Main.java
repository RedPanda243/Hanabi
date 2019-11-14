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
	public static String name;


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
			System.out.println("Inserisci porta remota");
			start(args[0],keyboard.readLine());
		}
		else if (args.length == 2)
		{
			System.out.println("Inserisci nome giocatore");
			start(args[0],args[1],keyboard.readLine());
		}
		else if (args.length == 3)
		{
			System.out.println("Inserisci tipo giocatore");
			start(args[0],args[1],args[2],keyboard.readLine());
		}
		else
		{
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			name = args[2];
			Socket socket = new Socket(host, port);
			PrintStream out = new PrintStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(name);
			out.flush();
			name = in.readLine();

			new Game(in);

			AbstractAgent agent = null;

			if (args[3].equalsIgnoreCase("human")) //TODO controlla solo gli agent automatici
				agent = new HumanAgent();
			else if (args[3].equalsIgnoreCase("strategy1"))
				agent = new Strategy1Agent();
			else
			{
				start(args[0],args[1],args[2]);
			}

			MathState last = new MathState(new State(in));
			//		HandCardsProbability prob = new HandCardsProbability(name, last);

			while(!last.gameOver())
			{
				System.out.println(last);
				agent.addHistory(last);
				if (last.getCurrentPlayer().equals(name))
				{
					Action a = agent.chooseAction(last);
					out.print(a.toString(0));
					out.flush();
					//		System.err.println(a.toString(0));
				}
				else
					System.out.println(new Turn(in));
				last = new MathState(new State(in));
			}
			System.out.println(last);
			System.out.println("Score: "+last.getScore());
		}
	}

	public static void main(String... args) throws IOException, JSONException
	{
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		start(args);
	}
}
