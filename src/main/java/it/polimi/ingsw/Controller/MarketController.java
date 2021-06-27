package it.polimi.ingsw.Controller;

import it.polimi.ingsw.Exceptions.WrongLeaderCardType;
import it.polimi.ingsw.Model.Abilities.Ability;
import it.polimi.ingsw.Model.Cards.LeaderCard;
import it.polimi.ingsw.Model.Deposit.Depot;
import it.polimi.ingsw.Model.Deposit.Shelf;
import it.polimi.ingsw.Model.Deposit.StrongBox;
import it.polimi.ingsw.Enums.MacroTurnType;
import it.polimi.ingsw.Enums.MicroTurnType;
import it.polimi.ingsw.Enums.Resource;
import it.polimi.ingsw.Model.Player.RealPlayer;

import java.util.ArrayList;
import java.util.EnumMap;

public class MarketController extends SelectionController{
    public MarketController(FaithTrackController ftc){
        super(ftc);
    }

    public boolean selectionFromShelf(Resource resType, int numberOfShelf){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return false;

        super.selectFromShelf(resType, numberOfShelf);
        return true;
    }

    public boolean selectionFromLeaderStorage(Resource resType, int serial, int resPosition){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return false;

        super.selectFromLeaderStorage(resType, serial, resPosition);
        return true;
    }

    public void selectFromSupportContainer(Resource resType, int quantity){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return;

        super.selectFromSupportContainer(resType, quantity);
    }

    public boolean deselectionFromShelf(Resource resType, int numberOfShelf){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return false;

        super.deselectFromShelf(resType, numberOfShelf);
        return true;
    }

    public void deselectFromSupportContainer(Resource resType, int quantity){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return;

        super.deselectFromSupportContainer(resType, quantity);
    }

    //moves all the resources selected from leader deposit and shelves to supportContainer (in player of turn)
    public void moveSelectedToSupportContainer(){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return;

        StrongBox supportContainer = table.turnOf().getSupportContainer();

        for (Shelf s : table.turnOf().getShelves()){
            try{
                EnumMap<Resource, Integer> shelfSelected = new EnumMap<>(Resource.class);
                if (s.getResourceType() != null){
                    shelfSelected.put(s.getResourceType(), s.getQuantitySelected());
                    table.payThrough(s);
                    table.addToSupportContainer(shelfSelected);
                }
            } catch (IndexOutOfBoundsException ignored){}
        }

        for(LeaderCard lc: table.turnOf().getLeaderCards()){
            try{
                table.addToSupportContainer(lc.getAbility().getSelected());
                table.payThrough(lc.getAbility());
            }
            catch (WrongLeaderCardType | NullPointerException ignored){}
        }
    }

    //moves the resources selected in supportContainer, in player of turn, into the Shelf with the capacity equals to numberOfShelf
    public void moveToShelf(int numberOfShelf){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return;

        enumMap = table.turnOf().getSupportContainer().getSelection();

        if ((enumMap == null) || (enumMap.size() != 1)) {
            table.turnOf().setErrorMessage("Wrong selection");
            return;
        }

        Resource resToBeAdded = null;
        for(Resource r : Resource.values())
            if(enumMap.containsKey(r))
                resToBeAdded = r;

        Shelf currentShelf = shelfFromCapacity(numberOfShelf);
        if (currentShelf == null)
            return;

        if (currentShelf.isEmpty())
            for (Shelf s: table.turnOf().getShelves())
                if ((currentShelf != s) && (resToBeAdded == s.getResourceType())) {
                    table.turnOf().setErrorMessage("Resource already contained in another Shelf");
                    return;
                }

        try{
            table.addAllIfPossibleToShelf(currentShelf.getCapacity(), resToBeAdded, enumMap.get(resToBeAdded));
            table.payThrough(table.turnOf().getSupportContainer());

        } catch (IllegalArgumentException e){
            table.turnOf().setErrorMessage("Wrong type of resource");
        }catch (IndexOutOfBoundsException e){
            table.turnOf().setErrorMessage("Insertion exceeding limits");
        }
    }

    //moves the resources selected in supportContainer, in player of turn, into the leader card with the id equals to the serial number specified
    public void moveToLeaderStorage(int serial){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return;

        LeaderCard specifiedLeaderCard = getUsableLeaderCard(serial);
        if(specifiedLeaderCard == null)
            return;

        enumMap = table.turnOf().getSupportContainer().getSelection();
        if (enumMap == null) {
            table.turnOf().setErrorMessage("No resources selected to be moved");
            return;
        }

        if (canLeaderContain(specifiedLeaderCard.getAbility(), enumMap, false) ) {
            addEnumMapToLC(specifiedLeaderCard.getAbility(), table.turnOf().getSupportContainer().getSelection());
            table.payThrough(table.turnOf().getSupportContainer());
        }
    }

