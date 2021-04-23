package it.polimi.ingsw.Controller;

import it.polimi.ingsw.Abilities.Ability;
import it.polimi.ingsw.Cards.LeaderCard;
import it.polimi.ingsw.Deposit.Depot;
import it.polimi.ingsw.Deposit.Shelf;
import it.polimi.ingsw.Deposit.StrongBox;
import it.polimi.ingsw.Enums.Resource;
import it.polimi.ingsw.Exceptions.WeDontDoSuchThingsHere;
import it.polimi.ingsw.Player.RealPlayer;

import java.util.ArrayList;
import java.util.EnumMap;

public class ResourceMoverController {
//    2 tipi di metodi: uno per mettere le cose dentro la shelf, uno per togliere da dentro la shelf, in entrambi i casi bisogna
//    fare l'azione opposta dalla mappa delle risorse ricevute dal mercato
//    __ funzionamento attraverso selezione
//
//    metodo che elimina la mappa delle risorse da aggiungere ma prima conta le contenute per poter dare eventuali punti vittoria agli altri player
//
//
//
//
//
//
    private RealPlayer player;
    private final StrongBox stillToBeSetBox;
    private EnumMap<Resource, Integer> enumMap;

    public ResourceMoverController(){
        this.player = null;
        this.stillToBeSetBox = new StrongBox();
        this.enumMap = new EnumMap<>(Resource.class);
    }

    public void update(RealPlayer player, EnumMap<Resource, Integer> toBePositioned ){
        this.player = player;
        this.stillToBeSetBox.mapSelection(stillToBeSetBox.content());
        this.stillToBeSetBox.pay();
        this.stillToBeSetBox.addEnumMap(toBePositioned);
    }

    public void clear(){
        this.player = null ;
        this.enumMap.clear();
    }

    public RealPlayer getPlayer(){
        return this.player;
    }

    public int done(){
        int resourcesNotPlaced = stillToBeSetBox.countAll();
        this.clear();
        return resourcesNotPlaced;
    }

    //prendo la shelf con capienza numberOfShelf, faccio quella selezione (singola), eventualmente mi ritorna un'eccezione
    public synchronized void selectFromShelf(Resource resType, int numberOfShelf){
        if ((numberOfShelf > 3) || (numberOfShelf < 1))
            //Error message: "Wrong number of Shelf"
            return;

        Shelf currentShelf = player.getShelves()[numberOfShelf-1];
        if (currentShelf.getResourceType() != resType)
            //Error message: "Wrong type of resource selected"
            return;

        try{
            currentShelf.singleSelection();
        } catch (IndexOutOfBoundsException e){
            //Error message: "Maximum already selected"
        }
    }

    //prendo la leader card con quel seriale, faccio quella selezione (singola), eventualmente mi ritorna un'eccezione
    public synchronized void selectFromLeaderStorage(Resource resType, int serial, int resPosition){
        LeaderCard specifiedLeaderCard = cardSpecified(serial);
        if(specifiedLeaderCard == null)
            return;

        try{
            if (specifiedLeaderCard.getAbility().getCapacity().containsKey(resType)){
                //Error message: "Resource type note allowed"
                return;
            }
            if (specifiedLeaderCard.getAbility().getContent().containsKey(resType)){
                //Error message: "Resource not contained"
                return;
            }
            enumMap = specifiedLeaderCard.getAbility().getSelected();
            specifiedLeaderCard.getAbility().select(resType, resPosition);
            if (enumMap.equals(specifiedLeaderCard.getAbility().getSelected())){
                //Error message: "Not selectable"
                return;
            }
        } catch (WeDontDoSuchThingsHere e){
            //Error message: "Wrong leader card"
            return;
        }
    }

    //seleziono quel numero di risorse tra quelle nel contenitore delle risorse, in corrispondenza di quel tipo di risorsa
    public synchronized void selectFromMarketResources(Resource resType, int quantity){
        if (quantity == 0)
            //Error message: "No quantity specified"
            return;

        enumMap.clear();
        enumMap.put(resType, quantity);

        try{
            stillToBeSetBox.mapSelection(enumMap);
        } catch (IndexOutOfBoundsException e){
            //Error message: "Selection exceeding limits"
        }
    }

