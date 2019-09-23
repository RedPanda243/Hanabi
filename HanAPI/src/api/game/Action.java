package api.game;

import sjson.JSONConvertible;
import sjson.JSONException;
import sjson.JSONObject;

import static api.game.ActionType.*;

public class Action extends JSONConvertible<JSONObject>
{
	private int player;
	private ActionType type;
	private int card;
	private int hintedPlayer;
//	private boolean[] cards;
	private Color color;
	private int value;

	public Action(JSONObject json) throws JSONException
	{
		super(json);
		try
		{
			player = Integer.parseInt(json.optString("player"));
			if (player<0 || player>Game.getInstance().getPlayers().length-1)
				throw new JSONException(new IndexOutOfBoundsException());
		}
		catch(NumberFormatException e)
		{
			throw new JSONException("Unreadable player");
		}

		type = ActionType.fromString(json.optString("type"));
		if (type == null)
			throw new JSONException("Unreadable action type");

		if (type == PLAY) {
			try {
				card = Integer.parseInt(json.optString("card"));
				if (card<0 || card>Game.getInstance().getNumberOfCardsPerPlayer()-1)
					throw new JSONException(new IndexOutOfBoundsException());
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable played card");
			}
		}else if (type == DISCARD) {
			try {
				card = Integer.parseInt(json.optString("card"));
				if (card<0 || card>Game.getInstance().getNumberOfCardsPerPlayer()-1)
					throw new JSONException(new IndexOutOfBoundsException());
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable discarded card");
			}
		}else if (type == HINT_COLOR) {
			color = Color.fromString(json.optString("color"));
			if (color == null)
				throw new JSONException("Unreadable hinted color");
			try {
				hintedPlayer = Integer.parseInt(json.optString("hinted"));
				if (hintedPlayer<0 || hintedPlayer>Game.getInstance().getPlayers().length-1)
					throw new JSONException(new IndexOutOfBoundsException());
				if (hintedPlayer == player)
					throw new JSONException("You cannot hint yourself");
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable hinted player");
			}
		}else if (type == HINT_VALUE){
			try
			{
				hintedPlayer = Integer.parseInt(json.optString("hinted"));
				if (hintedPlayer<0 || hintedPlayer>Game.getInstance().getPlayers().length-1)
					throw new JSONException(new IndexOutOfBoundsException());
				if (hintedPlayer == player)
					throw new JSONException("You cannot hint yourself");
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable hinted player");
			}

			try
			{
				value = Integer.parseInt(json.optString("value"));
				if (value<1 || value>5)
					throw new JSONException(new IndexOutOfBoundsException());
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable hinted value");
			}
		}
	}

	public Action clone()
	{
		return (Action)super.clone();
	}

	/**
	 * @return l'indice del giocatore che esegue l'azione
	 **/
	@SuppressWarnings("unused")
	public int getPlayer(){return player;}

	/**
	 * get the action type
	 * @return the type of action being performed
	 **/
	@SuppressWarnings("unused")
	public ActionType getActionType(){return type;}

	/**
	 * gets the psoition of the card being played/discarded
	 * @return the position of the card being played or discarded
	 * @throws IllegalActionException if the action type is not PLAY or DISCARD
	 **/
	@SuppressWarnings("unused")
	public int getCard() throws IllegalActionException{
		if(type != PLAY && type!= ActionType.DISCARD) throw new IllegalActionException("Card is not defined");
		return card;
	}

	/**
	 * gets the index of the player receiving the hint
	 * @return the index of the player receiving the hint
	 * @throws IllegalActionException if the action type is not HINT_COLOR or HINT_VALUE
	 **/
	@SuppressWarnings("unused")
	public int getHintReceiver() throws IllegalActionException{
		if(type != ActionType.HINT_COLOR && type!= ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a hint");
		return hintedPlayer;
	}

	/**
	 * gets the color hinted
	 * @return the color hinted
	 * @throws IllegalActionException if the action type is not HINT_COLOR
	 **/
	@SuppressWarnings("unused")
	public Color getColor() throws IllegalActionException{
		if(type != ActionType.HINT_COLOR) throw new IllegalActionException("Action is not a color hint");
		return color;
	}

	/**
	 * gets the value hinted
	 * @return the value hinted
	 * @throws IllegalActionException if the action type is not HINT_VALUE
	 **/
	@SuppressWarnings("unused")
	public int getValue() throws IllegalActionException{
		if(type != ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a value hint");
		return value;
	}

	/**
	 * A string representation of the action being performed
	 * @return a description of the action, depending on type
	 * */
	public String toString(){
		String playerName= Game.getInstance().getPlayers()[player];
		if (type == PLAY)
			return "Il giocatore "+playerName+ "("+player+") gioca la carta in posizione "+card;
		else if (type == DISCARD)
			return "Il giocatore "+playerName+ "("+player+") scarta la carta in posizione "+card;
		else if (type == HINT_COLOR)
			return "Il giocatore "+playerName+ "("+player+") mostra a "+Game.getInstance().getPlayers()[hintedPlayer]+"("+hintedPlayer
					+") le carte di colore "+color;
		else if (type == HINT_VALUE)
			return "Il giocatore "+playerName+ "("+player+") mostra a "+Game.getInstance().getPlayers()[hintedPlayer]+"("+hintedPlayer
					+") le carte di valore "+value;
		return "";
	}




}
