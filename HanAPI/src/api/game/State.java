package api.game;

import sjson.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

/**
 * Classe che rappresenta lo stato di una partita dal punto di vista di un giocatore
 */
@SuppressWarnings({"WeakerAccess","unused"})
public class State extends JSONObject
{
	public State(Stack<Card> deck) throws JSONException
	{
		super();
		set("discarded",new JSONArray());
		set("action", new JSONObject());
		set("red",new Firework());
		set("green",new Firework());
		set("white",new Firework());
		set("blue",new Firework());
		set("yellow",new Firework());
		set("current",""+Game.getInstance().getPlayer(0));
		set("order",""+0);
		set("fuse",""+3);
		set("hints",""+8);
		set("final",""+-1);
		JSONArray names = Game.getInstance().getPlayers();
		Card[] cards = new Card[Game.getInstance().getNumberOfCardsPerPlayer()];
		for (JSONData n:names)
		{
			for (int i=0; i<cards.length; i++)
				cards[i] = deck.pop();
			set(n.toString(),new Hand(cards));
		}
	}

	public State(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public State(Reader reader) throws JSONException
	{
		super(reader);

		String s;

		JSONArray array = getArray("discarded");
		if (array == null)
			throw new JSONException("Missing discarded");
		else
		{
			for(int i=0; i<array.size(); i++)
				array.replace(i,new Card(array.get(i).toString()));
			this.set("discarded",array);
		}

		JSONObject act = getObject("action");
		if (act==null)
			throw new JSONException("Missing action!");
		try
		{
			setAction(new Action(act.toString(0)));
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}



		for (Color color:Color.values())
		{
			array = getArray(color.toString().toLowerCase());
			if (array == null)
				throw new JSONException("Missing "+color+" firework");
			setFirework(color,new Firework(array.toString(0)));
		}


		for(JSONData d: Game.getInstance().getPlayers())
		{
			array = getArray(d.toString());
			if (array == null)
				throw new JSONException("Missing "+d.toString()+" hand!");
			setHand(d.toString(),new Hand(array.toString(0)));
		}

		s = getString("order");
		if (s==null)
			throw new JSONException("Missing order!");
		try
		{
			setOrder(Integer.parseInt(s));
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}

		s = getString("hints");
		if (s==null)
			throw new JSONException("Missing hint tokens!");
		try
		{
			setHintToken(Integer.parseInt(s));
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}

		s = getString("fuse");
		if (s==null)
			throw new JSONException("Missing fuse tokens!");
		try
		{
			setFuseToken(Integer.parseInt(s));
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}

		s = getString("current");
		if (s == null)
			throw new JSONException("Missing current player!");
		setCurrentPlayer(s);

		s = getString("final");
		if (s == null)
			throw new JSONException("Missing final turn!");
		try
		{
			setFinalActionIndex(Integer.parseInt(s));
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}
	}

	public void setAction(Action action) {
		set("action", action);
	}

	public State clone()
	{
		try
		{
			return new State(super.clone().toString(0));
		}
		catch(JSONException e){e.printStackTrace(System.err);return null;}
	}

	/**
	 * Verifica la legittimità di una mossa effettuata nello stato corrente.
	 * @param a la mossa da verificare
	 * @return true se la mossa è legittima, false altrimenti.
	 * @throws IllegalActionException se il parametro è null
	 **/
/*	public boolean legalAction(Action a) throws IllegalActionException{
		if(a==null) throw new IllegalActionException("Action is null");
		if(a.getPlayer()!= currentPlayer) return false;
		switch(a.getType()){
			case PLAY:
				return (a.getCard()>=0 && a.getCard()<hands[currentPlayer].length);
			case DISCARD:
				if(hints==8) throw new IllegalActionException("Discards cannot be made when there are 8 hint tokens");
				return (a.getCard()>=0 && a.getCard()<hands[currentPlayer].length);
			default:
				if(hints==0 || a.getHinted() <0 || a.getHinted()> players.length || a.getHinted() == a.getPlayer()) return false;
				return true;
		}
	}
*/



	/**
	 * @return una copia dello Stack di carte scartate
	 **/
	public JSONArray getDiscards()
	{
		return getArray("discarded");
	}
	public Action getAction(){
		return (Action) get("action");
	}


	/**
	 * @return una copia dello stack di carte giocate del colore dato. La carta di valore più alto è in cima allo stack
	 **/
	public Firework getFirework(Color c)
	{
		return (Firework) get(c.toString().toLowerCase());
	}

	/**
	 * @param player l'indice del giocatore selezionato
	 * @return Un array di Card, che rappresenta la mano del giocatore selezionato, null se non esiste un giocatore di indice indicato.
	 **/
	public Hand getHand(String player)
	{
		return (Hand)get(player);
	}

	/**
	 * @return il numero di gettoni informazione rimasti
	 **/
	public int getHintTokens(){return Integer.parseInt(getString("hints"));}

	/**
	 * @return il numero di gettoni errore rimasti
	 **/
	public int getFuseTokens(){return Integer.parseInt(getString("fuse"));}

	/**
	 * @return il nome del giocatore a cui tocca, null se il gioco è finito
	 **/
	public String getCurrentPlayer()
	{
		if (gameOver())
			return null;
		return getString("current");
	}

	/**
	 * @return il numero di turno
	 **/
	public int getOrder(){return Integer.parseInt(getString("order"));}

	/**
	 * Per azione finale si intende l'azione che fa pescare l'ultima carta del mazzo. Dopo l'azione finale tutti i giocatori hanno un ultimo turno
	 * @return il numero di turno dell'azione finale, -1 se il mazzo non è vuoto
	 **/
	public int getFinalActionIndex(){return Integer.parseInt(getString("final"));}

	public State setCurrentPlayer(String player) throws JSONException
	{
		if (!Game.getInstance().isPlaying(player))
			throw new JSONException("Unacceptable player "+player);
		set("current",player);
		return this;
	}

	public State setFinalActionIndex(int index) throws JSONException
	{
		if (index<-1)
			throw new JSONException("Unacceptable index");
		set("final",""+index);
		return this;
	}

	public State setFirework(Color c, Firework f) throws JSONException
	{
		if (f.getColor()!=null && c!=f.getColor())
			throw new JSONException("Color mismatch. Color is "+c+" but cards are "+f.getColor());
		set(c.toString().toLowerCase(),f);
		return this;
	}

	public State setFuseToken(int x) throws JSONException
	{
		if (x<0 || x>3)
			throw new JSONException("Unacceptable fuse token value");
		set("fuse",""+x);
		return this;
	}

	public State setHand(String player, Hand hand)
	{
		set(player,hand);
		return this;
	}

	public State setHintToken(int x) throws JSONException
	{
		if (x<0 || x>8)
			throw new JSONException("Unacceptable hint token value");
		set("hints",""+x);
		return this;
	}

	public State setOrder(int o) throws JSONException
	{
		if (o<0)
			throw new JSONException("Unacceptable order");
		set("order",""+o);
		return this;
	}

	/**
	 * @return il punteggio corrente (somma dei valori delle carte in cima agli stack delle carte giocate)
	 **/
	public int getScore(){
		int score = 0;
		if(getFuseTokens()==0) return 0;
		JSONArray a;
		for(Color c: Color.values())
		{
			a = getFirework(c);
			if (a.size()>0)
				score += ((Card)a.getObject(a.size()-1)).getValue();
		}
		return score;
	}

	/**
	 * @return true se tutti gli stack sono completati (valore 5 in cima), se i gettoni errore sono terminati o se tutti hanno giocato un turno dopo l'azione finale
	 **/
	public boolean gameOver()
	{
		int f = getFinalActionIndex();
		return ((f!=-1 &&getOrder()==f+1) || getFuseTokens() == 0 || getScore()==25);
	}


	public String toString(){
		String ret = "State: "+getOrder()+"\n";
		ret+="Players' hands:\n";
		for(int i=0; i<Game.getInstance().getPlayers().size(); i++){
			ret+="\t"+ Game.getInstance().getPlayer(i)+" ("+i+"): "+getHand(Game.getInstance().getPlayer(i))+"\n";
		}
//		System.err.println();
		ret+="Fireworks:\n";
		Firework fireworks;
		for(Color c: Color.values()) {
			fireworks = getFirework(c);
			ret += "\t" + c + "  " + (fireworks.size() == 0 ? "-" : (fireworks.peak())) + "\n";
		}
		ret+= "Hints: "+getHintTokens()+"\nFuse: "+getFuseTokens()+"\n";

		return ret;
	}
}



