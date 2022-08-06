package stone.rpbot.reactorRecorders;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import stone.rpbot.util.Helper;
import stone.rpbot.util.MutableInteger;
import stone.rpbot.util.SharedConstants;

public class KarmaCounter {

	private Map<Long, MutableInteger> karmaCounts;
	private Map<Long, MutableInteger> givenCounts;
	private static Pattern mentionPattern = Pattern.compile("(?:<@!?)(\\d*?)(?:>) *?([+-]{2}|[+-]= *(-?\\d+))");
	private static Pattern shortcutPattern = Pattern.compile("^((?:(?:\\+|-)=)|(?:\\+\\+|--)) *((?:-|\\+)?\\d+)?");
	// private static int MENTION_OFFSET = 3;
	private boolean shouldSave;
	private File karmaFile;
	private File givenFile;

	public KarmaCounter(boolean shouldSave) {
		this.shouldSave = shouldSave;
		karmaFile = new File(SharedConstants.REACT_RECORD_FOLDER + "karma" + ".txt");
		givenFile = new File(SharedConstants.REACT_RECORD_FOLDER + "givenKarma" + ".txt");
		if (shouldSave)
		{
			this.karmaCounts = Helper.readMap(karmaFile);
			this.givenCounts = Helper.readMap(givenFile);
		} else
		{
			this.karmaCounts = new TreeMap<Long, MutableInteger>();
			this.givenCounts = new TreeMap<Long, MutableInteger>();
		}
	}

	public KarmaCounter() { this(true); }

	public KarmaCounter(
			Map<Long, MutableInteger> karmaCounts2, Map<Long, MutableInteger> givenCounts2, File karmaFile2,
			File givenFile2, boolean shouldResetCounts, boolean shouldAffect
	) {
		if (shouldResetCounts)
		{
			this.karmaCounts = new TreeMap<>();
			this.givenCounts = new TreeMap<>();
		} else
		{
			this.karmaCounts = karmaCounts2;
			this.givenCounts = givenCounts2;
		}
		this.karmaFile = karmaFile2;
		this.givenFile = givenFile2;
		this.shouldSave = shouldAffect;
	}

	public Set<Long> findKarma(String content, Long author) {

		Matcher matcher = mentionPattern.matcher(content);
		Set<Long> receivers = new HashSet<>();
		if (author == 456226577798135808l)
		{ author = 827724526313537536l; }

		while (matcher.find())
		{
			int change = 0;
			String group2 = matcher.group(2);
			String group3 = matcher.group(3);
			if (group3 != null) //using a +=/-= syntax
			{
				change = Integer.valueOf(group3);
				if (group2.startsWith("-")) change = -change; //negate it if the karma is being subtracted
			} else { //using a ++ or -- syntax
				if (group2.equals("++")) {
					change = 1;
				} else if (group2.equals("--")) {
					change = -1;
				}
			}
			
			
			long receiver = Long.valueOf(matcher.group(1));
			receivers.add(receiver);
			giveKarma(receiver, author, change);
		}
		return receivers;
	}

	public void accept(Message message) {
		Set<Long> receivingUsers = findKarma(message.getContentRaw(), message.getAuthor().getIdLong());

		
		//doing it like this insures that we only make a call to discord only if the message is actually trying to add or subtract karma
		Matcher shortcutFinder = shortcutPattern.matcher(message.getContentRaw());
		if (shortcutFinder.find())
		{
			String amount = shortcutFinder.group(2);
			String decider = shortcutFinder.group(1);
			Action action = Action.actionOf(decider);
			int karmaAmount;
			if (amount != null)
			{
				karmaAmount = Integer.valueOf(amount);
			} else
			{
				karmaAmount = 0; //doesn't matter
			}
			long receiver = message.getReferencedMessage().getAuthor().getIdLong();
			long giver = message.getAuthor().getIdLong();
			Karma karma = new Karma(giver, receiver, action, karmaAmount);
			receivingUsers.add(receiver);
		}

		if (!receivingUsers.isEmpty())
			if (shouldSave)
			{
				{
					String output = "";
					Guild guild = message.getGuild();
					for (Long id : receivingUsers)
					{
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

	public void giveKarma(long receiver, long giver, int change) {
		if (receiver == 456226577798135808l)
		{ receiver = 827724526313537536l; }
		// receivers.add(receiver);
		MutableInteger oldCount = karmaCounts.get(receiver);
		if (oldCount != null)
		{
			oldCount.add(change);
		} else
		{
			karmaCounts.put(receiver, new MutableInteger(change));
		}
		if (change >= 1)
		{
			MutableInteger giverCount = givenCounts.get(giver);
			if (giverCount != null)
			{
				giverCount.add(change);
				;
			} else
			{
				givenCounts.put(giver, new MutableInteger(1));
			}
		}
		if (shouldSave)
		{
			Helper.writeMap(karmaCounts, karmaFile);
			Helper.writeMap(givenCounts, givenFile);
		}

	}
	
	private void giveKarma(Karma karma) {
		giveKarma(karma.receiver, karma.giver, karma.amount);
	}

	public int getKarma(long id) {
		MutableInteger count = karmaCounts.get(id);
		if (count != null)
			return count.intValue();
		return 0;
	}

	public int getGiven(long id) {
		MutableInteger count = givenCounts.get(id);
		if (count != null)
			return count.intValue();
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
		for (Entry<Long, MutableInteger> entry : karmaCounts.entrySet())
		{
			output += entry.toString();
			output += "\n";
		}
		output += "Given\n";
		for (Entry<Long, MutableInteger> entry : givenCounts.entrySet())
		{
			output += entry.toString();
			output += "\n";
		}
		return output;
		
	}

	public KarmaCounter copyOf(boolean shouldResetCounts, boolean shouldAffect) {
		return new KarmaCounter(karmaCounts, givenCounts, karmaFile, givenFile, shouldResetCounts, shouldAffect);
	}
	
	private class Karma {
		
		public long giver;
		public long receiver;
		
		public int amount;
		
		public Karma(long giver, long receiver, Action action, int amount) {
			this.giver = giver;
			this.receiver = receiver;
			switch (action)
			{
			case INCREMENT:
				this.amount = 1;
				break;
			case ADD:
				this.amount = amount;
				break;
			case DECREMENT:
				this.amount = -1;
				break;
			case SUBTRACT:
				this.amount = -amount;
				break;
			default:
				this.amount = 0;
				break;
			}
		}
		
	}
	
	protected enum Action {
		ADD, SUBTRACT, INCREMENT, DECREMENT;
		
		static Action actionOf(String str) {
			switch (str)
			{
			case "++":
				return INCREMENT;
			case "+=":
				return ADD;
			case "--":
				return DECREMENT;
			case "-=":
				return SUBTRACT;
			default:
				return null;
			}
		}
	}
}
