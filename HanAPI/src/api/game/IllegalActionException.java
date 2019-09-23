package api.game;

/**
 * A class for representing Illegal Actions in the game Hanabi
 **/
public class IllegalActionException extends Exception{

	public IllegalActionException(String msg){
		super(msg);
	}

	public IllegalActionException(Exception e)
	{
		super(e);
	}
}

