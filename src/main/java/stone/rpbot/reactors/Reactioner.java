package stone.rpbot.reactors;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

public class Reactioner implements Consumer<Message> {

	private Pattern pattern;
	private Emoji emoji;

	public Reactioner(Pattern pattern, CustomEmoji emote) {
		this.pattern = pattern;
		this.emoji = emote;
	}

	public Reactioner(Pattern pattern, String emote) {
		this.pattern = pattern;
		this.emoji = Emoji.fromUnicode(emote);
	}

	@Override
	public void accept(Message message) {

		if (pattern.matcher(message.getContentRaw().toLowerCase()).find())
		{
				message.addReaction(emoji).queue();
		}
	}

}
