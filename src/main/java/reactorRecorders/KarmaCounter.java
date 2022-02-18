package reactorRecorders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import util.Helper;
import util.MutableInteger;
import util.SharedConstants;

public class KarmaCounter implements ReactorRecord {

	private Map<Long, MutableInteger> karmaCounts;
	private Map<Long, MutableInteger> givenCounts;
	private static Pattern mentionPattern = Pattern.compile("(?:<@!)(\\d*?)(?:>)");
	private static int MENTION_OFFSET = 3;
	private boolean shouldSave;
	private File karmaFile;
	private File givenFile;

	public KarmaCounter(boolean shouldSave) {
		this.shouldSave = true;
		karmaFile = new File(SharedConstants.REACT_RECORD_FOLDER + "karma" + ".txt");
		givenFile = new File(SharedConstants.REACT_RECORD_FOLDER + "givenKarma" + ".txt");
		this.karmaCounts = Helper.readMap(karmaFile);
		this.givenCounts = Helper.readMap(givenFile);
	}

	public KarmaCounter() {
		this(true);
	}

	public List<Long> findKarma(String content, Long id) {
		String stripped = content.replaceAll(" ", "");
		Matcher matcher = mentionPattern.matcher(stripped);
		List<Long> receivers = new ArrayList<>();
		while (matcher.find())
		{
			int end = matcher.end();
			int change = 0;
			try
			{
				String decider = stripped.substring(end, end + 2);
				if (decider.equals("++"))
				{
					change = 1;
				}
				else if (decider.equals("--"))
				{
					change = -1;
				}
			} catch (IndexOutOfBoundsException e)
			{
			}
			if (change == 0) continue;
			System.out.println(matcher.group());
			String group = matcher.group();
			long receiver = Long.valueOf(group.substring(MENTION_OFFSET, group.length() - 1));
			receivers.add(receiver);
			MutableInteger oldCount = karmaCounts.get(receiver);
			if (oldCount != null)
			{
				oldCount.add(change);
			}
			else
			{
				karmaCounts.put(receiver, new MutableInteger(change));
			}
			if (change == 1)
			{
				MutableInteger giverCount = givenCounts.get(id);
				if (giverCount != null)
				{
					giverCount.increment();
				}
				else
				{
					givenCounts.put(id, new MutableInteger(1));
				}
			}
			if (shouldSave)
			{
				Helper.writeMap(karmaCounts, karmaFile);
				Helper.writeMap(givenCounts, givenFile);
			}

		}
		return receivers;
	}

	public void accept(Message message) {
		List<Long> users = findKarma(message.getContentRaw(), message.getAuthor().getIdLong());
		if (!users.isEmpty())
		{
			String output = "";
			JDA jda = message.getJDA();
			Long lastUser = null;
			for (Long id : users)
			{
				if (id.equals(lastUser)) continue;
				lastUser = id;
				User user = jda.getUserById(id);
				output += user.getName() + " now has " + getKarma(id) + " karma, ";
			}
			message.reply(output).mentionRepliedUser(false).queue();
			/*
			 * if (emote != null) { message.addReaction(emote).queue(); } else {
			 * message.addReaction(emoji).queue(); }
			 */
		}
	}

	public int getKarma(long id) {
		MutableInteger count = karmaCounts.get(id);
		if (count != null) return count.intValue();
		return 0;
	}

	public int getGiven(long id) {
		MutableInteger count = givenCounts.get(id);
		if (count != null) return count.intValue();
		return 0;
	}

	@Override
	public void save() {
		Helper.writeMap(karmaCounts, karmaFile);
		Helper.writeMap(givenCounts, givenFile);
	}

	

	@Override
	public boolean test(String t, Long u) {
		return false;
	}

	@Override
	public int getCount(long id) {
		// TODO Auto-generated method stub
		return 0;
	}

}
