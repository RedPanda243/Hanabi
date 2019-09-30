package api.game;

import api.main.HanabiClient;
import sjson.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Classe che rappresenta lo stato di una partita dal punto di vista di un giocatore
 * @author Francesco Pandolfi, Mihail Bida
 */
@SuppressWarnings({"WeakerAccess","unused"})
public class State extends JSONObject
{
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

		Card c;
		array = getArray("red");
		if (array == null)
			throw new JSONException("Missing red firework");
		else
		{
			if (array.size()>5)
				throw new JSONException("Malformed red firework stack, more than 5 cards!");
			for(int i=0; i<array.size(); i++)
			{
				c = new Card(array.get(i).toString());
				if (c.getValue()!=i)
					throw new JSONException("Malformed red firework stack, messy cards!");
				if (!c.getColor().equals(Color.RED))
					throw new JSONException("Malformed red firework stack, card with wrong color!");
				array.replace(i,c);
			}
		}

		array = getArray("green");
		if (array == null)
			throw new JSONException("Missing green firework");
		else
		{
			if (array.size()>5)
				throw new JSONException("Malformed green firework stack, more than 5 cards!");
			for(int i=0; i<array.size(); i++)
			{
				c = new Card(array.get(i).toString());
				if (c.getValue()!=i)
					throw new JSONException("Malformed green firework stack, messy cards!");
				if (!c.getColor().equals(Color.GREEN))
					throw new JSONException("Malformed green firework stack, card with wrong color!");
				array.replace(i,c);
			}
		}

		array = getArray("blue");
		if (array == null)
			throw new JSONException("Missing blue firework");
		else
		{
			if (array.size()>5)
				throw new JSONException("Malformed blue firework stack, more than 5 cards!");
			for(int i=0; i<array.size(); i++)
			{
				c = new Card(array.get(i).toString());
				if (c.getValue()!=i)
					throw new JSONException("Malformed blue firework stack, messy cards!");
				if (!c.getColor().equals(Color.BLUE))
					throw new JSONException("Malformed blue firework stack, card with wrong color!");
				array.replace(i,c);
			}
		}

		array = getArray("white");
		if (array == null)
			throw new JSONException("Missing white firework");
		else
		{
			if (array.size()>5)
				throw new JSONException("Malformed white firework stack, more than 5 cards!");
			for(int i=0; i<array.size(); i++)
			{
				c = new Card(array.get(i).toString());
				if (c.getValue()!=i)
					throw new JSONException("Malformed white firework stack, messy cards!");
				if (!c.getColor().equals(Color.WHITE))
					throw new JSONException("Malformed white firework stack, card with wrong color!");
				array.replace(i,c);
			}
		}

		array = getArray("yellow");
		if (array == null)
			throw new JSONException("Missing yellow firework");
		else
		{
			if (array.size()>5)
				throw new JSONException("Malformed yellow firework stack, more than 5 elements!");
			for(int i=0; i<array.size(); i++)
			{
				c = new Card(array.get(i).toString());
				if (c.getValue()!=i)
					throw new JSONException("Malformed yellow firework stack, messy cards!");
				if (!c.getColor().equals(Color.YELLOW))
					throw new JSONException("Malformed yellow firework stack, card with wrong color!");
				array.replace(i,c);
			}
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

	public State clone()
	{
		try
		{
			return new State(super.clone().toString(0));
		}
		catch(JSONException e){return null;}
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
		switch(a.getActionType()){
			case PLAY:
				return (a.getCard()>=0 && a.getCard()<hands[currentPlayer].length);
			case DISCARD:
				if(hints==8) throw new IllegalActionException("Discards cannot be made when there are 8 hint tokens");
				return (a.getCard()>=0 && a.getCard()<hands[currentPlayer].length);
			default:
				if(hints==0 || a.getHintReceiver() <0 || a.getHintReceiver()> players.length || a.getHintReceiver() == a.getPlayer()) return false;
				return true;
		}
	}
*/

	/**
	 * @param player l'indice del giocatore selezionato
	 * @return Un array di Card, che rappresenta la mano del giocatore selezionato.
	 * @throws ArrayIndexOutOfBoundsException se non esiste un giocatore di indice indicato.
	 **/
	public Hand getHand(int player)throws ArrayIndexOutOfBoundsException
	{
		return (Hand)getArray("hands").getArray(player);
	}

	/**
	 * @return una copia dello Stack di carte scartate
	 **/
	public JSONArray getDiscards()
	{
		return getArray("discarded");
	}

	/**
	 * @return una copia dello stack di carte giocate del colore dato. La carta di valore più alto è in cima allo stack
	 **/
	public JSONArray getFirework(Color c)
	{
		return getArray(c.toString().toLowerCase());
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
			throw new JSONException("Unacceptable player");
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

	public State setFuseToken(int x) throws JSONException
	{
		if (x<0 || x>3)
			throw new JSONException("Unacceptable fuse token value");
		set("fuse",""+x);
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
		for(int i=0; i<HanabiClient.getInstance().getGame().getPlayers().size(); i++){
			ret+="\t"+ HanabiClient.getInstance().getGame().getPlayers().getString(i)+" ("+i+"): ";
			Hand hand = getHand(i);
			for(JSONData c: hand) { //is a card!
				if (c!=null)
					ret += c.toString() + " ";
			}
			ret+="\n";
		}
//		System.err.println();
		ret+="Fireworks:\n";
		JSONArray fireworks;
		for(Color c: Color.values()) {
			fireworks = getFirework(c);
			ret += "\t" + c + "  " + (fireworks.size() == 0 ? "-" : ((Card)fireworks.get(fireworks.size()-1)).getValue()) + "\n";
		}
		ret+= "Hints: "+getHintTokens()+"\nFuse: "+getFuseTokens()+"\n";

		return ret;
	}
}



