package server_distributed.RuntimeServer;

import events_format.TimestampedEvent;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebSocket
public class SendingListener implements UpdateListener {


    private Session session;
    private ObjectMapper mapper;



    public SendingListener(){
        this.mapper = new ObjectMapper();
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason){
        System.out.println("session closed for "+reason);
    }


    public Session getSession() {
        return session;
    }

    @OnWebSocketConnect
    public void connect(Session session){
        System.out.println("connected.");
        this.session=session;

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                /*try {
                    outputSession.getRemote().sendString("keep-alive");
                } catch (IOException ex) {

                    System.out.println("Websocket Error " + ex.getMessage());
                }*/

                try {
                    session.getRemote().sendString("keep-alive");
                } catch (IOException ex) {

                    System.out.println("Websocket Error " + ex.getMessage());
                }

            }
        }, 15, 15, TimeUnit.SECONDS);
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {

        if(newEvents!=null){
            System.out.println("arrived event " + newEvents[0].getUnderlying().getClass().getName()+
                    "\n");
            Pair<Long,EventBean[]> arrivedEvents= new Pair<>(runtime.getEventService().getCurrentTime(), newEvents);


            for(EventBean e : arrivedEvents.getValue()){
                System.out.println("sending "+e.getUnderlying()+"\n");
                try {

                    if(e.getUnderlying() instanceof com.espertech.esper.common.internal.collection.Pair){

                        System.out.println("its a pair");
                        Map<String, Object> event = new HashMap<String, Object>();

                        com.espertech.esper.common.internal.collection.Pair actualEvent = (com.espertech.esper.common.internal.collection.Pair) e.getUnderlying();

                        event.putAll((Map<String, Object>)actualEvent.getFirst());
                        event.putAll((Map<String, Object>)actualEvent.getSecond());

                        session.getRemote().sendString(mapper.writeValueAsString(new TimestampedEvent(arrivedEvents.getKey(), event)));

                    }else session.getRemote().sendString(mapper.writeValueAsString(new TimestampedEvent(arrivedEvents.getKey(), (Map<String, Object>)e.getUnderlying())));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }




}
