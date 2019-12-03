import api.client.Main;
import api.client.StatisticState;
import api.game.*;
import api.client.AbstractAgent;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class Agent extends AbstractAgent
{

	private double SECURE_PLAYABILITY = 1;
	private boolean confirm;
	private int T;
	private String next;
	private

	public Agent(boolean log, String logpath, boolean confirm) throws FileNotFoundException
	{
		super(log,logpath);
		this.confirm = confirm;
		T = 0;
		next = null;
	}

	private String getNextPlayer()
	{
		if (next == null)
			next = sortPlayers()[0];
		return next;
	}

	public void notifyTurn(Turn turn)
	{
		super.notifyTurn(turn);
		log(turn+"\n");
	}

	public void notifyState(State state)
	{
		super.notifyState(state);
		try {
			StatisticState sstate = new StatisticState(state, stats);
			log(""+sstate);
//			stats.printPossibilities(System.out);
		/*	System.out.println("Suggerimenti possibili:");
			for (String player: Game.getInstance().getPlayers())
			{
				if (!player.equals(Main.playerName))
				{
					System.out.println(player);
					for (Action h:this.getPossibleHints(player))
						System.out.println("\t"+h);
				}
			}*/
		}
		catch (JSONException e){}
	}

	/**
	 * Strategy3 implementa un giocatore che segue una convenzione sui suggerimenti delle carte.
	 * Ogni giocatore suggerisce solo al suo successivo e mantiene il numero di turni con tokens>0 in cui l'ultima carta
	 * nella mano del successivo &egrave; rimasta in mano al proprietario. Sia T tale contatore.
	 * <ol>
	 *     <li>
	 * 	       Se tokens == 0
	 * 	       <ol>
	 * 	           <li>
	 * 	               Se ho una carta giocabile la gioco
	 * 	           </li>
	 * 	           <li>
	 * 	               Se ho una carta scartabile con sicurezza la scarto.
	 * 	           </li>
	 * 	           <li>
	 * 	               Scarto la carta con uselessness maggiore.
	 * 	           </li>
	 * 	       </ol>
	 * 	   </li>
	 *     <li>
	 *         Se T == 0 sia C l'ultima carta della mano del successivo
	 *         <ol>
	 *             <li>
	 *                 Se C &egrave; giocabile allora ne suggerisco il valore.
	 *             </li>
	 *             <li>
	 *                 Se C non &egrave; giocabile ma &egrave; scartabile ne suggerisco il colore.
	 *             </li>
	 *             <li>
	 *                 Se si arriva a questo punto significa che la carta deve essere tenuta in mano
	 *                 e quindi non deve ricevere nessun suggerimento in questo turno.
	 *                 Controllo dunque le mie carte: se ho una carta giocabile la gioco
	 *             </li>
	 *             <li>
	 *                 Se non ho nessuna carta giocabile d&ograve; il suggerimento che rende giocabili il maggior numero
	 *                 di carte e che non coinvolge l'ultima carta.
	 *             </li>
	 *             <li>
	 *                 Se nessun suggerimento rende giocabili altre carte d&ograve; quello che abbassa del massimo
	 *                 l'entropia totale delle carte del giocatore successivo. Tale suggerimento non deve coinvolgere
	 *                 l'ultima carta.
	 *             </li>
	 *         </ol>
	 *     </li>
	 *     <li>
	 *         Se T>0 && tokens>0
	 *         <ol>
	 *             <li>
	 *                 D&ograve; il suggerimento che rende giocabili il maggior numero di carte del giocatore successivo.
	 *                 Se nessun suggerimento rende giocabile nessuna carta vai al punto successivo
	 *             </li>
	 *             <li>
	 * 	               Se ho una carta giocabile la gioco
	 * 	  	       </li>
	 * 	           <li>
	 * 	               Se ho una carta scartabile con sicurezza la scarto.
	 * 	           </li>
	 * 	           <li>
	 * 	               D&ograve; il suggerimento che riduce del massimo l'entropia totale delle carte del giocatore
	 * 	               successivo
	 * 	           </li>
	 *         </ol>
	 *     </li>
	 * </ol>
	 * @return
	 */
	public Action chooseAction()
	{
		Action action = null;
		if (stats.getLastState().getHintTokens() == 0)
		{

		}
		return action;
	}

	public Action playSecure() {
		double[] p = stats.getPlayability(Main.playerName);
		Hand hand = stats.getLastState().getHand(Main.playerName);
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < p.length; i++)
		{
			if (p[i] >= 1)
				list.add(i);
		}
		while(list.size()>1)
		{
			if (hand.getCard(list.get(1)).getValue()>hand.getCard(list.get(0)).getValue())
				list.remove(0);
			else
				list.remove(1);
		}
		try
		{
			if (list.size()>0)
				return new Action(Main.playerName, ActionType.PLAY, list.get(0));
		}
		catch(JSONException e)
		{
			log(e);

		}
		return null;
	}

	public static void main(String[] args) throws Exception
	{
		boolean log = false;
		String logpath = null;
		boolean confirm = false;
		for (int i=0; i<args.length; i++)
		{
			if (args[i].equals("-l"))
			{
				log = true;
			}
			else if (args[i].equals("-f"))
			{
				i++;
				logpath = args[i];
			}
			if (args[i].equals("-c"))
				confirm = true;
		}
		AbstractAgent agent = new Agent(log,logpath,confirm);
		Main.setAgent(agent);
		Main.main(args);
	}
}

