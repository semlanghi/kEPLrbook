package server_distributed.RuntimeServer;

import server_distributed.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static spark.Spark.*;

public class RuntimeServer {




    public static void main(String[] args){

        port(7890);

        SendingListener socketList = new SendingListener();

        RuntimeSocketHandler handler = new RuntimeSocketHandler(socketList);


        webSocket("/inputsocket", handler);

        webSocket("/outputsocket", socketList);

        post("/query", ((request, response) -> {






            //File file = new File("/Users/samuelelanghi/Documents/Polimi/anno_5/Tesi/kEPLrbook/kEPLrbook_servers/src/main/resources/server_Res/actualQuery/exp_query.epl");

            File file = new File(new Util().getClass().getClassLoader().getResource("server_Res/actualQuery/exp_query.epl").getFile());

            System.out.println(file.getAbsolutePath());
            System.out.println(Util.readStringFromFile(file));

            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }

            boolean created;
            if(!file.exists()){
                created = file.createNewFile();

            }else created = true;

            if(created){

                FileWriter fw = new FileWriter(file, false);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(request.body());
                bw.flush();
                bw.close();
                fw.close();
                return "Query file created.";

            }else{

                return "Problem with the creation of the file.";
            }




            /*response.header("Content-Type", "text/plain");
            response.body(request.session().attribute("user_id") + ": query file created\n"
                    + env.isThisTheMatrix());*/

            //return response.body();

        }));

    }
}
