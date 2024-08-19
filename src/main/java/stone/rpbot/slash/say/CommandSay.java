package stone.rpbot.slash.say;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import stone.rpbot.audio.AudioSupplier;
import stone.rpbot.audio.AudioUtils;
import stone.rpbot.audio.MainAudioSendHandler;
import stone.rpbot.slash.SlashCommand;
import stone.rpbot.audio.SoundEffect;
import stone.rpbot.util.SharedConstants;

import java.io.BufferedInputStream;
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
            ProcessBuilder pb = new ProcessBuilder("say", "-pre", "[:phoneme on]", "-a", text, "-e", "1", "-fo", "stdout:au");
            
            AudioSupplier supplier = new SoundEffect(new BufferedInputStream(pb.start().getInputStream()));

            System.out.println(text);

            MainAudioSendHandler sendHandler = AudioUtils.getOrJoin(event);
            sendHandler.add(supplier);
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
