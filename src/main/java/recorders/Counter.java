package recorders;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Helper;
import util.MutableInteger;
import util.SharedConstants;

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

	public Counter(Pattern pattern, Map<Long, MutableInteger> counts, File file, boolean shouldAffect) {
		this.pattern = pattern;
		this.counts = counts;
		this.file = file;
		this.shouldAffect = shouldAffect;
	}
	public Counter(String term, Pattern pattern) {
		this.pattern = pattern;
		this.shouldAffect = true;
		file = new File(SharedConstants.COUNTER_FOLDER + term + ".txt");
		this.counts = Helper.readMap(file);
	}

	public Counter(String term, Pattern pattern, boolean shouldAffect) {
		this(term, pattern);
		this.shouldAffect = shouldAffect;
	}

	@Override
	public boolean test(String content, Long id) {
		int count = this.findMatches(content);
		if (count > 0) {
			MutableInteger oldCount = counts.get(id);
			if (oldCount != null) {
				oldCount.add(count);
			} else {
				counts.put(id, new MutableInteger(count));
			}
			if (shouldAffect)
			Helper.writeMap(counts, file);
			return true;
		}
		return false;
	}

	public int findMatches(String content) {
		Matcher matcher = pattern.matcher(content);
		int count = 0;
		while (matcher.find()) count++;
		return count;
	}

	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public void save() {
		Helper.writeMap(counts, file);
	}

	@Override
	public int getCount(long id) {
		MutableInteger count = counts.get(id);
		if (count != null)
			return count.intValue();
		return -1;
	}
}
