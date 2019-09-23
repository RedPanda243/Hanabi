package agents;
import game.*;


public abstract class Agent
{

	public abstract Action doAction() throws IllegalActionException;

	public abstract int getIndex();

	public abstract String getName();

	public Action discard(int i) throws IllegalActionException
	{
		return new Action(getIndex(), ActionType.DISCARD,i);
	}

	public Action play(int i) throws IllegalActionException
	{
		return new Action(getIndex(),ActionType.PLAY,i);
	}

	/**
	 * Restituisce il valore della prossima carta giocabile di colore specificato
	 * @param s lo stato corrente
	 * @param c il colore desiderato
	 * @return il valore della carta giocabile, -1 se sono state giocate tutte le carte del colore specificato
	 */
	public static int playable(State s, Color c){
		java.util.Stack<Card> fw = s.getFirework(c);
		if (fw.size()==5) return -1;
		else return fw.size()+1;
	}


	public Action hint(Color c, int hintReceiver) throws IllegalActionException
	{
		return new Action(getIndex(),ActionType.HINT_COLOR,hintReceiver,c);
	}

	public Action hint(int v, int hintReceiver) throws IllegalActionException
	{
		return new Action(getIndex(),ActionType.HINT_VALUE,hintReceiver,v);
	}
}
