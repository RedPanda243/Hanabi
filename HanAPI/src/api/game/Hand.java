package api.game;

import sjson.JSONArray;
import sjson.JSONException;

import java.io.Reader;
import java.io.StringReader;


public class Hand extends JSONArray
{

	public Hand(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public Hand(Reader r) throws JSONException
	{
		super(r);
		for (int i=0; i<this.size(); i++)
			this.replace(i,new Card(this.get(i).toString()));
		//Test per vedere se ogni elemento dell'array è una carta.
		//Usa il singleton Game per controllare che il numero di carte sia esatto (considera anche il caso di ultimo turno)
	}

	public Hand clone()
	{
		return (Hand)super.clone();
	}

	public Card getCard(int i)
	{
		return (Card)get(i);
	}
}
