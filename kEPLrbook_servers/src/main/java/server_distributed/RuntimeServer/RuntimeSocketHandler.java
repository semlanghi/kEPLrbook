package server_distributed.RuntimeServer;

import events_format.TimestampedEvent;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.module.ParseException;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server_distributed.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebSocket
public class RuntimeSocketHandler {

    private Session session;



    private File queryFile;

    private EPRuntime runtime;

    private EPStatement stmt;

    private EPCompiler compiler;

    private long lastMod;

    private CompilerArguments args = new CompilerArguments();

    private SendingListener socketList;

    private EPDeployment lastDeployment;

    private EPCompiled compiled;

    public EPCompiled getCompiled() {
        return compiled;
    }

    public EPCompiler getCompiler() {
        return compiler;
    }

    public EPRuntime getRuntime() {
        return runtime;
    }

    public EPStatement getStmt() {
        return stmt;
    }

    public SendingListener getSocketList() {
        return socketList;
    }

    public RuntimeSocketHandler(SendingListener listener){

        Configuration config = Util.createConfiguration();
        //config.getCommon().addEventType(NbaGame.class);


        runtime = EPRuntimeProvider.getDefaultRuntime(config);
        runtime.getEventService().clockExternal();
        runtime.getEventService().advanceTime(0);
        config.getCompiler().getByteCode().setIncludeDebugSymbols(true);



        args.setConfiguration(config);
        compiler = EPCompilerProvider.getCompiler();


        /*queryFile = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/" +
                "kEPLr_test/src/main/resources/server_Res/" +
                "actualQuery" +"/exp_query.epl");*/
        queryFile = new File(Util.class.getClassLoader().getResource("server_Res/actualQuery/exp_query.epl").getFile());

        lastMod= -1;
        socketList = listener;



    }

    @OnWebSocketConnect
    public void connected(Session session) throws IOException {

        //Session created how can i differentiate between one session and the other??

        System.out.println("Connected session.");

        this.session = session;

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

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        System.out.println("closed session");

    }


    public CompilerArguments getArgs() {
        return args;
    }

    public void setStmt(EPStatement stmt) {

        this.stmt = stmt;
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {

        // take the event in input in json format and automatically forward it to the
        // other session


        System.out.println("received "+message);

        if(message.equals("keep-alive")){
            return;
        }

        if(message.equals("finish")){

            socketList.getSession().getRemote().sendString(message);
            return;
        }

        if(!queryFile.exists()){
            session.getRemote().sendString("KO, no query file.");
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        TimestampedEvent eventTimestamped = mapper.readValue(message, TimestampedEvent.class);

        //System.out.println(eventTimestamped);
        Long i = new Long(eventTimestamped.getTs());


        for(long t = runtime.getEventService().getCurrentTime(); t<=i; t++){
            runtime.getEventService().advanceTime(t);
        }


        //eventTimestamped.setEventType("Cacca");


        Map<String, Object> map = new HashMap<String, Object>(eventTimestamped.getEvent());


        String eventType = (String)map.get("type");
        //System.out.println(map.getClass());
        runtime.getEventService().sendEventMap(map, eventType);



    }


}
