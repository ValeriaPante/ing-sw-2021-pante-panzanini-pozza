package ControllerTest;

import it.polimi.ingsw.Model.Abilities.ProductionPower.ProductionPower;
import it.polimi.ingsw.Model.Cards.DevCard;
import it.polimi.ingsw.Model.Cards.DevCardType;
import it.polimi.ingsw.Model.Cards.LeaderCard;
import it.polimi.ingsw.Controller.FaithTrackController;
import it.polimi.ingsw.Controller.LeaderController;
import it.polimi.ingsw.Enums.Colour;
import it.polimi.ingsw.Enums.LeaderCardType;
import it.polimi.ingsw.Enums.MacroTurnType;
import it.polimi.ingsw.Enums.Resource;
import it.polimi.ingsw.Model.Game.Table;
import it.polimi.ingsw.Model.Player.RealPlayer;
import it.polimi.ingsw.Network.Client.Messages.FromServerMessage;
import it.polimi.ingsw.Network.RequestHandlers.RequestHandler;
import it.polimi.ingsw.Network.Server.MessageSenderInterface;
import it.polimi.ingsw.PreGameModel.User;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderControllerTest {


    @Test
    public void discarding(){
        Table table = new Table(2);
        table.addPlayer(new RealPlayer(new User("user1", new FakeConnectionHandler(1))));
        table.addPlayer(new RealPlayer(new User("user2", new FakeConnectionHandler(2))));
        table.turnOf().setMacroTurnType(MacroTurnType.NONE);

        EnumMap<Resource, Integer> resourceReq = new EnumMap<>(Resource.class);
        Map<DevCardType, Integer> devCardReq = new HashMap<>();
        EnumMap<Resource, Integer> input = new EnumMap<>(Resource.class);

        devCardReq.put(new DevCardType(0, Colour.YELLOW), 1);
        devCardReq.put(new DevCardType(0, Colour.GREEN), 1);
        input.put(Resource.SERVANT, 1);

        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),21));
        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),45));

        LeaderController controller = new LeaderController(new FaithTrackController(table));
        controller.actionOnLeaderCard(21,true);
        assertEquals(1, table.turnOf().getLeaderCards().length);
        controller.actionOnLeaderCard(45,true);
        assertEquals(0, table.turnOf().getLeaderCards().length);
    }

    @Test
    public void activateCardWithoutDevTypeRequirements(){
        Table table = new Table(2);
        table.addPlayer(new RealPlayer(new User("user1", new FakeConnectionHandler(1))));
        table.addPlayer(new RealPlayer(new User("user2", new FakeConnectionHandler(2))));
        table.turnOf().setMacroTurnType(MacroTurnType.NONE);

        EnumMap<Resource, Integer> resourceReq = new EnumMap<>(Resource.class);
        Map<DevCardType, Integer> devCardReq = new HashMap<>();
        EnumMap<Resource, Integer> input = new EnumMap<>(Resource.class);

        devCardReq.put(new DevCardType(0, Colour.YELLOW), 1);
        devCardReq.put(new DevCardType(0, Colour.GREEN), 1);
        input.put(Resource.SERVANT, 1);

        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),21));
        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),45));

        LeaderController controller = new LeaderController(new FaithTrackController(table));
        controller.actionOnLeaderCard(21,false);
        assertFalse(table.turnOf().getLeaderCards()[0].hasBeenPlayed());
    }

    @Test
    public void activateCardWithoutResourceRequirements(){
        Table table = new Table(2);
        table.addPlayer(new RealPlayer(new User("user1", new FakeConnectionHandler(1))));
        table.addPlayer(new RealPlayer(new User("user2", new FakeConnectionHandler(2))));
        table.turnOf().setMacroTurnType(MacroTurnType.NONE);

        EnumMap<Resource, Integer> resourceReq = new EnumMap<>(Resource.class);
        Map<DevCardType, Integer> devCardReq = new HashMap<>();
        EnumMap<Resource, Integer> input = new EnumMap<>(Resource.class);

        resourceReq.put(Resource.SHIELD, 1);
        resourceReq.put(Resource.COIN, 1);
        input.put(Resource.SERVANT, 1);

        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),21));
        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),45));

        LeaderController controller = new LeaderController(new FaithTrackController(table));
        controller.actionOnLeaderCard(21,false);
        assertFalse(table.turnOf().getLeaderCards()[0].hasBeenPlayed());
    }

    @Test
    public void activateCard(){
        Table table = new Table(2);
        table.addPlayer(new RealPlayer(new User("user1", new FakeConnectionHandler(1))));
        table.addPlayer(new RealPlayer(new User("user2", new FakeConnectionHandler(2))));
        table.turnOf().setMacroTurnType(MacroTurnType.NONE);
        table.turnOf().getDevSlots()[0].addCard(new DevCard(2, new EnumMap<>(Resource.class), new DevCardType(1, Colour.YELLOW), new ProductionPower(new EnumMap<>(Resource.class), new EnumMap<>(Resource.class)), 21));

        EnumMap<Resource, Integer> resourceReq = new EnumMap<>(Resource.class);
        Map<DevCardType, Integer> devCardReq = new HashMap<>();
        EnumMap<Resource, Integer> input = new EnumMap<>(Resource.class);

        input.put(Resource.SERVANT, 1);
        devCardReq.put(new DevCardType(0, Colour.YELLOW), 1);

        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),21));
        table.turnOf().addLeaderCard(new LeaderCard(2, resourceReq, devCardReq, LeaderCardType.DISCOUNT, input, new EnumMap<>(Resource.class),45));

        LeaderController controller = new LeaderController(new FaithTrackController(table));
        controller.actionOnLeaderCard(21, false);
        assertEquals(2, table.turnOf().getLeaderCards().length);
        assertTrue(table.turnOf().getLeaderCards()[0].hasBeenPlayed());
        assertFalse(table.turnOf().getLeaderCards()[1].hasBeenPlayed());

        //riattiva una carta già attivata
        controller.actionOnLeaderCard(21,false);
        assertTrue(table.turnOf().getLeaderCards()[0].hasBeenPlayed());
        assertFalse(table.turnOf().getLeaderCards()[1].hasBeenPlayed());
    }
}