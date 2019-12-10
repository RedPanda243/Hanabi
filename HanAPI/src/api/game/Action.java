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
 * Classe che rappresenta una mossa come oggetto json. In base al valore dell'attributo "type" ci sono 3 tipi di Action:
 * <ul>
 *     <li>
 *         {<br>
 *             "player": nome del giocatore che effettua la mossa (string)<br>
 *             "type"  : "play" | "discard"<br>
 *             "card"  : indice della carta nella mano del giocatore che effettua la mossa (string)<br>
 *         }
 *     </li>
 *     <li>
 *         {<br>
 *             "player"        : nome del giocatore che effettua la mossa (string)<br>
 *             "type"          : "hint_color"<br>
 *             "hinted"        : nome del giocatore che riceve il suggerimento per colore(string)<br>
 *             "color"         : colore suggerito (string)<br>
 *             "cardsToReveal" : lista degli indici delle carte suggerite nella mano del giocatore che riceve il suggerimento (array)<br>
 *         }
 *     </li>
 *     <li>
 *         {<br>
 *             "player"        : nome del giocatore che effettua la mossa (string)<br>
 *             "type"          : "hint_value"<br>
 *             "hinted"        : nome del giocatore che riceve il suggerimento per valore(string)<br>
 *             "value"         : valore suggerito (string)<br>
 *             "cardsToReveal" : lista degli indici delle carte suggerite nella mano del giocatore che riceve il suggerimento (array)<br>
 *         }
 *     </li>
 * </ul>
 */
@SuppressWarnings({"WeakerAccess","unused"})
public class Action extends TypedJSON<JSONObject>
{
	/**
	 * Costruttore per una mossa che rappresenta la giocata o lo scarto di una carta
	 * @param player nome del giocatore che effettua la mossa
	 * @param type tipo di mossa ({@link ActionType#PLAY} o {@link ActionType#DISCARD})
	 * @param card indice della carta nella mano del giocatore che effettua la mossa
	 * @throws JSONException in caso di errore nella costruzione dell'oggetto json
	 */
	public Action(String player, ActionType type, int card) throws JSONException
	{
		super();
		json = new JSONObject();
		if (type == HINT_COLOR || type == HINT_VALUE)
			throw new JSONException("Il parametro type deve essere "+PLAY+" o "+DISCARD);
		setPlayer(player).setType(type).setCard(card);
	}

	/**
	 * Costruttore per una mossa che rappresenta un suggerimento per valore
	 * @param player nome del giocatore che effettua il suggerimento
	 * @param hinted nome del giocatore cui &egrave; rivolto il suggerimento
	 * @param value valore da suggerire
//	 * @param cardsToReveal lista degli indici delle carte suggerite nella mano del giocatore che riceve il suggerimento
	 * @throws JSONException in caso di errore nella costruzione dell'oggetto json
	 */
	public Action(String player, String hinted, int value/*, List<Integer> cardsToReveal*/) throws JSONException
	{
		super();
		json = new JSONObject();
		setPlayer(player).setType(HINT_VALUE).setHinted(hinted).setValue(value)/*.setCardsToReveal(cardsToReveal)*/;
	}

	/**
	 * Costruttore per una mossa che rappresenta un suggerimento per colore
	 * @param player nome del giocatore che effettua il suggerimento
	 * @param hinted nome del giocatore cui &egrave; rivolto il suggerimento
	 * @param color colore da suggerire
//	 * @param cardsToReveal lista degli indici delle carte suggerite nella mano del giocatore che riceve il suggerimento
	 * @throws JSONException in caso di errore nella costruzione dell'oggetto json
	 */
	public Action(String player, String hinted, Color color/*, List<Integer> cardsToReveal*/) throws JSONException
	{
		super();
		json = new JSONObject();
		setPlayer(player).setType(HINT_COLOR).setHinted(hinted)/*.setCardsToReveal(cardsToReveal)*/.setColor(color);
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
		json = new JSONObject(reader);

		if (json.names().size()!=0)
		{
			checkPlayer();
			checkType();
			ActionType type = getType();
			if ((type == PLAY) || (type == DISCARD))
			{
				checkCard();
			}
			else
			{
				if (type == HINT_COLOR)
					checkColor();
				else
					checkValue();
//				checkCardsToReveal();
				checkHinted();
			}
		}
	}

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "card"
	 * @throws JSONException se l'attributo "card" &egrave; mancante, se non &egrave; un intero o se non &egrave; previsto dal tipo di Action
	 */
	private void checkCard() throws JSONException
	{
		ActionType type = getType();
		if (!((type == PLAY) || (type == DISCARD)))
			throw new JSONException("L'attributo \"card\" non è posseduto da Action rappresentanti un suggerimento");

		String s = json.getString("card");
		if (s == null)
			throw new JSONException("Attributo \"card\" mancante");

		int max = Game.getInstance().getNumberOfCardsPerPlayer();
		try {
			int c = Integer.parseInt(s);
			if (c<0 || c>max-1)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			throw new JSONException("Attributo \"card\" deve essere un intero compreso tra 0 e "+(max-1)+" inclusi");
		}
	}

