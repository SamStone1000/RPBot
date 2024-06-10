package stone.rpbot.slash.song;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class SongCommandAutoComplete {
	public record PathAutoComplete(Path potential, int similarity) {
		public PathAutoComplete(Path potential, Path real) {
			this(potential, calculateSimilarity(real, potential));
		}

		private static int calculateSimilarity(Path real, Path potential) {
			String realString = real.toString();
			String potentialString = potential.toString();
			int i = 0;
			for (; i < realString.length(); i++) {
				if (realString.charAt(i) != potentialString.charAt(i)) {
					return -1;
				}
			}
			return i;
		}
	}

	public static void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
		System.out.println("autocompleteting");
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
				Path realPath = Path.of(song);
				Path songPath = Path.of("/home", "stone", "music").resolve(realPath);
				Path directory;
				Path partialName;
				if (song.length() == 0) {
					directory = Path.of("/home", "stone", "music");
					partialName = Path.of("");
				} else if (song.endsWith("/")) {
					directory = songPath;
					partialName = Path.of("");
				} else {
					directory = songPath.getParent();
					partialName = songPath.getFileName();
				}

				List<PathAutoComplete> autocompletes = new ArrayList<>();
				try (Stream<Path> potentialNames = Files.walk(directory, 1, FileVisitOption.FOLLOW_LINKS)) {
					potentialNames.filter(path -> !path.equals(directory)).forEach(path -> {
						PathAutoComplete autoComplete = new PathAutoComplete(path.getFileName(), partialName);
						if (autoComplete.similarity() >= 0)
							autocompletes.add(autoComplete);
					});
					autocompletes.sort((a, b) -> a.similarity() - b.similarity());
				} catch (IOException e) {
					System.out.println(e.toString());
					return;
				}
				System.out.println(autocompletes);
				interaction.replyChoiceStrings(autocompletes.stream().limit(OptionData.MAX_CHOICES)
						.map(autoComplete -> autoComplete.potential().toString()).toList()).queue();
			}
		}
	}
}
