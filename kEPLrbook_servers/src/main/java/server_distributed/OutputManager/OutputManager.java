package server_distributed.OutputManager;

import events_format.EventList;
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
import java.util.*;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;
import static spark.Spark.*;

public class OutputManager {

    public static void main(String[] args){

        WebSocketClient socketClient = new WebSocketClient();
        try {
            socketClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        socketClient.setMaxIdleTimeout(1000000);
        socketClient.setStopAtShutdown(true);

        try {
            port(1234);

            Future<Session> sessionFuture = socketClient.connect(new OutputSocketHandler(), new URI("ws://runtime:7890/outputsocket"), new ClientUpgradeRequest());

            staticFiles.location("/expected");

            get("/final_output", (((request, response) -> {

                File expected = new File("/expected/output.yml");
                File actual = new File("/actual/output.yml");
                File finalOutput = new File("/actual/comparison.txt");


                /*File expected = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "expected"+"/output.yml");
                File actual = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "actual"+"/output.yml");

                File finalOutput = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "actual"+"/comparison.txt");*/
                response.type("text/plain");
                if(Util.readStringFromFile(expected).equalsIgnoreCase("gotcha")){

                    return Util.readStringFromFile(actual);
                }else{

                    return Util.readStringFromFile(finalOutput);
                }

            })));


            get("/output", (req, res)->{

                int i=0;
                for(i = 0; i<5 && !sessionFuture.isDone(); i++){
                    System.out.println("Still not connected, recheck.");
                    sleep(1000);
                }

                if(i==5 && !sessionFuture.isDone()){
                    System.out.println("Tried 5 times, connection, not established, Exiting.");
                    sessionFuture.cancel(true);
                    return "Connection not established.";
                }





                /*File expected = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "expected"+"/output.yml");*/

                File expected = new File("/expected/output.yml");
                File actual = new File("/actual/output.yml");
                File finalOutput = new File("server_Res/actual/comparison.txt");

                /*File actual = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "actual"+"/output.yml");

                File finalOutput = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "actual"+"/comparison.txt");*/

                if(!expected.exists()||Util.readStringFromFile(expected).equalsIgnoreCase("gotcha")){
                    return Util.readStringFromFile(actual);
                }else{
                    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());


                    EventList listActual  = mapper.readValue(Util.readStringFromFile(actual), EventList.class);
                    EventList listExpected  = mapper.readValue(Util.readStringFromFile(expected), EventList.class);




                    StringBuilder builder = new StringBuilder();
                    Set<Long> timings1 = new HashSet<Long>(listExpected.getInputs().keySet());
                    timings1.addAll(listActual.getInputs().keySet());
                    ArrayList<Long> timings = new ArrayList<Long>(timings1);
                    Collections.sort(timings);
                    builder.append("Start Comparison:\n");

                    for(Long t : timings){
                        builder.append("Time "+t+":\n");

                        if(listExpected.getInputs().keySet().contains(t) && listActual.getInputs().keySet().contains(t)){

                            for(int j=0; j<Math.max(listExpected.getInputs().get(t).size(), listActual.getInputs().get(t).size()); j++){
                                builder.append(" - "+(j+1)+"th events: \n");

                                Map<String, Object> expectedEvent = new HashMap<>();
                                Map<String, Object> actualEvent = new HashMap<>();

                                if(j>listActual.getInputs().get(t).size()-1){
                                    builder.append("  - No actual event\n");
                                }else{
                                    actualEvent= listActual.getInputs().get(t).get(j);
                                    builder.append("  - Actual event : \n    "+actualEvent+"\n");
                                }
                                if(j>listExpected.getInputs().get(t).size()-1){
                                    builder.append("  - No expected event\n");
                                }else{
                                    expectedEvent= listExpected.getInputs().get(t).get(j);
                                    builder.append("  - Expected event: \n    "+expectedEvent+"\n");
                                }
                                if(j<=listExpected.getInputs().get(t).size()-1 && j<=listActual.getInputs().get(t).size()-1){

                                    /*Set<String> unionProp = new HashSet<String>(expectedEvent.keySet());
                                    unionProp.addAll(actualEvent.keySet());

                                    for(String prop : unionProp){

                                        if(!actualEvent.containsKey(prop)||!expectedEvent.containsKey(prop)){
                                            if(!actualEvent.containsKey(prop)){
                                                builder.append("\\s\\s\\s- Property "+prop+" not contained in the actual event\n");
                                            }
                                            if(!expectedEvent.containsKey(prop)){
                                                builder.append("\\s\\s\\s- Property "+prop+" not contained in the expected event\n");
                                            }
                                        }

                                    }*/

                                    if(!expectedEvent.equals(actualEvent))
                                        builder.append("  - Events are different\n");
                                    else builder.append("  - Events are equal\n");



                                }
                            }

                        }else {
                            if(!listExpected.getInputs().keySet().contains(t)){
                                builder.append("In the expected result time "+t + " is not present.\n");
                            }
                            if(!listActual.getInputs().keySet().contains(t)){
                                builder.append("In the actual result time "+t + " is not present.\n");
                            }
                        }

                        builder.append("******************************\n");

                    }

                    FileWriter writer = new FileWriter(finalOutput, false);
                    BufferedWriter buff = new BufferedWriter(writer);

                    buff.write(builder.toString());
                    buff.flush();

                    return builder.toString();



                }

            });

            post("/output", (req, res)->{

                /*File file = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                        "kEPLr_test/src/main/resources/server_Res/" +
                        "expected"+"/output.yml");*/

                File file = new File("/expected/output.yml");


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
                    return "Created expected output file.";
                }




                return "Input file not created.";

            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
