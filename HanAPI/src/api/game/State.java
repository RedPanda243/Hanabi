package api.game;

import sjson.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Classe che rappresenta lo stato di una partita dal punto di vista di un giocatore
 * @author Francesco Pandolfi, Mihail Bida
 */
public class State extends JSONConvertible<JSONObject>
{
	private Stack<Card> discards;
	private Map<Color,Stack<Card>> fireworks;
	private Hand[] hands;
	private int order;
	private int hints;
	private int fuse;
	private int currentPlayer;
	private int finalAction;

	public State(JSONObject json) throws JSONException
	{
		super(json);
		discards = new Stack<>();
		JSONArray array = json.optArray("discarded");
		if (array == null)
			throw new JSONException("Missing discarded");
		else
		{
			JSONObject obj;
			for(int i=0; i<array.size(); i++)
			{
				obj = array.optJSON(i);
				if (obj == null)
					throw new JSONException("");
				else
					discards.push(new Card(obj));
			}
		}

		fireworks = new HashMap<>();
		Stack<Card> box = new Stack<>();
		array = json.optArray("red");
		if (array == null)
			throw new JSONException("Missing red");
		else
		{
			JSONObject obj;
			for(int i=0; i<array.size(); i++)
			{
				obj = array.optJSON(i);
				if (obj == null)
					throw new JSONException("");
				else
					box.push(new Card(obj));
			}
		}
		fireworks.put(Color.RED,box);
		box = new Stack<>();
		array = json.optArray("white");
		if (array == null)
			throw new JSONException("Missing white");
		else
		{
			JSONObject obj;
			for(int i=0; i<array.size(); i++)
			{
				obj = array.optJSON(i);
				if (obj == null)
					throw new JSONException("");
				else
					box.push(new Card(obj));
			}
		}
		fireworks.put(Color.WHITE,box);
		box = new Stack<>();
		array = json.optArray("blue");
		if (array == null)
			throw new JSONException("Missing blue");
		else
		{
			JSONObject obj;
			for(int i=0; i<array.size(); i++)
			{
				obj = array.optJSON(i);
				if (obj == null)
					throw new JSONException("");
				else
					box.push(new Card(obj));
			}
		}
		fireworks.put(Color.BLUE,box);
		box = new Stack<>();
		array = json.optArray("green");
		if (array == null)
			throw new JSONException("Missing green");
		else
		{
			JSONObject obj;
			for(int i=0; i<array.size(); i++)
			{
				obj = array.optJSON(i);
				if (obj == null)
					throw new JSONException("");
				else
					box.push(new Card(obj));
			}
		}
		fireworks.put(Color.GREEN,box);
		box = new Stack<>();
		array = json.optArray("yellow");
		if (array == null)
			throw new JSONException("Missing yellow");
		else
		{
			JSONObject obj;
			for(int i=0; i<array.size(); i++)
			{
				obj = array.optJSON(i);
				if (obj == null)
					throw new JSONException("");
				else
					box.push(new Card(obj));
			}
		}
		fireworks.put(Color.YELLOW,box);

		array = json.optArray("hands");
		hands = new Hand[array.size()];
		for (int i=0; i<hands.length; i++)
			hands[i] = new Hand(array.optArray(i));

		try
		{
			order = Integer.parseInt(json.optString("order"));
			if (order<0)
				throw new NumberFormatException();
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}

		try
		{
			hints = Integer.parseInt(json.optString("hints"));
			if (hints<0 || hints>8)
				throw new NumberFormatException();
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}

		try
		{
			fuse = Integer.parseInt(json.optString("fuse"));
			if (fuse<0 || fuse>3)
				throw new NumberFormatException();
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}

		try
		{
			currentPlayer = Integer.parseInt(json.optString("current"));
			if (currentPlayer<0 || currentPlayer>Game.getInstance().getPlayers().length-1)
				throw new NumberFormatException();
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}

		try
		{
			finalAction = Integer.parseInt(json.optString("final"));
			if (finalAction<-1)
				throw new NumberFormatException();
		}
		catch (NumberFormatException e)
		{
			throw new JSONException(e);
		}
	}

