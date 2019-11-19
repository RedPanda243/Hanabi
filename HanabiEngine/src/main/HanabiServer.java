package main;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Classe eseguibile, crea e mantiene la partita.
 */
public class HanabiServer
{
	private static Socket[] players;
	private static Card drawn = null;
	private static boolean log = false;
	private static PrintStream logfile;
	private static String logpath = null;

	/**
	 * Il mescolamento delle carte del mazzo &egrave; simulato invertendo 2 carte random nel mazzo per 1000 volte
	 * @return Uno Stack di ServerCard in ordine casuale rappresentante un mazzo di carte mescolato.
	 **/
	private static Stack<Card> shuffle(Card[] cards){
		Card[] d = cards.clone();
		java.util.Random r = new java.util.Random();
		for(int i = 0; i<1000; i++){
			int a = r.nextInt(50);
			int b = r.nextInt(50);
			Card c = d[a];
			d[a]= d[b];
			d[b]=c;
		}
		Stack<Card> shuffle = new Stack<>();
		for(Card c: d) shuffle.push(c);
		return shuffle;
	}

	/**
	 * Avvia un server di una partita. L'applicazione attender&agrave; sulla porta specificata la connessione del numero di giocatori
	 * previsto, poi avvier&agrave; la partita.</br>
	 * La corretta partecipazione ad una partita richiede di rispettare il seguente protocollo:
	 * <ul>
	 *     <li>Invio del proprio nome come stringa terminata da un carattere '\n'</li>
	 *     <li>Ricezione del proprio nome usato dal gioco (modificato in caso di nome ripetuto).</li>
	 *     <li>Ricezione di un oggetto Game</li>
	 *     <li>
	 *         <ul>WHILE !GAMEOVER
	 *             <li>Ricezione oggetto State</li>
	 *             <li>Invio propria Action se &egrave; il proprio turno, altrimenti ricezione di un Turn</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 * </br></br>
	 * Opzioni di avvio:
	 * <ul>
	 * 		<li>-l = mostra il log della partita mossa per mossa</li>
	 * 		<li>-p "port" = imposta la porta tcp locale. Default: 9494</li>
	 *		<li>-n "numplayers" = imposta il numero di giocatori. Default: 2</li>
	 *		<li>-f "logfilepath"|null = imposta il percorso del file nel quale memorizzare il log a partita finita.
	 *									Se null (default) non verr&agrave; memorizzato</li>
	 * </ul>
	 * @param args
	 */
	public static void main(String[] args) throws IOException,JSONException
	{
		//Settaggio impostazioni, avvio e attesa delle connessioni dei giocatori.
		
		int port = 9494;
		int n = 2;

		JSONArray playerNames = new JSONArray();
		ArrayList<State> history = new ArrayList<>();
	//	ArrayList<Turn> turns = new ArrayList<>();


		for (int i=0; i<args.length; i++)
		{
			if (args[i].equals("-l"))
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

		if (logpath != null)
			logfile = new PrintStream(System.getProperty("user.dir")+"/"+logpath);
		else logfile = null;

		ServerSocket ss = new ServerSocket(port);
		log("Server avviato."); //TODO stampa tcp address (locale e remoto)
		int i = 0;
		players = new Socket[n];
		String pname;
		while (i<n)
		{ //Non c'è concorrenza, i giocatori sono accettati uno alla volta e giocheranno nell'ordine di arrivo
			//TODO Rimescola i giocatori quando li hai tutti!
			try
			{
				players[i] = ss.accept();
				pname = new BufferedReader(new InputStreamReader(players[i].getInputStream())).readLine();
				if (playerNames.has(pname))
				{ //Risoluzione conflitti per stesso nome
					int p=2;
					pname = pname+p;
					while(playerNames.has(pname))
					{
						p++;
						pname = pname.substring(0,pname.length()-1)+p;
					}
				}
				playerNames.add(pname);
				new PrintStream(players[i].getOutputStream()).println(pname);
				players[i].getOutputStream().flush();
				log("Giocatore "+i+" ("+pname+") connesso");
				i++;
			}
			catch(IOException | ClassCastException e)
			{
				logException(e);
			}
		}

		//Connessioni completate, avvio del gioco

		new Game(playerNames);
//		System.err.println(Game.getInstance().getPlayers());
		PrintStream ps;
		for (Socket socket:players)
		{
			ps = new PrintStream(socket.getOutputStream());
			ps.print(Game.getInstance().toString(0));
			ps.flush();
		}

		Card[] cards = {
				new Card(Color.BLUE,1),new Card(Color.BLUE,1), new Card(Color.BLUE,1),
				new Card(Color.BLUE,2),new Card(Color.BLUE,2),new Card(Color.BLUE,3),new Card(Color.BLUE,3),
				new Card(Color.BLUE,4),new Card(Color.BLUE,4),new Card(Color.BLUE,5),
				new Card(Color.RED,1),new Card(Color.RED,1), new Card(Color.RED,1),
				new Card(Color.RED,2),new Card(Color.RED,2),new Card(Color.RED,3),new Card(Color.RED,3),
				new Card(Color.RED,4),new Card(Color.RED,4),new Card(Color.RED,5),
				new Card(Color.GREEN,1),new Card(Color.GREEN,1), new Card(Color.GREEN,1),
				new Card(Color.GREEN,2),new Card(Color.GREEN,2),new Card(Color.GREEN,3),new Card(Color.GREEN,3),
				new Card(Color.GREEN,4),new Card(Color.GREEN,4),new Card(Color.GREEN,5),
				new Card(Color.WHITE,1),new Card(Color.WHITE,1), new Card(Color.WHITE,1),
				new Card(Color.WHITE,2),new Card(Color.WHITE,2),new Card(Color.WHITE,3),new Card(Color.WHITE,3),
				new Card(Color.WHITE,4),new Card(Color.WHITE,4),new Card(Color.WHITE,5),
				new Card(Color.YELLOW,1),new Card(Color.YELLOW,1), new Card(Color.YELLOW,1),
				new Card(Color.YELLOW,2),new Card(Color.YELLOW,2),new Card(Color.YELLOW,3),new Card(Color.YELLOW,3),
				new Card(Color.YELLOW,4),new Card(Color.YELLOW,4),new Card(Color.YELLOW,5)
		};

		Stack<Card> deck = shuffle(cards);
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
			history.add(last);
			sendTurn(last.getCurrentPlayer(),a,last);
			last = next;

			//PROVA *************************************
//			if(deck.empty()){
//				break;
//			}
		}
		sendState(last,players);
		if (logfile!=null)
			logfile.close();
	}

