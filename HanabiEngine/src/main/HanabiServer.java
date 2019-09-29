package main;

import agents.RemoteAgent;
import api.game.Card;
import api.game.Color;
import api.game.State;
import api.game.Turn;
import game.ServerCard;
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
	private static Card createCard(Color color, int value)
	{
		JSONObject obj = new JSONObject();
		obj.set("color",color.toString().toLowerCase());
		obj.set("value", ""+value);
		obj.set("value_revealed","false");
		obj.set("color_revealed","false");
		try
		{
			return new Card(obj.toString(0));
		}
		catch(JSONException e)
		{
			System.err.println("Error while generating deck");
			e.printStackTrace(System.err);
			System.exit(1);
			return null;
		}
	}

	private static State createInitState(Stack<Card> deck, int numPlayers)
	{
		JSONObject json = new JSONObject();
		json.set("discarded",new JSONArray());
		json.set("red",new JSONArray());
		json.set("green",new JSONArray());
		json.set("white",new JSONArray());
		json.set("blue",new JSONArray());
		json.set("yellow",new JSONArray());
		json.set("current",""+0);
		json.set("order",""+0);
		json.set("fuse",""+3);
		json.set("hints",""+8);
		json.set("final",""+-1);
		JSONArray hands = new JSONArray();
		JSONArray box;
		for (int i=0; i<numPlayers; i++)
		{
			box = new JSONArray();
			for (int j=0; j<(numPlayers>3?4:5); j++)
				box.add(deck.pop());
			hands.add(box);
		}
		json.set("hands",hands);
		try
		{
			return new State(json.toString(0));
		}
		catch(JSONException e) {return null;}
	}

	private static Card[] deck = {
			createCard(Color.BLUE,1),createCard(Color.BLUE,1), createCard(Color.BLUE,1),
			createCard(Color.BLUE,2),createCard(Color.BLUE,2),createCard(Color.BLUE,3),createCard(Color.BLUE,3),
			createCard(Color.BLUE,4),createCard(Color.BLUE,4),createCard(Color.BLUE,5),
			createCard(Color.RED,1),createCard(Color.RED,1), createCard(Color.RED,1),
			createCard(Color.RED,2),createCard(Color.RED,2),createCard(Color.RED,3),createCard(Color.RED,3),
			createCard(Color.RED,4),createCard(Color.RED,4),createCard(Color.RED,5),
			createCard(Color.GREEN,1),createCard(Color.GREEN,1), createCard(Color.GREEN,1),
			createCard(Color.GREEN,2),createCard(Color.GREEN,2),createCard(Color.GREEN,3),createCard(Color.GREEN,3),
			createCard(Color.GREEN,4),createCard(Color.GREEN,4),createCard(Color.GREEN,5),
			createCard(Color.WHITE,1),createCard(Color.WHITE,1), createCard(Color.WHITE,1),
			createCard(Color.WHITE,2),createCard(Color.WHITE,2),createCard(Color.WHITE,3),createCard(Color.WHITE,3),
			createCard(Color.WHITE,4),createCard(Color.WHITE,4),createCard(Color.WHITE,5),
			createCard(Color.YELLOW,1),createCard(Color.YELLOW,1), createCard(Color.YELLOW,1),
			createCard(Color.YELLOW,2),createCard(Color.YELLOW,2),createCard(Color.YELLOW,3),createCard(Color.YELLOW,3),
			createCard(Color.YELLOW,4),createCard(Color.YELLOW,4),createCard(Color.YELLOW,5)
	};

	/**
	 * Il mescolamento delle carte del mazzo &egrave; simulato invertendo 2 carte random nel mazzo per 1000 volte
	 * @return Uno Stack di ServerCard in ordine casuale rappresentante un mazzo di carte mescolato.
	 **/
	private static Stack<Card> shuffledDeck(){
		Card[] d = deck.clone();
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
	 * Uso:
	 * <ul>
	 * 		<li>-l = mostra il log della partita mossa per mossa</li>
	 * 		<li>-p "port" = imposta la porta tcp locale. Default: 9494</li>
	 *		<li>-g "numplayers" = imposta il numero di giocatori. Default: 2</li>
	 *		<li>-f "logfilepath"|null = imposta il percorso del file nel quale memorizzare il log a partita finita.
	 *									Se null (default) non verr&agrave; memorizzato</li>
	 * </ul>
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		int port = 9494;
		int n = 2;
		boolean log = false;
		String logpath = null;
		ArrayList<String> playerNames = new ArrayList<>();
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
		Socket s;
		String pname;
		while (i<n)
		{ //Non c'è concorrenza, i giocatori sono accettati uno alla volta e giocheranno nell'ordine di arrivo
			try
			{
				s = ss.accept();
				pname = new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();
				if (playerNames.contains(pname))
				{
					int p=2;
					pname = pname+p;
					while(playerNames.contains(pname))
					{
						p++;
						pname = pname.substring(0,pname.length()-1)+p;
					}
				}
				playerNames.add(pname);
				new PrintStream(s.getOutputStream()).println(pname);
				s.getOutputStream().flush();
				System.out.println("Player"+i+" ("+pname+") connesso");
				i++;
			}
			catch(IOException | ClassCastException e)
			{}
		}
		Stack<Card> deck = shuffledDeck();
		System.out.println("Creazione stato iniziale");
		history.add(createInitState(deck,n));
	}

}
