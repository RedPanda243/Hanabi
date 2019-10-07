package utils;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HandCardsProbability {
    private List<PairCardCount>[] possibleCard ;
    private String owner; //owner of the hand, 1 class for each player
    private Hand hand;

    public HandCardsProbability(String owner, State state) {
        this.owner = owner;
        hand = state.getHand(owner);
        //Game.getInstance().getNumberOfCardsPerPlayer()
        possibleCard = new List[Game.getInstance().getNumberOfCardsPerPlayer()];
        for (int i=0; i<possibleCard.length; i++)
            possibleCard[i] = generateCards();

    }

    private ArrayList<PairCardCount> generateCards()
    {
        try
        {
            PairCardCount[] cards = {
                    new PairCardCount(new Card(Color.BLUE,1), 3),
                    new PairCardCount(new Card(Color.BLUE,2), 2),
                    new PairCardCount(new Card(Color.BLUE,3), 2),
                    new PairCardCount(new Card(Color.BLUE,4), 2),
                    new PairCardCount(new Card(Color.BLUE,5), 1),

                    new PairCardCount(new Card(Color.RED,1), 3),
                    new PairCardCount(new Card(Color.RED,2), 2),
                    new PairCardCount(new Card(Color.RED,3), 2),
                    new PairCardCount(new Card(Color.RED,4), 2),
                    new PairCardCount(new Card(Color.RED,5), 1),

                    new PairCardCount(new Card(Color.GREEN,1), 3),
                    new PairCardCount(new Card(Color.GREEN,2), 2),
                    new PairCardCount(new Card(Color.GREEN,3), 2),
                    new PairCardCount(new Card(Color.GREEN,4), 2),
                    new PairCardCount(new Card(Color.GREEN,5), 1),

                    new PairCardCount(new Card(Color.WHITE,1), 3),
                    new PairCardCount(new Card(Color.WHITE,2), 2),
                    new PairCardCount(new Card(Color.WHITE,3), 2),
                    new PairCardCount(new Card(Color.WHITE,4), 2),
                    new PairCardCount(new Card(Color.WHITE,5), 1),

                    new PairCardCount(new Card(Color.YELLOW,1), 3),
                    new PairCardCount(new Card(Color.YELLOW,2), 2),
                    new PairCardCount(new Card(Color.YELLOW,3), 2),
                    new PairCardCount(new Card(Color.YELLOW,4), 2),
                    new PairCardCount(new Card(Color.YELLOW,5), 1)
            };
            return new ArrayList<>(Arrays.asList(cards));
        }
        catch(JSONException e){return null;}
    }

    //iterazione inutile per carte NON nuove, da migliorare
    //potrei fare store di qualche hand, scarti attuali
    public void updatePossibleCards(State state){
      //  checkDifferentHands(state); // conviene far aggiornare rispetto ai discard che magari tengo in vriabile
        for (int i=0; i<possibleCard.length; i++) {

            //remove discards
            removeCards(i, state.getDiscards());

            //remove Fireworks played cards
            for (Color co: Color.values())
                removeCards(i, state.getFirework(co));

            //remove other players hands
            for(JSONData d: Game.getInstance().getPlayers()) {
                if (!d.toString().equalsIgnoreCase(owner))
                    removeCards(i, state.getHand(d.toString()));
            }

            //use hints
        }

    }
    public String getPossibleHand(State state){
        updatePossibleCards(state);
        String result = "PROBA\n";
        for (int i=0; i<possibleCard.length; i++) {
            result +="\nPos " + i + ": ";
            for(PairCardCount p : possibleCard[i])
                result += p.getCard().toString()+" ";
            result += "\n";
        }
        return result;
    }
    public void removeCards(int index, JSONArray otherCards){
        List<PairCardCount> toRemove = new ArrayList<>();
        for (JSONData d : otherCards)
            for (PairCardCount p : possibleCard[index])
                if(p.getCard().equals(d)) {
                    if(p.decrease()==0)
                        toRemove.add(p);

                }
        possibleCard[index].removeAll(toRemove);
    }

    public void checkDifferentHands(State state){
        for (int i=0; i<Game.getInstance().getNumberOfCardsPerPlayer(); i++)
            if(!state.getHand(owner).getCard(i).equals(hand.getCard(i)))
                possibleCard[i] = generateCards();
            //aggiorna anche l'array, invece di fare cicli inutili in update
    }

    /*
    public static void main(String[] args) {
        HandCardsProbability h = new HandCardsProbability();
        for (PairCardCount c : h.possibleCard[0])
            System.out.println(c.toString());
    }
    */

    public class PairCardCount {
        private Card c;
        private int countLeft;
        public PairCardCount(Card c, int n_left){
            this.c = c;
            this.countLeft = n_left;
        }
        public Card getCard(){ return c; }
        public int getCount(){ return countLeft; }
        public void setCard(Card l){ this.c = l; }
        public void setCount(int r){ this.countLeft = r; }
        public int decrease(){ countLeft--; return countLeft; }
        @Override
        public String toString() {
            return c.toString()+" n="+countLeft;
        }
    }


}
