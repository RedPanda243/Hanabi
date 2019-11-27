package api.client;

import api.game.Card;
import api.game.Color;

import java.util.List;

public class Hint implements Cloneable
{
	private boolean is;
	private int value;
	private Color color;

	public Hint(boolean is, int value)
	{
		this(is,null,value);
	}

	public Hint(boolean is, Color color)
	{
		this(is,color,-1);
	}

	private Hint(boolean is, Color color, int value)
	{
		this.is = is;
		this.color = color;
		this.value = value;
	}

	public void apply(List<Card> list)
	{
		if (color == null)
		{
			if (is)
				Statistics.maintainValue(value,list);
			else
				Statistics.removeValue(value,list);
		}
		else
		{
			if (is)
				Statistics.maintainColor(color,list);
			else
				Statistics.removeColor(color,list);
		}
	}

	public Hint clone()
	{
		try
		{
			super.clone();
		}
		catch (CloneNotSupportedException e){}
		return new Hint(is,color,value);
	}

	public Hint not()
	{
		return new Hint(!is, color, value);
	}
}
