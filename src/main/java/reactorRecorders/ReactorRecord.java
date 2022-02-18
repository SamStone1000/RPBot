package reactorRecorders;

import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Message;
import recorders.Recorder;

public interface ReactorRecord extends Recorder, Consumer<Message> {

}
