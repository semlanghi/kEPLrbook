package server_distributed.RuntimeServer;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.module.ParseException;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPUndeployException;
import server_distributed.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class RuntimeServer {




    public static void main(String[] args){

        port(7890);

        SendingListener socketList = new SendingListener();

        RuntimeSocketHandler handler = new RuntimeSocketHandler(socketList);


        webSocket("/inputsocket", handler);

        webSocket("/outputsocket", socketList);

        post("/query", ((request, response) -> {

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

                try {
                    System.out.println("modifying the query file");
                    if(handler.getStmt()!=null)
                        handler.getStmt().removeAllListeners();
                    handler.getRuntime().getDeploymentService().undeployAll();
                    handler.getRuntime().getEventService().advanceTime(0);

                    Module mod = handler.getCompiler().readModule(file);
                    EPCompiled compiled = handler.getCompiler().compile(mod, handler.getArgs());

                    EPDeployment deployment = handler.getRuntime().getDeploymentService().deploy(compiled);

                    if(handler.getRuntime().getDeploymentService().getStatement(deployment.getDeploymentId(), "Prova")!=null){
                        handler.setStmt(handler.getRuntime().getDeploymentService().getStatement(deployment.getDeploymentId(), "Prova"));
                        handler.getStmt().addListener(socketList);

                    }

                    return "Query file created and compiled.";



                } catch (EPCompileException e) {
                    e.printStackTrace();
                    return "Problem with the compilation.";
                } catch (EPDeployException e) {
                    e.printStackTrace();
                    return "Problem with the deployment.";
                } catch (ParseException e) {
                    e.printStackTrace();
                    return "Problem with the parsing.";
                } catch (EPUndeployException e) {
                    e.printStackTrace();
                    return "Problem with the undeployment.";
                }




            }else{



                return "Problem with the creation of the file.";
            }


        }));



    }
}
