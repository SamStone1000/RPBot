package reactors;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

public class Reactioner implements Consumer<Message> {

	private Pattern pattern;
	private String emoji;
	private Emote emote;

	public Reactioner(Pattern pattern, Emote emote) {
		this.pattern = pattern;
		this.emote = emote;
	}

	public Reactioner(Pattern pattern, String emote) {
		this.pattern = pattern;
		this.emoji = emote;
	}

	@Override
	public void accept(Message message) {

		if (pattern.matcher(message.getContentRaw().toLowerCase()).find()) {
			System.out.println("h");
			if (emoji != null)
			message.addReaction(emoji).queue();
			else
			message.addReaction(emote).queue();
		}
	}

}
