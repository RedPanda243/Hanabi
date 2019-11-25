package main;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Classe eseguibile, crea e mantiene la partita.
 */
public class HanabiServer
{
	private Socket[] players;
	private Card drawn = null;
	private boolean log;
	private PrintStream logfile;
	private int port;
	private int n;
	private List<String> playerNames;
	private int games;
	private int score;

	private HanabiServer(int games, int port, int n, boolean log, String logpath) throws IOException
	{
		this.games = games;
		this.port = port;
		this.n = n;
		this.log = log;
		if (logpath != null)
			logfile = new PrintStream(System.getProperty("user.dir")+"/"+logpath);
		else logfile = null;

	}

	private void initAgents(List<String> local) throws IOException
	{
		playerNames = new ArrayList<>();
		ServerSocket ss = new ServerSocket(port);
//		log("Server avviato."); //TODO stampa tcp address (locale e remoto)
		players = new Socket[n];
		String pname;

		if(local!=null)
			launch(local);


		for (int i=0; i<n; i++)
		{ //Non c'è concorrenza, i giocatori sono accettati uno alla volta
			try
			{
				players[i] = ss.accept();
				pname = new BufferedReader(new InputStreamReader(players[i].getInputStream())).readLine();
				if (playerNames.contains(pname))
				{ //Risoluzione conflitti per stesso nome
					int p=2;
					pname = pname+p;
					while(playerNames.contains(pname))
					{
						p++;
						pname = pname.substring(0,pname.length()-1)+p;
					}
				}
				playerNames.add(pname);
				new PrintStream(players[i].getOutputStream()).println(pname);
				players[i].getOutputStream().flush();
//				log("Giocatore "+i+" ("+pname+") connesso");
			}
			catch(IOException | ClassCastException e)
			{
				log(e);
			}
		}

		//Shuffle dei giocatori, per stabilire l'ordine di gioco. Effettuo al massimo n scambi.
		java.util.Random r = new java.util.Random();
		int a,b;
		Socket s;
		String n;
		for (int i=0; i<players.length; i++)
		{
			a = r.nextInt(players.length);
			b = r.nextInt(players.length);
			s = players[a];
			players[a] = players[b];
			players[b] = s;
			n = playerNames.get(a);
			playerNames.set(a,playerNames.get(b));
			playerNames.set(b,n);
		}


	}

	private void launch(List<String> cmds) throws IOException
	{
		File dir = new File(System.getProperty("user.dir"));

		Thread t = new Thread(new Runnable() {
			@Override
			public void run()
			{
				for(String c:cmds)
				{
					try
					{
						Runtime.getRuntime().exec(c,null,dir);
					}
					catch(IOException e)
					{
						log(e);
					}
				}
			}
		});
		t.start();
	}

	/**
	 * Il mescolamento delle carte del mazzo &egrave; simulato invertendo 2 carte random nel mazzo per 1000 volte
	 * @return Uno Stack di Card in ordine casuale rappresentante un mazzo di carte mescolato.
	 **/
	private static Stack<Card> shuffle(List<Card> cards)
	{
		java.util.Random r = new java.util.Random();
		for(int i = 0; i<1000; i++){
			int a = r.nextInt(50);
			int b = r.nextInt(50);
			Card c = cards.get(a);
			cards.set(a,cards.get(b));
			cards.set(b,c);
		}
		Stack<Card> shuffle = new Stack<>();
		for(Card c: cards) shuffle.push(c);
		return shuffle;
	}

	/**
	 * Il mescolamento delle carte del mazzo &egrave; simulato riempendo lo stack una carta casuale alla volta
	 * @return Uno Stack di Card in ordine casuale rappresentante un mazzo di carte mescolato.
	 **/
	private static Stack<Card> shuffle1(List<Card> cards)
	{
		java.util.Random r = new java.util.Random();
		Stack<Card> shuffle = new Stack<>();
		while (cards.size()>0)
			shuffle.push(cards.remove(r.nextInt(cards.size())));
		return shuffle;
	}

