package game;

import api.game.Hand;
import sjson.JSONException;

import java.io.Reader;

public class ServerHand extends Hand
{
	public ServerHand(Reader r) throws JSONException
	{
		super(r);
		for (int i=0; i<this.size(); i++)
			this.replace(i,new ServerCard(this.get(i).toString()));
		//Test per vedere se ogni elemento dell'array è una carta.
		//Usa il singleton Game per controllare che il numero di carte sia esatto (considera anche il caso di ultimo turno)
	}

	public ServerHand clone()
	{
		return (ServerHand)super.clone();
	}

	public ServerCard getCard(int i)
	{
		return (ServerCard)super.getCard(i);
	}

	public void setCard(ServerCard card, int index)
	{
		replace(index,card);
	}
}