    //if there are only two containes with and if it is possible to swap the selection, this does it
    public void exchange(){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.PLACE_RESOURCES)
            return;

        //PART 1: counts the container with resources selected
        //==================================================================================================
        int numOfContainersSelected = 0;
        ArrayList<Shelf> shelvesSelected = new ArrayList<>();
        ArrayList<LeaderCard> leaderCardsSelected = new ArrayList<>();
        RealPlayer player = table.turnOf();

        for (Shelf s: player.getShelves()) {
            if (s.getQuantitySelected() > 0) {
                numOfContainersSelected++;
                shelvesSelected.add(s);
            }
        }

//        optional optimization:
//        if (numOfContainersSelected > 2)
//            //Error message: "More than two selections"
//            return;

        for (LeaderCard lc: player.getLeaderCards()){
            try{
                if ((lc.hasBeenPlayed()) && (!lc.getAbility().getSelected().equals(new EnumMap<Resource, Integer>(Resource.class)))) {
                    numOfContainersSelected++;
                    leaderCardsSelected.add(lc);
                }
            } catch (WrongLeaderCardType ignored) {}
        }

//        optional optimization:
//        if (numOfContainersSelected == 0)
//            //Error message: "Less than two selections"
//            return;

        if (player.getSupportContainer().areThereSelections())
            numOfContainersSelected++;

        if (numOfContainersSelected > 2){
            table.turnOf().setErrorMessage("More than two selections");
            return;
        }

        if (numOfContainersSelected < 2){
            table.turnOf().setErrorMessage("Less than two selections");
            return;
        }