	/**
	 * Avvia un server di una partita. L'applicazione attender&agrave; sulla porta specificata la connessione del numero di giocatori
	 * previsto, poi avvier&agrave; la partita.</br>
	 * La corretta partecipazione ad una partita richiede di rispettare il seguente protocollo:
	 * <ul>
	 *     <li>Invio del proprio nome preferito come stringa terminata da un carattere '\n'</li>
	 *     <li>Ricezione del proprio nome usato dal gioco (modificato in caso di nome chiesto da più giocatori).</li>
	 *     <li>Ricezione di un oggetto Game</li>
	 *     <li>
	 *         <ul>WHILE !GAMEOVER
	 *             <li>Ricezione oggetto State</li>
	 *             <li>Invio propria Action se &egrave; il proprio turno</li>
	 *             <li>Ricezione oggetto Turn</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 * </br></br>
	 * Opzioni di avvio:
	 * <ul>
	 * 		<li>-g "games"				= imposta il numero di partite da effettuare. Default: 1. Le partite verranno effettuate
	 * 									  una alla volta.</li>
	 * 		<li>-l			 			= mostra il log delle partite mossa per mossa</li>
	 * 		<li>-p "port" 				= imposta la porta tcp locale. Default: 9494</li>
	 *		<li>-n "numplayers" 		= imposta il numero di giocatori. Default: 2</li>
	 *		<li>-f "logfilepath"|null 	= imposta il percorso del file nel quale memorizzare il log a partita finita.
	 *									  Se null (default) non verr&agrave; memorizzato</li>
	 *		<li>-a "agent"				= imposta il tipo di un giocatore avviato in automatico. Pu&ograve; essere ripetuto
	 *									  per impostare più giocatori automatici. Richiede il comando shell di avvio del giocatore</li>
	 * </ul>
	 *
	 * Esempi di comandi di lancio (con terminale aperto nella root del progetto):
	 *
	 * java -jar HanabiServer -n 4 -a "java -jar HanabiStrategy1.jar 0 9494 PlayerStrategy1"
	 * Avvio di una sola partita tra 4 giocatori di cui 1 Strategy1 locale chiamato PlayerStrategy1.
	 *
	 * java -jar HanabiServer -g 2 -a "java -jar HanabiStrategy1.jar 0 9494 PlayerStrategy1" -n 3 -a "java -jar HanabiHuman.jar 0 9494"
	 * Avvio di 2 partite (eseguite sequenzialmente) tra 3 giocatori di cui 2 locali, uno Human e uno Strategy1
	 * @param args
	 */
	public static void main(String[] args) throws IOException,JSONException
	{
		//Settaggio impostazioni, avvio e attesa delle connessioni dei giocatori.

		int port = 9494;
		int n = 2;
		int g = 1;
		boolean log = false;
		ArrayList<String> lcmd = new ArrayList<>();
		String logpath = null;


		for (int i=0; i<args.length; i++)
		{
			if (args[i].equals("-g"))
			{
				i++;
				g = Integer.parseInt(args[i]);
			}
			else if (args[i].equals("-a"))
			{
				i++;
				lcmd.add(args[i]);
			}
			else if (args[i].equals("-l"))
			{
				log = true;
			}
			else if (args[i].equals("-p"))
			{
				i++;
				port = Integer.parseInt(args[i]);
			}
			else if (args[i].equals("-n"))
			{
				i++;
				n = Integer.parseInt(args[i]);
			}
			else if (args[i].equals("-f"))
			{
				i++;
				if (args[i].equals("null"))
					logpath = null;
				else
					logpath=""+args[i];
			}
		}


		HanabiServer server = new HanabiServer(g,port,n,log,logpath);

		double score = 0;
		double t;
		double max=0,min=25;
		for (int i=0; i<server.games; i++) {
			server.log("Partita "+(i+1)+" su "+server.games);
			server.initAgents(lcmd);
			//Connessioni completate, avvio del gioco
			t = server.startGame();
			if (t>max)
				max = t;
			if (t<min)
				min = t;
			score+=t;
		}
		score = score/server.games;
		server.log("MEDIUM SCORE: "+score);
		server.log("MAX SCORE: "+max);
		server.log("MIN SCORE: "+min);
		if (server.logfile!=null)
			server.logfile.close();
	}

