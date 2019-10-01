package agents;

import api.game.*;
import sjson.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class HumanAgent
{
	private static BufferedReader keyboard;
	private static String name;

/*
	public HumanAgent(String name, int index)
	{
		br = new BufferedReader(new InputStreamReader(System.in));
		playerName = name;
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Action doAction()
	{
		System.out.println("\nChoose one action: (play <cardnum> | discard <cardnum> | hint <playernum> (<color>|<value>))");
		Action action = null;
		while(action == null)
		{
			try
			{
				String[] parts = br.readLine().split(" ");
				if (parts[0].equals("play"))
				{
					action = play(Integer.parseInt(parts[1]));
				}
				else if (parts[0].equals("discard"))
				{
					action =  discard(Integer.parseInt(parts[1]));
				}
				else if (parts[0].equals("hint"))
				{
					try
					{
						action = hint(Integer.parseInt(parts[2]),Integer.parseInt(parts[1]));
					}
					catch(NumberFormatException nfe)
					{
						action = hint(Color.fromString(parts[2]),Integer.parseInt(parts[1]));
					}
				}
				else {
					System.out.println("unrecognized, retry");
				}
			}
			catch (IOException ioe){ioe.printStackTrace(System.err); System.exit(1);}
			catch(Exception e){e.printStackTrace(System.err);}
		}
		return action;
	}

	@Override
	public String getName()
	{
		return playerName;
	}
 */

	private static Action chooseAction()
	{
		System.out.println("\nChoose one action: (play <cardnum> | discard <cardnum> | hint <playernum> (<color>|<value>))");
		Action action = null;
		while(action == null)
		{
			try
			{
				String[] parts = keyboard.readLine().split(" ");
				if (parts[0].equals("play"))
					action = new Action(name,ActionType.PLAY,Integer.parseInt(parts[1]));
				else if (parts[0].equals("discard"))
				{
					action = new Action(name,ActionType.DISCARD,Integer.parseInt(parts[1]));
				}
				else if (parts[0].equals("hint"))
				{
					String hinted;
					try
					{
						hinted = Game.getInstance().getPlayer(Integer.parseInt(parts[1]));
					}
					catch (NumberFormatException e)
					{
						hinted = parts[1];
					}
					try
					{
						action = new Action(name,hinted,Integer.parseInt(parts[2]));
					}
					catch(NumberFormatException nfe)
					{
						action = new Action(name,hinted,Color.fromString(parts[2]));
					}
				}
				else {
					System.out.println("unrecognized, retry");
				}
			}
			catch (IOException ioe){ioe.printStackTrace(System.err); System.exit(1);}
			catch(Exception e){e.printStackTrace(System.err);}
		}
		return action;
	}

	/**
	 *
	 * @param args 0-host, 1-port, 2-name
	 */
	public static void main(String... args) throws IOException,JSONException
	{
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		if (args.length == 0)
		{
			System.out.println("Inserisci indirizzo remoto");
			String s = keyboard.readLine();
			if (s.contains(":"))
			{
				String[] split = s.split(":");
				main(split[0],split[1]);
			}
			else
				main(s);
		}
		else if (args.length == 1)
		{
			System.out.println("Inserisci porta remota");
			main(args[0],keyboard.readLine());
		}
		else if (args.length == 2)
		{
			System.out.println("Inserisci nome giocatore");
			main(args[0],args[1],keyboard.readLine());
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

			State last = new State(in);

			while(!last.gameOver())
			{
				System.out.println(last);
				if (last.getCurrentPlayer().equals(name))
				{
					Action a = chooseAction();
					out.print(a.toString(0));
					out.flush();
					System.err.println(a.toString(0));
				}
				else
					System.out.println(new Turn(in));
				last = new State(in);
			}
			System.out.println(last);
			System.out.println("Score: "+last.getScore());
		}
	}

}
