package stone.rpbot.slash.song;

import java.nio.file.Path;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import stone.rpbot.slash.song.FileUtil;

public abstract class SongCommandAutoComplete {
    public static void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        CommandAutoCompleteInteraction interaction = event.getInteraction();
        String group = interaction.getSubcommandGroup();
        switch (group) {
            case null:
                onEmptyGroup(interaction, interaction.getSubcommandName());
                break;
            default:
        }
    }

    private static void onEmptyGroup(CommandAutoCompleteInteraction interaction, String subcommandName) {
        switch (subcommandName) {
            case "add":
                String song = interaction.getOption("song").getAsString();
                if (FileUtil.checkFileString(song)) {
                    Path songPath = Path.of(song);
                    songPath.getParent();
                }
        }
    }
}