	/**
	 * @return la posizione della carta da giocare o scartare nella mano del giocatore che effettua la mossa, -1 se l'Action non possiede un campo "card"
	 **/
	public int getCard()
	{
		String c = json.getString("card");
		if (c!=null)
			return Integer.parseInt(json.getString("card"));
		else
			return -1;
	}

	/**
	 * Consente di impostare l'attributo "card" di questa Action
	 * @param c il valore da assegnare all'attributo "card"
	 * @return questa Action modificata
	 * @throws JSONException in caso di errori nell'impostazione
	 */
	public Action setCard(int c) throws JSONException
	{
		int c1 = getCard();
		json.set("card",""+c);
		try
		{
			checkCard();
		}
		catch (JSONException e)
		{
			if (c1>-1)
				json.set("card",""+c1);
			throw e;
		}
		return this;
	}

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "cardsToReveal"
	 * @throws JSONException se l'attributo "cardsToReveal" &egrave; mancante, se non &egrave; un {@link JSONArray} di interi rappresentanti posizioni di carte in una mano, o se non &egrave; previsto dal tipo di Action
	 */
/*	private void checkCardsToReveal() throws JSONException
	{
		ActionType type = getType();
		if ((type == PLAY) || (type == DISCARD))
			throw new JSONException("L'attributo \"cardsToReveal\" è posseduto solo da Action rappresentanti un suggerimento");

		JSONArray a = json.getArray("cardsToReveal");
		if (a == null)
			throw new JSONException("Attributo \"cardsToReveal\" mancante");
		else
		{
			int max = Game.getInstance().getNumberOfCardsPerPlayer();
			int i;
			ArrayList<Integer> l = new ArrayList<>();
			try {
				for (JSONData d:a) {
					i = Integer.parseInt(d.toString(0));
					if (i<0 || i>(max-1) || l.contains(i))
						throw new NumberFormatException();
					l.add(i);
				}
			}
			catch(NumberFormatException nfe)
			{
				throw new JSONException("L'attributo \"cardsToReveal\" deve contenere solo interi non ripetuti compresi tra 0 e "+(max-1)+" inclusi");
			}
		}
	}
*/
	/**
	 * @return un array contenente le posizioni nella mano del ricevente il suggerimento delle carte cui il suggerimento si riferisce
	 */
	public List<Integer> getCardsToReveal(State current) throws IllegalStateException
	{
		if (this.getType() == PLAY || this.getType() == DISCARD)
			return null;
		Hand hand = current.getHand(this.getHinted());
		ArrayList<Integer> list = new ArrayList<>();
		if (this.getType() == HINT_VALUE)
		{
			for (int i=0; i<hand.size(); i++)
			{
				if (hand.getCard(i).getValue() == 0)
					throw new IllegalStateException("Carta "+i+" sconosciuta");
				if (hand.getCard(i).getValue() == this.getValue())
					list.add(i);
			}
		}
		else if (this.getType() == HINT_COLOR)
		{
			for (int i=0; i<hand.size(); i++)
			{
				if (hand.getCard(i).getColor() == null)
					throw new IllegalStateException("Carta "+i+" sconosciuta");
				if (hand.getCard(i).getColor() == this.getColor())
					list.add(i);
			}
		}
		return list;
	}
/*	public List<Integer> getCardsToReveal()
	{
		JSONArray cards = json.getArray("cardsToReveal");
		if (cards != null)
		{
			ArrayList<Integer> c = new ArrayList<>();
			for(JSONData d: cards)
				c.add(Integer.parseInt(d.toString(0)));
			return c;
		}
		return null;
	}
*/
	/**
	 * Consente di impostare l'attributo "cardsToReveal" di questa Action
	 * @param cardsToReveal il valore da assegnare all'attributo "cardsToReveal"
	 * @return questa Action modificata
	 * @throws JSONException in caso di errori nell'impostazione
	 */
/*	public Action setCardsToReveal(List<Integer> cardsToReveal) throws JSONException
	{
		JSONArray array1 = json.getArray("cardsToReveal");

		JSONArray array = new JSONArray();
		for (int i:cardsToReveal)
			array.add("" + i);
		json.set("cardsToReveal", array);
		try
		{
			checkCardsToReveal();
		}
		catch(JSONException e)
		{
			if (array1 != null)
				json.set("cardsToReveal",array1);
			throw e;
		}
		return this;
	}
*/
	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "color"
	 * @throws JSONException se l'attributo "color" &egrave; mancante, se non &egrave; un valore previsto dalla classe {@link Color} o se non &egrave; previsto dal tipo di Action
	 */
	private void checkColor() throws JSONException
	{
		ActionType type = getType();
		if (type != HINT_COLOR)
			throw new JSONException("L'attributo \"color\" è posseduto solo da Action rappresentanti un suggerimento per colore");

		String s = json.getString("color");
		if (s == null)
			throw new JSONException("Attributo \"color\" mancante");
		if (Color.fromString(s) == null)
			throw new JSONException("Attributo \"color\" deve essere uno tra "+Arrays.toString(Color.values()));
	}

