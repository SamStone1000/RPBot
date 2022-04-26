package record;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import util.SharedConstants;

public class LyricStore extends ListenerAdapter {

	public static final String COMMAND_NAME = "lyric";
	public static final String TABLE = "LyricStore";
	
	private static final String MODAL_ID = "RPBot/lyricmodal";
	
	public LyricStore() {
		
		}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getName().equals(COMMAND_NAME))
		{
			TextInput lyric = TextInput.create("RPBot/lyric", "Lyrics", TextInputStyle.PARAGRAPH)
					.setPlaceholder("Put the song lyric here")
					.build();
			TextInput songname = TextInput.create("RPBot/songname", "Name", TextInputStyle.SHORT)
					.setPlaceholder("Put the song's name here")
					.setMaxLength(256)
					.build();
			TextInput artistname = TextInput.create("RPBot/artistname", "Artist", TextInputStyle.SHORT)
					.setPlaceholder("Put the song's artist here")
					.setMaxLength(256)
					.build();
			Modal modal = Modal.create(MODAL_ID, "Lyric Input")
					.addActionRow(lyric)
					.addActionRow(songname)
					.addActionRow(artistname)
					.build();
			event.replyModal(modal).queue();
		}
	}
	
	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if (event.getModalId().equals(MODAL_ID))
		{
			event.deferReply(true).queue();
			List<ModalMapping> modal = event.getValues();
			String[] lyric = new String[3];
			for (ModalMapping mapping : modal)
			{
				switch (mapping.getId())
				{
				case "RPBot/lyric" :
					lyric[0] = mapping.getAsString();
					break;
				case "RPBot/songname" :
					lyric[1] = mapping.getAsString();
					break;
				case "RPBot/artistname" :
					lyric[2] = mapping.getAsString();
					break;
				}
			}
			if (saveLyric(event.getMember().getIdLong(), lyric[0], lyric[1], lyric[2]))
			{
				event.getHook().editOriginal("Lyric successfully saved").queue();
			} else
			{
				event.getHook().editOriginal("Lyric failed to save :(").queue();
			}
		}
	}
	
	public static boolean saveLyric(long authorID, String lyric, String song, String artist) {
		try (PreparedStatement statement = SharedConstants.DATABASE_CONNECTION.prepareStatement("INSERT INTO "+TABLE+" VALUES(DEFAULT, ?, ?, ?, ?)"))
		{
			statement.setLong(1, authorID);
			statement.setString(2, lyric);
			statement.setString(3, song);
			statement.setString(4, artist);
			statement.execute();
			return true;
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try
			{
				SharedConstants.DATABASE_CONNECTION.commit();
			} catch (SQLException e)
			{
				// TODO Auto-generated catch block
				LoggerFactory.getLogger("Saving Lyric").warn(e.toString());
			}
		}
	}
}
