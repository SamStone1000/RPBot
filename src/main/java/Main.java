import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import reactorRecorders.E621Counter;
import reactorRecorders.KarmaCounter;
import reactors.Reactioner;
import record.Channels;
import recorders.Counter;
import recorders.MessageProcessers;
import util.MutableInteger;

public class Main extends ListenerAdapter {

	MessageProcessers messageProcessers;
	Channels channels;



	//args[0] should be the bots token, args[1] should be the Guild the bot works in, args[2] is the id of the channel to send vore to
	public static void main(String[] args) throws LoginException, InterruptedException, IOException, NumberFormatException {
		JDABuilder builder = JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS);
		//builder.addEventListeners(new Main());
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		JDA jda = builder.build();

		MessageProcessers messageProcessers = new MessageProcessers();
		//Counter voreCounter = new Counter("vore", Pattern.compile("(?:^|\\W)vore"));
		Reactioner mogusReactor = new Reactioner(Pattern.compile("(?:^|\\W)mogus(?:$|\\W)"), "ðŸ‘�");
		KarmaCounter karmaCounter = new KarmaCounter();
		E621Counter voreCounter = new E621Counter("vore", Pattern.compile("(?:^|\\W)vore"), true, Long.valueOf(args[2]), "vore", jda);

		//messageProcessers.addCounter("vore", voreCounter);
		messageProcessers.addReactor(mogusReactor);
		messageProcessers.setKarmaCounter(karmaCounter);
		messageProcessers.addReactorRecord("vore", voreCounter);

		jda.awaitReady();
		Channels channels = new Channels(jda, Long.parseLong(args[1]));
			
		
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
				Commands.slash("karma", "Checks karma")
				.addOptions(
						new OptionData(OptionType.USER, "user", "The user to get the current karma count of").setRequired(true)));
		commands.addCommands(
				Commands.slash("given", "Checks how much karma a user has given out")
				.addOptions(
						new OptionData(OptionType.USER, "user", "The user to give karma to").setRequired(true)));
		commands.addCommands(
				Commands.slash("shutdown", "shuts down bot"));
		//commands.addCommands(Commands.message("Give"));
		commands.queue();
		
		jda.addEventListener(new Main(messageProcessers, channels));
	}
	
	public Main(MessageProcessers processers, Channels channels) {
		this.messageProcessers = processers;
		this.channels = channels;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		//System.out.println("Message Received!");
		Message msg = event.getMessage();
		User author = msg.getAuthor();

		if (author.isBot()) return;
		messageProcessers.accept(msg);
		}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		String name = event.getName();
		switch (name)
		{
		case "count":
			User user = event.getOption("user").getAsUser();
			String term = event.getOption("term").getAsString();
			int count = messageProcessers.getCount(term, user.getIdLong());

			String output;
			if (count > 0) {
				output = user.getName() + " has said \""+term+"\" "+count+" times.";
			} else if (count == -1) {
				output = user.getName() + " has not said \""+term+".\"";
			} else {
				output = "\""+term+"\" is currently not being tracked.";
			}
			event.reply(output).queue();
			break;
		case "given":
			user = event.getOption("user").getAsUser();
			count = messageProcessers.karmaCounter.getGiven(user.getIdLong());
			output = user.getName() +" has given "+ count + " karma";
			event.reply(output).queue();
			break;
		case "karma":
			user = event.getOption("user").getAsUser();
			count = messageProcessers.karmaCounter.getKarma(user.getIdLong());
			output = user.getName() + " has " + count + " karma";
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
		case "shutdown":
			event.reply("ok").complete();
			if (event.getUser().getIdLong() == 275383746306244608l)
				System.exit(0);
			break;
		}
	}
	
	private void rebuild(MessageChannel channel) {
		channels.fetchAll(channel.getIdLong());
	}

	private void recount(MessageChannel messageChannel) {
		KarmaCounter karmaCounter = new KarmaCounter(false);
		Counter voreCounter = new Counter("vore", Pattern.compile("(?:^|\\W)vore"), false);
		
		MessageProcessers processer = new MessageProcessers();
		processer.addCounter("vore", voreCounter);
		processer.setKarmaCounter(karmaCounter);
		
		
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		event.reply("foo").setEphemeral(true).queue();
		event.getTarget().reply("cunk").queue();
	}
}