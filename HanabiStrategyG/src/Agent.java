import api.client.AbstractAgent;
import api.client.Main;
import api.client.StatisticState;
import api.client.Statistics;
import api.game.*;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class Agent extends AbstractAgent
{
	public double P = 1;
	public double U = 1;
	public int HT = 1;
	public int S = 0;
	public int IU = 1;
	public int DU = 1;
	public int IP = 1;
	public int DP = 1;


	public Agent(boolean log, String logpath) throws FileNotFoundException
	{
		super(log,logpath);
	}

	/**
	 * La strategia divide il gioco in due fasi a seconda della condizione deck+score-25&ge;"F". Se true prima fase, altrimenti seconda.
	 * Se "F"=0, durante la prima fase &egrave; ancora possibile raggiungere il punteggio perfetto, nella seconda fase invece no.
	 * Per questo motivo si dovrebbe cercare di prolungare la prima fase il pi&ugrave; possibile tenendo conto del fatto che
	 * scartare le carte avvicina alla seconda fase mentre giocarle o suggerire no.
	 * Di conseguenza si preferisce sempre giocare una carta piuttosto che scartarla.
	 * Inoltre la strategia cerca sempre di evitare situazioni in cui un giocatore non può giocare o scartare una carta
	 * con sicurezza o dare consigli.
	 *
	 * I termini racchiusi da doppi apici indicano parametri genetici. I valori di default sono:
	 * <ul>
	 *     <li>"P" = "U" = 1. Sono valori double che possono variare tra 0 e 1 inclusi</li>
	 *     <li>"HT" = 1. Valore intero che varia tra 1 e 8 inclusi</li>
	 *     <li>"S" = 0. Valore intero che varia tra 0 e 5 inclusi</li>
	 *     <li>"IP" = "DP" = "IU" = "DU" = 1. Valori interi che variano tra 0 e 5 inclusi</li>
	 *
	 * </ul>
	 * La strategia &egrave; definita in funzione del concetto di miglior suggerimento:
	 * <ol>
	 *     	<li>
	 *          Scorri i giocatori e seleziona il suggerimento che causa il miglior aumento di playability,
	 *          calcolato come numero di carte la cui playability arriva a 1. Se tale somma è &ge; "IP" dai il suggerimento.
	 *     	</li>
 	 * 	   	<li>
 	 * 	  	    Scorri i giocatori e seleziona il suggerimento che causa il miglior aumento di uselessness,
	 *			calcolato come numero di carte la cui uselessness arriva a 1. Se tale somma è &ge; "IU" dai il suggerimento.
 	 * 	  	</li>
 	 * 	  	<li>
 	 * 	  	    Scorri i giocatori e seleziona il suggerimento che causa la miglior diminuzione di playability, calcolata come numero
	 * 	  	    di carte la cui playability arriva a 0. Se tale somma è &ge; "DP" dai il suggerimento.
 	 * 	  	</li>
 	 * 	  	<li>
 	 * 	  	    Scorri i giocatori e seleziona il suggerimento che causa la miglior diminuzione di uselessness, calcolata come numero
	 * 	  	    di carte la cui uselessness arriva a 0. Se tale somma è &ge; "DU" dai il suggerimento.
 	 * 	  	</li>
 	 * 	  	<li>
 	 * 	  	    Dai il suggerimento (tra tutti i giocatori) che causa la miglior diminuzione di entropia, calcolata come somma delle
	 * 	  	    singole diminuzioni di entropia delle carte.
 	 * 	  	</li>
	 * </ol>
	 * Nota che l'ordine dei primi 4 controlli per il miglior suggerimento potrebbe essere parametro genetico
	 * (4 elementi diversi, 24 possibili ordinamenti)
	 *
	 * Uno schema della strategia è il seguente:
	 * <ol>
	 *     	<li>
	 *         	Scorri i giocatori (te escluso), se ne esiste uno che ha un numero di carte da giocare o scartare con sicurezza &le; "S"
	 *         	contrassegna quel giocatore come in pericolo.
	 *      </li>
	 *    	<li>
	 *         	Se esiste un giocatore in pericolo e se hint token &ge; "HT" dagli il miglior suggerimento.
	 *      </li>
	 *      <li>
	 *         	Se hint token &lt; "HT"
	 *         <ol>
	 *             <li>
	 *                 Se i "HT"-hint_token giocatori dopo di te non sono in pericolo e hanno una carta scartabile ciascuno gioca la
	 *                 carta con miglior playability &ge; "P"
	 *             </li>
	 *             <li>
	 *                 Scarta la carta con miglior uselessness &ge; "U"
	 *             </li>
	 *             <li>
	 *                 Gioca la carta con miglior playability &ge; "P"
	 *             </li>
	 *             <li>
	 * 	 	     	   Gioca o scarta la carta con entropia minore (in base al massimo valore tra playability e uselessness).
	 * 	 	   	   </li>
	 *         </ol>
	 *      </li>
	 *      <li>
	 *        (nessun giocatore era in pericolo e hint token &ge; "HT")
	 *         <ol>
	 *             <li>
	 *                 Gioca la carta con miglior playability &ge; "P"
	 *             </li>
	 *             <li>
	 *                 Dai il miglior suggerimento possibile
	 *             </li>
	 *         </ol>
	 *      </li>
	 * </ol>
	 * @return l'Action scelta
	 */
	@Override
	public Action chooseAction()
	{
		try {
			String[] comrades = this.sortPlayers();
			Action action;
			int tokens = stats.getLastState().getHintTokens();
			String inDanger = null;
			for (String s : comrades) {
				if (isInDanger(s)) {
					inDanger = s;
					break;
				}
			}
			if (inDanger != null && tokens >= HT) {
				log(inDanger+" e' in pericolo.\nHo abbastanza hint token");
				action = getBestHintFor(inDanger);
				if (action!=null)
					return action;
			/*	if (action == null) {
					log("Tuttavia non riesco a dare un suggerimento efficace\nI consigli possibili erano:");
					for (String player: Game.getInstance().getPlayers())
					{
						if (!player.equals(Main.playerName))
						{
							for (Action h:this.getPossibleHints(player))
								log("\t"+h);
						}
					}
				}
				else
					return action;*/
			}

			if (tokens<HT) {
				log(inDanger+" e' in pericolo.");
				int l = HT - tokens;
				boolean trytoplay = true;
				for (int i = 0; i < l && trytoplay && i < comrades.length; i++) {
					if (isInDanger(comrades[i]))
						trytoplay = false;
					else {
						if (countUseless(comrades[i]) < 1)
							trytoplay = false;
					}
				}
				if (trytoplay) {
					log("Provo comunque a giocare una carta");
					action = getBestPlayAction();
				}
				else {
					log("Provo a scartare una carta");
					action = getBestDiscardAction();
				}

				if (action == null) {
					log("Provo a giocare una carta");
					action = getBestPlayAction();
				}

				if (action == null){
					log("Gioco la carta migliore per entropia");
					action = getBestEntropyAction();
				}

			} else {
				log("Nessuno e' in pericolo.\nProvo a giocare una carta");
				action = getBestPlayAction();
				if (action == null){
					log("Do' il suggerimento migliore");
					action = getBestHint();
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

	private int countPlayable(String player)
	{
		int cont=0;
		double d[] = stats.getPlayability(player);
		for (double dd:d)
		{
			if (dd == 1)
				cont++;
		}
		return cont;
	}

	private int countUseless(String player)
	{
		int cont=0;
		double d[] = stats.getUselessness(player);
		for (double dd:d)
		{
			if (dd == 1)
				cont++;
		}
		return cont;
	}

	public Action getBestPlayAction() throws JSONException
	{
		double[] p = stats.getPlayability(Main.playerName);
		int max = 0;
		for (int i=1; i<p.length; i++)
		{
			if (p[i] > p[max])
				max = i;
		}
		if (p[max]>=P)
			return new Action(Main.playerName, ActionType.PLAY, max);
		return null;
	}

	public Action getBestDiscardAction() throws JSONException
	{
		double[] u = stats.getUselessness(Main.playerName);
		int max = 0;
		for (int i=1; i<u.length; i++)
		{
			if (u[i] > u[max])
				max = i;
		}
		if (u[max]>=U)
			return new Action(Main.playerName, ActionType.DISCARD, max);
		return null;
	}

	public Action getBestEntropyAction() throws JSONException
	{
		double[] e = stats.getCardEntropy(Main.playerName);
		double[] p = stats.getPlayability(Main.playerName);
		double[] u = stats.getUselessness(Main.playerName);
		int min = 0;
		for (int i=1; i<e.length; i++)
		{
			if (e[i] < e[min])
				min = i;
		}
		if (p[min]>u[min])
			return new Action(Main.playerName, ActionType.PLAY, min);
		return new Action(Main.playerName, ActionType.DISCARD, min);
	}

	public Action getBestHint() throws JSONException
	{
		Action ip = null;
		Action iu = null;
		Action dp = null;
		Action du = null;
		Action best_e = null;
		double e_v = 0;
		int ip_v = IP - 1;
		int iu_v = IU - 1;
		int dp_v = DP - 1;
		int du_v = DU - 1;
		for(String player:this.sortPlayers()) {
			List<Action> list = this.getPossibleHints(player);
			double[] ap = stats.getPlayability(player);
			double[] au = stats.getUselessness(player);
			double[] ae = stats.getCardEntropy(player);
			double[] p, u, e;

			Statistics box;
			int v;
			double d;
			for (Action action : list) {
				box = stats.getStatisticsIf(new Turn(action, action.getCardsToReveal(stats.getLastState())));
				p = box.getPlayability(player);
				u = box.getUselessness(player);
				e = box.getCardEntropy(player);

				d = 0;
				for (int i = 0; i < e.length; i++)
					d = ae[i] - e[i];
				if (d > e_v) {
					e_v = d;
					best_e = action;
				}

				v = 0;
				for (int i = 0; i < p.length; i++) {
					if (p[i] == 1 && ap[i] < 1)
						v++;
				}
				if (v > ip_v) {
					ip_v = v;
					ip = action;
				}

				v = 0;
				for (int i = 0; i < u.length; i++) {
					if (u[i] == 1 && au[i] < 1)
						v++;
				}
				if (v > iu_v) {
					iu_v = v;
					iu = action;
				}

				v = 0;
				for (int i = 0; i < p.length; i++) {
					if (p[i] == 0 && ap[i] > 0)
						v++;
				}
				if (v > dp_v) {
					dp_v = v;
					dp = action;
				}

				v = 0;
				for (int i = 0; i < u.length; i++) {
					if (u[i] == 0 && au[i] > 0)
						v++;
				}
				if (v > du_v) {
					du_v = v;
					du = action;
				}
			}
		}
		if (ip!=null)
			return ip;
		if (iu!=null)
			return iu;
		if (dp!=null)
			return dp;
		if (du!=null)
			return du;
		return best_e;
	}

	public Action getBestHintFor(String player) throws JSONException
	{
		double[] ap = stats.getPlayability(player);
		double[] au = stats.getUselessness(player);
		double[] ae = stats.getCardEntropy(player);
		double[] p,u,e;
		Action ip = null;
		Action iu = null;
		Action dp = null;
		Action du = null;
		Action best_e = null;
		double e_v = 0;
		int ip_v = IP-1;
		int iu_v = IU-1;
		int dp_v = DP-1;
		int du_v = DU-1;
		Statistics box;
		int v;
		double d;
		for (Action action:this.getPossibleHints(player))
		{
			log("Studio cosa succede se "+action);
			box = stats.getStatisticsIf(new Turn(action,action.getCardsToReveal(stats.getLastState())));
			p = box.getPlayability(player);
			log(Arrays.toString(p));
			u = box.getUselessness(player);
			e = box.getCardEntropy(player);

			d = 0;
			for (int i=0; i<e.length; i++)
				d += ae[i]-e[i];
			log("Decremento entropia: "+d);
			if (d>e_v)
			{
				e_v = d;
				best_e = action;
			}

			v = 0;
			for (int i=0; i<p.length; i++)
			{
				if (p[i] == 1 && ap[i]<1)
					v++;
			}
			log("Massimizzazioni playability: "+v);
			if (v>ip_v)
			{
				ip_v = v;
				ip = action;
			}

			v = 0;
			for (int i=0; i<u.length; i++)
			{
				if (u[i] == 1 && au[i]<1)
					v++;
			}
			log("Massimizzazioni uselessness: "+v);
			if (v>iu_v)
			{
				iu_v = v;
				iu = action;
			}

			v = 0;
			for (int i=0; i<p.length; i++)
			{
				if (p[i] == 0 && ap[i]>0)
					v++;
			}
			log("Minimizzazioni playability: "+v);
			if (v>dp_v)
			{
				dp_v = v;
				dp = action;
			}

			v = 0;
			for (int i=0; i<u.length; i++)
			{
				if (u[i] == 0 && au[i]>0)
					v++;
			}
			log("Minimizzazioni uselessness: "+v);
			if (v>du_v)
			{
				du_v = v;
				du = action;
			}
		}
		if (ip!=null) {
			log("Trovato un consiglio che incrementa le playability");
			return ip;
		}
		if (iu!=null) {
			log("Trovato un consiglio che incrementa le uselessness");
			return iu;
		}
		if (dp!=null) {
			log("Trovato un consiglio che decrementa le playability");
			return dp;
		}
		if (du!=null) {
			log("Trovato un consiglio che decrementa le uselessness");
			return du;
		}
		log("Do' il consiglio migliore per entropia");
		return best_e;
	}

	public boolean isInDanger(String player)
	{
		return countPlayable(player)+countUseless(player)<=S;
	}

	public void notifyState(State state)
	{
		super.notifyState(state);
		try
		{
			log(new StatisticState(state,stats).toString());
		}
		catch (JSONException e)
		{
			log(e);
		}
	}

	public void notifyTurn(Turn turn)
	{
		super.notifyTurn(turn);
		log(turn.toString());
	}

	/**Copiato da Strategy1
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		boolean log = false;
		String logpath = null;
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
		}
		AbstractAgent agent = new Agent(log,logpath);
		Main.setAgent(agent);
		Main.main(args);
	}
}
