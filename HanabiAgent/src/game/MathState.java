package game;

import api.game.*;
import sjson.JSONData;
import sjson.JSONException;
import math.MathCalc;

import java.math.RoundingMode;
import java.text.DecimalFormat;

@Deprecated
public class MathState extends State
{
	public MathState(State state) throws JSONException
	{
		super(state.toString(0));
	}

	public String toString()
	{
		String ret = "State: "+getOrder()+"\n";
		ret+="Players' hands:\n";
		Hand hand;
		String player;
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		for(int i = 0; i< Game.getInstance().getPlayers().size(); i++)
		{
			player = Game.getInstance().getPlayer(i);
			hand = getHand(player);
			ret+="\t"+player+" ("+i+"): "+hand+"\n";
			ret+="\t\t"+"Playability: {";
			for (int j=0; j<hand.size(); j++)
				ret+= df.format(MathCalc.getCardPlayability(this,j,player))+"; ";
			ret = ret.substring(0,ret.length()-2)+"}\n";
			ret+="\t\t"+"Uselessness: {";
			for (int j=0; j<hand.size(); j++)
				ret+= df.format(MathCalc.getCardUselessness(this,j,player))+"; ";
			ret = ret.substring(0,ret.length()-2)+"}\n\n";
		}
		ret+="Discarded: {";
		if (getDiscards().size()>0) {
			for (JSONData d : getDiscards())
				ret += d + ", ";
			ret = ret.substring(0, ret.length() - 2);
		}
		ret+="}\n";

//		ret+="Hints: {";
//		if (getHints().size()>0) {
//			for (JSONData d : getHints())
//				ret += d + ", ";
//			ret = ret.substring(0, ret.length() - 2);
//		}
//		ret+="}\n";

		ret+="Fireworks:\n";
		Firework fireworks;
		for(Color c: Color.values()) {
			fireworks = getFirework(c);
			ret += "\t" + c + "  " + (fireworks.size() == 0 ? "-" : (fireworks.peak())) + "\n";
		}
		ret+= "Hints: "+getHintTokens()+"\nFuse: "+getFuseTokens()+"\n";

		return ret;
	}
}
