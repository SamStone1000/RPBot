package stone.rpbot.record;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import stone.rpbot.util.Helper;
import stone.rpbot.util.SharedConstants;
import stone.rpbot.util.UnbanTask;

public class KickedUserHelper extends ListenerAdapter {

	private static Logger logger = LoggerFactory.getLogger("KickedUserRoles");
	private static Timer timer = new Timer();
	private static File unBanFile = new File(SharedConstants.ROLES_FOLDER + "banDurations.txt");

	public KickedUserHelper() {
		// jank
		new File(SharedConstants.ROLES_FOLDER).mkdirs();
	}

	public static void banUser(Member member, String reason) {
		saveUser(member);
		member.ban(0, TimeUnit.SECONDS).reason(reason).queue();
	}

	public static void tempBan(Member member, String reason, long millis) {
		banUser(member, reason);
		// member.getUser().openPrivateChannel().complete().sendMessage("https://discord.gg/rbeWFEsxhP").queue();
		Date date = new Date(System.currentTimeMillis() + millis);
		timer.schedule(new UnbanTask(member), date);
		writeUnBan(member, date);
	}

	public static void readRoles(Member member) {
		String id = member.getId();
		Guild guild = member.getGuild();
		File file = new File(SharedConstants.ROLES_FOLDER + id + ".roles");
		List<Role> roles = new ArrayList<Role>();
		try (InputStream in = new FileInputStream(file); BufferedInputStream buffer = new BufferedInputStream(in)) {
			int nickLength = buffer.read();
			if (nickLength != 0) {
				byte[] nickBytes = new byte[nickLength];
				buffer.read(nickBytes);
				String nick = new String(nickBytes, StandardCharsets.UTF_16BE);
				guild.modifyNickname(member, nick).queue();
			}

			byte[] roleBytes = new byte[Long.BYTES];

			while (buffer.read(roleBytes) > 0) {// stop searching if the end of file has been reached
				Role role = guild.getRoleById(Helper.bytesToLong(roleBytes));
				if (role != null)
					roles.add(role);
				else
					logger.debug("Role that used to exist found, discarding");

			}
			guild.modifyMemberRoles(member, roles).queue();
		} catch (IOException e) {
			logger.warn("Caught exception while reading roles");
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		readRoles(member);
		logger.info("Added roles back to " + member);
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		saveUser(event.getMember());
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		saveUser(event.getMember());
	}

	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if (event.getName().equals("kick")) {
			if (event.getMember().getPermissions().contains(Permission.KICK_MEMBERS)) {
				logger.debug("Kicking " + event.getTargetMember());
				kickUser(event.getTargetMember());
				// kickedUsers.add(event.getTargetMember().getIdLong());
				// writeLongs(kickedUsers);
				event.reply(event.getTargetMember().getEffectiveName() + " failed the vibecheck").queue();
			} else
				event.reply("literally said you had to have permission ediot").queue();
		}
	}

	private static void kickUser(Member member) {
		saveUser(member);
		member.kick().queue();
	}

	private static void saveUser(Member member) {
		List<Role> roles = member.getRoles();
		File file = new File(SharedConstants.ROLES_FOLDER + member.getId() + ".roles");

		try (FileOutputStream writer = new FileOutputStream(file);
				BufferedOutputStream buffer = new BufferedOutputStream(writer)) {
			file.createNewFile();
			String nick = member.getNickname();
			if (nick == null) {
				buffer.write(0);
			} else {
				byte[] nickBytes = nick.getBytes(StandardCharsets.UTF_16BE);
				buffer.write(nickBytes.length);
				buffer.write(nickBytes);
			}
			for (Role role : roles) {
				if (member.getGuild().getBotRole().canInteract(role))
					buffer.write(Helper.toBytes(role.getIdLong()));
			}

		} catch (IOException e) {
			logger.warn("Caught exception while writing roles");
		}
	}

	@Override
	public void onGuildUnban(GuildUnbanEvent event) {
		long id = event.getUser().getIdLong();
		removeUnBan(id);
	}

