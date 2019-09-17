package hanabAI;

/**The colours of Hanabi Cards**/
public enum Colour{
	BLUE,RED,GREEN,WHITE,YELLOW;

	public static Colour fromString(String s)
	{
		if (s.equalsIgnoreCase("blue"))
			return BLUE;
		else if (s.equalsIgnoreCase("red"))
			return RED;
		else if (s.equalsIgnoreCase("green"))
			return GREEN;
		else if (s.equalsIgnoreCase("white"))
			return WHITE;
		else if (s.equalsIgnoreCase("yellow"))
			return YELLOW;
		return null;
	}

	/**@return String representation of the colour**/
	public String toString(){
		switch(this){
			case BLUE: return "Blue";
			case RED: return"Red";
			case GREEN: return "Green";
			case WHITE: return "White";
			case YELLOW: return "Yellow";
		}
		return "";
	}
} 