        //PART 2: checks if it is possible to swap the selected resources and if so proceed
        //==================================================================================================
        if (player.getSupportContainer().areThereSelections()){
            enumMap = player.getSupportContainer().getSelection();
            if(shelvesSelected.size() == 1){ //Selections in stillToBeSetBox and one Shelf
                Shelf selectedShelf = shelvesSelected.get(0);

                Resource resSelectedInSupportContainer = movableResource(enumMap, selectedShelf);
                if (resSelectedInSupportContainer == null)
                    return;

                EnumMap<Resource, Integer> shelfSelection = new EnumMap<>(Resource.class);
                shelfSelection.put(selectedShelf.getResourceType(), selectedShelf.getQuantitySelected());
                table.payThrough(selectedShelf);
                table.addToSupportContainer(shelfSelection);

                enumMap = player.getSupportContainer().getSelection();
                table.payThrough(player.getSupportContainer());
                table.addAllIfPossibleToShelf(selectedShelf.getCapacity(), resSelectedInSupportContainer, enumMap.get(resSelectedInSupportContainer));
            } else { //Selections in stillToBeSetBox and one leaderCard
                Ability selectedLeaderCardAbility = leaderCardsSelected.get(0).getAbility();

                if (!canLeaderContain(selectedLeaderCardAbility, enumMap, true))
                    return;

                table.addToSupportContainer(selectedLeaderCardAbility.getSelected());
                selectedLeaderCardAbility.pay();
                enumMap = player.getSupportContainer().getSelection();
                table.payThrough(player.getSupportContainer());
                addEnumMapToLC(selectedLeaderCardAbility, enumMap);
            }
        } else { //selections between LeaderCards and Shelves
            if (shelvesSelected.size() == 2){ //Selections only in Shelves
                Shelf selectedShelf1 = shelvesSelected.get(0);
                Shelf selectedShelf2 = shelvesSelected.get(1);
                if ((selectedShelf2.getUsage() != selectedShelf2.getQuantitySelected()) || (selectedShelf1.getUsage() != selectedShelf1.getQuantitySelected())){
                    table.turnOf().setErrorMessage("Cannot swap shelves content");
                    return;
                }

                if ((selectedShelf1.getCapacity() < selectedShelf2.getQuantitySelected()) || (selectedShelf2.getCapacity() < selectedShelf1.getQuantitySelected())){
                    table.turnOf().setErrorMessage("Swap is exceeding one shelf limit");
                    return;
                }

                Resource shelf1Type = selectedShelf1.getResourceType();
                int shelf1Usage = selectedShelf1.getUsage();
                selectedShelf1.pay();

                Resource shelf2Type = selectedShelf2.getResourceType();
                int shelf2Usage = selectedShelf2.getUsage();
                selectedShelf2.pay();

                table.addAllIfPossibleToShelf(selectedShelf1.getCapacity(), shelf2Type, shelf2Usage);
                table.addAllIfPossibleToShelf(selectedShelf2.getCapacity(), shelf1Type, shelf1Usage);
            } else {
                if (shelvesSelected.size() == 1) { //Selections in one Shelf and one LeaderCard
                    Ability selectedLeaderCardAbility = leaderCardsSelected.get(0).getAbility();
                    Shelf selectedShelf = shelvesSelected.get(0);

                    enumMap = selectedLeaderCardAbility.getSelected();
                    Resource resourceInLeaderCard = movableResource(enumMap, selectedShelf);
                    if (resourceInLeaderCard == null)
                        return;

                    enumMap = selectedShelf.content();
                    if (!canLeaderContain(selectedLeaderCardAbility, enumMap, true))
                        return;

                    EnumMap<Resource, Integer> shelfSelection = selectedShelf.takeSelected();
                    enumMap = selectedLeaderCardAbility.getSelected();
                    table.addAllIfPossibleToShelf(selectedShelf.getCapacity(), resourceInLeaderCard, enumMap.get(resourceInLeaderCard));
                    selectedLeaderCardAbility.pay();
                    addEnumMapToLC(selectedLeaderCardAbility, shelfSelection);
                } else {//Selections only in LeaderCards
                    Ability leaderAbility1 = leaderCardsSelected.get(0).getAbility();
                    Ability leaderAbility2 = leaderCardsSelected.get(1).getAbility();

                    if (!canLeaderContain(leaderAbility1, leaderAbility2.getSelected(),true))
                        return;

                    if (!canLeaderContain(leaderAbility2, leaderAbility1.getSelected(), true))
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
        //==================================================================================================
    }

    //checks if it is possible to move the enumMap inside the shelf, if so returns the resource in the map otherwise null
    private Resource movableResource(EnumMap<Resource, Integer> enumMap, Shelf shelf){
        if(enumMap.size() != 1){
            table.turnOf().setErrorMessage("Too many type of resources selected");
            return null;
        }

        Resource resourceContained = null;
        for (Resource r: Resource.values())
            if(enumMap.containsKey(r)){
                resourceContained = r;
                break;
            }

        if (shelf.getUsage() == shelf.getQuantitySelected()){ //completely empty the shelf
            if (enumMap.get(resourceContained) > shelf.getCapacity()){
                table.turnOf().setErrorMessage("Shelf cannot contain that quantity");
                return null;
            }

            for (Shelf s: table.turnOf().getShelves())
                if ((shelf != s) && (resourceContained == s.getResourceType())){
                    table.turnOf().setErrorMessage("Resource already contained in another Shelf");
                    return null;
                }

        } else {
            if (shelf.getResourceType() != resourceContained){
                table.turnOf().setErrorMessage("Wrong type of Resource");
                return null;
            }

            if (enumMap.get(resourceContained) > (shelf.getCapacity() - (shelf.getUsage() - shelf.getQuantitySelected()))){
                table.turnOf().setErrorMessage("Shelf cannot contain that quantity");
                return null;
            }

        }
        return resourceContained;
    }

    //checks if it is possible to move the enumMap inside the LeaderCard, if so returns true otherwise false
    private boolean canLeaderContain(Ability ability, EnumMap<Resource, Integer> enumMap, boolean isForExchange){
        try{
            Depot depot = new Depot();
            depot.addEnumMap(ability.getCapacity());
            try {
                depot.removeEnumMapIfPossible(ability.getContent());
            } catch (NullPointerException ignored) {}
            if (isForExchange)
                depot.addEnumMap(ability.getSelected());
            try{
                depot.removeEnumMapIfPossible(enumMap);
                return true;
            } catch (IndexOutOfBoundsException e){
                table.turnOf().setErrorMessage("LeaderCard cannot contain that quantity");
                return false;
            }
        } catch (WrongLeaderCardType e){
            table.turnOf().setErrorMessage("Wrong leader card");
            return false;
        }
    }

    //adds an enumMap to a leaderCard with storage ability
    private void addEnumMapToLC (Ability ability, EnumMap<Resource, Integer> enumMap){
        for (Resource r: Resource.values())
            if (enumMap.containsKey(r))
                for (int i=0; i < enumMap.get(r); i++)
                    //@daniel
                    //msg(a tutti : changedLeaderCardMessage(id player di turno, id carta, array risorse contenute nella leader card)
                    ability.add(r);
    }

    //selects rows or columns in market
    public void selectFromMarket(int number, boolean isRowChosen){
        if (table.turnOf().getMacroTurnType() == MacroTurnType.DONE ||
                (table.turnOf().getMicroTurnType() != MicroTurnType.NONE &&
                        table.turnOf().getMicroTurnType() != MicroTurnType.SELECTION_IN_MARKET))
            return;

        if(number < 0){
            table.turnOf().setErrorMessage("Wrong position specified");
            return;
        }

        if (isRowChosen){
            try{
                table.getMarket().selectRow(number);
            } catch (IndexOutOfBoundsException e) {
                table.turnOf().setErrorMessage("Position specified exceeded limits");
                return;
            }
        } else {
            try{
                table.getMarket().selectColumn(number);
            } catch (IndexOutOfBoundsException e) {
                table.turnOf().setErrorMessage("Position specified exceeded limits");
                return;
            }
        }

        if (table.turnOf().getMicroTurnType() == MicroTurnType.NONE)
            table.turnOf().setMicroTurnType(MicroTurnType.SELECTION_IN_MARKET);
    }

    //move the resources selected in the market into player's support container
    public void takeFromMarket(){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.SELECTION_IN_MARKET)
            return;

        enumMap = table.takeFromMarket();
        table.turnOf().setMicroTurnType(MicroTurnType.PLACE_RESOURCES);

        if (enumMap.containsKey(Resource.WHITE)){
            int numOfTransmutationAbilities = 0;
            Ability transmutationAbility = null;

            for (LeaderCard lc : table.turnOf().getLeaderCards()){
                try{
                    if (lc.hasBeenPlayed()){
                        lc.getAbility().getWhiteInto();
                        numOfTransmutationAbilities++;
                        transmutationAbility = lc.getAbility();
                    }
                } catch (WrongLeaderCardType ignored) {}
            }

            if (numOfTransmutationAbilities == 0){
                enumMap.remove(Resource.WHITE);
            } else if (numOfTransmutationAbilities == 1) {
                int numOfWhite = enumMap.remove(Resource.WHITE);
                for (EnumMap.Entry<Resource, Integer> entry : transmutationAbility.getWhiteInto().entrySet())
                    enumMap.put(entry.getKey(), (entry.getValue() * numOfWhite) + (enumMap.getOrDefault(entry.getKey(), 0)));
            } else {
                table.turnOf().setMicroTurnType(MicroTurnType.SELECT_LEADER_CARD);
            }
        }

        if (enumMap.containsKey(Resource.FAITH))
            faithTrackController.movePlayerOfTurn(enumMap.remove(Resource.FAITH));

        table.updatePlayerOfTurnSupportContainer(enumMap);
        table.turnOf().setMacroTurnType(MacroTurnType.GET_FROM_MARKET);
    }

    public void quit(){
        if (table.turnOf().getMicroTurnType() == MicroTurnType.SELECTION_IN_MARKET){
            table.getMarket().deleteSelection();
            table.turnOf().setMicroTurnType(MicroTurnType.NONE);
        }

        if (table.turnOf().getMicroTurnType() == MicroTurnType.PLACE_RESOURCES){
            int faithPoints = table.turnOf().getSupportContainer().countAll();
            table.updatePlayerOfTurnSupportContainer(null);
            faithTrackController.moveAllTheOthers(faithPoints);
            table.turnOf().setMicroTurnType(MicroTurnType.NONE);
            table.turnOf().setMacroTurnType(MacroTurnType.DONE);
            deselectAllResources();
        }
    }

    //it is compulsory to use this when the player owns two leader cards with transmutation ability played
    public void selectTransmutation (int serial1, int quantity1, int serial2, int quantity2){
        if (table.turnOf().getMicroTurnType() != MicroTurnType.SELECT_LEADER_CARD)
            return;

        int numOfWhite = table.turnOf().getSupportContainer().content().get(Resource.WHITE);
        if((quantity1 + quantity2) != numOfWhite){
            table.turnOf().setErrorMessage("Wrong amount specified");
            return;
        }

        EnumMap<Resource, Integer> transmutation1 = getTransmutation(serial1);
        if (transmutation1 == null)
            return;

        EnumMap<Resource, Integer> transmutation2 = getTransmutation(serial2);
        if (transmutation2 == null)
            return;

        Depot depot = new Depot();
        for (int i=0; i<quantity1; i++)
            depot.addEnumMap(transmutation1);
        for (int i=0; i<quantity2; i++)
            depot.addEnumMap(transmutation2);
        enumMap = depot.content();

        if (enumMap.containsKey(Resource.FAITH))
            faithTrackController.movePlayerOfTurn(enumMap.remove(Resource.FAITH));

        EnumMap<Resource, Integer> whiteSelection = new EnumMap<>(Resource.class);
        whiteSelection.put(Resource.WHITE, numOfWhite);
        StrongBox sb = table.turnOf().getSupportContainer();
        sb.clearSelection();
        sb.mapSelection(whiteSelection);
        table.payThrough(sb);
        table.addToSupportContainer(enumMap);
        table.turnOf().setMicroTurnType(MicroTurnType.PLACE_RESOURCES);
    }

    private EnumMap<Resource, Integer> getTransmutation(int serial){
        Ability ability;
        try{
            ability = getUsableLeaderCard(serial).getAbility();
            return ability.getWhiteInto();
        } catch (NullPointerException e) {
            return null;
        } catch (WrongLeaderCardType e) {
            table.turnOf().setErrorMessage("The selected leader card cannot transmute");
            return null;
        }
    }
}