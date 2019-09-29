package api.game;

import sjson.JSONData;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;

public class Turn extends JSONObject
{
	public Turn(Reader reader) throws JSONException
	{
		super(reader);
		set("action",new Action(get("action").toString(0)));
		set("drawn", new Card(get("drawn").toString(0)));
	}

	public Action getAction()
	{
		return (Action)get("action");
	}

	public Card getDrawn()
	{
		JSONData c = get("drawn");
		if (c.toString().equals(""))
			return null;
		else
			return (Card)c;
	}

	public String toString()
	{
		Card c = getDrawn();
		return getAction().toString()+(c==null?"":"\nIl giocatore pesca "+c.toString());
	}
}
