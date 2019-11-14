package api.client;

import api.game.Action;
import api.game.ActionType;
import api.game.Game;
import api.game.State;
import sjson.JSONException;

import java.io.IOException;

public class HumanAgent extends AbstractAgent
{
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

	@Override
	public void addHistory(State state) {

	}

	public Action chooseAction(State current)
	{
		System.out.println("\nChoose one action: (play <cardnum> | discard <cardnum> | hint <playernum> (<color>|<value>))");
		Action action = null;
		while(action == null)
		{
			try
			{
				String[] parts = Main.keyboard.readLine().split(" ");
				if (parts[0].equals("play"))
					action = new Action(Main.name, ActionType.PLAY,Integer.parseInt(parts[1]));
				else if (parts[0].equals("discard"))
				{
					action = new Action(Main.name, ActionType.DISCARD,Integer.parseInt(parts[1]));
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
						//TODO
					//	action = new Action(Main.name,hinted,Integer.parseInt(parts[2]));
					}
					catch(NumberFormatException nfe)
					{
						//TODO
					//	action = new Action(Main.name,hinted,Color.fromString(parts[2]));
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

	}

}
