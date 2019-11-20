public class InputNode
{

	public enum InputType
	{
		PLAYABILITY,USELESSNESS;
		public String toString()
		{
			if (this == PLAYABILITY)
				return "P";
			else
				return "U";
		}
	}
/*
	private String player;
	private int card;
	private InputType type;*/
	private double value;

/*	public InputNode(String player, int card, InputType type)
	{
		this.player = ""+player;
		this.card = card;
		this.type = type;
	}
	*/

	public void setValue(double v)
	{
		value = v;
	}
}
