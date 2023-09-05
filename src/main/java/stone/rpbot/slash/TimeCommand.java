package stone.rpbot.slash;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class TimeCommand implements SlashCommand {

	private static final TimeUnit[] CLOCK_UNITS = { TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS };

	@Override
	public void onSlashCommand(SlashCommandInteractionEvent event) {
		System.out.println("aaaaaaaaa");
		Modifiers mod = Modifiers.STATIC;
		String input = event.getOption("input").getAsString();

		if (input.startsWith("+"))
			mod = Modifiers.ADDITIVE;
		else if (input.startsWith("-"))
			mod = Modifiers.SUBTRACTIVE;
		String[] tokens = input.split(":");
		if (tokens.length > 3) {
			event.reply("stupid").queue();
			return;
		}

		long amount = 0;
		int i = 3 - tokens.length;
		try {
			for (String token : tokens) {// HH:MM:SS format
				int tokenTime = Integer.parseInt(token);
				amount += CLOCK_UNITS[i].toMillis(tokenTime);
			}
		} catch (NumberFormatException exception) {
			event.reply(exception.getMessage()).setEphemeral(true).queue();
			return;
		}
		if (mod == Modifiers.SUBTRACTIVE)
			amount *= -1;
		event.reply(TimeFormat.DEFAULT.format(System.currentTimeMillis() + amount)).setEphemeral(true).queue();
	}

	public enum Modifiers {
		ADDITIVE, STATIC, SUBTRACTIVE;

	}

	public static void main(String[] args) {
		String[] tokens = "10".split(":");
		System.out.println(Arrays.toString(tokens));
	}
}
