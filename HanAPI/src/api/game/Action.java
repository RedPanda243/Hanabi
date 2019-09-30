package api.game;

import api.main.HanabiClient;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;
import java.io.StringReader;

import static api.game.ActionType.*;

public class Action extends JSONObject
{
	public Action(String player, ActionType type, int card) throws JSONException
	{
		super();
		if (type == HINT_COLOR || type == HINT_VALUE)
			throw new JSONException("Wrong type!");
		setPlayer(player);
		setType(type);
		setCard(card);
	}

	public Action(String player, String hinted, int value) throws JSONException
	{
		super();
		setPlayer(player);
		setType(HINT_VALUE);
		setHintReceiver(hinted);
		setValue(value);
	}

	public Action(String player, String hinted, Color color) throws JSONException
	{
		super();
		setPlayer(player);
		setType(HINT_COLOR);
		setHintReceiver(hinted);
		setColor(color);
	}

	public Action(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public Action(Reader reader) throws JSONException
	{
		super(reader);
		String s = getString("player");
		if (s == null)
			throw new JSONException("Missing player!");
		setPlayer(s);

		ActionType type = ActionType.fromString(getString("type"));
		if (type == null)
			throw new JSONException("Unreadable action type");

		if (type == PLAY)
		{
			s = getString("card");
			if (s == null)
				throw new JSONException("Missing card!");
			try
			{
				setCard(Integer.parseInt(s));
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable played card");
			}
		}
		else if (type == DISCARD) {
			s = getString("card");
			if (s == null)
				throw new JSONException("Missing card!");
			try
			{
				setCard(Integer.parseInt(s));
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable discarded card");
			}
		}
		else if (type == HINT_COLOR)
		{
			s = getString("color");
			if (s == null)
				throw new JSONException("Missing color!");
			if (Color.fromString(s)==null)
				throw new JSONException("Unreadble color");

			s = getString("hinted");
			if (s == null)
				 throw new JSONException("Missing hinted!");
			setHintReceiver(s);

		}
		else if (type == HINT_VALUE)
		{
			s = getString("hinted");
			if (s == null)
				throw new JSONException("Missing hinted!");
			setHintReceiver(s);

			s = getString("value");
			if (s == null)
				throw new JSONException("Missing value!");
			try
			{
				setValue(Integer.parseInt(s));
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unreadable hinted value");
			}
		}
	}

	public Action clone()
	{
		try
		{
			return new Action(super.clone().toString(0));
		}
		catch(JSONException e)
		{
			//Impossibile
			return null;
		}
	}

	/**
	 * @return l'indice del giocatore che esegue l'azione
	 **/
	public String getPlayer(){return getString("player");}

	/**
	 * get the action type
	 * @return the type of action being performed
	 **/
	public ActionType getActionType(){return ActionType.fromString(getString("type"));}

	/**
	 * gets the psoition of the card being played/discarded
	 * @return the position of the card being played or discarded, null if the action type is not PLAY or DISCARD
	 **/
	public int getCard()
	{
		ActionType type = getActionType();
		if(type != PLAY && type!= ActionType.DISCARD) return -1;
		return Integer.parseInt(getString("card"));
	}

	/**
	 * gets the name of the player receiving the hint
	 * @return the index of the player receiving the hint, null if the action type is not HINT_COLOR or HINT_VALUE
	 **/
	public String getHintReceiver()
	{
		ActionType type = getActionType();
		if(type != ActionType.HINT_COLOR && type!= ActionType.HINT_VALUE) return null;
		return getString("hinted");
	}

	/**
	 * gets the color hinted
	 * @return the color hinted, null if the action type is not HINT_COLOR
	 **/
	public Color getColor()
	{
		ActionType type = getActionType();
		if(type != ActionType.HINT_COLOR) return null;
		return Color.fromString(getString("color"));
	}

	/**
	 * gets the value hinted
	 * @return the value hinted, -1 if the action type is not HINT_VALUE
	 **/
	public int getValue()
	{
		ActionType type = getActionType();
		if(type != ActionType.HINT_VALUE) return -1;
		return Integer.parseInt(getString("value"));
	}

	public Action setCard(int c) throws JSONException
	{
		if (getActionType()==HINT_COLOR || getActionType()==HINT_VALUE)
			throw new JSONException("Hint actions have no cards");
		if (c<0 || c>Game.getInstance().getNumberOfCardsPerPlayer()-1)
			throw new JSONException(new IndexOutOfBoundsException());
		return this;
	}

	public Action setColor(Color color) throws JSONException
	{
		if (getActionType()==PLAY || getActionType()==DISCARD)
			throw new JSONException(getActionType()+" actions have no color");
		if (color == null)
			throw new JSONException("Null color");
		set("color",color.toString().toLowerCase());
		return this;
	}

	public Action setHintReceiver(String player) throws JSONException
	{
		if (!Game.getInstance().isPlaying(player))
			throw new JSONException("Unrecognized player");
		if (player.equals(getPlayer()))
			throw new JSONException("You cannot hint yourself");
		set("hinted",player);
		return this;
	}

	public Action setPlayer(String s) throws JSONException
	{
		if (!Game.getInstance().isPlaying(s))
			throw new JSONException("Unrecognized player");
		set("player",s);
		return this;
	}

	public Action setType(ActionType type)
	{
		set("type",type.toString().toLowerCase());
		return this;
	}

	public Action setValue(int v) throws JSONException
	{
		if (getActionType()==PLAY || getActionType()==DISCARD)
			throw new JSONException(getActionType()+" actions have no value");
		if (v<1 || v>5)
			throw new JSONException(new IndexOutOfBoundsException());
		set("value",""+v);
		return this;
	}

	/**
	 * A string representation of the action being performed
	 * @return a description of the action, depending on type
	 * */
	public String toString()
	{

		ActionType type = getActionType();
		String p = getPlayer();
		if (type == PLAY)
			return "Il giocatore "+p+"("+Game.getInstance().getPlayerTurn(p)+") gioca la carta in posizione "+getCard();
		else if (type == DISCARD)
			return "Il giocatore "+p+"("+Game.getInstance().getPlayerTurn(p)+") scarta la carta in posizione "+getCard();
		else {
			String h = getHintReceiver();
			if (type == HINT_COLOR)
				return "Il giocatore "+p+"("+Game.getInstance().getPlayerTurn(p)+") mostra a "
						+h+"("+Game.getInstance().getPlayerTurn(h)+") le carte di colore "+getColor();
			else if (type == HINT_VALUE)
				return "Il giocatore "+p+"("+Game.getInstance().getPlayerTurn(p)+") mostra a "
						+h+"("+Game.getInstance().getPlayerTurn(h)+") le carte di valore "+getValue();
		}
		return "";
	}




}