	/**
	 * @return il colore suggerito, null se l'Action non possiede un attributo "color"
	 **/
	public Color getColor()
	{
		String c = json.getString("color");
		if (c==null)
			return null;
		return Color.fromString(c);
	}

	/**
	 * Consente di impostare il campo "color" di questa Action
	 * @param color il valore da assegnare all'attributo "color"
	 * @return questa Action modificata
	 * @throws JSONException in caso di errore nell'impostazione
	 */
	public Action setColor(Color color) throws JSONException
	{
		Color c = getColor();
		json.set("color",color.toString().toLowerCase());
		try
		{
			checkColor();
		}
		catch(JSONException e)
		{
			if (c!=null)
				json.set("color",c.toString());
			throw e;
		}
		return this;
	}

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "hinted"
	 * @throws JSONException se l'attributo "hinted" &egrave; mancante
	 */
	private void checkHinted() throws JSONException
	{
		String s = json.getString("hinted");
		if (s == null)
			throw new JSONException("Attributo \"hinted\" mancante");
		if (!Game.getInstance().isPlaying(s))
			throw new JSONException("Giocatore "+s+" sconosciuto");
	}

	/**
	 * @return il nome del giocatore che riceve questo suggerimento, null se l'Action non contiene un attributo "hinted"
	 **/
	public String getHinted()
	{
		return json.getString("hinted");
	}

	/**
	 * Consente di impostare l'attributo "hinted" di questa Action
	 * @param player valore da assegnare all'attributo "hinted"
	 * @return questa Action modificata
	 * @throws JSONException in caso di errore nell'impostazione
	 */
	public Action setHinted(String player) throws JSONException
	{
		String hinted = getHinted();
		json.set("hinted",player);
		try
		{
			checkHinted();
		}
		catch(JSONException e)
		{
			if (hinted!=null)
				json.set("hinted",hinted);
			throw e;
		}
		return this;
	}

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "player"
	 * @throws JSONException se l'attributo "player" &egrave; mancante
	 */
	private void checkPlayer() throws JSONException
	{
		String s = json.getString("player");
		if (s == null)
			throw new JSONException("Attributo \"player\" mancante");
		if (!Game.getInstance().isPlaying(s))
			throw new JSONException("Giocatore "+s+" sconosciuto");
	}

	/**
	 * @return il nome del giocatore che esegue l'azione, null se l'Action non possiede un attributo "player"
	 **/
	public String getPlayer(){return json.getString("player");}

