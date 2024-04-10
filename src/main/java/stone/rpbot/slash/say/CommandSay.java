package stone.rpbot.slash.say;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import stone.rpbot.audio.AudioUtils;
import stone.rpbot.audio.MainAudioSendHandler;
import stone.rpbot.slash.SlashCommand;
import stone.rpbot.util.SharedConstants;

import java.lang.Exception;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommandSay implements SlashCommand {

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        try {
            String text = event.getOption("text").getAsString();
            Path tempFile = Files.createTempFile("rpbot-say", ".wav").toAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder("say", "-pre", "[:phoneme on]", "-a", text, "-e", "1", "-fo", tempFile.toString());
            pb.start().waitFor();
            
            MainAudioSendHandler sendHandler = AudioUtils.getOrJoin(event);
            sendHandler.addTempAudioFile(tempFile);
            event.reply("alright").setEphemeral(true).queue();
        } catch (Exception e) {
            event.reply(e.toString()).setEphemeral(true).queue();
        }
    }

    @Override
    public String getManInfo() {
        return "Does the funny astronaut voice";
    }
}
