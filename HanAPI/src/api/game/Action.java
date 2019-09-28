package api.game;

import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;

import static api.game.ActionType.*;

public class Action extends JSONObject
{
	public Action(Reader reader) throws JSONException
	{
		super(reader);
		int p;
		try
		{
			p = Integer.parseInt(getString("player"));
			if (p<0 || p>Game.getInstance().getPlayers().length-1)
				throw new JSONException(new IndexOutOfBoundsException());
		}
		catch(NumberFormatException e)
		{
			throw new JSONException("Unreadable player");
		}

		ActionType type = ActionType.fromString(getString("type"));
		if (type == null)
			throw new JSONException("Unreadable action type");
		int c,h;
		Color color;
		if (type == PLAY) {
			try {
				c = Integer.parseInt(getString("card"));
				if (c<0 || c>Game.getInstance().getNumberOfCardsPerPlayer()-1)
					throw new JSONException(new IndexOutOfBoundsException());
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable played card");
			}
		}else if (type == DISCARD) {
			try {
				c = Integer.parseInt(getString("card"));
				if (c<0 || c>Game.getInstance().getNumberOfCardsPerPlayer()-1)
					throw new JSONException(new IndexOutOfBoundsException());
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable discarded card");
			}
		}else if (type == HINT_COLOR) {
			color = Color.fromString(getString("color"));
			if (color == null)
				throw new JSONException("Unreadable hinted color");
			try {
				h = Integer.parseInt(getString("hinted"));
				if (h<0 || h>Game.getInstance().getPlayers().length-1)
					throw new JSONException(new IndexOutOfBoundsException());
				if (h == p)
					throw new JSONException("You cannot hint yourself");
			} catch (NumberFormatException e) {
				throw new JSONException("Unreadable hinted player");
			}
		}else if (type == HINT_VALUE){
			try
			{
				h = Integer.parseInt(getString("hinted"));
				if (h<0 || h>Game.getInstance().getPlayers().length-1)
					throw new JSONException(new IndexOutOfBoundsException());
				if (h == p)
					throw new JSONException("You cannot hint yourself");
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable hinted player");
			}

			try
			{
				p = Integer.parseInt(getString("value"));
				if (p<1 || p>5)
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
	public int getPlayer(){return Integer.parseInt(getString("player"));}

	/**
	 * get the action type
	 * @return the type of action being performed
	 **/
	public ActionType getActionType(){return ActionType.fromString(getString("type"));}

	/**
	 * gets the psoition of the card being played/discarded
	 * @return the position of the card being played or discarded
	 * @throws IllegalActionException if the action type is not PLAY or DISCARD
	 **/
	public int getCard() throws IllegalActionException
	{
		ActionType type = getActionType();
		if(type != PLAY && type!= ActionType.DISCARD) throw new IllegalActionException("Card is not defined");
		return Integer.parseInt(getString("card"));
	}

	/**
	 * gets the index of the player receiving the hint
	 * @return the index of the player receiving the hint
	 * @throws IllegalActionException if the action type is not HINT_COLOR or HINT_VALUE
	 **/
	public int getHintReceiver() throws IllegalActionException
	{
		ActionType type = getActionType();
		if(type != ActionType.HINT_COLOR && type!= ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a hint");
		return Integer.parseInt(getString("hinted"));
	}

	/**
	 * gets the color hinted
	 * @return the color hinted
	 * @throws IllegalActionException if the action type is not HINT_COLOR
	 **/
	public Color getColor() throws IllegalActionException
	{
		ActionType type = getActionType();
		if(type != ActionType.HINT_COLOR) throw new IllegalActionException("Action is not a color hint");
		return Color.fromString(getString("color"));
	}

	/**
	 * gets the value hinted
	 * @return the value hinted
	 * @throws IllegalActionException if the action type is not HINT_VALUE
	 **/
	public int getValue() throws IllegalActionException
	{
		ActionType type = getActionType();
		if(type != ActionType.HINT_VALUE) throw new IllegalActionException("Action is not a value hint");
		return Integer.parseInt(getString("value"));
	}

	/**
	 * A string representation of the action being performed
	 * @return a description of the action, depending on type
	 * */
	public String toString()
	{
		try
		{
			ActionType type = getActionType();
			String playerName = Game.getInstance().getPlayers()[getPlayer()];
			if (type == PLAY)
				return "Il giocatore " + playerName + "(" + getPlayer() + ") gioca la carta in posizione " + getCard();
			else if (type == DISCARD)
				return "Il giocatore " + playerName + "(" + getPlayer() + ") scarta la carta in posizione " + getCard();
			else {
				int h = getHintReceiver();
				if (type == HINT_COLOR)
					return "Il giocatore "+playerName+"("+getPlayer()+") mostra a "+Game.getInstance().getPlayers()[h]+"("+h
							+ ") le carte di colore " + getColor();
				else if (type == HINT_VALUE)
					return "Il giocatore "+playerName+"("+getPlayer()+") mostra a "+Game.getInstance().getPlayers()[h]+"("+h
							+ ") le carte di valore " + getValue();
			}
		}
		catch(IllegalActionException iae)
		{
			//Impossibile
			iae.printStackTrace(System.err);
		}
		return "";
	}




}
