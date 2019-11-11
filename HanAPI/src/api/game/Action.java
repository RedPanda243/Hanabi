package api.game;

import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static api.game.ActionType.*;

/**
 * Classe che rappresenta una mossa come oggetto json.
 */
@SuppressWarnings({"WeakerAccess","unused"})
public class Action extends JSONObject
{
	/**
	 * Costruttore per una mossa che rappresenta la giocata o lo scarto di una carta
	 * @param player Giocatore che effettua la mossa
	 * @param type tipo di mossa ({@link ActionType#PLAY} o {@link ActionType#DISCARD})
	 * @param card indice della carta nella mano del giocatore che effettua la mossa
	 * @throws JSONException in caso di errore nella costruzione dell'oggetto json
	 */
	public Action(String player, ActionType type, int card) throws JSONException
	{
		super();
		if (type == HINT_COLOR || type == HINT_VALUE)
			throw new JSONException("Wrong type!");
		setPlayer(player);
		setType(type);
		setCard(card);
	}

	/**
	 * Costruttore per una mossa che rappresenta un suggerimento per valore
	 * @param player Giocatore che effettua il suggerimento
	 * @param hinted Giocatore cui &egrave; rivolto il suggerimento
	 * @param value Valore da suggerire
	 * @param cardsToReveal Lista degli indici delle carte suggerite nella mano del giocatore che riceve il suggerimento
	 * @throws JSONException in caso di errore nella costruzione dell'oggetto json
	 */
	public Action(String player, String hinted, int value, int[] cardsToReveal) throws JSONException
	{
		super();
		setPlayer(player);
		setType(HINT_VALUE);
		setHintReceiver(hinted);
		setValue(value);
		setCardsToReveal(cardsToReveal);
	}

	/**
	 * Costruttore per una mossa che rappresenta un suggerimento per colore
	 * @param player Giocatore che effettua il suggerimento
	 * @param hinted Giocatore cui &egrave; rivolto il suggerimento
	 * @param color Colore da suggerire
	 * @param cardsToReveal Lista degli indici delle carte suggerite nella mano del giocatore che riceve il suggerimento
	 * @throws JSONException in caso di errore nella costruzione dell'oggetto json
	 */
	public Action(String player, String hinted, Color color, int[] cardsToReveal) throws JSONException
	{
		super();
		setPlayer(player);
		setType(HINT_COLOR);
		setHintReceiver(hinted);
		setColor(color);
		setCardsToReveal(cardsToReveal);
	}

