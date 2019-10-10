package math;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;
import sjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HandCardsProbability {
    private List<PairCardCount>[] possibleCard ;
    private String owner; //owner of the hand, 1 class for each player
    private List<State> hystory;

    public HandCardsProbability(String owner, State state) {
        this.owner = owner;
        hystory = new ArrayList<>();
        hystory.add(state);
        //Game.getInstance().getNumberOfCardsPerPlayer()
        possibleCard = new List[Game.getInstance().getNumberOfCardsPerPlayer()];
        for (int i=0; i<possibleCard.length; i++) {
            possibleCard[i] = generateCards();
            removePlayersHands(i,state);
        }
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
    public void removePlayersHands(int i, State state){
        for(JSONData d: Game.getInstance().getPlayers()) {
            if (!d.toString().equalsIgnoreCase(owner))
                removeCardsFromPossibleCardArray(i, state.getHand(d.toString()));
        }
    }

    public void updatePossibleCards(State state){
      //  checkDifferentHands(state); // conviene far aggiornare rispetto ai discard che magari tengo in vriabile
        //se per caso dovessi ricevere uno stato che ho gia' usato per aggiornare le probabilita'
//        if(hystory.get(hystory.size()-1).equals(state))
//            return;

        //sono stato io a scartare o giocare?
        if(hystory.get(hystory.size()-1).getCurrentPlayer().equalsIgnoreCase(owner)){
            if(state.getAction().getActionType().equals(ActionType.DISCARD)) {
                removeCardFromArrays(state.getDiscards().get(state.getDiscards().size() - 1));
            }else if(state.getAction().getActionType().equals(ActionType.PLAY)) {
                for (Color co: Color.values())
                    if(state.getFirework(co) != hystory.get(hystory.size()-1).getFirework(co)) {
                        removeCardFromArrays(state.getFirework(co).get(state.getFirework(co).size() - 1));
                        break;
                    }
            }
            setArraysForNewCard(state.getAction().getCard());
                //remove DISCARDS
            removeCardsFromPossibleCardArray(Game.getInstance().getNumberOfCardsPerPlayer()-1, state.getDiscards());
                //remove FIREWORKS
            for (Color co: Color.values())
                removeCardsFromPossibleCardArray(Game.getInstance().getNumberOfCardsPerPlayer()-1,state.getFirework(co));
                //remove PLAYERS HANDS
            removePlayersHands(Game.getInstance().getNumberOfCardsPerPlayer()-1, state);

                //TO DO
                //remove hints, se so che cosa è una carta

        }else {
            //non sono stato io quello a fare l'Action
            if (state.getAction().getActionType().equals(ActionType.DISCARD) ||
                    state.getAction().getActionType().equals(ActionType.PLAY)) {
                //non serve rimuovere la carta discard, perchè era nella mano, ho rimosso già le carte in mano
                //serve RIMUOVERE LA NUOVA CARTA PESCATA
                //JSONData toRemoveDiscard = state.getDiscards().get(state.getDiscards().size()-1);

                //la carta nuova è sempre la carta di posizione 0 o 4??
                removeCardFromArrays(state.getHand(state.getCurrentPlayer()).get(Game.getInstance().getNumberOfCardsPerPlayer()-1));

            } else if(state.getAction().getHintReceiver().equalsIgnoreCase(owner)){
                if (state.getAction().getActionType().equals(ActionType.HINT_COLOR)) {
                    removeColorFromArrays(state, state.getAction().getColor());
                } else if (state.getAction().getActionType().equals(ActionType.HINT_VALUE)) {
                    removeValueFromArrays(state, state.getAction().getValue());
                }

            }
        }
        //TODO
        //remove carte che so qual'e' dagli altri array, ma attenzioen a non eliminarlo anche quando l'ho giocato poi
        //da pensare meglio
        hystory.add(state);
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

    public void removeColorFromArrays(State state, Color co){
        Hand hand = state.getHand(owner);
        for (int i = 0; i < hand.size(); i++) {
            if (hand.getCard(i).getColor().equals(co)){
                for (Color c : Color.values())
                    if(!c.equals(co))
                        removeColorFromArray(i,c);
            }else{
                removeColorFromArray(i,co);
            }
        }
    }
    public void removeColorFromArray(int index, Color co){
        List<PairCardCount> toRemove = new ArrayList<>();
        for(PairCardCount p: possibleCard[index]) {
            if (p.getCard().getColor().equals(co))
                toRemove.add(p);
        }
        possibleCard[index].removeAll(toRemove);
    }

    public void removeValueFromArrays(State state, int va){
        Hand hand = state.getHand(owner);
        for (int i = 0; i < hand.size(); i++) {
            if (hand.getCard(i).getValue() == va){
                for (int j=0; j<5; j++)
                    if(j!=va)
                        removeValueFromArray(i,va);
            }else{
                removeValueFromArray(i,va);
            }
        }
    }

    public void removeValueFromArray(int index, int va){
        List<PairCardCount> toRemove = new ArrayList<>();
        for(PairCardCount p: possibleCard[index]) {
            if (p.getCard().getValue() == va)
                toRemove.add(p);
        }
        possibleCard[index].removeAll(toRemove);
    }

    public void removeCardFromArrays(JSONData cardToRemove){
        for (int i=0; i<possibleCard.length; i++) {
            removeCardFromPossibleCardArray(i,cardToRemove);
        }
    }
    public void removeCardFromPossibleCardArray(int index, JSONData cardToRemove){
        for(PairCardCount p: possibleCard[index])
            if(p.getCard().equals(cardToRemove))
                if(p.decrease()==0) {
                    //posso fare remove anche se itero perchè faccio brake
                    possibleCard[index].remove(p);
                    break;
                }
    }
    public void removeCardsFromArrays(JSONArray cardsToRemove){
        for (int i=0; i<possibleCard.length; i++) {
            removeCardsFromPossibleCardArray(i, cardsToRemove);
        }
    }

    public void removeCardsFromPossibleCardArray(int index, JSONArray cardsToRemove){
        List<PairCardCount> toRemove = new ArrayList<>();
        for (JSONData d : cardsToRemove)
            for (PairCardCount p : possibleCard[index])
                if(p.getCard().equals(d)) {
                    if(p.decrease()==0)
                        toRemove.add(p);

                }
        possibleCard[index].removeAll(toRemove);
    }

    public void setArraysForNewCard(int card){
        possibleCard[card].clear();
        for(int i = card; i<Game.getInstance().getNumberOfCardsPerPlayer()-1; i++) {
            for (PairCardCount p : possibleCard[i + 1])
                possibleCard[i].add(p);
        }
        possibleCard[Game.getInstance().getNumberOfCardsPerPlayer()-1] = generateCards();
    }

    /*
    public static void main(String[] args) {
        HandCardsProbability h = new HandCardsProbability();
        for (PairCardCount c : h.possibleCard[0])
            System.out.println(c.toString());
    }
    */

    /**
     * Restituisce le playability delle carte di player allo stato attuale
     * @param player
     * @return
     */
    public double[] getPlayability(String player)
    {
        double[] result = new double[Game.getInstance().getNumberOfCardsPerPlayer()];
        ArrayList<Card> playableCards = new ArrayList<>();
        for(Color co: Color.values()) {
            try {
                int value = hystory.get(hystory.size()-1).getFirework(co).peak();
                if (value != 5)
                    playableCards.add(new Card(co,value+1));
            } catch (JSONException e) {
                System.err.println("Non able to add cards in array Playable, while checking fireworks");
            }
        }
        int casiFavorevoli = 0, casiTotali = 0;
        if(player.equalsIgnoreCase(owner)){ //sono io
            for(int i=0; i<possibleCard.length; i++){
                for(PairCardCount p: possibleCard[i]){
                    casiTotali += p.getCount();
                    if(playableCards.contains(p.getCard()))
                        casiFavorevoli += p.getCount();
                }
                result[i] = casiFavorevoli/casiTotali;
            }
        }else{//non sono io

        }
        return result;
    }

    /**
     * Restituisce le uselessness delle carte di player allo stato attuale
     * @param player
     * @return
     */
    public double[] getUselessness(String player)
    {
        double[] result = new double[Game.getInstance().getNumberOfCardsPerPlayer()];
        ArrayList<Card> discardableCards = new ArrayList<>();
        for(Color co: Color.values()) {
            int value = hystory.get(hystory.size()-1).getFirework(co).peak();
            for(int i=0; i<value; i++){
                try {
                    discardableCards.add(new Card(co, i));
                } catch (JSONException e) {
                    System.err.println("Non able to add cards in array Discardable, while checking fireworks");
                }
            }
        }
        int casiFavorevoli = 0, casiTotali = 0;
        if(player.equalsIgnoreCase(owner)){//sono io
            for(int i=0; i<possibleCard.length; i++){
                for(PairCardCount p : possibleCard[i]){
                    casiTotali += p.getCount();
                    if(discardableCards.contains(p.getCard()))
                        casiFavorevoli += p.getCount();
                    else if(p.getCount()>1)
                        casiFavorevoli += p.getCount();
                }
                result[i] = casiFavorevoli/casiTotali;
            }
        }else{//non sono io

        }
        return new double[0];
    }

    /**
     * Restituisce le playability delle carte di player allo stato che si otterrebbe applicando next allo stato attuale
     * @param player
     * @param next
     * @return
     */
    public double[] getPlayability(String player, Action next)
    {
        return new double[0];
    }

    /**
     * Restituisce le uselessness delle carte di player allo stato che si otterrebbe applicando next allo stato attuale
     * @param player
     * @param next
     * @return
     */
    public double[] getUselessness(String player, Action next)
    {
        return new double[0];
    }

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
