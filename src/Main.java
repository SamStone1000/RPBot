import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.UTF8StreamJsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.requests.restaction.pagination.MessagePaginationActionImpl;

public class Main extends ListenerAdapter {

	static TreeMap<String, TreeMap<Long, MutableInt>> treeMaps;
	static String[] terms;
	static Pattern[] regex;
	static TextChannel voreChannel;
	static Guild guild;
	Thread building;

	//args[0] should be the bots token, args[1] should be the Guild the bot works in, args[2] is the id of the channel to send vore to
	public static void main(String[] args) throws LoginException, InterruptedException, FileNotFoundException {
		JDABuilder builder = JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS);
		builder.addEventListeners(new Main());
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		JDA jda = builder.build();

		terms = new String[] {"vore"};
		regex = new Pattern[] {Pattern.compile("(?:^|\\W)vore")};
		treeMaps = new TreeMap<>();

		for (String term : terms)
		{
			File file = new File(term + ".txt");
			if (file.exists()) {
				Scanner scanner;
				TreeMap<Long, MutableInt> readMap = new TreeMap<>();
					scanner = new Scanner(file);
					while (scanner.hasNext()) {
						String str = scanner.next();
						String[] pair = str.split(",");
						readMap.put(Long.parseLong(pair[0]), MutableInt.parseInt(pair[1]));
					}
					scanner.close();
				treeMaps.put(term, readMap);
			} else {
			treeMaps.put(term, new TreeMap<Long, MutableInt>());
			}
		}
		jda.awaitReady();
		guild = jda.getGuildById(args[1]);
		guild.loadMembers();
		voreChannel = guild.getTextChannelById(args[2]);
		
