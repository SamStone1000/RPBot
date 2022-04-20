import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import reactorRecorders.E621Counter;
import reactorRecorders.KarmaCounter;
import reactorRecorders.VoreCounter;
import reactors.Reactioner;
import record.Channels;
import record.KickedUserHelper;
import recorders.Counter;
import recorders.MessageProcessers;
import recorders.StatCounter;
import scheduled.CringeDLB;
import scheduled.RecurringMessage;
import util.MutableInteger;
import util.SharedConstants;

public class Main extends ListenerAdapter {

	MessageProcessers messageProcessers;
	Channels channels;
	Logger logger;
	private TreeMap<Long, List<Role>> kickedUserRoles;
	private Scheduler scheduler;

	//args[0] should be the bots token, args[1] should be the Guild the bot works in, args[2] is the id of the channel to send vore to, args[3] is the id to send freefall reminders to
	public static void main(String[] args) throws LoginException, InterruptedException, IOException, NumberFormatException, SchedulerException {
		JDABuilder builder = JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS);
		//builder.addEventListeners(new Main());
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		JDA jda = builder.build();

		MessageProcessers messageProcessers = new MessageProcessers();
		//Counter voreCounter = new Counter("vore", Pattern.compile("(?:^|\\W)vore"));
		Reactioner mogusReactor = new Reactioner(Pattern.compile("(?:^|\\W)mogus(?:$|\\W)"), "👍");
		KarmaCounter karmaCounter = new KarmaCounter();
		VoreCounter voreCounter = new VoreCounter("vore", Pattern.compile("\\b(vor(?:e[sd]?|ing))\\b", Pattern.CASE_INSENSITIVE), true, Long.valueOf(args[2]), "vore", jda, Pattern.compile("(vor(?:e[sd]?|ing))", Pattern.CASE_INSENSITIVE));
		
		
		//messageProcessers.addCounter("vore", voreCounter);
		messageProcessers.addReactor(mogusReactor);
		messageProcessers.setKarmaCounter(karmaCounter);
		messageProcessers.addReactorRecord("vore", voreCounter);
		//messageProcessers.addCounter("cunk", counter);
		
		Logger logger = LoggerFactory.getLogger("Main");
		
		jda.awaitReady();
		Channels channels = new Channels(jda, Long.parseLong(args[1]));
		KickedUserHelper kickedUserRoles = new KickedUserHelper();
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		jda.addEventListener(new Main(messageProcessers, channels, logger, scheduler));
		jda.addEventListener(kickedUserRoles);
		
		//welcome to jank city pop. 1
		SharedConstants.jda = jda;
		
		//jda.getPresence().setActivity(Activity.watching("for vore"));
		
		
		
		JobDetail freefallDetail = JobBuilder.newJob(RecurringMessage.class)
				.withIdentity("FreeFall Reminder", "Recurring Messages")
				.usingJobData("message", "Another 3 panels of Freefall have been added.")
				.usingJobData("channel", Long.valueOf(args[3]))
				.build();
		CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule("0 0 0 ? * MON,WED,FRI").inTimeZone(TimeZone.getTimeZone("CST"));
		Trigger freefallTrigger = TriggerBuilder.newTrigger()
				.withSchedule(schedule)
				.build();
		
		JobDetail DLBDetail = JobBuilder.newJob(CringeDLB.class)
				.withIdentity("Making sure DLB works", "Conditional Messages")
				.build();
		CronScheduleBuilder DLBSchedule = CronScheduleBuilder.cronSchedule("0 30 15 * * ?").inTimeZone(TimeZone.getTimeZone(ZoneOffset.of("-4")));
		Trigger DLBTrigger = TriggerBuilder.newTrigger()
				.withSchedule(DLBSchedule)
				.build();
		
		scheduler.scheduleJob(freefallDetail, freefallTrigger);
		scheduler.scheduleJob(DLBDetail, DLBTrigger);
		
