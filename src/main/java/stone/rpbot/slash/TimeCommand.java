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

	@Override
	public String getManInfo() {
		return "**NAME**\n" + "		time - a slash command to make markdown timestamps\n" + "\n**SYNOPSIS**\n"
				+ "		**time** [*time fields*] [*timestamp format*]\n" + "\n**DESCRIPTION**\n"
				+ "		**time** is a command intended to offer easy and flexible timestamps. "
				+ "It is designed to allow as much as possible without being needlessly complex, and if there is something you "
				+ "think **time** should be able to do please tell me.\n" + "\n**EXAMPLES**\n"
				+ "		**time** +4h 0m 0s\n"
				+ "			Makes a timestamp for 4 hours into the future but on the hour, ie if it was 1:35 PM right now this would make a timestamp for 5:00 PM\n"
				+ "		**time** +1d 9h 0m 0s\n" + "			Makes a timestamp for tommorrow at 9:00 AM\n"
				+ "		**time** T\n" + "			Makes a timestamp for right now but in the \"Long Time\" format\n"
				+ "\n**OPTION**\n"
				+ "		The arguments to **time** can be broken up into two groups: the time fields and timestamp format. "
				+ "The order the groups appear in doesn't matter, in fact they can mixed just fine. The order the arguments inside of the groups does matter though. The arguemnts are "
				+ "interpreted from left to right with each one applying sequentially. Generally this won't matter except in cases where "
				+ "time fields either overflow or multiple absolute fields or formats appear. In the case of multiple absolute fields or formats, the last one processed (rightmost) "
				+ "is the one that is applied. In overflow cases such as 25 hours being added, the 25 hours will be turned into an implicit +1d +1h "
				+ "which could end up being overriden by an absolute day field removing the implicit +1d\n"
				+ "\n		**Time Fields**\n"
				+ "				Time Fields are of the syntax [+-]n<time field> where the plus or minus indicate if the field is additive or subtractive. In case of no plus or minus the time field is absolute. "
				+ "n is any number indicated how much of the time field is to be added/subtracted/set. <time field> is a letter indicating which field is to be modified.\n"
				+ "			**Table of Time Fields**\n" + "				s - seconds of the minute\n"
				+ "				m - minutes of the hour\n" + "				h - hours of the day\n"
				+ "				D - days of the month\n" + "				M - months of the year\n"
				+ "				Y - years";
	}
}
