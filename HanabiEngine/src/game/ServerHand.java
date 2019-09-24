package game;

import api.game.Hand;
import sjson.JSONArray;
import sjson.JSONException;

public class ServerHand extends Hand
{
	public ServerHand(JSONArray array) throws JSONException
	{
		super(array);
		for (int i=0; i<cards.length; i++)
		{
			cards[i] = new ServerCard(cards[i].toJSON());
		}
	}

	public ServerHand clone()
	{
		return (ServerHand)super.clone();
	}


	public ServerCard getCard(int i)
	{
		return (ServerCard)super.getCard(i);
	}

	public void setCard(ServerCard card, int index)
	{
		cards[index] = card;
	}
}
