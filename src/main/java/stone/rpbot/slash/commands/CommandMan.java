package stone.rpbot.slash.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import stone.rpbot.slash.SlashCommand;
import stone.rpbot.slash.SlashManager;

public class CommandMan implements SlashCommand {

    private static final String COMMAND_NAME = "man";
    private static final String OPTION_COMMAND = "command";
    public final SlashManager manager;

    public CommandMan(SlashManager manager) {
        this.manager = manager;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        String commandStr = event.getOption(OPTION_COMMAND).getAsString();
        String page;
        if (commandStr.isEmpty()) {
            page = "What manual page do you want?\n" +
                "Try man man";
        } else {
            SlashCommand command = manager.getSlashCommand(commandStr);
            if (command != null) {
                page = command.getManInfo();
                if (page == null) {
                    page = "The specified man page does not exist at this time.";
                }
            } else {
                page = "That command does not exist.";
            }
        }
        event.reply(page).setEphemeral(true).queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(COMMAND_NAME, "Gets manual for specified command")
            .addOption(OptionType.STRING, OPTION_COMMAND, "The name of the command to get the manual for", true);
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getManInfo() {
        return "**NAME**\n"+
        		"  man - an interface to command manuals\n"
        		+ "**SYNOPSIS**\n"
        		+ "  man <command name>";
    }

}
