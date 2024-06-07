package stone.rpbot.slash;

import java.io.IOException;
import java.nio.file.Path;

import javax.sound.sampled.UnsupportedAudioFileException;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import stone.rpbot.audio.AudioQueue;
import stone.rpbot.audio.AudioUtils;
import stone.rpbot.audio.MainAudioSendHandler;
import stone.rpbot.audio.Track;

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

    private void onEmptyGroup(String subcommandName, SlashCommandInteraction interaction, AudioQueue queue) {
        switch (subcommandName) {
            case "toggle":
                queue.togglePlaying();
                interaction.reply("toggled!").queue();
                break;
            case "add":
                String songName = interaction.getOption("song").getAsString();
                Path root = Path.of("/home", "stone", "music");
                try {
                    Track.File songFile = new Track.File(root.resolve(songName), "idk", "nah");
                    queue.addTrack(songFile);
                } catch (UnsupportedAudioFileException | IOException e) {
                    interaction.reply("Something went wrong :( (Error 1)" + e).queue();
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