    //altri tre metodi per fare deselection
    public synchronized void deselectFromShelf(Resource resType, int numberOfShelf){
        if ((numberOfShelf > 3) || (numberOfShelf < 1))
            return;

        Shelf currentShelf = player.getShelves()[numberOfShelf-1];
        if (currentShelf.getResourceType() != resType)
            return;

        try{
            currentShelf.singleDeselection();
        } catch (IndexOutOfBoundsException e){
            //Error message: "Noting to deselect"
        }
    }

    public synchronized void deselectFromMarketResources(Resource resType, int quantity){
        if (quantity == 0)
            return;

        enumMap.clear();
        enumMap.put(resType, quantity);

        try{
            stillToBeSetBox.mapDeselection(enumMap);
        } catch (IndexOutOfBoundsException e){
            //Error message: "Deselection exceeding limits"
        }
    }

    //un metodo per spostare dai contenitori shelf e leader deposit a contenitore di mercato
    public void moveToStillToBeSetBox(){
        for (Shelf s : player.getShelves()){
            try{
                stillToBeSetBox.addEnumMap(s.takeSelected());
            } catch (IndexOutOfBoundsException ignored){}
        }

        //itero sulle leader card e prendo le risorse selezionate mettendole nello stillToBeSetBox
        for(LeaderCard lc: player.getLeaderCards()){
            try{
                stillToBeSetBox.addEnumMap(lc.getAbility().getSelected());
                lc.getAbility().pay();
            }
            catch (WeDontDoSuchThingsHere ignored){}
            catch (NullPointerException ignored){}
        }
    }

    //Due metodi per spostare selezionate da contenitore di mercato a shelf/leader --> controllando rispetto regole per ripetizione delle risorse nelle shelf
    //quel metodo deve anche fare deselezione nel caso in cui riesce a spostare le risorse
    public void moveToShelf(int numberOfShelf){
        enumMap = stillToBeSetBox.getSelection();
        if ((enumMap == null) || (enumMap.size() != 1))
            return;

        Resource resToBeAdded = null;
        for(Resource r : Resource.values())
            if(enumMap.containsKey(r))
                resToBeAdded = r;

        Shelf currentShelf = player.getShelves()[numberOfShelf-1];
        if (currentShelf.isEmpty())
            for (int i=0; i<3; i++)
                if(((i+1) != numberOfShelf) && ((player.getShelves()[i]).getResourceType() == resToBeAdded))
                        return;

        try{
            currentShelf.addAllIfPossible(resToBeAdded, enumMap.get(resToBeAdded));
        } catch (IllegalArgumentException e){
            //Error message: "Wrong type of resource"
            return;
        }catch (IndexOutOfBoundsException e){
            //Error message: "Insertion exceeding limits"
            return;
        }

        stillToBeSetBox.pay();
    }

    public void moveToLeaderStorage(int serial){
        LeaderCard specifiedLeaderCard = cardSpecified(serial);
        if(specifiedLeaderCard == null)
            return;

        enumMap = stillToBeSetBox.getSelection();
        if (enumMap == null)
            //Error message: "No resources selected to be moved"
            return;

        try {
            Depot depot = new Depot();
            depot.addEnumMap(enumMap);
            try{
                depot.addEnumMap(specifiedLeaderCard.getAbility().getContent());
            } catch (NullPointerException ignored){}
            enumMap = depot.content();
            depot.clearDepot();
            depot.addEnumMap(specifiedLeaderCard.getAbility().getCapacity());
            try{
                depot.removeEnumMapIfPossible(enumMap);
            } catch (IndexOutOfBoundsException e){
                //Error message: "Can't move resources there"
                return;
            }
            specifiedLeaderCard.getAbility().pay();
        } catch (WeDontDoSuchThingsHere e){
            //Error message: "Wrong leader card"
            return;
        }
    }