		scheduler.start();
		
		
		Guild guild = jda.getGuildById(args[1]);
		CommandListUpdateAction commands = guild.updateCommands();
		commands.addCommands(
				Commands.slash("count", "Gets the current count of the specified word and/or user")
				.addOptions(
						new OptionData(OptionType.USER, "user", "The user you want to query").setRequired(true),
						new OptionData(OptionType.STRING, "term", "The word you want to query about").setRequired(true)));
		commands.addCommands(
				Commands.slash("rebuild", "Rebuilds stuff")
				.addOption(OptionType.CHANNEL, "channel", "The specific channel to rebuild"));
		commands.addCommands(
				Commands.slash("recount", "Recounts stuff"));
		commands.addCommands(
				Commands.slash("karma", "Checks karma")
				.addOptions(
						new OptionData(OptionType.USER, "user", "The user to get the current karma count of").setRequired(true)));
		commands.addCommands(
				Commands.slash("given", "Checks how much karma a user has given out")
				.addOptions(
						new OptionData(OptionType.USER, "user", "The user to give karma to").setRequired(true)));
		commands.addCommands(
				Commands.slash("admin", "Collection of commands to help with adminning")
				.addOptions(
						new OptionData(OptionType.STRING, "command", "The command to fire").setRequired(true),
						new OptionData(OptionType.STRING, "arguments", "Arguments to pass to the command")));
		commands.addCommands(
				Commands.context(Type.USER, "kick"));
		//commands.addCommands(Commands.message("Give"));
		commands.queue();
		//new CringeDLB().execute(null);
	}
	
	public Main(MessageProcessers processers, Channels channels, Logger logger, Scheduler scheduler) {
		this.messageProcessers = processers;
		this.channels = channels;
		this.logger = logger;
		this.scheduler = scheduler;
		this.kickedUserRoles = new TreeMap<Long, List<Role>>();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message msg = event.getMessage();
		//logger.info(msg.getContentRaw());
		User author = msg.getAuthor();
		channels.syncChannel(msg);
		if (author.isBot()) return;
		messageProcessers.accept(msg);
		}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		String name = event.getName();
		switch (name)
		{
		case "count":
			event.deferReply().queue();
			Member user = event.getGuild().getMember(event.getOption("user").getAsUser());
			String term = event.getOption("term").getAsString();
			int count = messageProcessers.getCount(term, user.getIdLong());

			String output;
			if (count > 0) {
				output = user.getEffectiveName() + " has said \""+term+"\" "+count+" times.";
			} else if (count == -1) {
				output = user.getEffectiveName() + " has not said \""+term+".\"";
			} else {
				
				Counter tempCounter = new Counter(term, Pattern.compile(term), false);
				MessageProcessers tempProcessers = new MessageProcessers();
				tempProcessers.addCounter(term, tempCounter);
				channels.searchChannels(tempProcessers);
				count = tempProcessers.getCount(term, user.getIdLong());
				if (count > 0) {
					output = user.getEffectiveName() + " has said \""+term+"\" "+count+" times.";
				} else if (count == -1) {
					output = user.getEffectiveName() + " has not said \""+term+".\"";
				} else {
				output = "\""+term+"\" is currently not being tracked.";
				}
			}
			event.getHook().editOriginal(output).queue();
			break;
		case "given":
			user = event.getGuild().getMember(event.getOption("user").getAsUser());
			count = messageProcessers.karmaCounter.getGiven(user.getIdLong());
			output = user.getEffectiveName() +" has given "+ count + " karma";
			event.reply(output).queue();
			break;
		case "karma":
			user = event.getGuild().getMember(event.getOption("user").getAsUser());
			count = messageProcessers.karmaCounter.getKarma(user.getIdLong());
			output = user.getEffectiveName() + " has " + count + " karma";
			event.reply(output).queue();
			break;
		case "recount":
			if (event.getUser().getIdLong() == 275383746306244608l) {
			event.reply("alrighty then!").queue();
			recount(event.getChannel());
			} else {
				event.reply("heck no!").queue();
			}
			break;
		case "rebuild":
			if (event.getUser().getIdLong() == 275383746306244608l) {
				event.reply("alrighty then!").queue();
				rebuild(event.getChannel());
				} else {
					event.reply("heck no!").queue();
				}
				break;
		case "admin":
			if (event.getUser().getIdLong() == 275383746306244608l) {
				String command = event.getOption("command").getAsString();
				switch (command) {
				case "dumpCounts":
					event.reply(messageProcessers.toString()).queue();
					break;
				case "copyCat" :
				event.reply("a").setEphemeral(true).queue();
				event.getChannel().sendMessage(event.getOption("arguments").getAsString()).queue();
				break;
				case "stats" :
					event.reply("alrighty then!").queue();
					InteractionHook hook = event.getHook();
					MessageProcessers statTemp = new MessageProcessers();
					StatCounter statCounter = new StatCounter(false, event.getGuild().getTimeCreated());
					statTemp.statCounter = statCounter;
					Thread thread = new Thread(() -> {
						long start = System.currentTimeMillis();
						channels.searchChannels(statTemp);
						long end = System.currentTimeMillis();
						hook.editOriginal("Searched all channels in "+ (end - start) + " ms").queue();
						event.getChannel().sendFile(statTemp.statCounter.outputGraph(), "gramph.png").queue();
						});
					thread.start();
					break;
				case "kickMe" :
					event.reply("I think its time for you to go.").queue();
					Guild guild = event.getGuild();
					User kickedUser = event.getUser();
					kickedUser.openPrivateChannel().flatMap(channel -> channel.sendMessage("https://discord.gg/BUwvSyND")).complete();
					kickedUserRoles.put(kickedUser.getIdLong(), guild.getMember(kickedUser).getRoles());
					guild.kick(guild.getMember(event.getUser())).queue();
					break;
				case "tempBan" :
					KickedUserHelper.tempBan(event.getMember(), "just didn't like them", Long.valueOf(event.getOption("arguments").getAsString()));
				case "giveRole":
					Role role = event.getGuild().getRoleById(event.getOption("arguments").getAsString());
					event.getGuild().addRoleToMember(event.getMember(), role).queue();
					event.reply("alrighty then!").queue();
					break;
				case "shutdown":
					event.reply("ok").complete();
					if (event.getUser().getIdLong() == 275383746306244608l) try
					{
						scheduler.shutdown();
					} catch (SchedulerException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						System.exit(0);
					break;
				case "sendEmbed":
					EmbedBuilder embedBuilder = new EmbedBuilder();
					embedBuilder.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl());
					//embedBuilder.setTitle("<song name>");
					embedBuilder.setDescription("My neighbors ask me why I'm limping down the way\r\n"
							+ "And who that fellow was who came by yesterday\r\n"
							+ "I quickly turn my face before I start to blush\r\n"
							+ "'Cause frankly there is nothing I can say\r\n"
							+ "I've always had a thing for pushing the extremes\r\n"
							+ "And I've just got a thing you won't find in the magazines\r\n"
							+ "This molded silicone has got me begging please\r\n"
							+ "Give me more of what I really need\r\n"
							+ "'Cause there is no cock like horse cock\r\n"
							+ "Send your asshole into shock\r\n"
							+ "You need horse cock of course-cock\r\n"
							+ "Grab the lube and slam the day away\r\n"
							+ "My shaft is quivering, my balls are turning blue and\r\n"
							+ "I think of drinking in a foot or even two\r\n"
							+ "My favorite stallion that I keep in my top drawer\r\n"
							+ "Slip it in and out be dripping goo\r\n"
							+ "As I take more cock, horse cock\r\n"
							+ "Shut the door and turn the lock\r\n"
							+ "Is your cock a horse cock?\r\n"
							+ "You will never hear me saying neigh!\r\n"
							+ "'Cause there is no cock like horse cock\r\n"
							+ "Rub my dick inside a sock\r\n"
							+ "Don't stop now, horse cock\r\n"
							+ "Stretch out my insides and make me pray\r\n"
							+ "My lovely horse cock, horse cock\r\n"
							+ "Horse cock, horse cock");
					embedBuilder.addField("No Cock Like Horse Cock", "Pepper Coyote", false);
					//embedBuilder.setTimestamp(Instant.now());
					
					event.replyEmbeds(embedBuilder.build()).queue();
					break;
				}
			} else {
				event.reply("uh no");
			}
		}
	}

	private void rebuild(MessageChannel channel) {
		logger.info("Rebuilding channels cache");
		Thread thread = new Thread(() -> {
		channels.fetchAll(channel.getIdLong());
		});
		thread.start();
	}

	private void recount(MessageChannel messageChannel) {
		logger.info("Recounting entire count cache");
		MessageProcessers processer = messageProcessers.copyOf(true, false);
		Thread thread = new Thread(() -> {
		long start = System.currentTimeMillis();
		channels.searchChannels(processer);
		long end = System.currentTimeMillis();
		messageChannel.sendMessage("Searched all channels in "+ (end - start) + " ms").queue();
		processer.saveAll();
		messageProcessers.transferCounts(processer);
		});
		thread.start();
	}
	
//	@Override
//	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
//		Member member = event.getMember();
//		if (kickedUserRoles.containsKey(member.getIdLong())) {
//			Guild guild = event.getGuild();
//			List<Role> roles = kickedUserRoles.get(member.getIdLong());
//			guild.modifyMemberRoles(member, roles).queue();
//			kickedUserRoles.remove(member.getIdLong());
//		}
//	}
	
	
	
	
}