import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Main extends ListenerAdapter {

	static TreeMap<String, TreeMap<Long, MutableInt>> treeMaps;
	static String[] terms;

	public static void main(String[] args) throws LoginException, InterruptedException, FileNotFoundException {
		JDABuilder builder = JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES);
		builder.addEventListeners(new Main());
		JDA jda = builder.build();

		terms = new String[] {"vore"};
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
		CommandListUpdateAction commands = jda.getGuildById(903451374799429673L).updateCommands();

		commands.addCommands(new CommandData("count", "Gets the current count of the specified word and/or user").addOptions(new OptionData(OptionType.USER, "user", "The user you want to query"), new OptionData(OptionType.STRING, "term", "The word you want to query about")));
		commands.queue();




	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		//System.out.println("Message Received!");
		Message msg = event.getMessage();
		String content = msg.getContentRaw().toLowerCase();
		for (String term : terms) {
			int count = content.split(Pattern.quote(term), -1).length - 1;
		if (count > 0) {
			msg.reply(Integer.toString(count)).queue();
			Long id = msg.getAuthor().getIdLong();
			TreeMap<Long, MutableInt> map = treeMaps.get(term);
			MutableInt currentCount = map.get(id);
			if (currentCount != null)
			{
				currentCount.add(count);
			} else {
				map.put(id, new MutableInt(count));
			}
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
					desc += " has said the word "+term+ " ";
					MutableInt count = treeMaps.get(term).get(user.getIdLong());
					desc += count.toString()+" times!";
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
}