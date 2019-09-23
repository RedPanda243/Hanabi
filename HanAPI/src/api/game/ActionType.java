package api.game;
public enum ActionType
{
	PLAY,DISCARD,HINT_COLOR,HINT_VALUE;

	public static ActionType fromString(String s)
	{
		switch (s.toLowerCase())
		{
			case "play": return PLAY;
			case "discard": return DISCARD;
			case "hint value": return HINT_VALUE;
			case "hint color": return HINT_COLOR;
			default: return null;
		}
	}

	public String toString()
	{
		switch (this)
		{
			case PLAY: return "play";
			case DISCARD: return "discard";
			case HINT_COLOR: return "hint color";
			case HINT_VALUE: return "hint value";
			default: return null;
		}
	}

}
