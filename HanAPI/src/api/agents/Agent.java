package api.agents;

import api.game.*;
import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONString;

public abstract class Agent
{

	public abstract Action doAction() throws IllegalActionException;

	public abstract int getIndex();

	public abstract String getName();

	public Action discard(int i) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.put("type",new JSONString(ActionType.DISCARD.toString()));
		obj.put("player",new JSONString(""+getIndex()));
		obj.put("card",new JSONString(""+i));
		try
		{
			return new Action(obj);
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	public Action play(int i) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.put("type",new JSONString(ActionType.PLAY.toString()));
		obj.put("player",new JSONString(""+getIndex()));
		obj.put("card",new JSONString(""+i));
		try
		{
			return new Action(obj);
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	/**
	 * Restituisce il valore della prossima carta giocabile di colore specificato
	 * @param s lo stato corrente
	 * @param c il colore desiderato
	 * @return il valore della carta giocabile, -1 se sono state giocate tutte le carte del colore specificato
	 */
	public static int playable(State s, Color c){
		java.util.Stack<Card> fw = s.getFirework(c);
		if (fw.size()==5) return -1;
		else return fw.size()+1;
	}


	public Action hint(Color c, int hintReceiver) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.put("type",new JSONString(ActionType.HINT_COLOR.toString()));
		obj.put("player",new JSONString(""+getIndex()));
		obj.put("color",new JSONString(c.toString()));
		obj.put("hinted",new JSONString(""+hintReceiver));
		try
		{
			return new Action(obj);
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	public Action hint(int v, int hintReceiver) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.put("type",new JSONString(ActionType.HINT_VALUE.toString()));
		obj.put("player",new JSONString(""+getIndex()));
		obj.put("value",new JSONString(""+v));
		obj.put("hinted",new JSONString(""+hintReceiver));
		try
		{
			return new Action(obj);
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}
}
