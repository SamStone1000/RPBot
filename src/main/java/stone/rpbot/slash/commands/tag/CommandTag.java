package stone.rpbot.slash.commands.tag;

import stone.rpbot.slash.SlashCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandTag implements SlashCommand {

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String getManInfo() {
        return "does tag rating and stuff";
    }
}