	private static void log(String s)
	{
		if (log)
			System.out.println(s);
		if (logfile!=null)
			logfile.println(s);
	}

	private static void logException(Exception e)
	{
		if (log)
			e.printStackTrace(System.err);
		if (logfile!=null)
			e.printStackTrace(logfile);
	}

	private static State nextState(State current, Action move,Stack<Card> deck) throws JSONException
	{
		log("[ACTION "+move.getType().toString()+"] "+move.toString()+"\n"+"[DECK] = "+deck.size());
		State next = current.clone();
		next.setAction(move);
		if(deck.size()==0) {
			drawn = null;
			if(current.getFinalActionIndex() == -1)
				next.setFinalActionIndex(current.getOrder()+Game.getInstance().getPlayers().length);
		}
		else
			drawn = deck.pop(); //TODO Se l'Action è un suggerimento pesco a cazzo e la carta è persa!!!

		if (move.getType() == ActionType.PLAY)
		{
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
				{logException(ex);}
			}
		}
		else if (move.getType() == ActionType.DISCARD)
		{
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
				catch(JSONException e){logException(e);} //Impossibile
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
			catch (JSONException e){logException(e);} //Impossibile
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
		return next;
	}

	private static Action receiveAction(String player) throws IOException,JSONException
	{
		return new Action(new BufferedReader(new InputStreamReader(players[Game.getInstance().getPlayerTurn(player)].getInputStream())));
	}

	private static void sendState(State state, Socket[] players) throws IOException,JSONException
	{
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

	private static void sendTurn(String turnPlayer, Action action, State current) throws JSONException,IOException
	{
		int x = Game.getInstance().getPlayerTurn(turnPlayer);
		Turn t;
		if (action.getType() == ActionType.PLAY || action.getType() == ActionType.DISCARD)
			t = new Turn(action,drawn);
		else
			t = new Turn(action,action.getCardsToReveal(current));
		PrintStream ps;
		for (int i=0; i<players.length; i++)
		{
			if (i!=x)
			{
				ps = new PrintStream(players[i].getOutputStream());
				ps.print(t.toString(0));
	//			log("Sending to "+i+" "+t.toString(0));
				ps.flush();
			}
		}
	}
}