    //un metodo exchange per scambiare le risorse contenute in due contenitori: deve controllare che le risorse siano selezionate solo in due contenitori
    //poi deve controllare che lo spostamento sia fattibile, poi deve spostarle senza perderne traccia e poi deve fare deselezione nei due contenitori

    public void exchange(){
        //controllo che ci siano solo due contenitori con selezioni e me li segno
        int numOfContainersSelected = 0;
        ArrayList<Shelf> shelvesSelected = new ArrayList<>();
        ArrayList<LeaderCard> leaderCardsSelected = new ArrayList<>();

        for (Shelf s: player.getShelves()) {
            if (s.getQuantitySelected() != 0) {
                numOfContainersSelected++;
                shelvesSelected.add(s);
            }
        }
//        if (numOfContainersSelected > 2)
//            //Error message: "More than two selections"
//            return;

        for (LeaderCard lc: player.getLeaderCards()){
            try{
                if ((lc.hasBeenPlayed()) && (lc.getAbility().getSelected() != null)) {
                    numOfContainersSelected++;
                    leaderCardsSelected.add(lc);
                }
            } catch (WeDontDoSuchThingsHere ignored) {}
        }
//        if (numOfContainersSelected == 0)
//            //Error message: "Less than two selections"
//            return;

        if (stillToBeSetBox.areThereSelections())
            numOfContainersSelected++;

        if (numOfContainersSelected > 2)
            //Error message: "More than two selections"
            return;
        if (numOfContainersSelected < 2)
            //Error message: "Less than two selections"
            return;

        //prendo le risorse selezionate da entrambi e le salvo, provo a metterle nell'altro, togliendo quelle selezionate, nel caso ci siano eccezioni allora annullo
        //altrimenti mantengo la modifica

        if (stillToBeSetBox.areThereSelections()){
            enumMap = stillToBeSetBox.getSelection();
            if(shelvesSelected.size() == 1){ //Selections in stillToBeSetBox and one Shelf
                Shelf selectedShelf = shelvesSelected.get(0);

                Resource resContainedInSTBSB = movableResource(enumMap, selectedShelf);
                if (resContainedInSTBSB == null)
                    return;

                stillToBeSetBox.addEnumMap(selectedShelf.takeSelected());
                selectedShelf.addAllIfPossible(resContainedInSTBSB, enumMap.get(resContainedInSTBSB));
            } else { //Selections in stillToBeSetBox and one leaderCard
                Ability selectedLeaderCardAbility = leaderCardsSelected.get(0).getAbility();

                if (!canLeaderContain(selectedLeaderCardAbility, enumMap))
                    return;

                stillToBeSetBox.addEnumMap(selectedLeaderCardAbility.getSelected());
                selectedLeaderCardAbility.pay();
                addEnumMapToLC(selectedLeaderCardAbility, enumMap);
            }
        } else { //selections between LeaderCards and Shelves
            if (shelvesSelected.size() == 2){ //Selections only in Shelves
                Shelf selectedShelf1 = shelvesSelected.get(0);
                Shelf selectedShelf2 = shelvesSelected.get(1);
                if ((selectedShelf2.getUsage() != selectedShelf2.getQuantitySelected()) || (selectedShelf1.getUsage() != selectedShelf1.getQuantitySelected()))
                    //Error message: "Cannot swap shelves content"
                    return;

                if ((selectedShelf1.getCapacity() < selectedShelf2.getQuantitySelected()) || (selectedShelf2.getCapacity() < selectedShelf1.getQuantitySelected()))
                    //Error message: "Swap is exceeding one shelf limit"
                    return;
                Resource shelf1Type = selectedShelf1.getResourceType();
                int shelf1Usage = selectedShelf1.getUsage();
                selectedShelf1.pay();
                Resource shelf2Type = selectedShelf2.getResourceType();
                int shelf2Usage = selectedShelf2.getUsage();
                selectedShelf2.pay();
                selectedShelf1.addAllIfPossible(shelf2Type, shelf2Usage);
                selectedShelf2.addAllIfPossible(shelf1Type, shelf1Usage);
            } else {
                if (shelvesSelected.size() == 1) { //Selections in one Shelf and one LeaderCard
                    Ability selectedLeaderCardAbility = leaderCardsSelected.get(0).getAbility();
                    enumMap = selectedLeaderCardAbility.getSelected();
                    Shelf selectedShelf = shelvesSelected.get(0);

                    Resource resContainedInLSC = movableResource(enumMap, selectedShelf);
                    if (resContainedInLSC == null)
                        return;

                    if (!canLeaderContain(selectedLeaderCardAbility, enumMap))
                        return;

                    stillToBeSetBox.addEnumMap(selectedShelf.takeSelected());
                    selectedShelf.addAllIfPossible(resContainedInLSC, enumMap.get(resContainedInLSC));
                } else {//Selections only in LeaderCards
                    Ability leaderAbility1 = leaderCardsSelected.get(0).getAbility();
                    Ability leaderAbility2 = leaderCardsSelected.get(1).getAbility();

                    if (!canLeaderContain(leaderAbility1, leaderAbility2.getSelected()))
                        return;

                    if (!canLeaderContain(leaderAbility2, leaderAbility1.getSelected()))
                        return;

                    EnumMap<Resource, Integer> enumMap1 = new EnumMap<>(leaderAbility1.getSelected());
                    enumMap = leaderAbility2.getSelected();
                    leaderAbility1.pay();
                    leaderAbility2.pay();
                    addEnumMapToLC(leaderAbility1, enumMap);
                    addEnumMapToLC(leaderAbility2, enumMap1);
                }
            }
        }
    }

