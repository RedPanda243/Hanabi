package api.game;

import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Turn extends TypedJSON<JSONObject>
{
	public Turn(Action a, Card d) throws JSONException
	{
		json = new JSONObject();
		setAction(a);
		setDrawn(d);
	}

	public Turn(Action a, List<Integer> r) throws JSONException
	{
		json = new JSONObject();
		setAction(a);
		setRevealed(r);
	}

	public Turn(Reader reader) throws JSONException
	{
		json = new JSONObject(reader);
		JSONData d = json.get("action");
		if (d == null)
			throw new JSONException("Attributo \"action\" mancante");
		Action a = new Action(d.toString(0));
		setAction(a);
		if (a.getType() == ActionType.PLAY || a.getType() == ActionType.DISCARD)
		{
			d = json.get("drawn");
			if (d!=null)
				setDrawn(new Card(d.toString(0)));
			else
				throw new JSONException("Attributo \"drawn\" mancante");
		}
		else
		{
			d = json.getArray("revealed");
			if (d == null)
				throw new JSONException("Attributo \"revealed\" mancante");
			else
			{
				//TODO, rivedi tutti i controlli non sono fatti bene
			}
		}
	}

	public Action getAction()
	{
		return (Action)json.get("action");
	}

	public Card getDrawn()
	{
		JSONData c = json.get("drawn");
		if (c == null)
			return null;
		else
			return (Card)c;
	}

	public List<Integer> getRevealed()
	{
		JSONArray a = json.getArray("revealed");
		ArrayList<Integer> list = new ArrayList<>();
		for (JSONData d:a)
			list.add(Integer.parseInt(d.toString().substring(1,d.toString().length()-1)));
		return list;
	}

	public Turn setAction(Action action)
	{
		json.set("action",action);
		return this;
	}

	public Turn setDrawn(Card card) throws JSONException
	{
		ActionType type = getAction().getType();
		if (type == ActionType.HINT_COLOR || type == ActionType.HINT_VALUE)
			throw new JSONException("I suggerimenti non fanno pescare carte");
		json.set("drawn",card);
		return this;
	}

	public Turn setRevealed(List<Integer> revealed) throws JSONException
	{
		ActionType type = getAction().getType();
		if (type == ActionType.PLAY || type == ActionType.DISCARD)
			throw new JSONException("Giocare o scartare carte non rivela nessuna carta");
		JSONArray r = new JSONArray();
		for (int i:revealed)
			r.add(""+i);
		json.set("revealed",r);
		return this;
	}

	public String toString()
	{
		Card c = getDrawn();
		return getAction().toString()+(c==null?"(Revealed: "+getRevealed()+")":"\nIl giocatore pesca "+c.toString());
	}
}
