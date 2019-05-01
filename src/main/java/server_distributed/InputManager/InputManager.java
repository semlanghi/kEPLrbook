package server_distributed.InputManager;

import events_format.EventList;
import events_format.TimestampedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import server_distributed.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

import static spark.Spark.post;

public class InputManager {

    public static void main(String[] args){

        WebSocketClient socketClient = new WebSocketClient();
        try {
            socketClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        socketClient.setStopAtShutdown(true);







        try {

            Future<Session> sessionFuture = socketClient.connect(new InputSocketHandler(), new URI("ws://localhost:7890/inputsocket"), new ClientUpgradeRequest());

            post("/input", (req, res)->{

                ClassLoader loader = Util.class.getClassLoader();

                /*File file = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "actual" +"/input.yml");*/

                File file = new File(loader.getResource("server_Res/actual/input.yml").getFile());


                if(!file.getParentFile().exists())
                    file.getParentFile().mkdirs();

                boolean fcreated;
                if(!file.exists()){
                    fcreated= file.createNewFile();
                }else fcreated = true;

                if(fcreated){
                    FileWriter fw = new FileWriter(file.getAbsolutePath(), false);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(req.body());
                    bw.flush();
                    bw.close();
                    fw.close();

                }

                TimestampedEvent eventToSend;
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

                EventList list  = mapper.readValue(req.body(), EventList.class);

                /*YAMLFactory factory = new YAMLFactory();

                YAMLParser parser = factory.createParser(req.body());
                */
                ArrayList<Long> ts = new ArrayList<Long>(list.getInputs().keySet());
                Collections.sort(ts);

                mapper = new ObjectMapper();

                for(Long t : ts){
                    for(Map<String, Object> e : list.getInputs().get(t)){

                        eventToSend = new TimestampedEvent(t, e);

                        sessionFuture.get().getRemote().sendString(mapper.writeValueAsString(eventToSend));

                    }

                }

                sessionFuture.get().getRemote().sendString("finish");

                return "Input file created.";

            });
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: unable to connect to the runtime.");
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

}
