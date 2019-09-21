package main;

import agents.RemoteAgent;
import hanabAI.Agent;
import game.State;

import java.io.IOException;
import java.net.ServerSocket;

public class HanabiServer
{
	public static HanabiServer instance;

	private Agent[] players;
	private String logpath;
	private State currentState;

	private HanabiServer(Agent[] players, String logpath)
	{
		this.players = players;
		this.logpath = logpath;
		currentState = new State(players);
	}

	public State getCurrentState()
	{
		return currentState;
	}

	public Agent getPlayer(int index)
	{
		if (index<0 || index>players.length-1) throw new IndexOutOfBoundsException("players="+players.length+", index="+index);
		return players[index];
	}

	private void play()
	{

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
	 *									Se null non verr&agrave; memorizzato</li>
	 * </ul>
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		int port = 9494;
		int n = 2;
		boolean log = false;
		String logpath;

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
		RemoteAgent[] players = new RemoteAgent[n];
		while (i<players.length)
		{ //Non c'è concorrenza, i giocatori sono accettati uno alla volta e giocheranno nell'ordine di arrivo
			try
			{
				players[i] = new RemoteAgent(ss.accept());
				System.out.println("Player"+i+" ("+players[i].getName()+") connesso");
				i++;
			}
			catch(IOException | ClassCastException e)
			{}
		}

		instance = new HanabiServer(players,logpath);
		instance.play();
	}

}
