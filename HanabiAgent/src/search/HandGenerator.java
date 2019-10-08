package search;

import api.game.Card;
import api.game.Hand;
import api.game.State;
import math.MathCalc;

import java.util.ArrayList;
import java.util.List;

public class HandGenerator
{
	public static List<Hand> generateHands(State current, String owner)
	{
		ArrayList<Hand> l = new ArrayList<>();
		Hand box = current.getHand(owner).clone();
		List<Card>[] possible = new List[box.size()];
		for (int i=0; i<possible)
		return l;
	}
}
