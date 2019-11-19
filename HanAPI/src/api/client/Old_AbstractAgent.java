package api.client;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONException;

import java.util.ArrayList;
import java.util.List;

public abstract class Old_AbstractAgent
{
/*
	protected HandCardsProbability stats;
	protected List<State> history;

	public Old_AbstractAgent() {
		stats = null;
		history = new ArrayList<>();
	}


	public void addHistory(State state){
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
*/

	/**
	 * Restituisce una lista di hint che aggiungerebbero informazioni alla mano del giocatore receiver
	 * @param receiver
	 * @return
	 * @throws JSONException
	 */
/*	public List<Action> getPossibleHints(String receiver) throws JSONException
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

	public JSONArray sortPlayers() {
		//Riordino i giocatori e tolgo me stesso, riordino a partire dal mio successivo
		JSONArray players = Game.getInstance().getPlayers().clone();
		int myturn = Game.getInstance().getPlayerTurn(Main.name);
		for (int i = 0; i < myturn; i++)
			players.add(players.get(i));

		for (int i = 0; i <= myturn; i++)
			players.remove(0);
		return players;
	}

	public abstract Action chooseAction(State current);

 */
}