	private int startGame() throws IOException,JSONException
	{
		new Game(playerNames);
//		System.err.println(Game.getInstance().getPlayers());
		PrintStream ps;
		for (Socket socket:players)
		{
			ps = new PrintStream(socket.getOutputStream());
			ps.print(Game.getInstance().toString(0));
			ps.flush();
		}

		Stack<Card> deck = shuffle(Card.getAllCards());
//		System.err.println(Game.getInstance().getNumberOfCardsPerPlayer());
		State last = new State(deck);
		State next;

		while(!(last.gameOver()))
		{
			sendState(last,players);
			Action a = receiveAction(last.getCurrentPlayer());
		/*	if(a.names().size() == 0)
				throw new JSONException("Action null from player"+last.getCurrentPlayer());*/
			next = nextState(last,a,deck);
		//	history.add(last);
			sendTurn(last.getCurrentPlayer(),a,last);
			last = next;

			//PROVA *************************************
//			if(deck.empty()){
//				break;
//			}
		}
		sendState(last,players);
		log(""+last.getScore());

		for (Socket s: players)
			s.close();

		Game.close();

		return last.getScore();
	}

	private void log(String s)
	{
		if (log)
			System.out.println(s);
		if (logfile!=null)
			logfile.println(s);
	}

	private void log(Exception e)
	{
		if (log)
			e.printStackTrace(System.out);
		if (logfile!=null)
			e.printStackTrace(logfile);
	}

	private State nextState(State current, Action move,Stack<Card> deck) throws JSONException
	{
//		log("[ACTION "+move.getType().toString()+"] "+move.toString()+"\n"+"[DECK] = "+deck.size());
		State next = current.clone();
		next.setAction(move);
		 //TODO Se l'Action è un suggerimento pesco a cazzo e la carta è persa!!!

		if (move.getType() == ActionType.PLAY)
		{
			if(deck.size()==0) {
				drawn = null;
			}
			else {
				drawn = deck.pop();
				if(deck.size() == 0 && current.getFinalActionIndex() == -1)
					next.setFinalActionIndex(current.getOrder()+Game.getInstance().getPlayers().length);
			}

			Card played = next.getHand(move.getPlayer()).getCard(move.getCard());
			next.getHand(move.getPlayer()).removeCard(move.getCard());
			if (drawn != null)
				next.getHand(move.getPlayer()).addCard(drawn);
			try
			{
				next.getFirework(played.getColor()).addCard(played);
				if (next.getFirework(played.getColor()).peak() == 5 && next.getHintTokens()<8)
					next.setHintToken(next.getHintTokens()+1);
			}
			catch (JSONException e)
			{//Wrong card
				next.getDiscards().add(played);
				try
				{
					next.setFuseToken(next.getFuseTokens()-1);
				}
				catch (JSONException ex)//Impossibile
				{
					log(ex);}
			}
		}
		else if (move.getType() == ActionType.DISCARD)
		{
			if(deck.size()==0) {
				drawn = null;
			}
			else {
				drawn = deck.pop();
				if(deck.size() == 0 && current.getFinalActionIndex() == -1)
					next.setFinalActionIndex(current.getOrder()+Game.getInstance().getPlayers().length);
			}
			Card played = next.getHand(move.getPlayer()).getCard(move.getCard());
			next.getHand(move.getPlayer()).removeCard(move.getCard());
			if (drawn != null)
				next.getHand(move.getPlayer()).addCard(drawn);
			next.getDiscards().add(played);
			if (next.getHintTokens()<8)
				try
				{
					next.setHintToken(next.getHintTokens()+1);
				}
				catch(JSONException e){
					log(e);} //Impossibile
		}
		else if (next.getHintTokens()>0)
		{
			drawn = null;
			Hand hand = next.getHand(move.getHinted());
			int j;
			//COLORE
			if (move.getType() == ActionType.HINT_COLOR)
			{
				j = 0;
				for (int i = 0; i < hand.size(); i++)
				{
					if (hand.getCard(i).getColor().equals(move.getColor())) {
						if(move.getCardsToReveal(current).get(j) == i) {
							hand.getCard(i).setColorRevealed(true);
							j++;
						} else
							throw new JSONException("Carte to Hint ricevute da player diverse dalle reali da segnalare");
					}
				}
			}
			else //VALUE
			{
				j = 0;
				for (int i = 0; i < hand.size(); i++)
				{
					if (hand.getCard(i).getValue() == move.getValue()) {
						if(move.getCardsToReveal(current).get(j) == i) {
							hand.getCard(i).setValueRevealed(true);
							j++;
						} else
							throw new JSONException("Carte to Hint ricevute da player diverse dalle reali da segnalare");
					}
				}
			}
			try
			{
				next.setHintToken(next.getHintTokens()-1);
			}
			catch (JSONException e){
				log(e);} //Impossibile
		}
		else
		{
			return current; //Così il ciclo ricomincia.
		}
		if (Game.getInstance().getPlayerTurn(next.getCurrentPlayer())==Game.getInstance().getPlayers().length-1)
			next.setCurrentPlayer(Game.getInstance().getPlayer(0));
		else
			next.setCurrentPlayer(Game.getInstance().getPlayer(Game.getInstance().getPlayerTurn(next.getCurrentPlayer())+1));

		next.setOrder(next.getOrder()+1);
		next.setDeck(deck.size());
		return next;
	}

