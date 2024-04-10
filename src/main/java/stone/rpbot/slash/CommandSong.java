package stone.rpbot.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import stone.rpbot.audio.AudioQueue;
import stone.rpbot.audio.AudioUtils;
import stone.rpbot.audio.MainAudioSendHandler;
import stone.rpbot.audio.Track;

import java.io.IOException;
import java.nio.file.Path;
import javax.sound.sampled.UnsupportedAudioFileException;

public class CommandSong implements SlashCommand {
        
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        MainAudioSendHandler handler = AudioUtils.getOrJoin(event);
        AudioQueue queue = new AudioQueue();
        Path root = Path.of("/home", "sam", "tmp");
        try {
            queue.addTrack(new Track.File(root.resolve("1.wav"), "1", "Me"));
            queue.addTrack(new Track.File(root.resolve("2.wav"), "2", "Me"));
            queue.addTrack(new Track.File(root.resolve("3.wav"), "3", "Me"));
            queue.addTrack(new Track.File(root.resolve("4.wav"), "4", "Me"));
        } catch (UnsupportedAudioFileException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        queue.start();
        handler.add(queue);
        event.reply("done!").queue();
    }        

    @Override
    public String getManInfo() {
        return "music";
    }

}
