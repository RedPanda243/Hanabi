package api.main;

import api.agents.Agent;
import api.game.Game;
import api.game.State;
import sjson.JSONUtils;

import java.io.*;
import java.net.Socket;

@Deprecated
public class Main
{

	public static Game game;
	public static State currentState;

	/**
	 * Usage: java -jar *.jar "address" "port" ["agent_class"]
	 * @param args
	 */
	public static void main(String args[]) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String ip;
		int port;
		Agent agent = null;
		if (args.length<2)
		{
			System.out.println("Inserisci indirizzo tcp (ip:tcp)");
			String[] box = br.readLine().split(":");
			ip = box[0];
			if (box.length < 2 || box[1].equals(""))
			{
				System.out.println("Inserisci la porta");
				port = Integer.parseInt(br.readLine());
			}
			else
				port = Integer.parseInt(box[1]);
		}
		else
		{
			ip = args[0];
			port = Integer.parseInt(args[1]);
		}

		Class<? extends Agent> agent_class;
		Class clpar[] = {int.class,String[].class};
		Socket socket = new Socket(ip,port);
		PrintStream out = new PrintStream(socket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		int index = Integer.parseInt(in.readLine());
		if (args.length>2)
		{
			agent_class = (Class<? extends Agent>) Class.forName(args[2]);
			agent = agent_class.getConstructor(clpar).newInstance(index,new String[0]);
		}
		else
		{
			//TODO cerca con reflection tutti i figli di Agent, stampa la lista e fai scegliere all'utente.
		}
		/*
			TODO
			prevedi un meccanismo per introdurre parametri (stringhe) di costruzione. Un agent quindi dovra implementare un
			costruttore del tipo public Agent(String... pars)
		 */

		out.println(agent.getName());
		out.flush();

		game = JSONUtils.fromStream(Game.class,socket.getInputStream());

		currentState = JSONUtils.fromStream(State.class,socket.getInputStream());
		while(!currentState.gameOver())
		{
			if (currentState.getCurrentPlayer() == agent.getIndex())
			{

				out.print(agent.doAction());
				out.flush();
			}
			else
			{

			}
		}
	}
}
