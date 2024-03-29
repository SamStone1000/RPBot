package stone.rpbot.slash.commands.tag;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

public class TagModalHandler {
    
    public void onCreateTag(ModalInteractionEvent event, Map<String, ModalMapping> mappings) {
        DatabaseTagManager.createTag(mappings.get("tag_name"), mappings.get("tag_description"), DatabaseTagManager.getTag(mappings.get("tag_super")));
        event.reply("Done!").setEphemeral(true).queue();
    }
