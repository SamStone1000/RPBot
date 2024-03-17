package stone.rpbot.slash.commands.tag;

import stone.rpbot.slash.SlashCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandTag implements SlashCommand {

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        switch (subcommand) {
        case "create":
            
    }

    @Override
    public String getManInfo() {
        return "does tag rating and stuff";
    }
}
