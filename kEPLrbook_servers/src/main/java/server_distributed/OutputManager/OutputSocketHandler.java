package server_distributed.OutputManager;

import events_format.EventList;
import events_format.TimestampedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server_distributed.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@WebSocket
public class OutputSocketHandler {

    private static Session session;

    private File actual;

    private EventList list;

    private FileWriter fw;

    private BufferedWriter bw;

    public OutputSocketHandler(){

        /*actual = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                "kEPLr_test/src/main/resources/server_Res/" +
                "actual"+"/output.yml");*/
        actual = new File("/actual/output.yml");


        if(!actual.getParentFile().exists()){
            actual.getParentFile().mkdirs();
        }
        list= new EventList();


    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        this.session = session;

    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message){

        System.out.println(message);

        if(message.equals("keep-alive")){
            try {
                session.getRemote().sendString(message);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        if(message.equals("finish")){
            try {
                try {
                    fw = new FileWriter(actual, false);
                    bw = new BufferedWriter(fw);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("sto scrivendo");
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                String actualOut = mapper.writeValueAsString(list);
                System.out.println(actualOut);
                bw.write(actualOut);
                bw.flush();
                bw.close();
                fw.close();
                list = new EventList();

                return;

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            System.out.println("adding event to actual output");
            TimestampedEvent eventArrived =  mapper.readValue(message, TimestampedEvent.class);
            Long i = new Long(eventArrived.getTs());
            list.addEvent(i, eventArrived.getEvent());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OnWebSocketClose
    public void onClose(int statuscode, String reason){
        System.out.println("closed socket for "+reason);
    }
}
