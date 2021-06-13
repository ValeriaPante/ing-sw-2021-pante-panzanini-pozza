package it.polimi.ingsw.View.GUI;

import it.polimi.ingsw.Enums.Resource;
import it.polimi.ingsw.Messages.InGameMessages.ConcreteMessages.AnySelectionMessage;
import it.polimi.ingsw.Network.Client.MessageToServerCreator;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

import java.io.IOException;

public class ChooseOutputScene extends Initializable{
    private int chosen;
    private int toChoose;

    public ChooseOutputScene(){
        try {
            root = FXMLLoader.load(getClass().getResource("/Scenes/chooseOutputScene.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialise() {
        toChoose = observer.getModel().getPlayerFromId(observer.getModel().getLocalPlayerId()).getSupportContainer().get(Resource.ANY);

        updateCounter();

        root.lookup("#coin").setOnMouseClicked(mouseEvent -> {
            sendMessage(new AnySelectionMessage(Resource.COIN));
            chosen++;
            updateCounter();
            if(chosen == toChoose) disableClick();
        });

        root.lookup("#shield").setOnMouseClicked(mouseEvent -> {
            sendMessage(new AnySelectionMessage(Resource.SHIELD));
            chosen++;
            updateCounter();
            if(chosen == toChoose) disableClick();
        });

        root.lookup("#stone").setOnMouseClicked(mouseEvent -> {
            sendMessage(new AnySelectionMessage(Resource.STONE));
            chosen++;
            updateCounter();
            if(chosen == toChoose) disableClick();
        });

        root.lookup("#servant").setOnMouseClicked(mouseEvent -> {
            sendMessage(new AnySelectionMessage(Resource.SERVANT));
            chosen++;
            updateCounter();
            if(chosen == toChoose) disableClick();
        });
    }

    private void updateCounter(){
        Label counter = (Label) root.lookup("#counter");
        counter.setText(chosen+"/"+toChoose);
    }

    private void disableClick(){
        root.lookup("#coin").setDisable(true);
        root.lookup("#shield").setDisable(true);
        root.lookup("#stone").setDisable(true);
        root.lookup("#servant").setDisable(true);
        observer.getCurrentState().next();
    }
}
