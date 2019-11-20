package api.client;

import api.game.*;
import sjson.JSONData;
import sjson.JSONException;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class StatisticState extends State
{
	private Statistics stats;

	public StatisticState(State state, Statistics stats) throws JSONException
	{
		super(state.toString(0));
		this.stats = stats;
	}

	public String toString()
	{
		String ret = "State: "+getOrder()+"\n";
		ret+="Players' hands:\n";
		Hand hand;
		String player;
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		for(int i = 0; i< Game.getInstance().getPlayers().length; i++)
		{
			player = Game.getInstance().getPlayer(i);
			hand = getHand(player);
			ret+="\t"+player+" ("+i+"): "+hand+"\n";
			ret+="\t\t"+"Playability: {";
			double p[] = stats.getPlayability(player);
			for (int j=0; j<hand.size(); j++)
				ret+= df.format(p[j])+"; ";
//				ret+=p[j]+"; ";
			ret = ret.substring(0,ret.length()-2)+"}\n";
			ret+="\t\t"+"Uselessness: {";
			double u[] = stats.getUselessness(player);
			for (int j=0; j<hand.size(); j++)
				ret+= df.format(u[j])+"; ";
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
			ret += "\t" + c + "  " + (fireworks.peak() == 0 ? "-" : (fireworks.peak())) + "\n";
		}
		ret+= "Hints: "+getHintTokens()+"\nFuse: "+getFuseTokens()+"\n";

		ret+= "Final "+ getFinalActionIndex()+"\n";

		return ret;
	}
}
