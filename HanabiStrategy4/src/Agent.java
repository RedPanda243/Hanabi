import api.client.Main;
import api.client.StatisticState;
import api.client.Statistics;
import api.game.*;
import api.client.AbstractAgent;
import sjson.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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


	}

	private String getNextPlayer()
	{
		if (next == null)
			next = sortPlayers()[0];
		return next;
	}

	public void notifyTurn(Turn turn)
	{
		log("\n"+turn);
		dropPlay(); //Le convenzioni su carte da giocare devono valere solo per un mio turno
		updateConv(turn); //Le convenzioni derivanti dal turno vanno aggiornate prima di stats!
		super.notifyTurn(turn); //aggiorno stats

		dropKeep(turn); //Dopo devo eliminare le eventuali convenzioni su carte che sono diventate certe!

		log("\tConvenzioni aggiornate:");
		for (String pl:Game.getInstance().getPlayers())
		{
			log("\t\t"+pl+":");
			log("\t\t\tDa giocare: " + cmap.get(pl)[0]);
			log("\t\t\tDa tenere: " + cmap.get(pl)[1]);
		}
		/*	log(turn+"\n");
		log("Convenzioni dopo drop:");
		log("\tDa giocare: "+cmap.get(Main.playerName)[0]);
		log("\tDa tenere: "+cmap.get(Main.playerName)[1]);*/
	}

	private void dropKeep(String player)
	{
		double[] p = stats.getPlayability(player);
		double[] u = stats.getUselessness(player);
		for (int i=0; i<p.length; i++)
		{
			if (u[i] == 0 || u[i] == 1 || p[i] == 1)
				cmap.get(player)[1].remove((Integer)i);
		}
	}

	private void dropKeep(Turn turn)
	{
		String hinted = turn.getAction().getHinted();
		if (hinted != null)
			dropKeep(hinted);
		else
		{
			for (String p:Game.getInstance().getPlayers())
				dropKeep(p);
		}
	}

	private void updateConv(Turn turn)
	{
		log("\nUpdate convenzioni:");
		String hinted = turn.getAction().getHinted();
		String l;
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		if (hinted != null)
		{
			double[] p = stats.getPlayability(hinted);
			l = "{";
			for (int j=0; j<p.length; j++)
				l+= df.format(p[j])+", ";
			l = l.substring(0,l.length()-2)+"}\n";
			log("\tPlayability corrente: "+l);

			Statistics s = stats.getStatisticsIf(turn);
			double[] p1 = s.getPlayability(hinted);
			l = "{";
			for (int j=0; j<p1.length; j++)
				l+= df.format(p1[j])+", ";
			l = l.substring(0,l.length()-2)+"}\n";
			log("\tPlayability futura: "+ l);

			List<Integer> revealed = turn.getRevealed();
			log("\tCarte rivelate: "+revealed);
			for (int i = revealed.size()-1; i > -1; i--) {
				if (p1[revealed.get(i)] > p[revealed.get(i)])
				{
					if (!cmap.get(hinted)[0].contains(revealed.get(i)))
					{
						log("\tla carta "+revealed.get(i)+" e' da giocare");
						cmap.get(hinted)[0].add(revealed.get(i));
						cmap.get(hinted)[1].remove(revealed.get(i));
						break;
					}
				}
				if (p1[revealed.get(i)] < p[revealed.get(i)])
				{
					if (!cmap.get(hinted)[1].contains(revealed.get(i)))
					{
						log("\tla carta "+revealed.get(i)+" e' da tenere");
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

			List<Integer> list = cmap.get(turn.getAction().getPlayer())[0];
			list.remove((Integer)i);
			for (int j = 0; j<list.size(); j++)
			{
				if (list.get(j)>i)
					list.set(j,list.get(j)-1);
			}

			list = cmap.get(turn.getAction().getPlayer())[1];
			list.remove((Integer)i);
			for (int j = 0; j<list.size(); j++)
			{
				if (list.get(j)>i)
					list.set(j,list.get(j)-1);
			}
		}
	}

	public void notifyState(State state)
	{
		if (cmap.size() == 0)
		{
			for (String s:Game.getInstance().getPlayers())
			{
				ArrayList<Integer>[] a = new ArrayList[2];
				a[0] = new ArrayList<>(); //0 = giocabili per convenzione
				a[1] = new ArrayList<>(); //1 = non scartabili per convenzione
				cmap.put(s,a);
			}
		}
		super.notifyState(state);
		try
		{
			StatisticState sstate = new StatisticState(state, stats);
			log(""+sstate);
			log("Possibili carte possedute:");
			for (int i=0; i<state.getHand(Main.playerName).size(); i++)
			{
				log("\t"+i+": "+stats.calcCards(Main.playerName,i));
			}
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
	 * 	 	 		 	Ciclo di nuovo tra i miei compagni: se uno ha una carta non scartabile ma non lo sa
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
		if (confirm)
		{
			log("\nPremi INVIO per prossimo turno...");
			try
			{
				new BufferedReader(new InputStreamReader(System.in)).readLine();
			}
			catch(IOException e){}
		}
		try
		{
			Action action = null;
			int tokens = stats.getLastState().getHintTokens();
			log("\nTocca a me");
			//Provo a giocare una carta sicura per playability o convenzione
			action = playSecure();

			if (action == null && tokens>0)
			{
				for (String hinted:sortPlayers())
				{
					/*
					Genero i suggerimenti disponibili su carte giocabili del compagno.
					Tutti questi suggerimenti seguono la convenzione dell'agente.
				 	*/
					List<Action> hints = getHintsOnPlayableCards(hinted);

				/*	if (hints.size()>1)
					{
						//Filtro per numero di reveal
						hints = filterForRevealNumber(hinted,hints);
					}
				 */

					if (hints.size()>1)
					{
						//Filtro prendendo i suggerimenti sulle carte con uselessness attuale minore
						hints = filterForMinorUselessness(hinted, hints);
					}

					if (hints.size()>1)
					{
						//Filtro prendendo il suggerimento che dà il massimo aumento di playability
						hints = filterForBestPlayabilityIncrement(hinted,hints);
					}

					if (hints.size() > 0) {
						action = hints.get(0);
						break;
					}
					else
					{
						//Se non riesco a suggerire per giocare, suggerisco per non scartare
						hints = getHintsForKeepingCards(hinted);

						if (hints.size() > 0)
						{
							//Filtro prendendo i suggerimenti che danno la miglior diminuzione di playability totale
							hints = filterForBestPlayabilityDecrement(hinted,hints);
						}

						if (hints.size() > 0) {
							action = hints.get(0);
							break;
						}
					}
				}
			}

			if (action == null)
			{
				//Arrivo a questo punto se non ho hint token o se non riesco a suggerire ne per giocare ne per tenere.

				//Ora potrei dover scartare quindi inizio con il calcolarmi il margin
				int margin = getMargin();
				log("\nMargin = "+margin+", Tokens = "+tokens);
				//Se tokens < 8 provo a recuperarne scartando una carta sicura
				if (tokens < 8) {
					log("Provo a giocare una carta sicura nelle prime "+margin+" a sinistra");
					action = discardSecure(margin);
					if (action == null)
					{
						log("Non ho trovato carte sicure");
						if (tokens > 0)
						{
							action = discard(margin);
						}
						else
							action = bestHintForEntropy();
					}
				}
				else
				{
					//Se ho 8 tokens scartare non serve a niente, quindi dò il miglior suggerimento per entropia
					action = bestHintForEntropy();
				}
			}

			return action;


		}
		catch(JSONException e)
		{
			log(e);
			return null;
		}
	}

	public Action bestHintForEntropy() throws JSONException
	{
		log("Cerco il consiglio migliore per diminuzione di entropia tra tutti i miei compagni");
		double best = 0,cont;
		Action action = null;
		double[] e,e1;
		Statistics stats1;
		for (String s:sortPlayers())
		{
			e = stats.getCardEntropy(s);
			for(Action h:getPossibleHints(s))
			{
				stats1 = stats.getStatisticsIf(new Turn(h,h.getCardsToReveal(stats.getLastState())));
				e1 = stats1.getCardEntropy(s);
				cont = 0;
				for (int i=0; i<e1.length; i++)
					cont = cont+e[i]-e1[i];
				if (cont > best)
				{
					best = cont;
					action = h;
				}
			}
		}
		return action;
	}

	public int getMargin()
	{
		//Definisco "margin" come numero di carte massime / 2 + numero di 5.
		Hand hand = stats.getLastState().getHand(Main.playerName);
		int margin = Game.getInstance().getNumberOfCardsPerPlayer()/2;
		for (int i = 0; i<Game.getInstance().getNumberOfCardsPerPlayer()/2 && margin<hand.size(); i++)
		{
			if (hand.getCard(i).getValue() == 5)
				margin++;
		}
		return margin;
	}

	public Action discardSecure(int margin) throws JSONException
	{
		//Cerco nelle prime "margin" carte a sinistra. Se ne ho con uselessness 1 scarto quella più a sinistra.
		double[] u = stats.getUselessness(Main.playerName);
		for (int i=0; i<margin; i++)
		{
			if (!cmap.get(Main.playerName)[1].contains(i) && u[i] == 1)
				return new Action(Main.playerName,ActionType.DISCARD,i);
		}
		return null;
	}

	public Action discard(int margin) throws JSONException
	{

		//Scarto la carta più a sinistra delle prime "margin" con minor playability tra quelle con maggior uselessness.
		//Ovviamente la carta non deve essere da tenere per convenzione

		Hand hand = stats.getLastState().getHand(Main.playerName);
		double[] u = stats.getUselessness(Main.playerName);


		log("Scarto la carta più a sinistra con minor playability tra quelle con maggior uselessness");

		double umax = 0;
		List<Integer> dis = new ArrayList<>(); //Contiene le carte con maggior uselessness

		for (int i=0; i<margin; i++)
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
			double[] p = stats.getPlayability(Main.playerName);
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

	public List<Action> filterForBestPlayabilityDecrement(String hinted, List<Action> possibleHints) throws JSONException
	{
		log("Filtro prendendo i suggerimenti che producono la miglior diminuzione di playability");
		double[]p = stats.getPlayability(hinted);
		double[] p1;
		double decrement = Double.MIN_VALUE;
		double boxd;
		List<Action> hints = new ArrayList<>();
		Statistics s1;
		for (Action h:possibleHints)
		{
			boxd = 0;
			s1 = stats.getStatisticsIf(new Turn(h,h.getCardsToReveal(stats.getLastState())));
			p1 = s1.getPlayability(hinted);
			for (int i=0; i<p1.length; i++)
				boxd = boxd+p[i]-p1[i];
			if (boxd>decrement)
			{
				decrement = boxd;
				hints.clear();
			}

			if (boxd == decrement)
				hints.add(h);
		}
		log("Rimangono "+hints.size()+" suggerimenti");
		for (Action h:hints)
			log("\t"+h);
		return hints;
	}

	public List<Action> filterForMinorUselessness(String hinted, List<Action> possibleHints)
	{
		log("Filtro prendendo i suggerimenti che coinvolgono le carte con minor uselessness");
		double[] u = stats.getUselessness(hinted);
		List<Action> hints = new ArrayList<>();
		//Cerco gli indici delle carte di minor uselessness
		List<Integer> i_min = new ArrayList<>();
		double umin = 1;
		for (int i=0; i<stats.getLastState().getHand(hinted).size(); i++)
		{
			if (u[i]<umin)
			{
				i_min.clear();
				umin = u[i];
			}
			else if (u[i] == umin)
				i_min.add(i);
		}

		//Prendo i suggerimenti che coinvolgono almeno una delle carte di minor uselessness

		for (Action h:possibleHints)
		{
			for(int i:i_min)
			{
				if (h.getCardsToReveal(stats.getLastState()).contains(i))
				{
					hints.add(h);
					break;
				}
			}
		}
		log("Rimangono "+hints.size()+" suggerimenti");
		for (Action h:hints)
			log("\t"+h);
		return hints;
	}

	public List<Action> filterForBestPlayabilityIncrement(String hinted, List<Action> possibleHints) throws JSONException
	{
		log("Filtro per incremento di playability della carta più a destra tra quelle indicate");
		List<Action> hints = new ArrayList<>();
		double increment = Double.MIN_VALUE;
		double boxi;
		//double[] p = stats.getPlayability(hinted);
		//double[] p1;
		Statistics s1;
		double p,p1;
		for (Action h: possibleHints)
		{
			List<Integer> revealed = h.getCardsToReveal(stats.getLastState());
			p = stats.getPlayability(hinted)[revealed.get(revealed.size()-1)];
			s1 = stats.getStatisticsIf(new Turn(h,h.getCardsToReveal(stats.getLastState())));
			p1 = s1.getPlayability(hinted)[revealed.get(revealed.size()-1)];
			boxi = p1-p;
	/*		for (int i=0; i<p1.length; i++)
			{
				boxi = boxi + (p1[i]-p[i]);
			}
	 */
			if (increment<boxi)
			{
				increment = boxi;
				hints.clear();
			}
			if (boxi == increment)
				hints.add(h);
		}
		log("Rimangono "+hints.size()+" suggerimenti");
		for (Action h:hints)
			log("\t"+h);
		return hints;
	}

	public List<Action> filterForRevealNumber(String hinted, List<Action> possibleHints)
	{
		log("Filtro per numero di reveal");
		int r = 0;
		List<Action> hints = new ArrayList<>();
		List<Integer> l_index;
		Hand hand = stats.getLastState().getHand(hinted);
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
		log("Rimangono "+hints.size()+" suggerimenti");
		for (Action h:hints)
			log("\t"+h);
		return hints;
	}

	public List<Action> getHintsForKeepingCards(String hinted) throws JSONException
	{
		List<Action> hints = new ArrayList<>();
		log("\nCerco suggerimenti su carte da tenere di "+hinted+" conformi alla convenzione");
		ArrayList<Integer> keep = new ArrayList<>();
		Hand hand = stats.getLastState().getHand(hinted);
		double[] u = stats.getUselessness(hinted);
		double[] p = stats.getPlayability(hinted);
		for (int i=0; i<hand.size(); i++)
		{
			if (!stats.isPlayable(hand.getCard(i)) && p[i]>0 && !stats.isUseless(hand.getCard(i)) && u[i]>0 && !cmap.get(hinted)[1].contains(i))
				keep.add(i);
		}
		log("Trovate "+keep.size()+" carte da tenere");
		if (keep.size()>0)
		{
			List<Action> possibleHints = getPossibleHints(hinted);
			List<Integer> l_index;
			for (Action h:possibleHints)
			{
				l_index = h.getCardsToReveal(stats.getLastState());
				for (int i:keep) {
					if (l_index.contains(i)) //Una carta da tenere è suggerita
					{
						Statistics s1 = stats.getStatisticsIf(new Turn(h, l_index));
						if ((l_index.lastIndexOf(i) == l_index.size() - 1 && s1.getPlayability(hinted)[i]<stats.getPlayability(hinted)[i])
								|| (s1.getPlayability(hinted)[i] == 0 && s1.getUselessness(hinted)[i] == 0))
						{ //La carta da tenere è la più a destra oppure raggiunge playability 0 e uselessness 0
							hints.add(h);
							break;
						}
					}
				}
			}
		}
		log("Trovati "+hints.size()+" suggerimenti su carte da tenere");
		for (Action h:hints)
			log("\t"+h);
		return hints;
	}

	public List<Action> getHintsOnPlayableCards(String hinted) throws JSONException
	{
		List<Action> hints = new ArrayList<>();
		log("Cerco suggerimenti su carte giocabili di "+hinted+" conformi alla convenzione");
		ArrayList<Integer> playable = new ArrayList<>();
		Hand hand = stats.getLastState().getHand(hinted);
		double[] p = stats.getPlayability(hinted);
		for (int i=0; i<hand.size(); i++)
		{
			//Per ogni carta del compagno verifico che sia giocabile e non già sicura (per convenzione o playability)
			if (stats.isPlayable(hand.getCard(i)) && p[i]<1 && !cmap.get(hinted)[0].contains(i))
			{
				//Se c'è un 5 indico quello
				if (hand.getCard(i).getValue() == 5)
				{
					playable.clear();
					playable.add(i);
					break;
				}
				//Altrimenti aggiungo la carta alla lista delle giocabili
				playable.add(i);
			}

		}
		log("Trovate "+playable.size()+" carte giocabili");
		if (playable.size()>0)
		{
			//Se ha carte giocabili cerco tutti i suggerimenti conformi alla convenzione che potrei dare su quella carta
			List<Action> possibleHints = getPossibleHints(hinted);
			//Quindi filtro tutti i suggerimenti possibili
			List<Integer> l_index;
			log("Filtro dai seguenti possibili hints:");
			for (Action h:possibleHints)
				log("\t"+h);
			for (Action possibile : possibleHints) {
				l_index = possibile.getCardsToReveal(stats.getLastState());
				for (int i : playable)
				{
					if (l_index.contains(i)) //Una carta giocabile è suggerita
					{
						Statistics s1 = stats.getStatisticsIf(new Turn(possibile, l_index));
						if ((l_index.lastIndexOf(i) == l_index.size() - 1 && s1.getPlayability(hinted)[i]>stats.getPlayability(hinted)[i])
							|| s1.getPlayability(hinted)[i] == 1)
						{ //La carta giocabile è la più a destra oppure raggiunge playability 1
							hints.add(possibile);
							break;
						}
					}
				}
			}
		}
		log("Trovati "+hints.size()+" suggerimenti su carte giocabili");
		for (Action h:hints)
			log("\t"+h);

		return hints;
	}

	public Action playSecure()
	{
		try
		{
			Hand hand = stats.getLastState().getHand(Main.playerName);
			int index = -1;

			log("Cerco di giocare una carta sicura per playability");
			double[] p = stats.getPlayability(Main.playerName);
			for (int i = 0; i < p.length; i++)
			{
				if (p[i] == 1)
				{
					index = i;
					if (hand.getCard(i).getValue() == 5) //Se ho un 5 con 100% di playability lo gioco subito
						break;
				}
			}
			if (index > -1)
			{
				log("Trovata: gioco la carta "+hand.getCard(index)+" in posizione "+index);
				return new Action(Main.playerName, ActionType.PLAY, index);
			}
			log("Nessuna carta sicura per playability = 100%");

			log("Cerco di giocare una carta sicura per convenzione");
			List<Integer> cp = cmap.get(Main.playerName)[0];
			if (cp.size() > 0)
			{
				index = cp.get(0);
				for (int i = 1; i < cp.size(); i++) {
					if (hand.getCard(cp.get(i)).getValue() == 5) {
						index = cp.get(i);
						break;
					} else if (cp.get(i) > index) {
						index = cp.get(i);
					}
				}
				log("Trovata: gioco la carta "+hand.getCard(index)+" in posizione "+index);
				return new Action(Main.playerName, ActionType.PLAY, index);
			}
			log("Nessuna carta sicura per convenzione");
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

