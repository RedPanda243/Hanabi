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
	public Turn(Action a, Card oldcard, Card newcard) throws JSONException
	{
		json = new JSONObject();
		setAction(a);
		setDrawn(newcard);
		setCard(oldcard);
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
		/*	else
				throw new JSONException("Attributo \"drawn\" mancante");*/

			d = json.get("card");
			if (d!=null)
				setCard(new Card(d.toString(0)));
			else
				throw new JSONException("Attributo \"card\" mancante");
		}
		else
		{
			d = json.getArray("revealed");
			if (d == null)
				throw new JSONException("Attributo \"revealed\" mancante");
			else
			{
				//TODO, rivedi tutti i controlli non sono fatti bene
				//Questi dovrebbero essere int che indicano posizioni nella mano del giocatore
			}
		}
	}

	public Action getAction()
	{
		return (Action)json.get("action");
	}

	public Card getCard()
	{
		JSONData c = json.get("card");
		if (c == null)
			return null;
		else
			return (Card)c;
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

	public Turn setCard(Card card) throws JSONException
	{
		ActionType type = getAction().getType();
		if (type == ActionType.HINT_COLOR || type == ActionType.HINT_VALUE)
			throw new JSONException("I suggerimenti non fanno giocare|scartare carte");
		json.set("card",card);
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
		String s ="";
		String player = getAction().getPlayer();
		s = s+"Il giocatore "+player+"("+Game.getInstance().getPlayerTurn(player)+") ";
		if (getAction().getType() == ActionType.PLAY)
			s = s+"gioca "+getCard()+" e pesca "+getDrawn();
		else if (getAction().getType() == ActionType.DISCARD)
			s = s+"scarta "+getCard()+" e pesca "+getDrawn();
		else
		{
			String hinted = getAction().getHinted();
			if (getAction().getType() == ActionType.HINT_COLOR)
			{
				s = s+  "suggerisce a "+hinted+"("+Game.getInstance().getPlayerTurn(hinted)+") le carte " +
						getRevealed()+" di colore "+getAction().getColor();
			}
			else
			{
				s = s+  "suggerisce a "+hinted+"("+Game.getInstance().getPlayerTurn(hinted)+") le carte " +
						getRevealed()+" di valore "+getAction().getValue();
			}

		}
		return s;
	}
}
