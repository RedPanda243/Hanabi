import api.client.AbstractAgent;
import api.client.Main;
import api.game.Action;
import api.game.Game;

import java.util.ArrayList;
import java.util.HashMap;

public class Agent extends AbstractAgent
{
	private HashMap<String,InputNode> input; //Nome del nodo = player/card/P|U
	OutputNode[] output;

	private Agent()
	{
		super();
		input = null;
	}

	public static void main(String args[]) throws Exception
	{
		AbstractAgent agent = new Agent();
		Main.setAgent(agent);
		Main.main(args);
	}

	@Override
	public Action chooseAction()
	{
		if (input == null)
		{
			init();
		}
	}

	private void init()
	{
		for (int i=0; i<Game.getInstance().getPlayers().length; i++)
		{
			for (int j=0; j<Game.getInstance().getNumberOfCardsPerPlayer(); j++)
			{
				input.put(getInputNodeName(Game.getInstance().getPlayer(i),j, InputNode.InputType.PLAYABILITY), new InputNode());
				input.put(getInputNodeName(Game.getInstance().getPlayer(i),j, InputNode.InputType.USELESSNESS), new InputNode());
			}
		}

	}

	public String getInputNodeName(String player, int card, InputNode.InputType type)
	{
		return player+"/"+card+"/"+type;
	}

	public InputNode getInputNode(String player, int card, InputNode.InputType type)
	{
		return input.get(getInputNodeName(player, card, type));
	}
}
