package api.game;

import agents.Agent;
import sjson.JSONArray;
import sjson.JSONObject;
import sjson.JSONObjectConvertible;
import sjson.JSONString;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Classe che rappresenta lo stato di una partita dal punto di vista di un giocatore
 * @author Francesco Pandolfi, Mihail Bida
 */
public class State implements Cloneable, JSONObjectConvertible
{
	private String[] players;
	private Stack<Card> discards;
	private Map<Color,Stack<Card>> fireworks;
	private Card[][] hands;
	private int order;
	private int hints;
	private int fuse;
	private State previousState;
	private Action previousAction;
	private int currentPlayer;
	private int finalAction;

	/**Costruttore del primo stato di una partita.
	 * @param players l'array che contiene i giocatori nell'ordine in cui devono giocare
	 * @param deck il mazzo di carte, già mischiato
	 * @throws IllegalArgumentException se i parametri sono null o di dimensione sbagliata**/
	public State(Agent[] players, Stack<Card> deck) throws IllegalArgumentException
	{
		if(players==null || players.length<2 || players.length >5 || deck == null || deck.size() !=50)
			throw new IllegalArgumentException("incorrect parameters");
		this.players = players.clone();
		discards = new Stack<>();
		fireworks = new HashMap<>();
		for(Color c: Color.values())fireworks.put(c,new Stack<>());
		hands = new Card[players.length][players.length>3?4:5];
		for(int i = 0; i<hands.length; i++)
		{
			for (int j = 0; j < hands[i].length; j++)
			{
				hands[i][j] = deck.pop();
				hands[i][j].setOwner(this.players[i]);
			}
		}
		order = 0;
		hints = 8;
		fuse = 3;
		currentPlayer = 0;
		finalAction = -1;
	}



