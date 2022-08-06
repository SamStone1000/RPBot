package stone.rpbot.recorders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Message;
import stone.rpbot.reactorRecorders.KarmaCounter;
import stone.rpbot.reactorRecorders.ReactorRecord;
import stone.rpbot.util.MutableInteger;
import stone.rpbot.util.SharedConstants;

public class MessageProcessers implements Consumer<Message> {

	private Map<String, Recorder> counters;
	private List<Consumer<Message>> reactors;
	private Map<String, ReactorRecord> reactorRecords;
	public StatCounter statCounter;
	public KarmaCounter karmaCounter;

	public MessageProcessers() {
		new File(SharedConstants.COUNTER_FOLDER).mkdirs();
		new File(SharedConstants.REACT_RECORD_FOLDER).mkdirs();
		counters = new TreeMap<>();
		reactors = new ArrayList<>();
		reactorRecords = new TreeMap<>();
	}

	public void addCounter(String term, Recorder counter) { counters.put(term, counter); }

	public void addReactor(Consumer<Message> reactor) { reactors.add(reactor); }

	public void addReactorRecord(String identifier, ReactorRecord reactorRecord) {
		reactorRecords.put(identifier, reactorRecord);
	}

	public void setKarmaCounter(KarmaCounter karmaCounter) { this.karmaCounter = karmaCounter; }

	@Override
	public void accept(Message message) {
		String content = message.getContentRaw().toLowerCase();
		Long id = message.getAuthor().getIdLong();
		for (Consumer<Message> reactor : reactors)
		{ reactor.accept(message); }

		for (Consumer<Message> reactorRecord : reactorRecords.values())
		{ reactorRecord.accept(message); }
		if (karmaCounter != null)
			karmaCounter.accept(message);
		if (statCounter != null)
			statCounter.accept(message);
		for (BiPredicate<String, Long> counter : counters.values())
		{ counter.test(content, id); }
	}

	public void accept(String content, long id) {
		for (BiPredicate<String, Long> counter : counters.values())
		{ counter.test(content, id); }
		for (ReactorRecord reactor : reactorRecords.values())
		{ reactor.test(content, id); }
		if (karmaCounter != null)
			karmaCounter.findKarma(content, id);
	}

	public int getCount(String str, long id) {
		Recorder counter = counters.get(str);
		if (counter != null)
		{
			return counter.getCount(id);
		} else
		{
			ReactorRecord reactorCounter = reactorRecords.get(str);
			if (reactorCounter != null)
			{
				return reactorCounter.getCount(id);
			} else
				return -2;
		}
	}

	public void saveAll() {
		for (Recorder recorder : counters.values())
		{ recorder.save(); }

		for (Recorder reactor : reactorRecords.values())
		{ reactor.save(); }
		karmaCounter.save();
	}

	public void transferCounts(MessageProcessers processer) {
		for (Entry<String, Recorder> counter : counters.entrySet())
		{ counter.getValue().transfer(processer.getCounter(counter.getKey())); }

		for (Entry<String, ReactorRecord> recorder : reactorRecords.entrySet())
		{ recorder.getValue().transfer(processer.getReactorRecord(recorder.getKey())); }
		karmaCounter.transfer(processer.karmaCounter);
	}

	private Recorder getReactorRecord(String key) { return reactorRecords.get(key); }

	private Recorder getCounter(String key) { return counters.get(key); }

	@Override
	public String toString() {
		String output = "";
		for (Entry<String, Recorder> recorder : counters.entrySet())
		{
			output += recorder.getKey() + "\n";
			output += recorder.getValue().toString() + "\n";
		}
		for (Entry<String, ReactorRecord> recorder : reactorRecords.entrySet())
		{
			output += recorder.getKey() + "\n";
			output += recorder.getValue().toString() + "\n";
		}
		output += karmaCounter.toString();
		return output;
	}

	public MessageProcessers copyOf(boolean shouldResetCounts, boolean shouldAffect) {
		MessageProcessers output = new MessageProcessers();
		for (Entry<String, Recorder> recorder : counters.entrySet())
		{ output.addCounter(recorder.getKey(), recorder.getValue().copyOf(shouldResetCounts, shouldAffect)); }
		for (Entry<String, ReactorRecord> recorder : reactorRecords.entrySet())
		{ output.addReactorRecord(recorder.getKey(), recorder.getValue().copyOf(shouldResetCounts, shouldAffect)); }
		output.setKarmaCounter(karmaCounter.copyOf(shouldResetCounts, shouldAffect));
		return output;
	}

}
