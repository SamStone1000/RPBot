package reactorRecorders;

import java.util.Map;

import net.dv8tion.jda.api.entities.Message;
import recorders.Recorder;
import util.MutableInteger;

public class KarmaCounter implements ReactorRecord {
	
	public KarmaCounter() {
		
	}

	@Override
	public void save() { // TODO Auto-generated method stub
	 }

	@Override
	public int getCount(long id) { // TODO Auto-generated method stub
	return 0; }

	@Override
	public void transfer(Recorder recorder) { // TODO Auto-generated method stub
	 }

	@Override
	public Map<Long, MutableInteger> getCounts() { // TODO Auto-generated method stub
	return null; }

	@Override
	public boolean test(String t, Long u) { // TODO Auto-generated method stub
	return false; }

	@Override
	public void accept(Message t) { // TODO Auto-generated method stub
	 }

	@Override
	public ReactorRecord copyOf(boolean shouldAffect, boolean shouldAffect2) { // TODO Auto-generated method stub
	return null; }
}