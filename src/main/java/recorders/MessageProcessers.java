package recorders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Message;
import reactorRecorders.KarmaCounter;
import reactorRecorders.ReactorRecord;
import util.SharedConstants;

public class MessageProcessers implements Consumer<Message> {

	private Map<String, Recorder> counters;
	private List<Consumer<Message>> reactors;
	private Map<String, ReactorRecord> reactorRecords;
	public KarmaCounter karmaCounter;

	public MessageProcessers() {
		new File(SharedConstants.COUNTER_FOLDER).mkdirs();
		new File(SharedConstants.REACT_RECORD_FOLDER).mkdirs();
		counters = new TreeMap<>();
		reactors = new ArrayList<>();
		reactorRecords = new TreeMap<>();
	}

	public void addCounter(String term, Recorder counter) {
		counters.put(term, counter);
	}

	public void addReactor(Consumer<Message> reactor) {
		reactors.add(reactor);
	}

	public void addReactorRecord(String identifier, ReactorRecord reactorRecord) {
		reactorRecords.put(identifier, reactorRecord);
	}
	public void setKarmaCounter(KarmaCounter karmaCounter) {
		this.karmaCounter = karmaCounter;
	}
	@Override
	public void accept(Message message) {
		String content = message.getContentRaw().toLowerCase();
		Long id = message.getAuthor().getIdLong();
		//System.out.println(id);
		for (Consumer<Message> reactor : reactors) {
			reactor.accept(message);
		}

		for (Consumer<Message> reactorRecord : reactorRecords.values()) {
			reactorRecord.accept(message);
		}
		karmaCounter.accept(message);
		for (BiPredicate<String, Long> counter : counters.values()) {
			counter.test(content, id);
		}
	}
	
	public void accept(String content, long id) {
		for (BiPredicate<String, Long> counter : counters.values()) {
			counter.test(content, id);
		}
		karmaCounter.test(content, id);
	}

	public int getCount(String str, long id) {
		Recorder counter = counters.get(str);
		if (counter != null) {
			return counter.getCount(id);
		} else {
			ReactorRecord reactorCounter = reactorRecords.get(str);
			if (reactorCounter != null) {
				return reactorCounter.getCount(id);
			} else return -2;
		}
	}

}
