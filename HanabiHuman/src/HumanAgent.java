import api.client.AbstractAgent;
import api.client.Main;
import api.client.StatisticState;
import api.game.*;
import sjson.JSONData;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class HumanAgent extends AbstractAgent
{
	public HumanAgent(String logpath) throws FileNotFoundException
	{
		super(true,logpath);
	}

	public Action chooseAction()
	{
		log("\nChoose one action: (play <cardnum> | discard <cardnum> | hint <playernum> (<color>|<value>))");
		Action action = null;
		while(action == null)
		{
			try
			{
				String[] parts = Main.keyboard.readLine().split(" ");
				if (parts[0].equals("play"))
					action = new Action(Main.playerName, ActionType.PLAY,Integer.parseInt(parts[1]));
				else if (parts[0].equals("discard"))
				{
					action = new Action(Main.playerName, ActionType.DISCARD,Integer.parseInt(parts[1]));
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
					if (hinted.equals(Main.playerName))
						return chooseAction();
					try
					{
						//TODO
						action = new Action(Main.playerName,hinted,Integer.parseInt(parts[2]));
					}
					catch(NumberFormatException nfe)
					{
						//TODO
						action = new Action(Main.playerName,hinted,Color.fromString(parts[2]));
					}
				}
				else {
					System.out.println("unrecognized, retry");
				}
			}
			catch (IOException ioe){log(ioe); System.exit(1);}
			catch(Exception e){log(e);}
		}
		log("");
		return action;
	}

	public void notifyState(State state)
	{
		super.notifyState(state);
		try {
			StatisticState sstate = new StatisticState(state, stats);
			log(sstate.toString());
			if (state.getCurrentPlayer().equals(Main.playerName))
			{
				log("Suggerimenti possibili:");
				for (String player: Game.getInstance().getPlayers())
				{
					if (!player.equals(Main.playerName))
					{
						log(player);
						for (Action h:this.getPossibleHints(player))
							log("\t"+h);
					}
				}
			}
		}
		catch (JSONException e){}
	}

	public void notifyTurn(Turn turn)
	{
		super.notifyTurn(turn);
		System.out.println(turn+"\n");
	}

	/**
	 *
	 * @param args 0-host, 1-port, 2-name
	 */
	public static void main(String... args) throws IOException,JSONException
	{
		String logpath = null;
		for (int i=0; i<args.length; i++)
		{
			if (args[i].equals("-f"))
			{
				i++;
				logpath = args[i];
			}
		}
		AbstractAgent agent = new HumanAgent(logpath);
		Main.setAgent(agent);
		Main.main(args);
	}

}
