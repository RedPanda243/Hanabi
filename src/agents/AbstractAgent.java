package agents;
import hanabAI.*;


public abstract class AbstractAgent implements Agent{

	protected Colour[] colours;
	protected int[] values;
	protected boolean firstAction = true;
	protected int numPlayers;
	protected int index;

	/**
	 * Default constructor, does nothing.
	 * **/
	public AbstractAgent(){}

	/**
	 * Initialises variables on the first call to do action.
	 * @param s the State of the game at the first action
	 **/
	public void init(State s){
		numPlayers = s.getPlayers().length;
		if(numPlayers>3){
			colours = new Colour[4];
			values = new int[4];
		}
		else{
			colours = new Colour[5];
			values = new int[5];
		}
		index = s.getNextPlayer();
		firstAction = false;
	}

	/**
	 * Returns this agent's name.
	 * @return the agent's name as String
	 * */
	public String toString(){return this.getClass().getName();}

	public abstract Action chooseAction(State s) throws IllegalActionException;

	public Action doAction(State s){
		/*
		if(firstAction){
			init(s);
		}
		*/
		//Assume players index is sgetNextPlayer()
		index = s.getNextPlayer();
//		getHints(s);
		try
		{
			return chooseAction(s);
		}catch(IllegalActionException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Something has gone very wrong");
		}
	}

	//updates colours and values from hints received
/*	public void getHints(State s){
		try{
			State t = (State) s.clone();
			for(int i = 0; i<Math.min(numPlayers-1,s.getOrder());i++){
				Action a = t.getPreviousAction();
				if((a.getType()==ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE) && a.getHintReceiver()==index){
					boolean[] hints = t.getPreviousAction().getHintedCards();
					for(int j = 0; j<hints.length; j++){
						if(hints[j]){
							if(a.getType()==ActionType.HINT_COLOUR)
								colours[j] = a.getColour();
							else
								values[j] = a.getValue();
						}
					}
				}
				t = t.getPreviousState();
			}
		}
		catch(IllegalActionException e){e.printStackTrace();}
	}
*/
	//returns the value of the next playable card of the given colour
	public int playable(State s, Colour c){
		java.util.Stack<Card> fw = s.getFirework(c);
		if (fw.size()==5) return -1;
		else return fw.size()+1;
	}

	public Action play(int i) throws IllegalActionException
	{
		colours[i] = null;
		values[i] = 0;
		return new Action(index, toString(), ActionType.PLAY,i);
	}

	public Action discard(int i) throws IllegalActionException
	{
		colours[i] = null;
		values[i] = 0;
		return new Action(index, toString(), ActionType.DISCARD,i);
	}

	public Action hint(State s, Colour c, int hintReceiver) throws IllegalActionException
	{
		Card[] hand = s.getHand(hintReceiver);
		boolean[] col = new boolean[hand.length];
		for(int k = 0; k< col.length; k++){
			col[k]=c.equals((hand[k]==null?null:hand[k].getColour()));
		}
		return new Action(index,toString(),ActionType.HINT_COLOUR,hintReceiver,col,c);
	}

	public Action hint(State s, int v, int hintReceiver) throws IllegalActionException
	{
		Card[] hand = s.getHand(hintReceiver);
		boolean[] val = new boolean[hand.length];
		for(int k = 0; k< val.length; k++){
			val[k]=v == (hand[k]==null?-1:hand[k].getValue());
		}
		return new Action(index,toString(),ActionType.HINT_VALUE,hintReceiver,val,v);
	}
}
