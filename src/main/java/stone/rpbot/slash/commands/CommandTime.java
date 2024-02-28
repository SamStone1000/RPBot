package stone.rpbot.slash.commands;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.TimeFormat;
import stone.rpbot.slash.SlashCommand;

public class CommandTime implements SlashCommand {

    private static final String COMMAND_NAME = "time";
    
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
        event.reply(String.format("`%s`", getTimeStamp(input))).setEphemeral(true).queue();
    }

    public String getTimeStamp(String input) {
        OffsetDateTime dateTime = OffsetDateTime.now(ZoneId.of("America/Louisville"));
        TimeFormat format = TimeFormat.RELATIVE;
        for (String token : input.split(" ")) {
            if (token.contains(":")) {
                // handleClock(dateTime, token);
            } else if (isSimpleUnit(token)) {
                dateTime = handleSimpleUnit(dateTime, token);
            } else if (isFormatUnit(token)) {
                format = handleFormatUnit(token);
            }
        }
        return format.format(dateTime);
    }

    private TimeFormat handleFormatUnit(String token) {
        return TimeFormat.fromStyle(token);
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

    public boolean isSimpleUnit(String token) {
        char last = token.charAt(token.length() - 1);
        for (char unit : SIMPLE_FIELDS.keySet()) {
            if (unit == last)
                if (token.length() > 1) {
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    }

    public boolean isFormatUnit(String token) {
        return token.length() == 1;
    }

    public enum Modifiers {
        RELATIVE, ABSOLUTE;

    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("time", "Produces a Discord timestamp from input")
            .addOption(OptionType.STRING, "input", "Input can take the form of absolute inputs or relative inputs prefixed with a +/-", true);
    }

    @Override
    public String getManInfo() {
        return "**NAME**\n" + "		time - a slash command to make markdown timestamps\n" + "\n**SYNOPSIS**\n"
            + "		**time** [*time fields* *timestamp format*]\n" + "\n**DESCRIPTION**\n"
            + "		**time** is a command intended to offer easy and flexible timestamps. "
            + "It is designed to allow as much as possible without being needlessly complex.\n" + "\n**EXAMPLES**\n"
            + "		**time** +4h 0m 0s\n"
            + "			Makes a timestamp for 4 hours into the future but on the hour, ie if it was 1:35 PM right now this would make a timestamp for 5:00 PM\n"
            + "		**time** +1d 9h 0m 0s\n" + "			Makes a timestamp for tommorrow at 9:00 AM\n"
            + "		**time** T\n" + "			Makes a timestamp for right now but in the \"Long Time\" format\n"
            + "\n**OPTIONS**\n"
            + "		The arguments to **time** can be broken up into two groups: the time fields and timestamp format. "
            + "The order the groups appear in doesn't matter. The order the arguments inside of the groups does matter though. The arguemnts are "
            + "interpreted from left to right with each one applying sequentially. Generally this won't matter.\n"
            + "\n		**Time Fields**\n"
            + "				Time Fields are of the syntax [+-]n<time field> where the plus or minus indicate if the field is additive or subtractive. In case of no plus or minus the time field is absolute. "
            + "n is any number indicated how much of the time field is to be added/subtracted/set. <time field> is a letter indicating which field is to be modified.\n"
            + "			**Table of Time Fields**\n" + "				s - seconds of the minute\n"
            + "				m - minutes of the hour\n" + "				h - hours of the day\n"
            + "				D - days of the month\n" + "				M - months of the year\n"
            + "				Y - years\n" + "			**Table of Formats**\n"
            + "				t - short time, HH:MM\n" + "				T - long time | HH:MM:SS\n"
            + "				d - short date | MM/DD/YY, the order is probably dependent on the client's date localization\n"
            + "				D - long date | <month as a word> DD, YYYY\n"
            + "				f - long date with short time | <month> DD, YYYY at HH:MM\n"
            + "				F - long date with day of week and short time | <day of week>, <month> DD, YYYY at HH:MM";
    }
}
