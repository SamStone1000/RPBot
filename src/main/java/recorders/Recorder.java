package recorders;

import java.util.function.BiPredicate;

public interface Recorder extends BiPredicate<String, Long> {

	public void save();
	public int getCount(long id);
}
