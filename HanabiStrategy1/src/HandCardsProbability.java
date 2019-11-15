import api.game.*;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HandCardsProbability {
    private List<PairCardCount>[] possibleCard;
    private String owner; //owner of the hand, 1 class for each player
    private List<State> hystory;

    public HandCardsProbability(String owner, State state) {
        this.owner = owner;
        hystory = new ArrayList<>();
        hystory.add(state);
        //Game.getInstance().getNumberOfCardsPerPlayer()
        possibleCard = new List[Game.getInstance().getNumberOfCardsPerPlayer()];
        for (int i = 0; i < possibleCard.length; i++) {
            possibleCard[i] = generateCards();
            removePlayersHands(possibleCard[i], state);
        }
    }

    private ArrayList<PairCardCount> generateCards() {
        try {
            PairCardCount[] cards = {
                    new PairCardCount(new Card(Color.BLUE, 1), 3),
                    new PairCardCount(new Card(Color.BLUE, 2), 2),
                    new PairCardCount(new Card(Color.BLUE, 3), 2),
                    new PairCardCount(new Card(Color.BLUE, 4), 2),
                    new PairCardCount(new Card(Color.BLUE, 5), 1),

                    new PairCardCount(new Card(Color.RED, 1), 3),
                    new PairCardCount(new Card(Color.RED, 2), 2),
                    new PairCardCount(new Card(Color.RED, 3), 2),
                    new PairCardCount(new Card(Color.RED, 4), 2),
                    new PairCardCount(new Card(Color.RED, 5), 1),

                    new PairCardCount(new Card(Color.GREEN, 1), 3),
                    new PairCardCount(new Card(Color.GREEN, 2), 2),
                    new PairCardCount(new Card(Color.GREEN, 3), 2),
                    new PairCardCount(new Card(Color.GREEN, 4), 2),
                    new PairCardCount(new Card(Color.GREEN, 5), 1),

                    new PairCardCount(new Card(Color.WHITE, 1), 3),
                    new PairCardCount(new Card(Color.WHITE, 2), 2),
                    new PairCardCount(new Card(Color.WHITE, 3), 2),
                    new PairCardCount(new Card(Color.WHITE, 4), 2),
                    new PairCardCount(new Card(Color.WHITE, 5), 1),

                    new PairCardCount(new Card(Color.YELLOW, 1), 3),
                    new PairCardCount(new Card(Color.YELLOW, 2), 2),
                    new PairCardCount(new Card(Color.YELLOW, 3), 2),
                    new PairCardCount(new Card(Color.YELLOW, 4), 2),
                    new PairCardCount(new Card(Color.YELLOW, 5), 1)
            };
            return new ArrayList<>(Arrays.asList(cards));
        } catch (JSONException e) {
            return null;
        }
    }

    public List<PairCardCount> removePlayersHands(List<PairCardCount> array, State state) {
        for (JSONData d : Game.getInstance().getPlayers()) {
            if (!d.toString().equalsIgnoreCase(owner))
                removeCardsFromPossibleCardArray(array, state.getHand(d.toString()).toJSON());
        }
        return array;
    }

    public void updatePossibleCards(State state) throws JSONException {
        //  checkDifferentHands(state); // conviene far aggiornare rispetto ai discard che magari tengo in vriabile
        //se per caso dovessi ricevere uno stato che ho gia' usato per aggiornare le probabilita'
//        if(hystory.get(hystory.size()-1).equals(state))
//            return;

        State lastState = hystory.get(hystory.size()-1);
        //sono stato io a scartare o giocare?
        System.out.println("[UPDATE] "+owner+", hystory size="+hystory.size());
        System.out.println("currentPlayer="+state.getCurrentPlayer()+" lastPlayer="+lastState.getCurrentPlayer());
        System.out.println("[STATE ACTION] "+state.getAction().toString());

        if (state.getAction().getPlayer().equalsIgnoreCase(owner) && (state.getAction().getType().equals(ActionType.DISCARD)
                ||  state.getAction().getType().equals(ActionType.PLAY))) {
            if (state.getAction().getType().equals(ActionType.DISCARD)) {
                removeCardFromArrays(state.getDiscards().get(state.getDiscards().size() - 1));
            } else if (state.getAction().getType().equals(ActionType.PLAY)) {
                for (Color co : Color.values())
                    if (state.getFirework(co) != hystory.get(hystory.size() - 1).getFirework(co)) {
                        int value = hystory.get(hystory.size() - 1).getFirework(co).peak();
                        if(value!=0) removeCardFromArrays(new Card(co, value));
                        break;
                    }
            }
            possibleCard = setArraysForNewCard(possibleCard, state.getAction().getCard());
            //remove DISCARDS
            possibleCard[Game.getInstance().getNumberOfCardsPerPlayer() - 1] = removeCardsFromPossibleCardArray(possibleCard[Game.getInstance().getNumberOfCardsPerPlayer() - 1], state.getDiscards());
            //remove FIREWORKS
            for (Color co : Color.values())
                possibleCard[Game.getInstance().getNumberOfCardsPerPlayer() - 1] = removeCardsFromPossibleCardArray(possibleCard[Game.getInstance().getNumberOfCardsPerPlayer() - 1], state.getFirework(co).toJSON());
            //remove PLAYERS HANDS
            possibleCard[Game.getInstance().getNumberOfCardsPerPlayer() - 1] = removePlayersHands(possibleCard[Game.getInstance().getNumberOfCardsPerPlayer() - 1], state);

            //TO DO
            //remove hints, se so che cosa è una carta

        } else {
            //non sono stato io quello a fare l'Action
            if (state.getAction().getType().equals(ActionType.DISCARD) ||
                    state.getAction().getType().equals(ActionType.PLAY)) {
                //non serve rimuovere la carta discard, perchè era nella mano, ho rimosso già le carte in mano
                //serve RIMUOVERE LA NUOVA CARTA PESCATA
                //JSONData toRemoveDiscard = state.getDiscards().get(state.getDiscards().size()-1);

                //la carta nuova è sempre la carta di posizione 0 o 4??
                removeCardFromArrays(state.getHand(state.getCurrentPlayer()).getCard(state.getHand(state.getCurrentPlayer()).size() - 1));

            } else if (state.getAction().getHinted().equalsIgnoreCase(owner)) {
                if (state.getAction().getType().equals(ActionType.HINT_COLOR)) {
                    removeColorFromArrays(state.getAction().getCardsToReveal(), state.getAction().getColor(), possibleCard);
                } else if (state.getAction().getType().equals(ActionType.HINT_VALUE)) {
                    removeValueFromArrays(state.getAction().getCardsToReveal(), state.getAction().getValue(), possibleCard);
                }

            }
        }
        //TODO
        //remove carte che so qual'e' dagli altri array, ma attenzioen a non eliminarlo anche quando l'ho giocato poi
        //da pensare meglio
        hystory.add(state);
    }


    public String getPossibleHand() throws JSONException {
        String result = "PROBA\n";
        for (int i = 0; i < possibleCard.length; i++) {
            result += "\nPos " + i + ": ";
            for (PairCardCount p : possibleCard[i])
                result += p.getCard().toString() + " ";
            result += "\n";
        }
        return result;
    }

    public List<PairCardCount>[] removeColorFromArrays(List<Integer> posCards, Color co, List<PairCardCount>[] arrays) throws JSONException {
        for (int i = 0; i < arrays.length; i++) {
            if (posCards.contains(i)) { // array da elimnare tutti tranne co
                for (Color c : Color.values()) {
                    if (!c.equals(co))
                        arrays[i] = removeColorFromArray(arrays[i], c);
                }
            } else { //eliminare solo co
                arrays[i] = removeColorFromArray(arrays[i], co);
            }
        }
        return arrays;
    }

    public List<PairCardCount> removeColorFromArray(List<PairCardCount> array, Color co) {
        List<PairCardCount> toRemove = new ArrayList<>();
        for (PairCardCount p : array) {
            if (p.getCard().getColor().equals(co))
                toRemove.add(p);
        }
        array.removeAll(toRemove);
        return array;
    }

    public List<PairCardCount>[] removeValueFromArrays(List<Integer> posCards, int va, List<PairCardCount>[] arrays) throws JSONException {
//        //PROVA ****************************
//        System.out.println(Arrays.toString(toReveal.toArray()));
//        //PROVA ****************************
        for (int i = 0; i < arrays.length; i++) {
            if (posCards.contains(i)) { // e' una delle posizioni indicate
                for (int j = 1; j < 6; j++) {
                    if (j != va)
                        arrays[i] = removeValueFromArray(arrays[i], j);
                }
            } else {// non e' una delle posizioni indicate
                arrays[i] = removeValueFromArray(arrays[i], va);
            }
        }
//        //PROVA****************************************
//        for(List<PairCardCount> l : arrays){
//            for(PairCardCount p : l)
//                System.out.println("REMOVINGVALUE"+p.toString());
//        }
//        System.out.println("**********************************************");
//        //PROVA****************************************
        return arrays;
    }

    public List<PairCardCount> removeValueFromArray(List<PairCardCount> array, int va) {
        List<PairCardCount> toRemove = new ArrayList<>();
//        System.out.println("REMOVING value: "+va);
        for (PairCardCount p : array) {
            if (p.getCard().getValue() == va)
                toRemove.add(p);
        }
        array.removeAll(toRemove);
        return array;
    }

    public void removeCardFromArrays(JSONData cardToRemove) {
        for (int i = 0; i < possibleCard.length; i++) {
            removeCardFromPossibleCardArray(i, cardToRemove);
        }
    }

    public void removeCardFromPossibleCardArray(int index, JSONData cardToRemove) {
        for (PairCardCount p : possibleCard[index])
            if (p.getCard().equals(cardToRemove))
                if (p.decrease() == 0) {
                    //posso fare remove anche se itero perchè faccio brake
                    possibleCard[index].remove(p);
                    break;
                }
    }

    public List<PairCardCount>[] removeCardsFromArrays(List<PairCardCount>[] arrays, JSONArray cardsToRemove) {
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = removeCardsFromPossibleCardArray(arrays[i], cardsToRemove);
        }
        return arrays;
    }

    public List<PairCardCount> removeCardsFromPossibleCardArray(List<PairCardCount> array, JSONArray cardsToRemove) {
        List<PairCardCount> toRemove = new ArrayList<>();
        for (JSONData d : cardsToRemove) {
            for (PairCardCount p : array)
                if (p.getCard().equals(d)) {
                    if (p.decrease() == 0)
                        toRemove.add(p);
                }
        }
        array.removeAll(toRemove);
        return array;
    }

    public List<PairCardCount>[] setArraysForNewCard(List<PairCardCount>[] possibleCardForPlayer, int card) {
        List<PairCardCount>[] list = possibleCardForPlayer;
        for (int i = card; i < list.length-1; i++) {
            list[i].clear();
            for (PairCardCount p : list[i + 1])
                list[i].add(p);

        }
        list[Game.getInstance().getNumberOfCardsPerPlayer() - 1] = generateCards();
        return list;
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
     *
     * @param player
     * @return
     */
    public double[] getPlayability(String player) throws JSONException {
        double[] result = new double[Game.getInstance().getNumberOfCardsPerPlayer()];
        ArrayList<Card> playableCards = new ArrayList<>();
        for (Color co : Color.values()) {
            try {
                int value = hystory.get(hystory.size() - 1).getFirework(co).peak();
                if (value != 5)
                    playableCards.add(new Card(co, value + 1));
            } catch (JSONException e) {
                System.err.println("Non able to add cards in array Playable, while checking fireworks");
            }
        }
//        //PROVA*************************************
//        for(Card c : playableCards)
//            System.out.println(c.toString());
//        //PROVA*************************************
        if (player.equalsIgnoreCase(owner)) { //sono io
            for (int i = 0; i < possibleCard.length; i++) {
                result[i] = getPlayabiltyForArray(possibleCard[i], playableCards);
            }
        } else {//non sono io
            List<PairCardCount>[] possibleCardForPlayer = getProbabilityArraysForPlayer(player);
            for (int i = 0; i < possibleCardForPlayer.length; i++) {
                result[i] = getPlayabiltyForArray(possibleCardForPlayer[i], playableCards);
            }
        }
//        //PROVA*************************************
//        System.out.println(player);
//        for(int k=0; k<result.length; k++)
//            System.out.println(result[k]);
//        //PROVA*************************************
        return result;
    }
    public double getPlayabiltyForArray(List<PairCardCount> array, ArrayList<Card> playableCards){
        double casiFavorevoli = 0, casiTotali = 0;

        for (PairCardCount p : array) {
            casiTotali += p.getCount();
            if (playableCards.contains(p.getCard()))
                casiFavorevoli += p.getCount();
        }
        return casiFavorevoli / casiTotali;
    }

    public List<PairCardCount>[] getProbabilityArraysForPlayer(String player) throws JSONException {
        List<PairCardCount>[] possibleCardForPlayer = new List[Game.getInstance().getNumberOfCardsPerPlayer()];
        for (int i = 0; i < possibleCardForPlayer.length; i++) {
            possibleCardForPlayer[i] = generateCards();
            //removePlayersHands(i,state);
        }
        State s;
        for (int j = 1; j < hystory.size() - 1; j++) {
            s = hystory.get(j);
            if (s.getCurrentPlayer().equalsIgnoreCase(player) && (hystory.get(j + 1).getAction().equals(ActionType.PLAY) || hystory.get(j + 1).getAction().equals(ActionType.DISCARD))) {
                possibleCardForPlayer = setArraysForNewCard(possibleCardForPlayer, s.getAction().getCard());
            } else if (s.getAction().equals(ActionType.HINT_COLOR) && s.getAction().getHinted().equals(player)) {
                possibleCardForPlayer = removeColorFromArrays(s.getAction().getCardsToReveal(), s.getAction().getColor(), possibleCardForPlayer);
            } else if (s.getAction().equals(ActionType.HINT_VALUE) && s.getAction().getHinted().equals(player)) {
                possibleCardForPlayer = removeValueFromArrays(s.getAction().getCardsToReveal(), s.getAction().getValue(), possibleCardForPlayer);
            }
        }

        //remove discards, played and hands
        possibleCardForPlayer = removeCardsFromArrays(possibleCardForPlayer, hystory.get(hystory.size() - 1).getDiscards());

        //remove FIREWORKS
        for (Color co : Color.values())
            possibleCardForPlayer[Game.getInstance().getNumberOfCardsPerPlayer() - 1] = removeCardsFromPossibleCardArray(possibleCardForPlayer[Game.getInstance().getNumberOfCardsPerPlayer() - 1], hystory.get(hystory.size() - 1).getFirework(co).toJSON());

        //////////////
        //CHECK
        /////////////
        for (JSONData name : Game.getInstance().getPlayers()) {
            if (!name.toString().equals(owner) && !name.toString().equals(player))
                possibleCardForPlayer = removeCardsFromArrays(possibleCardForPlayer, hystory.get(hystory.size() - 1).getHand(name.toString()).toJSON());
        }

        return possibleCardForPlayer;
    }

    /**
     * Restituisce le uselessness delle carte di player allo stato attuale
     * @param player
     * @return
     */
    public double[] getUselessness(String player) throws JSONException {
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
        if(player.equalsIgnoreCase(owner)){//sono io
            for(int i=0; i<possibleCard.length; i++){
                result[i] = getUselessnessForArray(possibleCard[i], discardableCards);
            }
        }else{//non sono io
            List<PairCardCount>[] possibleCardForPlayer = getProbabilityArraysForPlayer(player);
            for(int i=0; i<possibleCardForPlayer.length; i++){
                result[i] = getUselessnessForArray(possibleCardForPlayer[i], discardableCards);
            }
        }
        return result;
    }
    public double getUselessnessForArray(List<PairCardCount> array, ArrayList<Card> discardableCards){
        double casiFavorevoli = 0, casiTotali = 0;

        for(PairCardCount p : array){
            casiTotali += p.getCount();
            if(discardableCards.contains(p.getCard()))
                casiFavorevoli += p.getCount();
            else if(p.getCount()>1)
                casiFavorevoli += p.getCount();
        }

        return casiFavorevoli / casiTotali;
    }

    /**
     * Restituisce le playability delle carte di player allo stato che si otterrebbe applicando next allo stato attuale
     * @param player
     * @param next
     * @return
     */
    public double[] getPlayability(String player, Action next) throws JSONException {

        double[] result = new double[Game.getInstance().getNumberOfCardsPerPlayer()];
        ArrayList<Card> playableCards = new ArrayList<>();
        for (Color co : Color.values()) {
            try {
                int value = hystory.get(hystory.size() - 1).getFirework(co).peak();
                if (value != 5)
                    playableCards.add(new Card(co, value + 1));
            } catch (JSONException e) {
                System.err.println("Non able to add cards in array Playable, while checking fireworks");
            }
        }

        List<PairCardCount>[] possibleCardForPlayer;
        if (player.equalsIgnoreCase(owner)) //sono io e suppong l'hint sia per me, o l'action
            possibleCardForPlayer = possibleCard;
        else //non sono io
            possibleCardForPlayer = getProbabilityArraysForPlayer(player);

//        //PROVA****************************************
//        for(List<PairCardCount> l : possibleCardForPlayer){
//            for(PairCardCount p : l)
//                System.out.println("PREHINT["+player+"]"+p.toString());
//        }
//        System.out.println("**********************************************");
//        //PROVA****************************************

        if(next.getType().equals(ActionType.HINT_COLOR))
            possibleCardForPlayer = removeColorFromArrays(next.getCardsToReveal(),next.getColor(),possibleCardForPlayer);
        else if(next.getType().equals(ActionType.HINT_VALUE))
            possibleCardForPlayer = removeValueFromArrays(next.getCardsToReveal(),next.getValue(),possibleCardForPlayer);
//        else if(next.getType().equals(ActionType.DISCARD)){
//            possibleCardForPlayer = setArraysForNewCard(possibleCardForPlayer,next.getCard());
//        }
//        PLAY
        for (int i = 0; i < possibleCardForPlayer.length; i++) {
            result[i] = getPlayabiltyForArray(possibleCardForPlayer[i], playableCards);
        }

//        //PROVA****************************************
//        for(List<PairCardCount> l : possibleCardForPlayer){
//            for(PairCardCount p : l)
//                System.out.println("POST HINT["+player+"]"+p.toString());
//        }
//        System.out.println("**********************************************");
//        //PROVA****************************************

        return result;
    }

    /**
     * Restituisce le uselessness delle carte di player allo stato che si otterrebbe applicando next allo stato attuale
     * @param player
     * @param next
     * @return
     */
    public double[] getUselessness(String player, Action next) throws JSONException {
        double[] result = new double[Game.getInstance().getNumberOfCardsPerPlayer()];
        ArrayList<Card> playableCards = new ArrayList<>();
        for (Color co : Color.values()) {
            try {
                int value = hystory.get(hystory.size() - 1).getFirework(co).peak();
                if (value != 5)
                    playableCards.add(new Card(co, value + 1));
            } catch (JSONException e) {
                System.err.println("Non able to add cards in array Playable, while checking fireworks");
            }
        }

        List<PairCardCount>[] possibleCardForPlayer;
        if (player.equalsIgnoreCase(owner)) //sono io e suppong l'hint sia per me, o l'action
            possibleCardForPlayer = possibleCard;
        else //non sono io
            possibleCardForPlayer = getProbabilityArraysForPlayer(player);


        if(next.getType().equals(ActionType.HINT_COLOR))
            possibleCardForPlayer = removeColorFromArrays(next.getCardsToReveal(),next.getColor(),possibleCardForPlayer);
        else if(next.getType().equals(ActionType.HINT_VALUE))
            possibleCardForPlayer = removeValueFromArrays(next.getCardsToReveal(),next.getValue(),possibleCardForPlayer);
//        else if(next.getType().equals(ActionType.DISCARD)){
//            possibleCardForPlayer = setArraysForNewCard(possibleCardForPlayer,next.getCard());
//        }
//        PLAY
        for (int i = 0; i < possibleCardForPlayer.length; i++) {
            result[i] = getUselessnessForArray(possibleCardForPlayer[i], playableCards);
        }
        return result;
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
