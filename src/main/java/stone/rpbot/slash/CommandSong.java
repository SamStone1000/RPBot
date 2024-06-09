package stone.rpbot.slash;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.UnsupportedAudioFileException;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import stone.rpbot.audio.AudioQueue;
import stone.rpbot.audio.AudioUtils;
import stone.rpbot.audio.MainAudioSendHandler;
import stone.rpbot.audio.Track;
import stone.rpbot.slash.song.SongCommandAutoComplete;
import stone.rpbot.slash.song.SongFileAdderWalker;

public class CommandSong implements SlashCommand {

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        MainAudioSendHandler handler = AudioUtils.getOrJoin(event);
        AudioQueue queue = handler.getSongQueue();

        SlashCommandInteraction interaction = event.getInteraction();
        String group = interaction.getSubcommandGroup();
        switch (group) {
            case null:
                onEmptyGroup(interaction.getSubcommandName(), interaction, queue);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SongCommandAutoComplete.onAutoComplete(event);
    }

    private void onEmptyGroup(String subcommandName, SlashCommandInteraction interaction, AudioQueue queue) {
        switch (subcommandName) {
            case "toggle":
                queue.togglePlaying();
                interaction.reply("toggled!").queue();
                break;
            case "add":
                String songName = interaction.getOption("song").getAsString();
                Path root = Path.of("/home", "stone", "music");
                FileVisitor<Path> visitor = new SongFileAdderWalker(queue);
                try {
                Files.walkFileTree(root.resolve(songName), visitor);
            } catch (IOException e) {
                interaction.reply("Something went wrong :( (error 2)").queue();
                return;
            }
                interaction.reply("Queued up song!").queue();
                break;
            case "next":
                queue.skip();
                interaction.reply("skipped!").queue();
                break;
            case "volume":
                String volume = interaction.getOption("volume").getAsString();
                try {
                int volumeInt = Integer.parseInt(volume);
                if (volume.startsWith("-") || volume.startsWith("+")) {
                    queue.addVolume(volumeInt);
                } else {
                    queue.setVolume(volumeInt);
                }
                } catch (NumberFormatException e) {
                    interaction.reply("Volume should be a number, either without a prefix to set the volume to an absolute value, or with a +/- to change it by a relative amount").queue();
                    return;
                }
                interaction.reply("volume set!").queue();
                break;
        }
    }

    @Override
    public String getManInfo() {
        return "music";
    }

}