    private synchronized Resource movableResource(EnumMap<Resource, Integer> enumMap, Shelf shelf){
        if(enumMap.size() != 1)
            //Error message: "Too many type of resources selected"
            return null;

        Resource resourceContained = null;
        for (Resource r: Resource.values())
            if(enumMap.containsKey(r))
                resourceContained = r;
        if (shelf.getUsage() == shelf.getQuantitySelected()){ //completely empty the shelf
            if (enumMap.get(resourceContained) > shelf.getCapacity())
                //Error message: "Shelf cannot contain that quantity"
                return null;

            for (Shelf s: player.getShelves())
                if ((shelf != s) && (resourceContained == s.getResourceType()))
                    //Error message: "Resource already contained in another Shelf"
                    return null;
        } else {
            if (shelf.getResourceType() != resourceContained)
                //Error message: "Wrong type of Resource"
                return null;

            if (enumMap.get(resourceContained) > (shelf.getCapacity() -(shelf.getUsage() - shelf.getQuantitySelected())))
                //Error message: "Shelf cannot contain that quantity"
                return null;
        }
        return resourceContained;
    }

    private synchronized boolean canLeaderContain(Ability ability, EnumMap<Resource, Integer> enumMap){
        Depot depot = new Depot();
        depot.addEnumMap(ability.getCapacity());
        depot.removeEnumMapIfPossible(ability.getContent());
        depot.addEnumMap(ability.getSelected());
        try{
            depot.removeEnumMapIfPossible(enumMap);
        } catch (IndexOutOfBoundsException e){
            //Error message: "LeaderCard cannot contain that quantity"
            return false;
        }
        return true;
    }

    private synchronized void addEnumMapToLC (Ability ability, EnumMap<Resource, Integer> enumMap){
        for (Resource r: Resource.values())
            if (enumMap.containsKey(r))
                for (int i=0; i < enumMap.get(r); i++)
                    ability.add(r);
    }

    private synchronized LeaderCard cardSpecified(int serial){
        boolean ownCard = false;
        LeaderCard specifiedLeaderCard = null;
        for (LeaderCard lc : player.getLeaderCards()) {
            if (lc.getId() == serial){
                ownCard = true;
                specifiedLeaderCard = lc;
            }
        }
        if (!ownCard)
            //Error message: "Not own leader card"
            return null;
        if (!specifiedLeaderCard.hasBeenPlayed())
            //Error message: "Card not played"
            return null;

        return specifiedLeaderCard;
    }

}