	/**
	 * Verifica la legittimità di una mossa effettuata nello stato corrente.
	 * @param a la mossa da verificare
	 * @return true se la mossa è legittima, false altrimenti.
	 * @throws IllegalActionException se il parametro è null
	 **/
	public boolean legalAction(Action a) throws IllegalActionException{
		if(a==null) throw new IllegalActionException("Action is null");
		if(a.getPlayer()!= currentPlayer) return false;
		switch(a.getType()){
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

	/**
	 * Restituisce i nomi dei giocatori, nell'ordine in cui giocano
	 * @return un array che contiene una String per ogni giocatore
	 **/
	public String[] getPlayers(){
		return players;
	}

	/**
	 * Restituisce la mano del giocatore selezionato
	 * @param player l'indice del giocatore selezionato
	 * @return Un array di Card, che rappresenta la mano di un giocatore.
	// * @throws ArrayIndexOutOfBounds se non esiste un giocatore di indice indicato.
	 **/
	public Card[] getHand(int player)throws ArrayIndexOutOfBoundsException{
		if(player<0 || player>= players.length) throw new ArrayIndexOutOfBoundsException();
//		if(player==observer) return new Card[hands[player].length];
		return hands[player].clone();
	}

	/**
	 * @return lo stato precedente
	 **/
	public State getPreviousState(){
		State s = previousState;
		return s;
	}

	/**
	 * @return l'ultima mossa fatta
	 **/
	public Action getPreviousAction(){return previousAction;}

	/**
	 * Gets the card played in the previous move,
	 * or null if it is the first move,
	 * or the previous move was a hintPlayable.
	 * @return the card played in the previous action,
	 * or null if there is no previous action, or the action was a hintPlayable.
	 * */
/*	public Card previousCardPlayed(){
		try{
			return previousState.hands[previousAction.getPlayer()][previousAction.getCard()];
		}
		catch(Exception e){return null;}
	}
*/

	/**
	 * @return una copia dello Stack di carte scartate
	 **/
	public Stack<Card> getDiscards(){return (Stack<Card>) discards.clone();}

	/**
	 * Get the stack of cards representing the specified firework
	 * @return a clone of the stack of cards representing the firework of the given colour. The highest card is at the top of the stack.
	 **/
	public Stack<Card> getFirework(Color c){return (Stack<Card>) fireworks.get(c).clone();}

	/**
	 * Get the number of hintPlayable tokens available
	 * @return number of hints remaining
	 **/

	public int getHintTokens(){return hints;}

	/**
	 * Get the number of fuse tokens available
	 * @return number of fuse tokens remaining
	 **/
	public int getFuseTokens(){return fuse;}

	/**
	 * Gets the nextplayer, or -1 if gameOver
	 * @return the agent index of the nextplayer, or -1 if the game is over.
	 **/
	public int getCurrentPlayer(){return (gameOver()?-1: currentPlayer);}

	/**
	 * Gets the order of the state, starting from 1.
	 * @return the order the state appears in the game, from first (1) to last.
	 **/
	public int getOrder(){return order;}

	/**
	 * Returns the order of the final action, if it is known.
	 * When the final card is drawn from the deck, every player has one more action remaining.
	 * The order of the final action, is the order of the action that drew the final card,
	 * plus the number of players in the game.
	 * @return the order of the final action, or -1 if the deck is not empty
	 **/
	public int getFinalActionIndex(){return finalAction;}

	/**
	 * Get the current score
	 * @return the sum of the highest value cards in each firework
	 **/
	public int getScore(){
		int score = 0;
		if(fuse==0) return 0;
		for(Color c: Color.values())
			if(!fireworks.get(c).isEmpty())score+=fireworks.get(c).peek().getValue();
		return score;
	}

	/**
	 * Tests if the game is over
	 * @return true if all fireworks have been made, the deck has run out, or a fues has exploded.
	 **/
	public boolean gameOver(){
		return ((finalAction!=-1 &&order==finalAction+1) || fuse == 0 || getScore()==25);
	}

	/**
	 * Produces a clone of the state
	 **/
	public Object clone(){
		try{
			State s = (State) super.clone();
			s.players = players.clone();
			s.discards = (Stack<Card>)discards.clone();
			s.hands = new Card[hands.length][hands[0].length];
			for(int i = 0; i<hands.length; i++)
			{
				for (int j=0; j<s.hands[i].length; j++)
					s.hands[i][j] = hands[i][j].clone();
			}
			s.fireworks = (Map<Color,Stack<Card>>)((HashMap)fireworks).clone();
			for(Color c: Color.values()) s.fireworks.put(c,(Stack<Card>)fireworks.get(c).clone());
			return s;
		}
		catch(CloneNotSupportedException e){return null;}
	}

	/**
	 * Returns a string describing the state of the game, including:
	 * the state of each players hand;
	 * the top card of each fire work;
	 * and the last action.
	 * */
	public String toString(){
		String ret = "State: "+order+"\n";
//		ret+="Last move: "+previousAction+"\n";
//		ret+="Observer: "+observer+"\n";
		ret+="Players' hands:\n";
		for(int i = 0; i< players.length; i++){
			ret+="\t"+ players[i].getName()+" ("+i+"): ";
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

	/**
	 * Crea una rappresentazione in testo JSON dello stato corrente. Nel JSON non sono rappresentati l'ultima azione e lo stato
	 * precedente.
	 * @return il JSONObject che rappresenta lo stato.
	 */
	@Override
	public JSONObject toJSON()
	{
		JSONObject obj = new JSONObject();
		JSONArray dis = new JSONArray();
		for (Card c: discards)
			dis.add(c.toJSON());
		obj.put("discarded",dis);
		JSONArray array = new JSONArray();
		for (Card c: this.fireworks.get(Color.BLUE))
			array.add(c.toJSON());
		obj.put("blue",array);
		array = new JSONArray();
		for (Card c: this.fireworks.get(Color.GREEN))
			array.add(c.toJSON());
		obj.put("green",array);
		array = new JSONArray();
		for (Card c: this.fireworks.get(Color.YELLOW))
			array.add(c.toJSON());
		obj.put("yellow",array);
		array = new JSONArray();
		for (Card c: this.fireworks.get(Color.WHITE))
			array.add(c.toJSON());
		obj.put("white",array);
		array = new JSONArray();
		for (Card c: this.fireworks.get(Color.RED))
			array.add(c.toJSON());
		obj.put("red",array);
		obj.put("fuse",""+fuse);
		obj.put("hints",""+hints);
		obj.put("order",""+order);
		obj.put("current",""+currentPlayer);
		obj.put("gameover",""+this.gameOver());
		array = new JSONArray();
		for(String s:players)
			array.add(new JSONString(s));
		obj.put("names",array);

		JSONArray hands= new JSONArray();
		for (int i=0; i<players.length; i++)
		{
			array = new JSONArray();
			for (Card c: getHand(i))
				array.add(c.toJSON());
			hands.add(array);
		}
		obj.put("hands",hands);
		return obj;
	}
}



