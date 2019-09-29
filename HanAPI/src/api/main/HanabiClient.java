package api.main;

import api.game.*;
import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class HanabiClient
{
	private static HanabiClient instance = null;
	private Socket socket;
	private Game game;
	private String name;
	private PrintStream out;
	private BufferedReader in;
	private State currentState;

	private HanabiClient(String host, int port, String playerName) throws IOException
	{
		socket = new Socket(host,port);
		out = new PrintStream(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.println(playerName);
		out.flush();
		name = in.readLine();
		try
		{
			game = new Game(in);
		}
		catch(JSONException e)
		{
			throw new IOException(e);
		}
	}

	public static HanabiClient connect(String host, int port, String playerName) throws IOException
	{
		if (instance!=null)
			throw new IllegalStateException("Client is already connected");
		instance = new HanabiClient(host,port,playerName);
		return instance;
	}

	public static HanabiClient getInstance()
	{
		if (instance == null)
			throw new IllegalStateException("Client is not connected");
		return instance;
	}

	public void close() throws IOException
	{
		socket.close();
		instance = null;
	}

	public Game getGame()
	{
		return game;
	}

	public int getPlayerIndex()
	{
		for (int i=0; i<game.getPlayers().size(); i++)
			if (game.getPlayers().getString(i).equals(name))
				return i;
		return -1;
	}

	public Action discard(int i) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.set("type","discard");
		obj.set("player",""+getPlayerIndex());
		obj.set("card",""+i);
		try
		{
			return new Action(obj.toString(0));
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	public Action play(int i) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.set("type","play");
		obj.set("player",""+getPlayerIndex());
		obj.set("card",""+i);
		try
		{
			return new Action(obj.toString(0));
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	public Action hint(Color c, int hintReceiver) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.set("type","hint color");
		obj.set("player",new JSONString(""+getPlayerIndex()));
		obj.set("color",new JSONString(c.toString()));
		obj.set("hinted",new JSONString(""+hintReceiver));
		try
		{
			return new Action(obj.toString(0));
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	public Action hint(int v, int hintReceiver) throws IllegalActionException
	{
		JSONObject obj = new JSONObject();
		obj.set("type","hint value");
		obj.set("player",new JSONString(""+getPlayerIndex()));
		obj.set("value",new JSONString(""+v));
		obj.set("hinted",new JSONString(""+hintReceiver));
		try
		{
			return new Action(obj.toString(0));
		}
		catch(JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	public State getCurrentState()
	{
		return currentState;
	}

	public State waitForNewState() throws JSONException
	{
		currentState = new State(in);
		return currentState;
	}

	public State sendAction(Action a) throws JSONException
	{
		if (currentState.getCurrentPlayer() == getPlayerIndex())
		{
			out.print(a.toString(0));
			out.flush();
			return waitForNewState();
		}
		else
			throw new IllegalStateException("Not your turn!");
	}
}
