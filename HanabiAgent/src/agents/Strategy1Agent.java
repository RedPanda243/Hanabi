package agents;

import api.game.*;
import main.Main;
import math.HandCardsProbability;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Agente che segue questa strategia:
 * <ol>
 *     <li>
 *         Gioca una carta con 100% di playability
 *     </li>
 *     <li>
 *         Se hai hint token ed è possibile rendere giocabile al 100% una carta di un compagno fallo
 *     </li>
 *     <li>
 *         Se hai hint token e un compagno ha una carta con 0% uselessness indicagliela
 *     </li>
 *     <li>
 *         Scarta una carta con 100% di uselessness se non hai 8 hint token
 *     </li>
 *     <li>
 *         Se hai hint token dai il suggerimento che coinvolge più carte
 *     </li>
 *     Da qui in poi spero di non arrivarci mai
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
	HandCardsProbability stats;
	List<State> history;
	public Strategy1Agent()
	{
		super();
		stats = null;
		history = new ArrayList<>();
	}

	@Override
	public Action chooseAction(State state) //State è inutile ma mi serve nell'astratta per casi generali
	{
		try {
			System.out.println( Arrays.toString(stats.getPlayability(Main.name)) );
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Action action = play100();
		if (action == null)
			action = hint100();
		if (action == null)
			action = hint0();
		if (action == null)
			action = discard100();
		if (action == null)
			action = hintMost();
		if (action == null)
			action = discardMost();
		if (action == null)
			action = playMost();
		return action;
	}

	@Override
	public void addHistory(State state)
	{
		history.add(state);
		if (stats == null)
			stats = new HandCardsProbability(Main.name,state);
		else {
			try {
				stats.updatePossibleCards(state);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				System.out.println(stats.getPossibleHand());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public Action play100()
	{
		double[] p = new double[0];
		try {
			p = stats.getPlayability(Main.name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try
		{
			for (int i = 0; i < p.length; i++)
			{
				if (p[i] == 1)
					return new Action(Main.name, ActionType.PLAY, i);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Action hint100()
	{
		try
		{
			if (history.get(history.size()-1).getHintTokens()>0)
			{
				JSONArray players = sortPlayers();
				List<Action> l;
				double[] p,p1;
				Hand handPlayerToHint;
				for (JSONData name : players)
				{
					handPlayerToHint = history.get(history.size()-1).getHand(name.toString());
					l = getPossibleHints(name.toString());
					p = stats.getPlayability(name.toString());
					for (Action a : l)
					{
						p1 = stats.getPlayability(name.toString(),a);
						for (int i=0; i<p.length; i++)
						{
							if (p1[i] == 1 && p[i]<1) {
								if(a.getActionType().equals(ActionType.HINT_COLOR)){
									if(!handPlayerToHint.getCard(i).isColorRevealed())
										return a;
								}else { //hint value
									if(!handPlayerToHint.getCard(i).isValueRevealed())
										return a;
								}
							}
						}
					}
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Action hint0()
	{
		try
		{
			if (history.get(history.size()-1).getHintTokens()>0)
			{
				JSONArray players = sortPlayers();
				List<Action> l;
				double[] p,p1;
				Hand handPlayerToHint;
				for (JSONData name : players)
				{
					handPlayerToHint = history.get(history.size()-1).getHand(name.toString());
					l = getPossibleHints(name.toString());
					p = stats.getUselessness(name.toString()); //uselessness prima dell'aiuto
					for (Action a : l) //testo tutte le azioni tra quelle possibili
					{
						p1 = stats.getUselessness(name.toString(),a); //uselessness dopo l'aiuto
						for (int i=0; i<p.length; i++)
						{
							if (p1[i] == 0 && p[i]>0)
								if(a.getActionType().equals(ActionType.HINT_COLOR)){
									if(!handPlayerToHint.getCard(i).isColorRevealed())
										return a;
								}else { //hint value
									if(!handPlayerToHint.getCard(i).isValueRevealed())
										return a;
								}
						}
					}
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Action discard100()
	{
		double[] p = new double[0];
		try {
			p = stats.getUselessness(Main.name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (history.get(history.size()-1).getHintTokens()<8)
		{
			try
			{
				for (int i = 0; i < p.length; i++)
				{
					if (p[i] == 1)
						return new Action(Main.name, ActionType.DISCARD, i);
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace(System.err);
			}
		}
		return null;
	}

	public Action hintMost()
	{
		try
		{
			State current = history.get(history.size()-1);
			if (current.getHintTokens()>0)
			{
				JSONArray players = sortPlayers();
				List<Action> l;
				Action best = null;
				int bestcont = 0,cont = 0;
				Hand hand;
				Card box;
				for (JSONData name : players)
				{
					l = getPossibleHints(name.toString());
					hand = current.getHand(name.toString());
					for (Action a:l)
					{
						cont = 0;
						if (a.getActionType() == ActionType.HINT_COLOR)
						{
							for (JSONData card: hand)
							{
								box = (Card)card;
								if (!box.isColorRevealed() && box.getColor()==a.getColor())
									cont++;
							}
							if (cont>bestcont)
							{
								bestcont = cont;
								best = a;
							}
						}
						else if (a.getActionType() == ActionType.HINT_VALUE)
						{
							for (JSONData card: hand)
							{
								box = (Card)card;
								if (!box.isValueRevealed() && box.getValue()==a.getValue())
									cont++;
							}
							if (cont>bestcont)
							{
								bestcont = cont;
								best = a;
							}
						}
						else
							throw new JSONException("Wrong action type");
					}
				}
				return best;
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Action discardMost()
	{
		try
		{
			double[] p = stats.getUselessness(Main.name);
			int card = 0;
			double max = p[card];
			double box;
			for (int i = 1; i < p.length; i++) {
				box = p[i];
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

	public Action playMost()
	{
		try
		{
			double[] p = stats.getPlayability(Main.name);
			int card = 0;
			double max = p[card];
			double box;
			for (int i = 1; i < p.length; i++) {
				box = p[i];
				if (box > max)
				{
					max = box;
					card = i;
				}
			}
			return new Action(Main.name, ActionType.PLAY, card);
		}
		catch(JSONException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}



	private JSONArray sortPlayers()
	{
		//Riordino i giocatori e tolgo me stesso, riordino a partire dal mio successivo
		JSONArray players = Game.getInstance().getPlayers().clone();
		int myturn = Game.getInstance().getPlayerTurn(Main.name);
		for (int i=0; i<myturn; i++)
			players.add(players.get(i));

		for (int i=0; i<=myturn; i++)
			players.remove(0);
		return players;
	}

	/**
	 * Dà la lista di hint che aggiungerebbero informazioni alla mano del giocatore receiver
	 * @param receiver
	 * @return
	 * @throws JSONException
	 */
	private List<Action> getPossibleHints(String receiver) throws JSONException
	{
		ArrayList<Action> list = new ArrayList<>();
		Hand hand = history.get(history.size()-1).getHand(receiver).clone();
		Card card;
		List<Integer> valueAdded = new ArrayList<>();
        List<Color> colorAdded = new ArrayList<>();
		List<Integer> cardsHinted;
//		for (int j=0; j<hand.size(); j++){
//			System.out.println("[CARD] "+hand.getCard(j).getValue()+"-"+hand.getCard(j).getColor()+" //pos j= "+j+" //indexOf="+hand.indexOf(hand.getCard(j)));
//		}

		int value;
		Color col;

		System.out.println("[HAND]"+hand.toString());

        for(int i=0; i<hand.size(); i++){
            value = hand.getCard(i).getValue();
            col = hand.getCard(i).getColor();

            if(valueAdded.indexOf(value)==-1){ //numero incontrato non ancora aggiunto
                cardsHinted = new ArrayList<>();
                cardsHinted.add(i);
                valueAdded.add(value);
                for(int j=i+1; j<hand.size(); j++) {
                    if (hand.getCard(j).getValue() == value)
                        cardsHinted.add(j);
                }
                list.add(new Action(Main.name, receiver, value, cardsHinted));
            } //fine if value

            if(colorAdded.indexOf(col)==-1){ //colore incontrato non ancora aggiunto
                cardsHinted = new ArrayList<>();
                cardsHinted.add(i);
                colorAdded.add(col);
                for(int j=i+1; j<hand.size(); j++){
					try {
						if(hand.getCard(j).getColor().equals(col))
							cardsHinted.add(j);
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("[POSSIBILE HINT]j="+j+" color="+col);
					}
				}
                list.add(new Action(Main.name, receiver, col, cardsHinted));
            } //fine if color
        } //fine for HAND

//        for(Action a : list)
//            System.out.println("[HINT POSSIBLE] "+a.toString()+ " --pos "+a.getCardsToReveal().toString());

//		for (int i=1; i<6; i++)
//		{
//			cardsHinted = new ArrayList<>();
//			for (JSONData d:hand) {
//				card = (Card)d;
//				System.out.println("CHECKING CARD "+card.getValue()+"-"+ card.getColor()+" in pos "+hand.getPosizione(card));
//				if (card.getValue() == i && !card.isValueRevealed()){
//					cardsHinted.add(hand.getPosizione(card));
//					System.out.println("ADDED pos "+hand.getPosizione(card)+" for card "+card.getValue());
//				}
//
//			}
//			if(cardsHinted.size()>0) {
//				list.add(new Action(Main.name, receiver, i, cardsHinted));
//				System.out.println("TOHINT CARDS: "+ Arrays.toString(cardsHinted.toArray()));
//			}
//		}

//		for(Color color:Color.values())
//		{
//			cardsHinted = new ArrayList<>();
//			for (JSONData d:hand)
//			{
//				card = (Card)d;
//				if (card.getColor() == color && !card.isColorRevealed())
//					cardsHinted.add(hand.getPosizione(card));
//
//			}
//			if(cardsHinted.size()>0)
//				list.add(new Action(Main.name,receiver,color,cardsHinted));
//		}
		return list;
	}
}
