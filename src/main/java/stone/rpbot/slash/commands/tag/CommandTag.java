package stone.rpbot.slash.commands.tag;

import stone.rpbot.slash.SlashCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class CommandTag implements SlashCommand {

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        switch (subcommand) {
        case "create":
            this.createTag(event);
            return;
        case "rate":
            
            return;
        }
    }

    private void createTag(SlashCommandInteractionEvent event) {
        TextInput name = TextInput.create("tag_name", "Name", TextInputStyle.SHORT)
            .setRequiredRange(1, 512)
            .build();
        
        TextInput description = TextInput.create("tag_description", "Description", TextInputStyle.PARAGRAPH)
            .build();
        
        TextInput superTag = TextInput.create("tag_super", "Super Tag Descriptor", TextInputStyle.SHORT)
            .build();

        Modal createModal = Modal.create("create_modal", "Tag Creation")
            .addActionRows(
                           ActionRow.of(name),
                           ActionRow.of(description),
                           ActionRow.of(superTag))
            .build();

        event.replyModal(createModal);
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public Map<String, ModalHandler> getModals() {
        Map<String, ModalHandler> modals = new HashMap<>();
        modals.put("create_modal", TagModalHandler::onCreate());
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
