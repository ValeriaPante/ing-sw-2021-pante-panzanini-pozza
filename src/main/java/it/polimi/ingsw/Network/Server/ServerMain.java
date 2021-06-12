package it.polimi.ingsw.Network.Server;

import it.polimi.ingsw.Network.RequestHandlers.PreGameRequestHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {

    public static void main(String[] args){

        ExecutorService executorService = Executors.newCachedThreadPool();

        PreGameRequestHandler controller = new PreGameRequestHandler(executorService);

        Server server = new Server(42000, controller, executorService);
        server.start();

    }

}
