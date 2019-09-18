package agents;

import hanabAI.Action;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HumanAgent extends AbstractAgent
{
	BufferedReader br;

	public HumanAgent()
	{
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	@Override
	public Action chooseAction(State s) throws IllegalActionException
	{
		System.out.println("\nChoose one action: (play <cardnum> | discard <cardnum> | hint <playernum> (<color>|<value>))");
/*		List<Action> available = new ArrayList<>();
		Action a;
		a = this.playKnown(s);
		if (a!=null)
		{
			available.add(a);
			System.out.println(available.size()-1+")"+a);
		}
		a = discardKnown(s);
		if (a!=null)
		{
			available.add(a);
			System.out.println(available.size()-1+")"+a);
		}
		a = hintPlayableColour(s);
		if (a!=null)
		{
			available.add(a);
			System.out.println(available.size()-1+")"+a);
		}
		a = hintPlayableState(s);
		if (a!=null)
		{
			available.add(a);
			System.out.println(available.size()-1+")"+a);
		}
		a = playGuess(s);
		if (a!=null)
		{
			available.add(a);
			System.out.println(available.size()-1+")"+a);
		}
		a = discardGuess(s);
		if (a!=null)
		{
			available.add(a);
			System.out.println(available.size()-1+")"+a);
		}
		a = hintRandom(s);
		if (a!=null)
		{
			available.add(a);
			System.out.println(available.size()-1+")"+a);
		}

 */
		while(true)
		{
			try
			{
				String[] parts = br.readLine().split(" ");
				if (parts[0].equals("play"))
				{
					return play(Integer.parseInt(parts[1]));
				}
				else if (parts[0].equals("discard"))
				{
					return discard(Integer.parseInt(parts[1]));
				}
				else if (parts[0].equals("hint"))
				{
					try
					{
						return hint(s,Integer.parseInt(parts[2]),Integer.parseInt(parts[1]));
					}
					catch(NumberFormatException nfe)
					{
						return hint(s, Colour.fromString(parts[2]),Integer.parseInt(parts[1]));
					}
				}
				else {
					System.out.println("unrecognized, retry");
					continue;
				}
			}
			catch (IOException ioe){ioe.printStackTrace(System.err); System.exit(1);}
			catch(Exception e){}
		}
	}
}
