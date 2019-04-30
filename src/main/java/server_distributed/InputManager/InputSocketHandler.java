package server_distributed.InputManager;


import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;

@WebSocket
public class InputSocketHandler {

    private static Session session;

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        this.session = session;

    }



    @OnWebSocketMessage
    public void onMessage(Session session, String message){
        System.out.println("received message " +message);
        if(message.equals("KO, no query file.")){
            session.close();
        }
        if(message.equals("keep-alive")){
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @OnWebSocketClose
    public void onClose(int statuscode, String reason){
        System.out.println("closed socket for "+reason);
    }

}
