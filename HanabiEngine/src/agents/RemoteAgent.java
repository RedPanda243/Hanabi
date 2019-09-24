package agents;

import game.IllegalActionException;
import game.ServerState;
import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class RemoteAgent extends Agent
{
	private JSONObject playerInfo;
	private String name;
	private Socket socket;
	PrintStream ps;
	private int index;

	@Override
	public int getIndex() {
		return index;
	}

	public RemoteAgent(Socket s, int index) throws IOException
	{
		socket = s;
		this.index = index;
		ps = new PrintStream(socket.getOutputStream());
		playerInfo = (JSONObject) JSONUtils.fromStream(socket.getInputStream());
		name = playerInfo.optString("name");
		if (name==null)
			name = socket.getInetAddress().getHostAddress();
	}

	public void close() throws IOException
	{
		socket.close();
	}

	@Override
	public Action doAction() throws IllegalActionException
	{
//		sendState(s);
		try
		{
			return Action.fromJSON((JSONObject)JSONUtils.fromStream(socket.getInputStream()));
		}
		catch(IOException | JSONException e)
		{
			throw new IllegalActionException(e);
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	public JSONObject getPlayerInfo()
	{
		return playerInfo;
	}

	public void sendState(ServerState s)
	{
		ps.print(s.toJSON());
		ps.flush();
	}
}
