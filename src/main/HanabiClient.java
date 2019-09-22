package main;

import agents.HumanAgent;
import game.Action;
import sjson.JSONObject;
import sjson.JSONUtils;

import java.io.PrintStream;
import java.net.Socket;

public class HanabiClient
{
	/**
	 * Avvia un giocatore
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		Socket s = new Socket(args[0],Integer.parseInt(args[1]));
		PrintStream ps = new PrintStream(s.getOutputStream());
		JSONObject settings = (JSONObject)JSONUtils.fromStream(s.getInputStream());
		HumanAgent player = new HumanAgent(args[2],Integer.parseInt(settings.optString("player_index")));
		System.out.println("Assigned index "+player.getIndex());
		ps.println("{\"name\":\""+player.getName()+"\"}");
		JSONObject state = (JSONObject) JSONUtils.fromStream(s.getInputStream());
		Action a;
		while(state.optString("gameover").equals("false"))
		{
			System.out.println(state.toString());
			if (state.optString("current").equals(args[2]))
			{
				a = player.doAction();
		//		System.out.println(a);
				ps.print(a.toJSON());

			}
			state = (JSONObject) JSONUtils.fromStream(s.getInputStream());
		}
	}
}
