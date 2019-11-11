package api.game;

import sjson.JSONData;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;

public class Turn extends JSONObject
{
	public Turn(Action a, Card d) throws JSONException
	{
		super();
		setAction(a);
		setDrawn(d);
	}

	public Turn(Action a)
	{
		super();
		setAction(a);
	}

	public Turn(Reader reader) throws JSONException
	{
		super(reader);
		JSONData d = get("action");
		if (d == null)
			throw new JSONException("Missing action");
		Action a = new Action(d.toString(0));
		setAction(a);
		if (a.getType() == ActionType.PLAY || a.getType() == ActionType.DISCARD)
		{
			d = get("drawn");
/*			if (d == null)
				throw new JSONException("Missing drawn card");
*/
			if (d!=null)
				setDrawn(new Card(d.toString(0)));
		}
	}

	public Action getAction()
	{
		return (Action)get("action");
	}

	public Card getDrawn()
	{
		JSONData c = get("drawn");
		if (c == null)
			return null;
		else
			return (Card)c;
	}

	public Turn setAction(Action action)
	{
		set("action",action);
		return this;
	}

	public Turn setDrawn(Card card) throws JSONException
	{
		ActionType type = getAction().getType();
		if (type == ActionType.HINT_COLOR || type == ActionType.HINT_VALUE)
			throw new JSONException("Hint actions do not draw");
		set("drawn",card);
		return this;
	}

	public String toString()
	{
		Card c = getDrawn();
		return getAction().toString()+(c==null?"":"\nIl giocatore pesca "+c.toString());
	}
}
