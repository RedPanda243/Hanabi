package game;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONObjectConvertible;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static api.game.ActionType.DISCARD;
import static api.game.ActionType.PLAY;

/**
 * @author Francesco Pandolfi, Mihail Bida
 */
public class ServerState extends State
{



	/**
	 * Costruttore di default richiesto dal metodo clone() di JSONObject. In teoria non andrebbe mai usato fuori da questa classe.
	 * @param json
	 * @throws JSONException
	 */
	public ServerState(Reader reader) throws JSONException
	{
		super(reader);
		for (int i=0; i<hands.length; i++)
		{
			hands[i] = new ServerHand(hands[i].toJSON());
		}
		/*
		Stack<Card> discards1 = new Stack<>();
		for (int i=0; i<discards.size(); i++)
			discards1.push(new ServerCard(discards.elementAt(i).toJSON()));
		*/
		/*
		if(players==null || players.length<2 || players.length >5 || deck == null || deck.size() !=50)
			throw new IllegalArgumentException("incorrect parameters");
		this.players = players.clone();
		discards = new Stack<>();
		fireworks = new HashMap<>();
		for(Color c: Color.values())fireworks.put(c,new Stack<>());
		hands = new ServerCard[players.length][players.length>3?4:5];
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
		 */
	}

	public ServerState clone()
	{
		return (ServerState) super.clone();
	}

	/**
	 *Crea un nuovo stato a partire da quello attuale (this), l'ultima mossa fatta e il mazzo di carte da cui far pescare il giocatore.
	 *@param deck il mazzo
	 *@param action l'azione compiuta
	 *@throws IllegalActionException se l'azione compiuta è illecita nello stato attuale
	 **/
	public ServerState nextState(Action action, Stack<ServerCard> deck) throws IllegalActionException {
		if(!legalAction(action)) throw new IllegalActionException("Invalid action!: "+action);
		if(gameOver()) throw new IllegalActionException("Game Over!");
		JSONObject json = this.toJSON();

		ServerCard c;
		if (action.getActionType() == PLAY)
		{
			c = (ServerCard) hands[action.getPlayer()].getCard(action.getCard());
			Stack<Card> fw = fireworks.get(c.getColor());
			if((fw.isEmpty() && c.getValue() == 1) || (!fw.isEmpty() && fw.peek().getValue()==c.getValue()-1)){
				json.put(c.getColor().toString().toLowerCase(),json.optArray(c.getColor().toString().toLowerCase()).add(c));
//				s.fireworks.get(c.getColor()).push(c);
				if(s.fireworks.get(c.getColor()).size()==5 && s.hints<8) s.hints++;
			}
			else{
				s.discards.push(c);
				s.fuse--;
			}

			if(!deck.isEmpty())
				((ServerHand)s.hands[action.getHintReceiver()]).setCard(deck.pop(),action.getCard());
			else
				((ServerHand)s.hands[action.getHintReceiver()]).setCard(null,action.getCard());
			if(deck.isEmpty() && finalAction==-1) s.finalAction = order+ players.length;
		}

		switch(action.getActionType()){
			case PLAY:

				break;
			case DISCARD:
				c = hands[action.getPlayer()][action.getCard()];
				s.discards.push(c);
				if(!deck.isEmpty())
					((ServerHand)s.hands[action.getHintReceiver()]).setCard(deck.pop(),action.getCard());

				else
					((ServerHand)s.hands[action.getHintReceiver()]).setCard(null,action.getCard());
				//Occhio che qui il null può generare confusione. Di fatto però nella mano c'è un buco

				if(deck.isEmpty() && finalAction==-1) s.finalAction = order+ players.length;
				if(hints<8) s.hints++;
				break;
			case HINT_COLOR:
				s.hints--;
				for (int i=0; i<s.hands[action.getPlayer()].length; i++)
				{
					if (s.hands[action.getHintReceiver()].getCard(i).getColor().equals(action.getColor()))
						((ServerHand)s.hands[action.getHintReceiver()]).getCard(i).revealColour();
				}
				break;
			case HINT_VALUE:
				s.hints--;
				for (int i=0; i<s.hands[action.getPlayer()].length; i++)
				{
					if (s.hands[action.getHintReceiver()].getCard(i).getValue() == action.getValue())
						((ServerHand)s.hands[action.getHintReceiver()]).getCard(i).revealValue();
				}
				break;
			default: break;
		}
		s.order++;
		s.previousAction = action;
		s.currentPlayer = (currentPlayer +1)% players.length;
		s.previousState = this;
		return s;
	}

	/**
	 * Verifica la legittimità di una mossa effettuata nello stato corrente.
	 * @param a la mossa da verificare
	 * @return true se la mossa è legittima, false altrimenti.
	 * @throws IllegalActionException se il parametro è null
	 **/
	public boolean legalAction(Action a) throws IllegalActionException{
		if(a==null) throw new IllegalActionException("Action is null");
		if(a.getPlayer()!= getCurrentPlayer()) return false;
		if (a.getActionType() == PLAY || a.getActionType()==DISCARD)
			return (a.getCard()>=0 && a.getCard()<getHand(getCurrentPlayer()).size());
		if(getHintTokens()==0 || a.getHintReceiver() <0 || a.getHintReceiver()> players.length || a.getHintReceiver() == a.getPlayer()) return false;
		return true;
		}
	}



}



