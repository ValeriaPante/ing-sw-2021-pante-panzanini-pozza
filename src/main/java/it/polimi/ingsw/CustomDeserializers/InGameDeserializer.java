package it.polimi.ingsw.CustomDeserializers;

import com.google.gson.*;
import it.polimi.ingsw.Enums.Resource;
import it.polimi.ingsw.Messages.InGameMessages.*;
import it.polimi.ingsw.Messages.InGameMessages.ConcreteMessages.*;

import java.lang.reflect.Type;

/**
 *This class implements the interface for being used as a deserializer for json objects.
 * If the parameter passed is null, this class creates a message corresponding to none action ("noActionMessage")
 * The comments kept inside are useful if there will be, in the future, the possibility of using clients different
 * from the one we wrote in order to notify that the message sent is not corresponding to a message corresponding
 * to an action of the controllers server-side. In that case the method should throw a runtime exception
 * and the caller should catch it
 */
public class InGameDeserializer implements JsonDeserializer<InGameMessage> {
    @Override
    public InGameMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Resource resource;

        try{
            switch (jsonObject.get("type").getAsString()){
                case "AllProductionPowerSelection":
                    return new AllProductionPowerSelectionMessage();

                case "AnySelection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new AnySelectionMessage(resource);

                case "BackFromAnySelection":
                    return new BackFromAnySelectionMessage();

                case "BuyDevCard":
                    return new BuyDevCardMessage();

                case "CardProductionSelection":
                    return new CardProductionSelectionMessage(jsonObject.get("integer").getAsInt());

                case "ChooseDevCard":
                    return new ChooseDevCardMessage(jsonObject.get("integer").getAsInt());

                case "ChooseDevSlot":
                    return new ChooseDevSlotMessage(jsonObject.get("integer").getAsInt());

                case "DiscountAbility":
                    return new DiscountAbilityMessage(jsonObject.get("integer").getAsInt());

                case "EndTurn":
                    return new EndTurnMessage();

                case "Exchange":
                    return new ExchangeMessage();

                case "LeaderCardAction":
                    return new LeaderCardActionMessage(jsonObject.get("integer").getAsInt(), jsonObject.get("aBoolean").getAsBoolean());

                case "LeaderDiscard":
                    return new LeaderDiscardMessage(jsonObject.get("integer").getAsInt());

                case "LeaderStorageSelection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new LeaderStorageSelectionMessage(jsonObject.get("id").getAsInt(), jsonObject.get("resPosition").getAsInt(), resource);

                case "MarketSelection":
                    return new MarketSelectionMessage(jsonObject.get("integer").getAsInt(), jsonObject.get("aBoolean").getAsBoolean());

                case "MoveToLeaderStorage":
                    return new MoveToLeaderStorageMessage(jsonObject.get("integer").getAsInt());

                case "MoveToShelf":
                    return new MoveToShelfMessage(jsonObject.get("integer").getAsInt());

                case "MoveToSupportContainer":
                    return new MoveToSupportContainerMessage();

                case "PaySelected":
                    return new PaySelectedMessage();

                case "ProductionActivation":
                    return new ProductionActivationMessage();

                case "QuitFromMarket":
                    return new QuitFromMarketMessage();

                case "SelectResource":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new SelectResourceMessage(jsonObject.get("integer").getAsInt(), resource);

                case "ShelfDeselection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new ShelfDeselectionMessage(jsonObject.get("integer").getAsInt(), resource);

                case "ShelfSelection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new ShelfSelectionMessage(jsonObject.get("integer").getAsInt(), resource);

                case "StrongBoxDeselection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new StrongBoxDeselectionMessage(jsonObject.get("integer").getAsInt(), resource);

                case "StrongBoxSelection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new StrongBoxSelectionMessage(jsonObject.get("integer").getAsInt(), resource);

                case "SupportContainerDeselection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    if (resource == null)
                        return new NoActionMessage();
                    return new SupportContainerDeselectionMessage(jsonObject.get("integer").getAsInt(), resource);

                case "SupportContainerSelection":
                    resource = Resource.fromAlias(jsonObject.get("resource").getAsString());
                    return (resource == null) ? new NoActionMessage() : new SupportContainerSelectionMessage(jsonObject.get("integer").getAsInt(), resource);

                case "TakeFromMarket":
                    return new TakeFromMarketMessage();

                case "Transmutation":
                    return new TransmutationMessage(jsonObject.get("serial1").getAsInt(), jsonObject.get("serial2").getAsInt(), jsonObject.get("quantity1").getAsInt(), jsonObject.get("quantity2").getAsInt());

                case "DeselectAll":
                    return new DeselectAllResources();

                default:
                    return new NoActionMessage();
            }
        } catch (NullPointerException e) {
            return new NoActionMessage();
        }
    }
}
