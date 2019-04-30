package events_format;

import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TypeName("Listing")
public class EventList {

    @Id
    private String id;

    Map<Long, List<Map<String, Object>>> inputs;

    public EventList() {
        id="EventList";
        inputs = new HashMap<Long, List<Map<String, Object>>>();
    }

    public Map<Long, List<Map<String, Object>>> getInputs() {

        return inputs;
    }

    public void setInputs(Map<Long, List<Map<String, Object>>> inputs) {
        this.inputs = inputs;
    }

    public EventList(Map<Long, List<Map<String, Object>>> eventsMap) {

        this.inputs = eventsMap;
    }

    public void addEvent(Long time, Map<String, Object> ev){
        if(inputs.keySet().contains(time))
            inputs.get(time).add(ev);
        else {
            inputs.put(time, new ArrayList<>());
            inputs.get(time).add(ev);
        }
    }

    @Override
    public String toString() {
        return "EventList{" +
                "inputs=" + inputs +
                '}';
    }
}