		CommandListUpdateAction commands = guild.updateCommands();
		commands.addCommands(
				new CommandData("count", "Gets the current count of the specified word and/or user")
				.addOptions(
						new OptionData(OptionType.USER, "user", "The user you want to query").setRequired(true), 
						new OptionData(OptionType.STRING, "term", "The word you want to query about").setRequired(true)));
		commands.addCommands(
				new CommandData("rebuild", "Rebuilds the tracked term counts from message history"));
		commands.queue();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		//System.out.println("Message Received!");
		Message msg = event.getMessage();
		User author = msg.getAuthor();
		if (author.isBot()) return;
		Long id = author.getIdLong();
		String content = msg.getContentRaw().toLowerCase();
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i];
			Matcher matcher = regex[i].matcher(content);
			int count = 0;
			while (matcher.find()) count++;
		if (count > 0) {
			if (term.equals("vore"))
			{
				//System.out.println("vore");
				try
				{
					gete621(voreChannel, "vore");
					//System.out.println("sent!");
				} catch (MalformedURLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			TreeMap<Long, MutableInt> map = treeMaps.get(term);
			MutableInt currentCount = map.get(id);
			if (currentCount != null)
			{
				currentCount.add(count);
			} else {
				//map.put(id, new MutableInt(count));
			}
			if (building == null || !building.isAlive())
				writeMap(map, term + ".txt");
		}
		}
	}
	
	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		if (event.getName().equals("count")) {
			EmbedBuilder embed = new EmbedBuilder();
			String desc= "";
			if (event.getOption("user") != null) {
				if (event.getOption("term") != null) { //nonnull user, nonnull term
					User user = event.getOption("user").getAsUser();
					String term = event.getOption("term").getAsString();
					desc = user.getName();
					desc += " has said \""+term+ "\" ";
					MutableInt count = null;
					TreeMap<Long, MutableInt> map = treeMaps.get(term);
					if (map != null) {
						count = map.get(user.getIdLong());
						if (count != null)
							desc += count.toString()+" times";
						else
							desc = "\""+term + "\" is not being tracked";
					}
					//desc += Objects.toString(count, "0")+" times!";
				}
				else { //nonnull user, null term
					User user = event.getOption("user").getAsUser();
				}
			}
			else {
				if (event.getOption("term") != null) { //null user, nonnull term
					String term = event.getOption("term").getAsString();
				}
				else { //null user, null term
					
				}
			}
			
			if (desc.equals("")) desc = "foobar";
			embed.setDescription(desc);
			event.replyEmbeds(embed.build()).setEphemeral(false).queue();
		}
		else if (event.getName().equals("rebuild")) {
			User user = event.getUser();
			if (user.getIdLong() != 275383746306244608L) {
				event.reply("You can not execute this command").queue();
			}
			else {
				event.reply("Acknowledged").queue();
				treeMaps = new TreeMap<>();
				List<Member> members = guild.getMembers();
				for (int i = 0; i < terms.length; i++) {
					TreeMap<Long, MutableInt> map = new TreeMap<>();
					int length = members.size();
					for (int j = 0; j < length; j++) {
						Member member = members.get(j);
						if (member.getUser().isBot()) continue;
						map.put(member.getIdLong(), new MutableInt(0));
					}
					treeMaps.put(terms[i], map);
				}
				OptionMapping temp = event.getOption("term");
				String[] tempTerms;
				if (temp != null)
					tempTerms = new String[] {temp.getAsString()};
				else
					tempTerms = terms;
				List<TextChannel> channels = guild.getTextChannels();
				Counting tempCount = new Counting(tempTerms, regex, treeMaps);
				MutableInt searched = new MutableInt(0);
				int channelCount = channels.size();
				TextChannel commandChannel = event.getTextChannel();
				ArrayList<MessagePaginationAction> histories = new ArrayList<>();
				for (TextChannel channel : channels) histories.add(channel.getIterableHistory());
				Runnable runnable = () -> {
					long truestart = System.currentTimeMillis();
				for (MessagePaginationAction history : histories) {
					long start = System.currentTimeMillis();
						//event.getHook().editOriginal("Searching channel \""+channel.getName()+"\" "+searched+"/"+channelCount).queue();
						try
						{
							history.forEachAsync(tempCount).get();
						} catch (InterruptedException | ExecutionException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						searched.add(1);
						long end = System.currentTimeMillis();
						commandChannel.sendMessage("Searched channel \""+history.getChannel().getName()+"\" "+searched+"/"+channelCount+" Took "+(end - start)+" ms").queue();
				}
				//treeMaps = tempCount.getMaps();
				long trueend = System.currentTimeMillis();
				writeMaps(treeMaps);
				commandChannel.sendMessage(user.getAsMention()+" Finished map rebuild in "+(trueend - truestart)+" ms!").queue();
				//commandChannel.sendMessage(user.getAsMention()).queue();
				};
				building = new Thread(runnable);
				building.start();
			}
		}
	}


	public <k, v> void writeMap(Map<k, v> map, String file){
		String str = "";
		Iterator<Map.Entry<k, v>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<k, v> entry = itr.next();
			str += entry.getKey() + ",";
			str += entry.getValue() + "\n";
		}

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(new File(file));
			fileWriter.write(str);
			fileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public <K, V> void writeMaps(TreeMap<String, TreeMap<K, V>> map) {
		Iterator<Map.Entry<String, TreeMap<K, V>>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, TreeMap<K, V>> entry = itr.next();
			writeMap(entry.getValue(), entry.getKey() + ".txt");
		}
	}
	
	private void gete621(TextChannel channel, String search) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("https://e621.net/posts.json?tags="+search+"+order:random+limit:1").openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", "RPBot/2.0 by SamStone");
		connection.connect();
		InputStream stream = connection.getInputStream();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> jsonMap = mapper.readValue(stream, LinkedHashMap.class);
		ArrayList posts = (ArrayList) jsonMap.get("posts");
		LinkedHashMap post = (LinkedHashMap) posts.get(0);
		LinkedHashMap file = (LinkedHashMap) post.get("file");
		String url = (String) file.get("url");
		String md5 = (String) file.get("md5");
		
		HttpURLConnection connection2 = (HttpURLConnection) new URL(url).openConnection();
		connection2.setRequestMethod("GET");
		connection2.setRequestProperty("User-Agent", "RPBot/2.0 by SamStone");
		connection2.connect();
		//System.out.println(url.substring(url.length() - md5.length() - 3));
		channel.sendFile(connection2.getInputStream(), url.substring(url.length() - md5.length() - 3)).queue();
	}
}