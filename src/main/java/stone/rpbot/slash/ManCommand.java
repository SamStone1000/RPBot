package stone.rpbot.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ManCommand implements SlashCommand {

	public static final String OPTION_COMMAND = "command";
	public final SlashManager manager;

	public ManCommand(SlashManager manager) {
		this.manager = manager;
	}

	@Override
	public void onSlashCommand(SlashCommandInteractionEvent event) {
		String commandStr = event.getOption(OPTION_COMMAND).getAsString();
		SlashCommand command = manager.getSlashCommand(commandStr);
		String page = command != null ? command.getManInfo() : null;
		if (page == null) {
			page = "The specified man page does not exist at this time.";
		}
		event.reply(page).setEphemeral(true).queue();
	}

	@Override
	public String getManInfo() {
		return null;
	}

}