	private Action receiveAction(String player) throws IOException,JSONException
	{
		return new Action(new BufferedReader(new InputStreamReader(players[Game.getInstance().getPlayerTurn(player)].getInputStream())));
	}

	private void sendState(State state, Socket[] players) throws IOException,JSONException
	{
//		log(state.toString());
		State box;
		for (int i=0; i<players.length; i++)
		{
			box = state.clone();
			Hand hand = box.getHand(Game.getInstance().getPlayer(i));
			Card card;
			for (int k=0; k<hand.size(); k++)
			{
				card = hand.getCard(k);
				if (!card.isColorRevealed())
					card.setColor(null);
				if (!card.isValueRevealed())
					card.setValue(0);
			}
	//		box.setHand(Game.getInstance().getPlayer(i),hand);
			PrintStream ps = new PrintStream(players[i].getOutputStream());
			ps.print(box.toString(0));
			ps.flush();
		}
	}

	private void sendTurn(String turnPlayer, Action action, State current) throws JSONException,IOException
	{
		Turn t,t1;
		if (action.getType() == ActionType.PLAY || action.getType() == ActionType.DISCARD) {
			if (drawn==null)
			{
				t = new Turn(action, current.getHand(turnPlayer).getCard(action.getCard()), null);
				t1 = new Turn(action, current.getHand(turnPlayer).getCard(action.getCard()), null);
			}
			else
			{
				t = new Turn(action, current.getHand(turnPlayer).getCard(action.getCard()), drawn);
				t1 = new Turn(action, current.getHand(turnPlayer).getCard(action.getCard()), new Card(null, 0));
			}
		}
		else
		{
			t = new Turn(action, action.getCardsToReveal(current));
			t1 = new Turn(action, action.getCardsToReveal(current));
		}
		PrintStream ps;
		int x = Game.getInstance().getPlayerTurn(action.getPlayer());
		for (int i=0; i<players.length; i++)
		{
			ps = new PrintStream(players[i].getOutputStream());
			if (i!=x)
				ps.print(t.toString(0));
			else
				ps.print(t1.toString(0));
	//			log("Sending to "+i+" "+t.toString(0));
			ps.flush();
		}

	}
}
