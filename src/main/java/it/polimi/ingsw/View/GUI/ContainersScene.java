package it.polimi.ingsw.View.GUI;

import it.polimi.ingsw.Enums.Resource;
import it.polimi.ingsw.Network.Client.MessageToServerCreator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ContainersScene extends Initializable{

    public ContainersScene(){
        try {
            root = FXMLLoader.load(getClass().getResource("/Scenes/containersScene.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialise(){
        root.lookup("#back").setOnMouseClicked(mouseEvent -> Transition.hideDialog());


        // drag and drop from and to support container
        //--------------------------------------------------------------------------------------------------------
        HashMap<Resource, Integer> supportContainer = observer.getModel().getPlayerFromId(observer.getModel().getLocalPlayerId()).getSupportContainer();

        Label coinCount = (Label) root.lookup("#coin");
        int count = supportContainer.getOrDefault(Resource.COIN, 0);
        coinCount.setText(String.valueOf(count));
        if(count > 0){
            Node coin = root.lookup("#coinImage");
            coin.setOnDragDetected(dragEvent -> {
                Dragboard db = ((Node) dragEvent.getSource()).startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("COIN");
                db.setContent(content);
                dragEvent.consume();
            });
            coin.setOnDragEntered(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerSelectionMessage(1, Resource.COIN)));
            coin.setOnDragExited(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerDeselectionMessage(1, Resource.COIN)));
        }

        Label shieldCount = (Label) root.lookup("#shield");
        count = supportContainer.getOrDefault(Resource.SHIELD, 0);
        shieldCount.setText(String.valueOf(count));
        if(count > 0) {
            Node shield = root.lookup("#shieldImage");
            shield.setOnDragDetected(dragEvent -> {
                Dragboard db = ((Node) dragEvent.getSource()).startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("SHIELD");
                db.setContent(content);
                dragEvent.consume();
            });
            shield.setOnDragEntered(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerSelectionMessage(1, Resource.SHIELD)));
            shield.setOnDragExited(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerDeselectionMessage(1, Resource.SHIELD)));
        }

        Label stoneCount = (Label) root.lookup("#stone");
        count = supportContainer.getOrDefault(Resource.STONE,0);
        stoneCount.setText(String.valueOf(count));
        if(count > 0) {
            Node stone = root.lookup("#stoneImage");
            stone.setOnDragDetected(dragEvent -> {
                Dragboard db = ((Node) dragEvent.getSource()).startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("STONE");
                db.setContent(content);
                dragEvent.consume();
            });
            stone.setOnDragEntered(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerSelectionMessage(1, Resource.STONE)));
            stone.setOnDragExited(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerDeselectionMessage(1, Resource.STONE)));
        }

        Label servantCount = (Label) root.lookup("#servant");
        count = supportContainer.getOrDefault(Resource.STONE, 0);
        servantCount.setText(String.valueOf(count));
        if(count > 0) {
            Node servant = root.lookup("#servantImage");
            servant.setOnDragDetected(dragEvent -> {
                Dragboard db = ((Node) dragEvent.getSource()).startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("SERVANT");
                db.setContent(content);
                dragEvent.consume();
            });
            servant.setOnDragEntered(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerSelectionMessage(1, Resource.SERVANT)));
            servant.setOnDragExited(dragEvent -> sendMessageToServer(MessageToServerCreator.createSupportContainerDeselectionMessage(1, Resource.SERVANT)));
        }


        Node container = root.lookup("#supportContainer");
        container.setOnDragOver(dragEvent -> {
            if (dragEvent.getGestureSource() != container && dragEvent.getDragboard().hasString()) {
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            dragEvent.consume();
        });
        container.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                sendMessageToServer(MessageToServerCreator.createMoveToSupportContainerMessage());
                success = true;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });
        //--------------------------------------------------------------------------------------------------------

        //drag and drop from e to shelves
        //--------------------------------------------------------------------------------------------------------
        for(int j = 0; j < 3; j++){
            HashMap<Resource, Integer> shelf = observer.getModel().getPlayerFromId(observer.getModel().getLocalPlayerId()).getShelf(j);
            if(!shelf.isEmpty()){
                for(Map.Entry<Resource, Integer> entry: shelf.entrySet()){
                    for(int i = 0; i < entry.getValue(); i++){
                        InputStream in = Transition.class.getResourceAsStream("/accessible/assets/imgs/" +entry.getKey().toString().toLowerCase()+".png");
                        ImageView image = new ImageView();
                        image.setImage(new Image(in));
                        image.setFitWidth(50);
                        image.setPreserveRatio(true);
                        image.setId(String.valueOf(i+1));
                        image.setOnDragDetected(dragEvent -> {
                            Dragboard db = ((Node) dragEvent.getSource()).startDragAndDrop(TransferMode.ANY);
                            ClipboardContent content = new ClipboardContent();
                            content.putString(entry.getKey().toString());
                            db.setContent(content);
                            dragEvent.consume();
                        });
                        image.setOnDragEntered(dragEvent -> {
                            int numberOfShelf = Integer.parseInt(((Node) dragEvent.getSource()).getId());
                            sendMessageToServer(MessageToServerCreator.createShelfSelectionMessage(numberOfShelf, entry.getKey()));
                        });
                        image.setOnDragExited(dragEvent -> {
                            int numberOfShelf = Integer.parseInt(((Node) dragEvent.getSource()).getId());
                            sendMessageToServer(MessageToServerCreator.createSupportContainerDeselectionMessage(numberOfShelf, entry.getKey()));
                        });

                        ((AnchorPane) root.lookup("#shelf"+ (j + 1) + (i + 1))).getChildren().add(image);
                    }
                }
            }
        }

        for(int i = 1; i < 3; i++){
            for(int j = 1; j < i + 1; j++){
                AnchorPane shelf = (AnchorPane) root.lookup("#shelf"+ (i) + (j));
                shelf.setId(String.valueOf(i));
                shelf.setOnDragOver(dragEvent -> {
                    if (dragEvent.getGestureSource() != container && dragEvent.getDragboard().hasString()) {
                        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }

                    dragEvent.consume();
                });
                shelf.setOnDragDropped(dragEvent -> {
                    Dragboard db = dragEvent.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        AnchorPane source = (AnchorPane) dragEvent.getSource();
                        if(source.getChildren().size() == 0) sendMessageToServer(MessageToServerCreator.createExchangeMessage());
                        else sendMessageToServer(MessageToServerCreator.createMoveToShelfMessage(Integer.parseInt(source.getId())));
                        success = true;
                    }
                    dragEvent.setDropCompleted(success);
                    dragEvent.consume();
                });
            }
        }
        //--------------------------------------------------------------------------------------------------------

        //drag and drop from and to leader cards
        //--------------------------------------------------------------------------------------------------------
        HashMap<Integer, Resource[]> lc = observer.getModel().getPlayerFromId(observer.getModel().getLocalPlayerId()).getAllLeaderStorages();
        int availableSpace = 1;
        for (Map.Entry<Integer, Resource[]> storage: lc.entrySet()) {
            AnchorPane leaderCard = (AnchorPane) root.lookup("#lc"+availableSpace);
            InputStream in = Transition.class.getResourceAsStream("/accessible/assets/imgs/" +storage.getKey()+".png");
            ImageView image = new ImageView();
            image.setImage(new Image(in));
            image.setFitWidth(200);
            image.setPreserveRatio(true);
            leaderCard.getChildren().add(image);
            Resource[] resources = storage.getValue();
            for(int i = 1; i < 3; i++){
                AnchorPane space = (AnchorPane) root.lookup("#lc"+ (availableSpace) +(i));
                space.setOnDragOver(dragEvent -> {
                    if (dragEvent.getGestureSource() != container && dragEvent.getDragboard().hasString()) {
                        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }

                    dragEvent.consume();
                });
                space.setOnDragDropped(dragEvent -> {
                    Dragboard db = dragEvent.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        AnchorPane source = (AnchorPane) dragEvent.getSource();
                        if(source.getChildren().size() == 0) sendMessageToServer(MessageToServerCreator.createExchangeMessage());
                        else sendMessageToServer(MessageToServerCreator.createMoveToShelfMessage(Integer.parseInt(source.getId())));
                        success = true;
                    }
                    dragEvent.setDropCompleted(success);
                    dragEvent.consume();
                });
                if(resources != null){
                    for(int j = 0; j < resources.length; j++){
                        InputStream input = Transition.class.getResourceAsStream("/accessible/assets/imgs/" +resources[j].toString().toLowerCase()+".png");
                        ImageView resourceImage = new ImageView();
                        resourceImage.setImage(new Image(input));
                        resourceImage.setFitWidth(60);
                        resourceImage.setPreserveRatio(true);
                        resourceImage.setOnDragDetected(dragEvent -> {
                            Dragboard db = ((Node) dragEvent.getSource()).startDragAndDrop(TransferMode.ANY);
                            ClipboardContent content = new ClipboardContent();
                            content.putString("");
                            db.setContent(content);
                            dragEvent.consume();
                        });
                        int numberOfCard = storage.getKey();
                        int position = j;
                        Resource resourceType = resources[j];
                        resourceImage.setOnDragEntered(dragEvent -> sendMessageToServer(MessageToServerCreator.createLeaderStorageSelectionMessage(resourceType,position, numberOfCard)));
                        resourceImage.setOnDragExited(dragEvent -> sendMessageToServer(MessageToServerCreator.createLeaderStorageSelectionMessage(resourceType,position, numberOfCard)));
                        space.getChildren().add(resourceImage);
                    }
                }
            }
            availableSpace++;

            Transition.setOnContainersScene(true);

            root.lookup("#back").setOnMouseClicked(mouseEvent -> {
                Transition.setOnContainersScene(false);
                Transition.hideDialog();
            });
        }



        //--------------------------------------------------------------------------------------------------------

    }


}
