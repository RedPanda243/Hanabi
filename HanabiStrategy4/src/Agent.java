import api.client.Main;
import api.client.StatisticState;
import api.client.Statistics;
import api.game.*;
import api.client.AbstractAgent;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Agent extends AbstractAgent
{

//	private double SECURE_PLAYABILITY = 1;
	private boolean confirm;
	private int T;
	private String next;
	private HashMap<String,ArrayList<Integer>[]> cmap;

	public Agent(boolean log, String logpath, boolean confirm) throws FileNotFoundException
	{
		super(log,logpath);
		this.confirm = confirm;
		T = 0;
		next = null;
		cmap = new HashMap<>();

		for (String s:Game.getInstance().getPlayers())
		{
			ArrayList<Integer>[] a = new ArrayList[2];
			a[0] = new ArrayList<>(); //0 = giocabili per convenzione
			a[1] = new ArrayList<>(); //1 = non scartabili per convenzione
			cmap.put(s,a);
		}
	}

	private String getNextPlayer()
	{
		if (next == null)
			next = sortPlayers()[0];
		return next;
	}

	public void notifyTurn(Turn turn)
	{
		updateConv(turn); //Le convenzioni derivanti dal turno vanno aggiornate prima di stats!
		super.notifyTurn(turn); //aggiorno stats
		dropConv(turn); //Dopo devo eliminare le eventuali convenzioni su carte che sono diventate certe!
		log(turn+"\n");
	}

	private void dropConv(String player)
	{
		double[] p = stats.getPlayability(player);
		double[] u = stats.getUselessness(player);
		for (int i=0; i<p.length; i++)
		{
			if (p[i] == 0 || p[i] == 1)
				cmap.get(player)[0].remove((Integer)i);
			if (u[i] == 0 || u[i] == 1)
				cmap.get(player)[1].remove((Integer)i);
		}
	}

	private void dropConv(Turn turn)
	{
		String hinted = turn.getAction().getHinted();
		if (hinted != null)
			dropConv(hinted);
		else
		{
			for (String p:Game.getInstance().getPlayers())
				dropConv(p);
		}
	}

	private void updateConv(Turn turn)
	{
		String hinted = turn.getAction().getHinted();
		if (hinted != null)
		{
			double[] p = stats.getPlayability(hinted);
			Statistics s = stats.getStatisticsIf(turn);
			double[] p1 = s.getPlayability(hinted);
			List<Integer> revealed = turn.getAction().getCardsToReveal(stats.getLastState());
			for (int i = revealed.size()-1; i > -1; i--) {
				if (p1[revealed.get(i)] > p[revealed.get(i)])
				{
					if (!cmap.get(hinted)[0].contains(revealed.get(i)))
					{
						cmap.get(hinted)[0].add(revealed.get(i));
						cmap.get(hinted)[1].remove(revealed.get(i));
						break;
					}
				}
				if (p1[i] < p[i])
				{
					if (!cmap.get(hinted)[1].contains(revealed.get(i)))
					{
						cmap.get(hinted)[1].add(revealed.get(i));
						cmap.get(hinted)[0].remove(revealed.get(i));
						break;
					}
				}
			}
		}
		else
		{
			int i = turn.getAction().getCard();
			cmap.get(turn.getAction().getPlayer())[0].remove((Integer)i);
			cmap.get(turn.getAction().getPlayer())[1].remove((Integer)i);
		}
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
	 * Strategy4 implementa un giocatore che segue una convenzione sui suggerimenti delle carte.
	 * Tale convenzione consiste nel non dare mai suggerimenti per indicare carte da scartare.
	 * Ogni suggerimento ricevuto va inteso come invito a giocare o tenere la carta più a destra tra quelle
	 * indicate. Se la carta aumenta la propria playability va giocata, tenuta altrimenti
	 *
	 * In questo modo un giocatore pu&ograve; sapere che una sua carta &egrave; sicuramente giocabile anche se la sua
	 * playability &egrave; minore di 1 oppure pu&ograve; sapere che una carta ad alta uselessness non va scartata.
	 *
	 * Per mantenere queste informazioni l'agente implementa due liste di appoggio di tutte le carte che sono sicure
	 * "per convenzione", una lista per le giocabili, una per le non scartabili.
	 *
	 * Una carta sicura "per convenzione" viene eliminata dalla propria lista di appoggio se la sua playability
	 * o la sua uselessness, a seconda della lista, raggiunge uno dei due estremi 0 o 1.
	 * Il senso &egrave; che se ho una certezza matematica sulla carta qualsiasi informazione per convenzione perde di
	 * significato.
	 *
	 * Un problema legato alle convenzioni potrebbe emergere nelle partite ad almeno 3 giocatori, nelle quali due giocatori
	 * possono suggerire allo stesso compagno. Il secondo giocatore a dare il suggerimento dovrebbe conoscere lo stato
	 * attuale delle convenzioni del giocatore che riceve il suggerimento, al fine di evitare consigli inutili.
	 *
	 * Di conseguenza, ogni giocatore mantiene una mappa di liste carte sicure per convenzione, 2 per giocatore, e la
	 * aggiorna dopo ogni suggerimento
	 *
	 * La strategia segue questo schema:
	 * <ol>
	 *     <li>
	 *			Cerco di giocare una carta sicura:
	 *			<ol>
	 *			 	<li>
	 *			 	  	Se conosco carte giocabili per convenzione gioco quella pi&ugrave; a destra se non ho 5.
	 *			 	  	(perch&egrave; presumibilmente &egrave; quella di cui so meno, essendo pescata per ultima, quindi
	 *			 	  	spero che le altre raggiungano presto conoscenza completa)
	 *			 	</li>
	 *			 	<li>
	 *			 	  	Se ho carte con playability 1 gioco quella pi&ugrave; a destra se non ho 5.
	 *			 	  	(per limitare il numero di shift nelle statistiche)
	 *			 	</li>
	 *			 </ol>
	 *		</li>
	 *
	 *		<li>
	 *		  	Se ho hint token:
	 *		  	<ol>
	 *				<li>
	 * 	 				Ciclo tra i miei compagni in ordine di gioco: se uno ha una carta giocabile ma non lo sa
	 * 	 				(ne per playability ne per convenzione) gli suggerisco in modo adeguato: la carta deve essere
	 * 	 				quella pi&ugrave; a destra tra quelle coinvolte nel suggerimento che aumentano la propria
	 * 	 				playability.
	 * 	 				Se non &egrave; possibile dare un suggerimento adeguato provo con la prossima carta.
	 * 	 	 		</li>
	 * 	 	 		<li>
	 * 	 	 		 	Ciclo di nuovo tra i miei compagni: se uno ha un a carta non scartabile ma non lo sa
	 * 	 	 		 	(ne per uselessness ne per convenzione) gli suggerisco in modo adeguato (come prima
	 * 	 	 		 	ma le carte devono diminuire la playability).
	 * 	 	 		 	Se non &egrave; possibile dare un suggerimento adeguato provo con la prossima carta.
	 * 	 	 		</li>
	 *		  	</ol>
	 *		</li>
	 *		<li>
	 *			Scarto la carta pi&ugrave; a sinistra, tra quelle con uselessness maggiore, che non appartiene
	 *			alla lista di carte non scartabili per convenzione.
	 *		</li>
	 *	</ol>
	 * @return
	 */
	public Action chooseAction()
	{
		try {
			Action action = null;
			Hand hand = stats.getLastState().getHand(Main.playerName);
			log("Cerco di giocare una carta sicura");
			double[] p = stats.getPlayability(Main.playerName);
			int index = -1;
			for (int i=0; i<p.length; i++)
			{
				if (p[i] == 1) {
					index = i;
					if (hand.getCard(i).getValue() == 5)
						break;
				}
			}
			if (index >-1)
				return new Action(Main.playerName,ActionType.PLAY,index);
			log("Nessuna carta sicura per playability = 100%");

			List<Integer> cp = cmap.get(Main.playerName)[0];
			if (cp.size() > 0)
			{
				int max = cp.get(0);
				for (int i = 1; i < cp.size(); i++) {
					if (hand.getCard(cp.get(i)).getValue() == 5) {
						max = cp.get(i);
						break;
					} else if (cp.get(i) > max) {
						max = cp.get(i);
					}
				}
				return new Action(Main.playerName, ActionType.PLAY, max);
			}
			log("Nessuna sicura per convenzione");


			if (stats.getLastState().getHintTokens()>0)
			{
				//Cerco le carte giocabili nella mano del compagno
				String hinted = sortPlayers()[0];
				ArrayList<Integer> playable = new ArrayList<>();
				hand = stats.getLastState().getHand(hinted);
				p = stats.getPlayability(hinted);
				for (int i=0; i<hand.size(); i++)
				{
					if (stats.isPlayable(hand.getCard(i)) && p[i]<1)
					{
						//Se c'è un 5 indico quello
						if (hand.getCard(i).getValue() == 5)
						{
							playable.clear();
							playable.add(i);
							break;
						}
						playable.add(i);
					}

				}
				if (playable.size()>0) {
					//Genero i suggerimenti disponibili su carte giocabili
					List<Action> possibleHints = getPossibleHints(hinted);
					List<Action> hints = new ArrayList<>();
					List<Integer> l_index;
					for (Action possibile : possibleHints) {
						for (int i : playable) {
							l_index = possibile.getCardsToReveal(stats.getLastState());
							if (l_index.contains(i)) //La carta giocabile è suggerita
							{
								Statistics s1 = stats.getStatisticsIf(new Turn(possibile, l_index));
								if (l_index.lastIndexOf(i) == l_index.size() - 1 || s1.getPlayability(hinted)[i] == 1) { //La carta giocabile è la più a destra oppure raggiunge playability 1
									hints.add(possibile);
									break;
								}
							}
						}
					}
					//hints contiene tutti i suggerimenti fattibili seguendo la convenzione
					//Adesso filtro per numero di reveal
					int r = 0;
					possibleHints = hints;
					hints = new ArrayList<>();
					for (Action h : possibleHints) {
						l_index = h.getCardsToReveal(stats.getLastState());
						int cont = 0;
						for (int i : l_index) {
							if (h.getColor() != null) {
								if (!hand.getCard(i).isColorRevealed())
									cont++;
							} else {
								if (!hand.getCard(i).isValueRevealed())
									cont++;
							}
						}
						if (cont > r) {
							r = cont;
							hints.clear();
						}
						if (cont == r)
							hints.add(h);
					}

					if (hints.size() > 1)
					{ //Filtro per uselessness minore

					}
				}
				else
				{ //Suggerisco per non scartare: prendo quello che rende minore la playability della carta da non scartare

				}
			}

			//Scarto una carta
			double[] u = stats.getUselessness(Main.playerName);
			double umax = 0;
			List<Integer> dis = new ArrayList<>();
			int leftmargin = Game.getInstance().getNumberOfCardsPerPlayer()/2;
			for (int i = 0; i<Game.getInstance().getNumberOfCardsPerPlayer()/2 && leftmargin<hand.size(); i++)
			{
				if (hand.getCard(i).getValue() == 5)
					leftmargin++;
			}
			for (int i=0; i<leftmargin; i++)
			{
				if (!cmap.get(Main.playerName)[1].contains(i))
				{
					if (u[i] > umax)
					{
						umax = u[i];
						dis.clear();
					}
					if (u[i] == umax)
						dis.add(i);
				}
			}

			if (dis.size()>1)
			{
				p = stats.getPlayability(Main.playerName);
				int min = 0;
				double pmin = p[dis.get(0)];
				for (int i = 1; i<dis.size(); i++)
				{
					if (p[dis.get(i)]<pmin)
					{
						pmin = p[dis.get(i)];
						min = i;
					}
				}
				return new Action(Main.playerName,ActionType.DISCARD,dis.get(min));
			}

			return new Action(Main.playerName,ActionType.DISCARD,dis.get(0));
		}
		catch(JSONException e)
		{
			log(e);
			return null;
		}
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