	/**
	 * Consente di impostare l'attributo "player" di questa Action
	 * @param s il valore da assegnare all'attributo "player"
	 * @return questa Action modificata
	 * @throws JSONException in caso di errore nell'impostazione
	 */
	public Action setPlayer(String s) throws JSONException
	{
		String player = getPlayer();
		json.set("player",s);
		try {
			checkPlayer();
		}
		catch(JSONException e)
		{
			if (player != null)
				json.set("player",player);
			throw e;
		}
		return this;
	}

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "type"
	 * @throws JSONException se l'attributo "type" &egrave; mancante o di valore diverso da quelli definiti da {@link ActionType}
	 */
	private void checkType() throws JSONException
	{
		String t = json.getString("type");
		if (t == null)
			throw new JSONException("Attributo \"type\" mancante");
		if (ActionType.fromString(t) == null)
			throw new JSONException("L'attributo \"type\" può assumere valori "+Arrays.toString(ActionType.values()));

	}

	/**
	 * @see ActionType
	 * @return il tipo di Action, null se l'Action non possiede un attributo "type"
	 **/
	public ActionType getType(){return ActionType.fromString(json.getString("type"));}

	/**
	 * Consente di impostare l'attributo "type" di questa Action
	 * @param type il valore da assegnare all'attributo "type"
	 * @return questa Action modificata
	 */
	public Action setType(ActionType type)
	{
		if ((type != null)&&(type.toString()!=null))
			json.set("type",type.toString().toLowerCase());
		return this;
	}

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "value"
	 * @throws JSONException se l'attributo "value" &egrave; mancante o se il suo valore non &egrave; un intero compreso tra 1 e 5 inclusi
	 */
	private void checkValue() throws JSONException
	{
		String s = json.getString("value");
		if (s == null)
			throw new JSONException("Attributo \"value\" mancante");
		try
		{
			int i = Integer.parseInt(s);
			if (i<1 || i>5)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			throw new JSONException("Attributo \"value\" deve essere un intero compreso tra 1 e 5 inclusi");
		}
	}

	/**
	 * @return il valore delle carte suggerite, -1 se l'Action non possiede un attributo "value"
	 **/
	public int getValue()
	{
		String s = json.getString("value");
		if (s==null)
			return -1;
		return Integer.parseInt(s);
	}

	/**
	 * Consente di impostare l'attributo "value" di questa Action
	 * @param v il valore da assegnare all'attributo "value"
	 * @return questa Action modificata
	 * @throws JSONException in caso di errore nell'impostazione
	 */
	public Action setValue(int v) throws JSONException
	{
		int v1 = getValue();
		json.set("value",""+v);
		try
		{
			checkValue();
		}
		catch(JSONException e)
		{
			if (v1>-1)
				json.set("value",""+v1);
			throw e;
		}
		return this;
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
	 * @return una rappresentazione testuale (non formattata json) di questa Action. Usa {@link Action#toString(int)} per una rappresentazione in formato json.
	 * */
	public String toString()
	{
		ActionType type = getType();
		String p = getPlayer();
		if (type == PLAY)
			return p+"("+Game.getInstance().getPlayerTurn(p)+") gioca la carta in posizione "+getCard()+" nella propria mano";
		else if (type == DISCARD)
			return p+"("+Game.getInstance().getPlayerTurn(p)+") scarta la carta in posizione "+getCard()+" nella propria mano";
		else {
			String h = getHinted();
			if (type == HINT_COLOR)
				return p+"("+Game.getInstance().getPlayerTurn(p)+") mostra le carte di colore "+getColor()+" a "
						+h+"("+Game.getInstance().getPlayerTurn(h)+")";
			else if (type == HINT_VALUE)
				return p+"("+Game.getInstance().getPlayerTurn(p)+") mostra le carte di valore "+getValue()+" a "
						+h+"("+Game.getInstance().getPlayerTurn(h)+")";
		}
		return "";
	}

	public static List<Action> getAllAction(String player)
	{
		try {
			ArrayList<Action> list = new ArrayList<>();

			for (int i = 0; i < Game.getInstance().getNumberOfCardsPerPlayer(); i++)
			{
				list.add(new Action(player, PLAY, i));
				list.add(new Action(player,DISCARD,i));
			}

			for (String name : Game.getInstance().getPlayers())
			{
				if (!name.equals(player))
				{
					for (int i=1; i<6; i++)
						list.add(new Action(player,name,i));
					for (Color color:Color.values())
						list.add(new Action(player,name,color));
				}
			}

			return list;
		}
		catch(JSONException e){return null;}
	}

}
