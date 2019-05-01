package server_distributed;

import events_format.EventList;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.util.EventTypeBusModifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;

public class Util {



    public static String yamlToJson(String yml){

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            EventList list  = mapper.readValue(yml, EventList.class);
            String json = jsonMapper.writeValueAsString(list);

            return json;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Conversion Problem.";
    }

    public static String readStringFromFile(File file) throws IOException {

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        String line;

        while((line=in.readLine()) != null){

            builder.append(line);
            builder.append("\n");

        }

        builder.setLength(builder.length()-1);

        line = builder.toString();


        return line;

    }



    public static Configuration createConfiguration(){

        Configuration config = new Configuration();

        //config.getCompiler().getByteCode().setAllowSubscriber(true);
        config.getCompiler().getByteCode().setAccessModifiersPublic();
        config.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);
        return config;
    }
}
