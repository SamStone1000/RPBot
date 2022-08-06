package stone.rpbot.recorders;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import stone.rpbot.util.Helper;
import stone.rpbot.util.MutableInteger;
import stone.rpbot.util.SharedConstants;

/**
 * Counts the number of occurences of a string in the given message and adds it
 * to the record
 *
 * @author SamSt
 *
 */
public class Counter implements Recorder {

	/**
	 *
	 */
	protected Map<Long, MutableInteger> counts;
	/**
	 * What the class is actually tracking, should find Strings similar to term
	 */
	protected Pattern pattern;

	protected File file;
	protected boolean shouldAffect;

	public Counter(
			Pattern pattern, Map<Long, MutableInteger> counts, File file, boolean shouldAffect,
			boolean shouldResetCounts
	) {
		this.pattern = pattern;
		if (shouldResetCounts)
		{
			this.counts = new TreeMap<Long, MutableInteger>();
		} else
		{
			this.counts = counts;
		}
		this.file = file;
		this.shouldAffect = shouldAffect;
	}

	public Counter(String term, Pattern pattern) {
		this.pattern = pattern;
		this.shouldAffect = true;
		file = new File(SharedConstants.COUNTER_FOLDER + term + ".txt");
		if (shouldAffect)
			this.counts = Helper.readMap(file);
		else
			this.counts = new TreeMap<Long, MutableInteger>();
	}

	public Counter(String term, Pattern pattern, boolean shouldAffect) {
		this(term, pattern);
		this.shouldAffect = shouldAffect;
	}

	@Override
	public boolean test(String content, Long id) {
		int count = this.findMatches(content);
		if (count > 0)
		{
			if (id == 456226577798135808l)
			{ id = 827724526313537536l; }
			MutableInteger oldCount = counts.get(id);
			if (oldCount != null)
			{
				oldCount.add(count);
			} else
			{
				counts.put(id, new MutableInteger(count));
			}
			if (shouldAffect)
				Helper.writeMap(counts, file);
			// LoggerFactory.getLogger("test").debug(counts.get(id).toString());
			return true;
		}
		return false;
	}

	public int findMatches(String content) {
		Matcher matcher = pattern.matcher(content);
		int count = 0;
		while (matcher.find())
			count++;
		return count;
	}

	public Pattern getPattern() { return pattern; }

	@Override
	public void save() { Helper.writeMap(counts, file); }

	@Override
	public int getCount(long id) {
		MutableInteger count = counts.get(id);
		if (count != null)
			return count.intValue();
		return -1;
	}

	public void transfer(Recorder counter) { this.counts = counter.getCounts(); }

	@Override
	public Map<Long, MutableInteger> getCounts() { return counts; }

	@Override
	public String toString() {
		String output = "";
		for (Entry<Long, MutableInteger> entry : counts.entrySet())
		{
			output += entry.toString();
			output += "\n";
		}
		return output;
	}

	@Override
	public Recorder copyOf(boolean shouldResetCounts, boolean shouldAffect) {
		return new Counter(pattern, counts, file, shouldAffect, shouldResetCounts);
	}
}
