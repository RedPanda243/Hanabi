package agents;

import api.game.*;
import main.Main;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;
import math.MathCalc;

import java.util.ArrayList;
import java.util.List;

/**
 * Agente che segue questa strategia:
 * <ol>
 *     <li>
 *         Gioca una carta con 100% di playability
 *     </li>
 *     <li>
 *         Scarta una carta con 100% di uselessness se non ho 8 hint token
 *     </li>
 *     <li>
 *         Se ho hint token rimanenti uso ricerca in spazio degli stati per calcolare l'hint migliore
 *         @see Strategy1Agent#hint(State)
 *     </li>
 *     <li>
 *         Scarto carta con uselessness maggiore (sopra un certo limite)
 *     </li>
 *     <li>
 *         Gioco carta con playability maggiore
 *     </li>
 * </ol>
 */
public class Strategy1Agent extends AbstractAgent
{
	@Override
	public Action chooseAction(State current)
	{
		Action action = play100(current);
		if (action == null)
			action = discard100(current);
		if (action == null)
			action = hint(current);
		if (action == null)
			action = discard(current);
		if (action == null)
			action = play(current);
		return action;
	}

	/**
	 * L'aiuto migliore viene scelto secondo questo algoritmo, ripetuto ad ogni passo per ogni giocatore, partendo dal più vicino
	 * <ol>
	 *     <li>
	 *         Suggerisci in modo da rendere giocabile al 100% una carta
	 *     </li>
	 *     <li>
	 *         Suggerisci un 5
	 *     </li>
	 *     <li>
	 *         Dai un suggerimento sull'ultima carta pescata se questa è giocabile. Scegli tra valore e colore in base a quante
	 *         carte vengono coinvolte nell'hint
	 *     </li>
	 *     <li>
	 *         Dai il suggerimento che coinvolge più carte.
	 *         (Qui in teoria andrebbe usata la ricerca nello spazio degli stati per calcolare il migliore!!!)
	 *     </li>
	 * </ol>
	 * @param current
	 * @return
	 */
	public Action hint(State current)
	{
		Action action = null;
		if (current.getHintTokens()>0)
		{
			try {
				action = hint1(current);
				if (action == null)
					action = hint2(current);
				if (action == null)
					action = hint3(current);
				if (action == null)
					action = hint4(current);
			}
			catch(JSONException e)
			{
				e.printStackTrace(System.err);
				action = null;
			}
		}
		return action;
	}

	private Action hint1(State current) throws JSONException
	{
		JSONArray players = sortPlayers();
		Hand hand;
		for (JSONData p: players)
		{
			hand = current.getHand(p.toString()).clone();
			for (int i=0; i<hand.size(); i++)
			{
				if (!hand.getCard(i).isColorRevealed())
					hand.getCard(i).setColor(null);
				if (!hand.getCard(i).isValueRevealed())
					hand.getCard(i).setValue(0);
			} //Maschero i valori delle carte del giocatore p


		}
	}

	public Action play100(State current)
	{
		try {
			for (int i = 0; i < current.getHand(Main.name).size(); i++) {
				if (MathCalc.getCardPlayability(current, i, Main.name) == 1)
					return new Action(Main.name, ActionType.PLAY, i);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Action discard100(State current)
	{
		if (current.getHintTokens()<8) {
			try {
				for (int i = 0; i < current.getHand(Main.name).size(); i++) {
					if (MathCalc.getCardUselessness(current, i, Main.name) == 1)
						return new Action(Main.name, ActionType.DISCARD, i);
				}
			} catch (JSONException e) {
				e.printStackTrace(System.err);
			}
		}
		return null;
	}

	public Action play(State current)
	{
		try {
			int card = 0;
			double max = MathCalc.getCardPlayability(current,0,Main.name);
			double box;
			for (int i = 1; i < current.getHand(Main.name).size(); i++) {
				box = MathCalc.getCardPlayability(current, i, Main.name);
				if (box > max)
				{
					max = box;
					card = i;
				}
			}
			return new Action(Main.name, ActionType.DISCARD, card);
		}
		catch(JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Action discard(State current)
	{
		try {
			int card = 0;
			double max = MathCalc.getCardUselessness(current,0,Main.name);
			double box;
			for (int i = 1; i < current.getHand(Main.name).size(); i++) {
				box = MathCalc.getCardUselessness(current, i, Main.name);
				if (box > max)
				{
					max = box;
					card = i;
				}
			}
			return new Action(Main.name, ActionType.DISCARD, card);
		}
		catch(JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

	private JSONArray sortPlayers()
	{
		JSONArray players = Game.getInstance().getPlayers().clone();
		int myturn = Game.getInstance().getPlayerTurn(Main.name);
		for (int i=0; i<myturn; i++)
		{
			players.add(players.get(i));
			players.remove(i);
		}
		players.remove(myturn); //Riordino i giocatori e tolgo me stesso
		return players;
	}

	/**
	 * Dà la lista di hint che aggiungerebbero informazioni alla mano del giocatore receiver
	 * @param current
	 * @param receiver
	 * @return
	 * @throws JSONException
	 */
	private List<Action> getPossibleHints(State current, String receiver) throws JSONException
	{
		ArrayList<Action> list = new ArrayList<>();
		Hand hand = current.getHand(receiver).clone();
		Card card;
		for (int i=1; i<6; i++)
		{
			for (JSONData d:hand)
			{
				card = (Card)d;
				if (card.getValue() == i && !card.isValueRevealed())
					list.add(new Action(Main.name,receiver,i));
			}
		}

		for(Color color:Color.values())
		{
			for (JSONData d:hand)
			{
				card = (Card)d;
				if (card.getColor() == color && !card.isColorRevealed())
					list.add(new Action(Main.name,receiver,color));
			}
		}
		return list;
	}
}
