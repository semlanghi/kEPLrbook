package events_format;

import java.util.Map;

public class TimestampedEvent {

    private Long ts;
    private Map<String, Object> event;
    private String eventType;

    public TimestampedEvent(){
    }

    public TimestampedEvent(Long ts, Map<String, Object> event) {
        this.ts = ts;
        this.event = event;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Map<String, Object> getEvent() {
        return event;
    }

    public void setEvent(Map<String, Object> event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "TimestampedEvent{" +
                "ts=" + ts +
                ", event=" + event +
                '}';
    }
}