	private void removeUnBan(long id) {
		if (unBanFile.exists()) {
			byte[] entireFile = new byte[(int) unBanFile.length()];
			try (InputStream in = new FileInputStream(unBanFile);
					BufferedInputStream buffer = new BufferedInputStream(in)) {
				buffer.read(entireFile);
			} catch (IOException e) {
				logger.warn(e.toString());
			}

			try (OutputStream out = new FileOutputStream(unBanFile);
					BufferedOutputStream buffer = new BufferedOutputStream(out)) {
				byte[] userIdBytes = new byte[Long.BYTES];
				byte[] dateBytes = new byte[Long.BYTES];
				byte[] guildBytes = new byte[Long.BYTES];
				for (int offset = 0; offset < entireFile.length;) {
					System.arraycopy(entireFile, offset, userIdBytes, 0, userIdBytes.length);
					offset += userIdBytes.length; // continue past the user id
					if (Helper.bytesToLong(userIdBytes) != id) {
						System.arraycopy(entireFile, offset, guildBytes, 0, guildBytes.length);
						offset += guildBytes.length; // continue past the guild id
						System.arraycopy(entireFile, offset, dateBytes, 0, dateBytes.length);
						buffer.write(userIdBytes);
						buffer.write(guildBytes);
						buffer.write(dateBytes);
					}
					offset += dateBytes.length; // continue past the date
				}
			} catch (IOException e) {
				logger.warn(e.toString());
			}
		}
	}

	private static void writeLongs(SortedSet<Long> kickedUsers2) {
		File file = new File(SharedConstants.ROLES_FOLDER + "jank.txt");
		try (FileOutputStream writer = new FileOutputStream(file);
				BufferedOutputStream buffer = new BufferedOutputStream(writer)) {
			file.createNewFile();
			for (long role : kickedUsers2) {
				buffer.write(Helper.toBytes(role));
			}

		} catch (IOException e) {
			logger.warn("Caught exception while writing longs");
			logger.warn(e.toString());
		}
	}

	public static BigInteger kickForVore(Member member, int voreCount) {
		int consequenceMeter = 0;
		Guild guild = member.getGuild();
		long id = member.getIdLong();
		if (!member.getRoles().contains(guild.getRoleById(949142927567908886l)))
			consequenceMeter++;
		if (id == 248241320441806851l)
			consequenceMeter++;
		if (id == 282722155085692929l) // for an endosomatophillia enjoyer
			consequenceMeter = -1;
		final BigInteger banDuration;
		switch (consequenceMeter) {
		case 0:
			banDuration = BigInteger.valueOf(voreCount);
			break;
		case 1:
			banDuration = BigInteger.valueOf(voreCount).pow(2);
			break;
		case 2:
			banDuration = BigInteger.TWO.pow(voreCount);
			break;
		default:
			banDuration = BigInteger.ZERO;
			break;
		}

		if (!banDuration.equals(BigInteger.ZERO)) {
			BigInteger milliBan = banDuration.multiply(BigInteger.valueOf(60 * 1000))
					.add(BigInteger.valueOf(System.currentTimeMillis())); // convert from minutes to milliseconds
			PrivateChannel channel = member.getUser().openPrivateChannel().complete();
			channel.sendMessage("You have been banned for " + banDuration + " minutes\nhttps://discord.gg/rbeWFEsxhP")
					.complete();
			banUser(member, "nom");
			if (milliBan.bitLength() < 64) {// if larger then max long, just gonna forget about it instead of keeping
											// track of it
				Date date = new Date(milliBan.longValue());
				timer.schedule(new UnbanTask(member), date);
				writeUnBan(member, date);
			}
		}
		return banDuration;
	}

	private static void writeUnBan(Member member, Date date) {
		try (FileOutputStream fos = new FileOutputStream(unBanFile, true);
				BufferedOutputStream buffer = new BufferedOutputStream(fos)) {
			buffer.write(Helper.toBytes(member.getIdLong()));
			buffer.write(Helper.toBytes(member.getGuild().getIdLong()));
			buffer.write(Helper.toBytes(date.getTime()));
		} catch (IOException e) {
			logger.warn(e.toString());
		}
	}

	public static void saveAll(Guild guild) {
		List<Member> members = guild.getMembers();
		for (Member member : members) {
			saveUser(member);
			logger.debug("Saved " + member.getEffectiveName());
		}
	}
}
