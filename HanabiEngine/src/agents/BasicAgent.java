package agents;

public class BasicAgent //extends Agent
{
/*
	@Override
	public Action chooseAction(State s) throws IllegalActionException
	{
//		getHints(s);
		Action a = playKnown(s);
		if(a==null) a = discardKnown(s);
		if(a==null) a = hintPlayable(s);
		if(a==null) a = playGuess(s);
		if(a==null) a = discardGuess(s);
		if(a==null) a = hintRandom(s);
		return a;
	}

	//discard a random card
	public Action discardGuess(State s) throws IllegalActionException{
		if (s.getHintTokens() != 8) {
			java.util.Random rand = new java.util.Random();
			int cardIndex = rand.nextInt(colors.length);
			colors[cardIndex] = null;
			values[cardIndex] = 0;
			return new Action(index, toString(), ActionType.DISCARD, cardIndex);
		}
		return null;
	}

	//discards the first card known to be unplayable.
	public Action discardKnown(State s) throws IllegalActionException{
		if (s.getHintTokens() != 8) {
			for(int i = 0; i< colors.length; i++){
				if(colors[i]!=null && values[i]>0 && values[i]<playable(s, colors[i])){
					colors[i] = null;
					values[i] = 0;
					return new Action(index, toString(), ActionType.DISCARD,i);
				}
			}
		}
		return null;
	}

	public Action hintPlayable(State s) throws IllegalActionException
	{
		return hintPlayable(s,(Math.random()>0.5));
	}

	//gives hintPlayable of first playable card in next players hand
	//flips a coin to determine whether it is a colour hintPlayable or value hintPlayable
	//return null if no hintPlayable token left, or no playable cards
	private Action hintPlayable(State s, boolean flag) throws IllegalActionException {
		if(s.getHintTokens()>0){
			for(int i = 1; i<numPlayers; i++){
				int hintee = (index+i)%numPlayers;
				Card[] hand = s.getHand(hintee);
				for(int j = 0; j<hand.length; j++){
					Card c = hand[j];
					if(c!=null && c.getValue()==playable(s,c.getColor())){
						//flip coin
						if(flag){//give colour hintPlayable
							boolean[] col = new boolean[hand.length];
							for(int k = 0; k< col.length; k++){
								col[k]=c.getColor().equals((hand[k]==null?null:hand[k].getColor()));
							}
							return new Action(index,toString(),ActionType.HINT_COLOR,hintee,col,c.getColor());
						}
						else{//give value hintPlayable
							boolean[] val = new boolean[hand.length];
							for(int k = 0; k< val.length; k++){
								val[k]=c.getValue() == (hand[k]==null?-1:hand[k].getValue());
							}
							return new Action(index,toString(),ActionType.HINT_VALUE,hintee,val,c.getValue());
						}
					}
				}
			}
		}
		return null;
	}

	public Action hintPlayableColour(State s) throws IllegalActionException
	{
		return hintPlayable(s,true);
	}

	public Action hintPlayableState(State s) throws IllegalActionException
	{
		return hintPlayable(s,false);
	}

	//gives random hintPlayable of a card in next players hand
	//flips a coin to determine whether it is a colour hintPlayable or value hintPlayable
	//return null if no hintPlayable token left
	public Action hintRandom(State s) throws IllegalActionException{
		if(s.getHintTokens()>0){
			int hintee = (index+1)%numPlayers;
			Card[] hand = s.getHand(hintee);

			java.util.Random rand = new java.util.Random();
			int cardIndex = rand.nextInt(hand.length);
			while(hand[cardIndex]==null) cardIndex = rand.nextInt(hand.length);
			Card c = hand[cardIndex];

			if(Math.random()>0.5){//give colour hintPlayable
				boolean[] col = new boolean[hand.length];
				for(int k = 0; k< col.length; k++){
					col[k]=c.getColor().equals((hand[k]==null?null:hand[k].getColor()));
				}
				return new Action(index,toString(),ActionType.HINT_COLOR,hintee,col,c.getColor());
			}
			else{//give value hintPlayable
				boolean[] val = new boolean[hand.length];
				for(int k = 0; k< val.length; k++){
					if (hand[k] == null) continue;
					val[k]=c.getValue() == (hand[k]==null?-1:hand[k].getValue());
				}
				return new Action(index,toString(),ActionType.HINT_VALUE,hintee,val,c.getValue());
			}

		}

		return null;
	}

	//with probability 0.05 for each fuse token, play a random card
	public Action playGuess(State s) throws IllegalActionException{
		java.util.Random rand = new java.util.Random();
		for(int i = 0; i<s.getFuseTokens(); i++){
			if(rand.nextDouble()<0.05){
				int cardIndex = rand.nextInt(colors.length);
				colors[cardIndex] = null;
				values[cardIndex] = 0;
				return new Action(index, toString(), ActionType.PLAY, cardIndex);
			}
		}
		return null;
	}

	//plays the first card known to be playable.
	public Action playKnown(State s) throws IllegalActionException{
		for(int i = 0; i< colors.length; i++){
			if(colors[i]!=null && values[i]==playable(s, colors[i])){
				colors[i] = null;
				values[i] = 0;
				return new Action(index, toString(), ActionType.PLAY,i);
			}
		}
		return null;
	}

 */
}
