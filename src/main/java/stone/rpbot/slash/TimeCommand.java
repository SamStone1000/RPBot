package stone.rpbot.slash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.ZoneId;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class TimeCommand implements SlashCommand {

    private static final TimeUnit[] CLOCK_UNITS = { TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS };
    private static final Map<Character, ChronoField> SIMPLE_UNITS = new HashMap<>();

    static {
	SIMPLE_UNITS.put('h', ChronoField.HOUR_OF_DAY);
	SIMPLE_UNITS.put('m', ChronoField.MINUTE_OF_HOUR);
	SIMPLE_UNITS.put('s', ChronoField.SECOND_OF_MINUTE);
	SIMPLE_UNITS.put('d', ChronoField.DAY_OF_MONTH);
	//SIMPLE_UNITS.put('m', ChronoField.MONTH_OF_YEAR);
	SIMPLE_UNITS.put('y', ChronoField.YEAR);
    }
    
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
	String input = event.getOption("input").getAsString();
	OffsetDateTime dateTime = OffsetDateTime.now(ZoneId.of("America/Louisville"));
	for (String token : input.split(" ")) {
	    if(token.contains(":")) {
		//handleClock(dateTime, token);
	    } else {
		dateTime = handleSimpleUnit(dateTime, token);
	    }
	}
	event.reply(TimeFormat.DEFAULT.format(dateTime) + " " + dateTime).setEphemeral(true).queue();
    }

    public OffsetDateTime handleSimpleUnit(OffsetDateTime dateTime, String token) {
	Character unit = token.charAt(token.length() - 1);
	int amount = Integer.valueOf(token.substring(0, token.length() - 1));

	System.out.println(unit + " " + amount);
	ChronoField timeField = SIMPLE_UNITS.get(unit);

	if (token.charAt(0) == '+' || token.charAt(0) == '-') //means this token
	    {					              //is a relative amount
		int timeAmount = dateTime.get(timeField);
		return dateTime.with(timeField, timeAmount + amount);
	    } else //means this token is absolute and is overwriting a field
	    {
		return dateTime.with(timeField, amount);
	    }
    }
		    
    public enum Modifiers {
	RELATIVE, ABSOLUTE;

    }

    public static void main(String[] args) {
	String[] tokens = "10".split(":");
	System.out.println(Arrays.toString(tokens));
    }
}
