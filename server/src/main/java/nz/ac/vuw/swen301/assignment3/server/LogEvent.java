package nz.ac.vuw.swen301.assignment3.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LogEvent {

    public final String id;
    public final String message;
    public final String timeStamp;
    public final String thread;
    public final String logger;
    public final LogStorage.Level level;
    public final String errorDetails;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LogEvent(@JsonProperty("id") String id,@JsonProperty("message") String message,@JsonProperty("timeStamp") String timeStamp,@JsonProperty("thread") String thread,
                    @JsonProperty("logger")String logger,@JsonProperty("level") String level,@JsonProperty("errorDetails") String errorDetails) {
        this.id = id;
        this.message = message;
        this.timeStamp = timeStamp;
        this.thread = thread;
        this.logger = logger;
        this.level = LogStorage.Level.valueOf(level);
        this.errorDetails=errorDetails;
    }

    public LogEvent(@JsonProperty("id") String id,@JsonProperty("message") String message,@JsonProperty("timeStamp") String timeStamp,@JsonProperty("thread") String thread,
                    @JsonProperty("logger")String logger,@JsonProperty("level") String level) {
        this.id = id;
        this.message = message;
        this.timeStamp = timeStamp;
        this.thread = thread;
        this.logger = logger;
        this.level = LogStorage.Level.valueOf(level);
        this.errorDetails="N/A";
    }

}
