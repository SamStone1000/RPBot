package stone.rpbot.slash;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class TimeCommand implements SlashCommand {

	private static final TimeUnit[] CLOCK_UNITS = { TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS };
	private static final Map<Character, ChronoField> SIMPLE_FIELDS = new HashMap<>();

	static {
		SIMPLE_FIELDS.put('h', ChronoField.HOUR_OF_DAY);
		SIMPLE_FIELDS.put('m', ChronoField.MINUTE_OF_HOUR);
		SIMPLE_FIELDS.put('s', ChronoField.SECOND_OF_MINUTE);
		SIMPLE_FIELDS.put('d', ChronoField.DAY_OF_MONTH);
		SIMPLE_FIELDS.put('D', ChronoField.DAY_OF_MONTH);
		SIMPLE_FIELDS.put('M', ChronoField.MONTH_OF_YEAR);
		SIMPLE_FIELDS.put('y', ChronoField.YEAR);
		SIMPLE_FIELDS.put('Y', ChronoField.YEAR);
	}

	@Override
	public void onSlashCommand(SlashCommandInteractionEvent event) {
		String input = event.getOption("input").getAsString();
		OffsetDateTime dateTime = OffsetDateTime.now(ZoneId.of("America/Louisville"));
		for (String token : input.split(" ")) {
			if (token.contains(":")) {
				// handleClock(dateTime, token);
			} else {
				dateTime = handleSimpleUnit(dateTime, token);
			}
		}
		event.reply(String.format("`%s`", TimeFormat.RELATIVE.format(dateTime))).setEphemeral(true).queue();
	}

	public OffsetDateTime handleSimpleUnit(OffsetDateTime dateTime, String token) {
		Character unit = token.charAt(token.length() - 1);
		int amount = Integer.valueOf(token.substring(0, token.length() - 1));
		ChronoField timeField = SIMPLE_FIELDS.get(unit);

		if (token.charAt(0) == '+' || token.charAt(0) == '-') // means this token
		{ // is a relative amount
			return dateTime.plus(amount, timeField.getBaseUnit());
		} else // means this token is absolute and is overwriting a field
		{
			return dateTime.with(timeField, amount);
		}
	}

	public enum Modifiers {
		RELATIVE, ABSOLUTE;

	}
}
