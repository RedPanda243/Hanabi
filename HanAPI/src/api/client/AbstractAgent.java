package api.client;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractAgent
{
	protected Statistics stats;

	public AbstractAgent() {
		stats = null;
	}

	public abstract Action chooseAction();

/*	public Action getAction()
	{
		Action action = chooseAction();
		stats.updateAction(action);
		return action;
	}
*/
	public void notifyState(State state)
	{
		if (stats == null)
			stats = new Statistics();
		stats.addState(state);
	}

	public void notifyTurn(Turn turn)
	{
		stats.updateTurn(turn);
	}


	public List<Action> getPossibleHints(String receiver) throws JSONException
	{
		ArrayList<Action> list = new ArrayList<>();
		Hand hand = stats.getLastState().getHand(receiver).clone();
		Card card;
		List<Integer> valueAdded = new ArrayList<>();
		List<Color> colorAdded = new ArrayList<>();
		List<Integer> cardsHinted;
//		for (int j=0; j<hand.size(); j++){
//			System.out.println("[CARD] "+hand.getCard(j).getValue()+"-"+hand.getCard(j).getColor()+" //pos j= "+j+" //indexOf="+hand.indexOf(hand.getCard(j)));
//		}

		int value;
		Color col;

//		System.out.println("[HAND]"+hand.toString());

		for(int i=0; i<hand.size(); i++){
			value = hand.getCard(i).getValue();
			col = hand.getCard(i).getColor();

			if(!hand.getCard(i).isValueRevealed() && valueAdded.indexOf(value)==-1){ //numero incontrato non ancora aggiunto
				cardsHinted = new ArrayList<>();
				cardsHinted.add(i);
				valueAdded.add(value);
				for(int j=i+1; j<hand.size(); j++) {
					if (hand.getCard(j).getValue() == value)
						cardsHinted.add(j);
				}
				list.add(new Action(Main.playerName, receiver, value/*, cardsHinted*/));
			} //fine if value

			if(!hand.getCard(i).isColorRevealed() && colorAdded.indexOf(col)==-1){ //colore incontrato non ancora aggiunto
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
				list.add(new Action(Main.playerName, receiver, col/*, cardsHinted*/));
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
//				list.add(new Action(Main.playerName, receiver, i, cardsHinted));
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
//				list.add(new Action(Main.playerName,receiver,color,cardsHinted));
//		}
		return list;
	}

	/**
	 * Riordina i giocatori a partire dal successivo a questo Agent. Questo Agent &egrave; escluso dalla lista
	 * @return
	 */
	public String[] sortPlayers()
	{
		//Riordino i giocatori a partire dal mio successivo e tolgo me stesso
		List<String> players = Arrays.asList(Game.getInstance().getPlayers());
		int myturn = Game.getInstance().getPlayerTurn(Main.playerName);
		for (int i = 0; i < myturn; i++) //per ogni giocatore prima di me
			players.add(players.get(i)); //lo aggiungo in coda alla lista

		for (int i = 0; i <= myturn; i++) //per ogni giocatore prima di me e me stesso
			players.remove(0); //cancello la copia in testa alla lista, le altre shiftano a sinistra
		return players.toArray(new String[0]);
	}
}
