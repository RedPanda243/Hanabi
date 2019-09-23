package api.game;

import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONObjectConvertible;

import static api.game.ActionType.*;

public class Action implements Cloneable, JSONObjectConvertible
{
	private int player;
	private ActionType type;
	private int card;
	private int hintee;
//	private boolean[] cards;
	private Color color;
	private int value;

	public Action(JSONObject json) throws JSONException
	{
		try
		{
			player = Integer.parseInt(json.optString("player"));
		}
		catch(NumberFormatException e)
		{
			throw new JSONException("Unreadable player");
		}
		type = ActionType.fromString(json.optString("type"));
		if (type == null)
			throw new JSONException("Unreadable type");

		if (type == PLAY) {
			try {
				card = Integer.parseInt(json.optString("card"));
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable played card");
			}
		}else if (type == DISCARD) {
			try {
				card = Integer.parseInt(json.optString("card"));
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable discarded card");
			}
		}else if (type == HINT_COLOR) {
			color = Color.fromString(json.optString("color"));
			if (color == null)
				throw new JSONException("Unreadable hinted color");
			try {
				hintee = Integer.parseInt(json.optString("hinted"));
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable hinted player");
			}
		}else if (type == HINT_VALUE){
			try
			{
				hintee = Integer.parseInt(json.optString("hinted"));
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable hinted player");
			}
			try
			{
				value = Integer.parseInt(json.optString("value"));
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable hinted value");
			}
		}
	}

	public Action clone()
	{
		Action a;
		try
		{
			a = (Action)super.clone();
			a.player = player;
			a.type = type;
			a.card = card;
//			a.cards = cards.clone();
			a.hintee = hintee;
			a.color = color;
			a.value = value;
		}
		catch(CloneNotSupportedException e)
		{
			a = null;
		}
		return a;
	}

	/**
	 * get the player index
	 * @return the index of the player performing the action
	 **/
	public int getPlayer(){return player;}

	/**
	 * get the action type
	 * @return the type of action being performed
	 **/
	public ActionType getType(){return type;}

	/**
	 * gets the psoition of the card being played/discarded
	 * @return the position of the card being played or discarded
	 * @throws IllegalActionException if the action type is not PLAY or DISCARD
	 **/
	public int getCard() throws IllegalActionException{
		if(type != PLAY && type!= ActionType.DISCARD) throw new IllegalActionException("Card is not defined");
		return card;
	}

	/**
	 * gets the index of the player receiving the hint
	 * @return the index of the player receiving the hint
	 * @throws IllegalActionException if the action type is not HINT_COLOR or HINT_VALUE
	 **/
	public int getHintReceiver() throws IllegalActionException{
		if(type != ActionType.HINT_COLOR && type!= ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a hint");
		return hintee;
	}

	/**
	 * gets the color hinted
	 * @return the color hinted
	 * @throws IllegalActionException if the action type is not HINT_COLOR
	 **/
	public Color getColor() throws IllegalActionException{
		if(type != ActionType.HINT_COLOR) throw new IllegalActionException("Action is not a color hint");
		return color;
	}

	/**
	 * gets the value hinted
	 * @return the value hinted
	 * @throws IllegalActionException if the action type is not HINT_VALUE
	 **/
	public int getValue() throws IllegalActionException{
		if(type != ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a value hint");
		return value;
	}

	@Override
	public JSONObject toJSON()
	{
		JSONObject obj = new JSONObject();
		obj.put("type",type.toString());
		obj.put("player",""+player);
		if (type == PLAY)
			obj.put("card",""+card);
		else if (type == DISCARD)
			obj.put("card",""+card);
		else if (type == HINT_VALUE)
		{
			obj.put("value", "" + value);
			obj.put("hinted", "" + hintee);
		}
		else if (type == HINT_COLOR)
		{
			obj.put("color", color.toString());
			obj.put("hinted", "" + hintee);
		}else
			return null;
		return obj;
	}

	/**
	 * A string representation of the action being performed
	 * @return a description of the action, depending on type
	 * */
	public String toString(){
		String ret,playerName= HanabiServer.instance.getPlayer(player).getName();
		if (type == PLAY)
			return "Player "+playerName+ "("+player+") plays the card at position "+card;
		else if (type == DISCARD)
			return "Player "+playerName+ "("+player+") discards the card at position "+card;
		else if (type == HINT_COLOR)
		{
			ret = "Player "+playerName+ "("+player+") gives the hint: \"Player "+hintee+", cards at position"+(cards.length>1?"s":"");
			for(int i=0; i<cards.length; i++)
				ret += (cards[i]?" "+i:"");
			ret+= " have color "+ color +"\"";
			return ret;
		}
		else if (type == HINT_VALUE)
		{
			ret = "Player "+playerName+ "("+player+") gives the hint: \"Player "+hintee+", cards at position"+(cards.length>1?"s":"");
			for(int i=0; i<cards.length; i++)
				ret += (cards[i]?" "+i:"");
			ret+= " have value "+value+"\"";
			return ret;
		}
		else return "";
	}


}
