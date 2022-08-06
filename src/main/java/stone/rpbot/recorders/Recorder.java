package stone.rpbot.recorders;

import java.util.Map;
import java.util.NavigableMap;
import java.util.function.BiPredicate;

import stone.rpbot.reactorRecorders.ReactorRecord;
import stone.rpbot.util.MutableInteger;

public interface Recorder extends BiPredicate<String, Long> {

	public void save();

	public int getCount(long id);

	// public void setCounts(Map<Long, MutableInteger> map);
	public void transfer(Recorder recorder);

	public Map<Long, MutableInteger> getCounts();

	Recorder copyOf(boolean shouldResetCounts, boolean shouldAffect);
}
