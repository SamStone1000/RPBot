package reactorRecorders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import util.Helper;
import util.MutableInteger;
import util.SharedConstants;

public class KarmaCounter {

	private Map<Long, MutableInteger> karmaCounts;
	private Map<Long, MutableInteger> givenCounts;
	private static Pattern mentionPattern = Pattern.compile("(?:<@!*?)(\\d*?)(?:>) *?([+-]{2})");
	//private static int MENTION_OFFSET = 3;
	private boolean shouldSave;
	private File karmaFile;
	private File givenFile;

	public KarmaCounter(boolean shouldSave) {
		this.shouldSave = shouldSave;
		karmaFile = new File(SharedConstants.REACT_RECORD_FOLDER + "karma" + ".txt");
		givenFile = new File(SharedConstants.REACT_RECORD_FOLDER + "givenKarma" + ".txt");
		if (shouldSave) {
		this.karmaCounts = Helper.readMap(karmaFile);
		this.givenCounts = Helper.readMap(givenFile);
		} else {
			this.karmaCounts = new TreeMap<Long, MutableInteger>();
			this.givenCounts = new TreeMap<Long, MutableInteger>();
		}
	}

	public KarmaCounter() {
		this(true);
	}

	public KarmaCounter(Map<Long, MutableInteger> karmaCounts2, Map<Long, MutableInteger> givenCounts2, File karmaFile2,
			File givenFile2, boolean shouldResetCounts, boolean shouldAffect) {
		if (shouldResetCounts) {
			this.karmaCounts = new TreeMap<>();
			this.givenCounts = new TreeMap<>();
		} else {
		this.karmaCounts = karmaCounts2;
		this.givenCounts = givenCounts2;
		}
		this.karmaFile = karmaFile2;
		this.givenFile = givenFile2;
		this.shouldSave = shouldAffect;
	}

	public List<Long> findKarma(String content, Long id) {
		String stripped = content.replaceAll(" ", "");
		Matcher matcher = mentionPattern.matcher(content);
		List<Long> receivers = new ArrayList<>();
		if (id == 456226577798135808l) {
			id = 827724526313537536l;
		}
		while (matcher.find())
		{
			//int end = matcher.end();
			int change = 0;
			try
			{
				String decider = matcher.group(2);
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
		//String group = matcher.group();
			//long receiver = Long.valueOf(group.substring(MENTION_OFFSET, group.length() - 1));
			long receiver = Long.valueOf(matcher.group(1));
			if (receiver == 456226577798135808l) {
				receiver = 827724526313537536l;
			}
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
			if (shouldSave) {
		{
			String output = "";
			Guild guild = message.getGuild();
			Long lastUser = null;
			for (Long id : users)
			{
				if (id.equals(lastUser)) continue;
				lastUser = id;
				Member user = guild.retrieveMemberById(id).complete();
				output += user.getEffectiveName() + " now has " + getKarma(id) + " karma, ";
			}
			message.reply(output).mentionRepliedUser(false).queue();
			/*
			 * if (emote != null) { message.addReaction(emote).queue(); } else {
			 * message.addReaction(emoji).queue(); }
			 */
		}
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

	public void save() {
		Helper.writeMap(karmaCounts, karmaFile);
		Helper.writeMap(givenCounts, givenFile);
	}

	public void transfer(KarmaCounter karmaCounter) {
		this.karmaCounts = karmaCounter.karmaCounts;
		this.givenCounts = karmaCounter.givenCounts;
	}

	@Override
	public String toString() {
		String output = "Karma\n";
		for (Entry<Long, MutableInteger> entry : karmaCounts.entrySet()) {
			output += entry.toString();
			output += "\n";
		}
		output += "Given\n";
		for (Entry<Long, MutableInteger> entry : givenCounts.entrySet()) {
			output += entry.toString();
			output += "\n";
		}
		return output;
		
	}

	public KarmaCounter copyOf(boolean shouldResetCounts, boolean shouldAffect) {
		return new KarmaCounter(karmaCounts, givenCounts, karmaFile, givenFile, shouldResetCounts, shouldAffect);
	}
}
