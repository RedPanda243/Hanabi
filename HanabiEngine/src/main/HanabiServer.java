package main;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

public class HanabiServer
{
	private static Socket[] players;
	private static Card drawn = null;

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
	 *     <li>Ricezione del proprio nome usato dal gioco (eventualmente modificato, succede in caso di nome gi&agrave; preso).</li>
	 *     <li>Ricezione di un oggetto Game</li>
	 *     <li>
	 *         <ul>WHILE !GAMEOVER
	 *             <li>Ricezione oggetto State</li>
	 *             <li>Invio propria Action se &egrave; il proprio turno, ricezione di un Turn altrimenti</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 * </br></br>
	 * Opzioni di avvio:
	 * <ul>
	 * 		<li>-l = mostra il log della partita mossa per mossa</li>
	 * 		<li>-p "port" = imposta la porta tcp locale. Default: 9494</li>
	 *		<li>-g "numplayers" = imposta il numero di giocatori. Default: 2</li>
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
		boolean log = false;
		String logpath = null;
		JSONArray playerNames = new JSONArray();
		ArrayList<State> history = new ArrayList<>();
		ArrayList<Turn> turns = new ArrayList<>();

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
			else if (args[i].equals("-g"))
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
		ServerSocket ss = new ServerSocket(port);
		System.out.println("Server avviato."); //TODO stampa tcp address (locale e remoto)
		int i = 0;
		players = new Socket[n];
		String pname;
		while (i<n)
		{ //Non c'è concorrenza, i giocatori sono accettati uno alla volta e giocheranno nell'ordine di arrivo
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
				System.out.println("Giocatore "+i+" ("+pname+") connesso");
				i++;
			}
			catch(IOException | ClassCastException e)
			{e.printStackTrace(System.err);}
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
			next = nextState(last,a,deck);
			history.add(last);
			sendTurn(last.getCurrentPlayer(),a);
			last = next;
		}
		sendState(last,players);
	}

	private static State nextState(State current, Action move,Stack<Card> deck) throws IOException,JSONException
	{
		State next = current.clone();
		if (move.getActionType() == ActionType.PLAY)
		{
			drawn = deck.pop();
			Card played = next.getHand(move.getPlayer()).getCard(move.getCard());
			next.getHand(move.getPlayer()).replace(move.getCard(),drawn);
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
				{ex.printStackTrace(System.err);}
			}
		}
		else if (move.getActionType() == ActionType.DISCARD)
		{
			drawn = deck.pop();
			Card played = next.getHand(move.getPlayer()).getCard(move.getCard());
			next.getHand(move.getPlayer()).replace(move.getCard(),drawn);
			next.getDiscards().add(played);
			if (next.getHintTokens()<8)
				try
				{
					next.setHintToken(next.getHintTokens()+1);
				}
				catch(JSONException e){e.printStackTrace(System.err);} //Impossibile
		}
		else if (next.getHintTokens()>0)
		{
			//System.out.println("Hint per "+Game.getInstance().getPlayerTurn(move.getHintReceiver()));
			next.getHints().add(move);
			drawn = null;
			Hand hand = next.getHand(move.getHintReceiver());
			if (move.getActionType() == ActionType.HINT_COLOR)
			{
				for (int i = 0; i < hand.size(); i++)
				{
					if (hand.getCard(i).getColor().equals(move.getColor()))
						hand.getCard(i).setColorRevealed(true);
				}
			}
			else
			{
				for (int i = 0; i < hand.size(); i++)
				{
					if (hand.getCard(i).getValue() == move.getValue())
						hand.getCard(i).setValueRevealed(true);
				}
			}
			try
			{
				next.setHintToken(next.getHintTokens()-1);
			}
			catch (JSONException e){e.printStackTrace(System.err);} //Impossibile
		}
		else
		{
			return current; //Così il ciclo ricomincia.
		}
		if (Game.getInstance().getPlayerTurn(next.getCurrentPlayer())==Game.getInstance().getPlayers().size()-1)
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

	private static void sendState(State state, Socket[] players) throws IOException
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
					card.set("color","");
				if (!card.isValueRevealed())
					card.set("value","0");
				hand.replace(k,card);
				//forse questo replace è inutile
			}
			box.setHand(Game.getInstance().getPlayer(i),hand);
			PrintStream ps = new PrintStream(players[i].getOutputStream());
			ps.print(box.toString(0));
			ps.flush();
		}
	}

	private static void sendTurn(String turnPlayer, Action action) throws JSONException,IOException
	{
		int x = Game.getInstance().getPlayerTurn(turnPlayer);
		Turn t;
		if (action.getActionType() == ActionType.PLAY || action.getActionType() == ActionType.DISCARD)
			t = new Turn(action,drawn);
		else
			t = new Turn(action);
		PrintStream ps;
		for (int i=0; i<players.length; i++)
		{
			if (i!=x)
			{
				ps = new PrintStream(players[i].getOutputStream());
				ps.print(t.toString(0));
	//			System.out.println("Sending to "+i+" "+t.toString(0));
				ps.flush();
			}
		}
	}
}
