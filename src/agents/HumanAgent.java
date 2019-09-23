package agents;

import game.Color;
import game.Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HumanAgent extends Agent
{
	private BufferedReader br;
	private String playerName;
	private int index;


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
}