	/**
	 * @see JSONObject#JSONObject(String)
	 * @param s Rappresentazione testuale del json che rappresenta la mossa
	 * @throws JSONException se il json &egrave; malformato
	 */
	public Action(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	/**
	 * @see JSONObject#JSONObject(Reader)
	 * @param reader Reader da cui leggere carattere per carattere il json che rappresenta la mossa
	 * @throws JSONException in caso di errori I/O o se il json &egrave; malformato
	 */
	public Action(Reader reader) throws JSONException
	{
		super(reader);

		if (names().size()!=0) {
			checkPlayer();
			checkType();
			ActionType type = getActionType();
			if (type == PLAY)
			{
				checkCard();
			} else if (type == DISCARD) {
				s = getString("card");
				if (s == null)
					throw new JSONException("Attributo \"card\" mancante");
				try {
					setCard(Integer.parseInt(s));
				} catch (NumberFormatException e) {
					throw new JSONException("Attributo \"card\" deve essere un intero");
				}
			} else if (type == HINT_COLOR) {
				s = getString("color");
				if (s == null)
					throw new JSONException("Attributo \"color\" mancante");
				if (Color.fromString(s) == null)
					throw new JSONException("Attributo \"color\" deve essere uno tra "+Arrays.toString(Color.values()));

				a = getArray("cardsToReveal");
				if (a == null)
					throw new JSONException("Attributo \"cardsToReveal\" mancante");
				else
				{
					try {
						for (JSONData d:a)
							Integer.parseInt(d.toString(0));
					}
					catch(NumberFormatException nfe)
					{
						throw new JSONException("L'attributo \"cardsToReveal\" deve contenere solo interi");
					}
					this.set("cardsToReveal",s);
				}

				s = getString("hinted");
				if (s == null)
					throw new JSONException("Attributo \"hinted\" mancante");
				setHintReceiver(s);

			} else if (type == HINT_VALUE) {
				s = getString("hinted");
				if (s == null)
					throw new JSONException("Attributo \"hinted\" mancante");
				setHintReceiver(s);

				a = getArray("cardsToReveal");
				if (a == null)
					throw new JSONException("Attributo \"cardsToReveal\" mancante");
				else
				{
					try {
						for (JSONData d:a)
							Integer.parseInt(d.toString(0));
					}
					catch(NumberFormatException nfe)
					{
						throw new JSONException("L'attributo \"cardsToReveal\" deve contenere solo interi");
					}
					this.set("cardsToReveal",s);
				}

				s = getString("value");
				if (s == null)
					throw new JSONException("Attributo \"value\" mancante");
				try {
					setValue(Integer.parseInt(s));
				} catch (NumberFormatException e) {
					throw new JSONException("Unreadable hinted value");
				}
			}
		}
	}

	private void checkCard() throws JSONException
	{
		String s = getString("card");
		if (s == null)
			throw new JSONException("Attributo \"card\" mancante");
		try {
			setCard(Integer.parseInt(s));
		} catch (NumberFormatException e) {
			throw new JSONException("Attributo \"card\" deve essere un intero");
		}
	}


	/**
	 * Usato nei costruttori, verifica l'integrit&agrave; del campo "player"
	 * @throws JSONException se l'attributo "player" &egrave; mancante
	 */
	private void checkPlayer() throws JSONException
	{
		String s = getString("player");
		if (s == null)
			throw new JSONException("Attributo \"player\" mancante");
	}

	/**
	 * Usato nei costruttori, verifica l'integrit&agrave; del campo "type"
	 * @throws JSONException se l'attributo "type" &egrave; mancante
	 */
	private void checkType() throws JSONException
	{
		ActionType type = ActionType.fromString(getString("type"));
		if (type == null)
			throw new JSONException("Attributo \"type\" mancante");
	}

	/**
	 * @see JSONObject#clone()
	 * @return una copia di questa Action
	 */
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
	 * @return il nome del giocatore che esegue l'azione
	 **/
	public String getPlayer(){return getString("player");}

	/**
	 * @see ActionType
	 * @return il tipo di mossa
	 **/
	public ActionType getActionType(){return ActionType.fromString(getString("type"));}

	/**
	 * @return la posizione della carta da giocare o scartare nella mano del giocatore che effettua la mossa, null se la mossa rappresenta un suggerimento
	 **/
	public int getCard()
	{
		ActionType type = getActionType();
		if(type == PLAY || type == DISCARD)
			return Integer.parseInt(getString("card"));
		else return -1;
	}

	public int[] getCardsToReveal() throws JSONException
	{
		if(this.getActionType().equals(HINT_VALUE) || this.getActionType().equals(HINT_COLOR))
		{
			JSONArray array = getArray("cardsToReveal");
			for (JSONData d: array)
			{

			}
			String s = getString("cardsToReveal");
			String [] resString  = s.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
			List<Integer> results = new ArrayList<>();
			for (String toAdd : resString){
				try{
					results.add(Integer.parseInt(toAdd));
				} catch (NumberFormatException nfe) {
					throw new JSONException("Not able to convert to int, getCardsToReveal");
				}
			}
			return results;
		}else return  null;
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
		set("card",""+c);
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

	public void setCardsToReveal(int[] cardsToReveal)
	{
		JSONArray array = new JSONArray();
		for (int i:cardsToReveal)
			array.add("" + i);
		set("cardsToReveal", array);
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
			return p+"("+Game.getInstance().getPlayerTurn(p)+") play the card of index "+getCard();
		else if (type == DISCARD)
			return p+"("+Game.getInstance().getPlayerTurn(p)+") discards the card of index "+getCard();
		else {
			String h = getHintReceiver();
			if (type == HINT_COLOR)
				return p+"("+Game.getInstance().getPlayerTurn(p)+") shows "+getColor()+" cards to "
						+h+"("+Game.getInstance().getPlayerTurn(h)+")";
			else if (type == HINT_VALUE)
				return p+"("+Game.getInstance().getPlayerTurn(p)+") shows "+getValue()+"-cards to "
						+h+"("+Game.getInstance().getPlayerTurn(h)+")";
		}
		return "";
	}




}