	public State clone()
	{
		return (State)super.clone();
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
		return hands[player].clone();
	}

	/**
	 * @return una copia dello Stack di carte scartate
	 **/
	public Stack<Card> getDiscards()
	{
		Stack<Card> stack = new Stack<>();
		for(Card c:discards)
		{
			stack.push(c.clone());
		}
		return stack;
	}

	/**
	 * @return una copia dello stack di carte giocate del colore dato. La carta di valore più alto è in cima allo stack
	 **/
	public Stack<Card> getFirework(Color c)
	{
		Stack<Card> stack = new Stack<>();
		for(Card ca:fireworks.get(c))
		{
			stack.push(ca.clone());
		}
		return stack;
	}

	/**
	 * @return il numero di gettoni informazione rimasti
	 **/
	public int getHintTokens(){return hints;}

	/**
	 * @return il numero di gettoni errore rimasti
	 **/
	public int getFuseTokens(){return fuse;}

	/**
	 * @return l'indice del giocatore a cui tocca, -1 se il gioco è finito
	 **/
	public int getCurrentPlayer(){return (gameOver()?-1: currentPlayer);}

	/**
	 * @return il numero di turno
	 **/
	public int getOrder(){return order;}

	/**
	 * Per azione finale si intende l'azione che fa pescare l'ultima carta del mazzo. Dopo l'azione finale tutti i giocatori hanno un ultimo turno
	 * @return il numero di turno dell'azione finale, -1 se il mazzo non è vuoto
	 **/
	public int getFinalActionIndex(){return finalAction;}

	/**
	 * @return il punteggio corrente (somma dei valori delle carte in cima agli stack delle carte giocate)
	 **/
	public int getScore(){
		int score = 0;
		if(fuse==0) return 0;
		for(Color c: Color.values())
			if(!fireworks.get(c).isEmpty())score+=fireworks.get(c).peek().getValue();
		return score;
	}

	/**
	 * @return true se tutti gli stack sono completati (valore 5 in cima), se i gettoni errore sono terminati o se tutti hanno giocato un turno dopo l'azione finale
	 **/
	public boolean gameOver(){
		return ((finalAction!=-1 &&order==finalAction+1) || fuse == 0 || getScore()==25);
	}


	public String toString(){
		String ret = "State: "+order+"\n";
//		ret+="Last move: "+previousAction+"\n";
//		ret+="Observer: "+observer+"\n";
		ret+="Players' hands:\n";
		for(int i=0; i<Game.getInstance().getPlayers().length; i++){
			ret+="\t"+ Game.getInstance().getPlayers()[i]+" ("+i+"): ";
//			System.err.println(Arrays.toString(hands[i]));
			for(Card c: hands[i]) {
				if (c!=null)
					ret += c.toString() + " ";
			}
			ret+="\n";
		}
//		System.err.println();
		ret+="Fireworks:\n";
		for(Color c: Color.values())
			ret+="\t"+c+"  "+(fireworks.get(c).isEmpty()? "-" : fireworks.get(c).peek().getValue()) +"\n";
		ret+= "Hints: "+hints+"\nFuse: "+fuse+"\n";
		/*
		if (observer>-1) {
			ret+=("Color observer: " +players[observer].getKnownColours(0)+" "+players[observer].getKnownColours(1)+" "+players[observer].getKnownColours(2)+" "+players[observer].getKnownColours(3)+" "+players[observer].getKnownColours(4)+"\n");
			ret+=("Values observer: " +players[observer].getKnownValues(0)+" "+players[observer].getKnownValues(1)+" "+players[observer].getKnownValues(2)+" "+players[observer].getKnownValues(3)+" "+players[observer].getKnownValues(4)+"\n");
		}
		*/
		return ret;
	}
}



