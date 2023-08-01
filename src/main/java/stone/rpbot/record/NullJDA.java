package stone.rpbot.record;

import java.time.Duration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleConnectionMetadata;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.sticker.StickerPack;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.entities.sticker.StickerUnion;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.requests.restaction.GuildAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import okhttp3.OkHttpClient;

public class NullJDA implements JDA {

	public static final JDA EMPTY = new NullJDA();

	@Override
	public SnowflakeCacheView<StageChannel> getStageChannelCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<ThreadChannel> getThreadChannelCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<Category> getCategoryCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<TextChannel> getTextChannelCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<NewsChannel> getNewsChannelCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<ForumChannel> getForumChannelCache() {
		return null;
	}

	@Override
	public Status getStatus() {
		return null;
	}

	@Override
	public EnumSet<GatewayIntent> getGatewayIntents() {
		return null;
	}

	@Override
	public EnumSet<CacheFlag> getCacheFlags() {
		return null;
	}

	@Override
	public boolean unloadUser(long userId) {
		return false;
	}

	@Override
	public long getGatewayPing() {
		return 0l;
	}

	@Override
	public JDA awaitStatus(Status status, Status... failOn) throws InterruptedException {
		return null;
	}

	@Override
	public boolean awaitShutdown(long duration, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public int cancelRequests() {
		return 0;
	}

	@Override
	public ScheduledExecutorService getRateLimitPool() {
		return null;
	}

	@Override
	public ScheduledExecutorService getGatewayPool() {
		return null;
	}

	@Override
	public ExecutorService getCallbackPool() {
		return null;
	}

	@Override
	public OkHttpClient getHttpClient() {
		return null;
	}

	@Override
	public DirectAudioController getDirectAudioController() {
		return null;
	}

	@Override
	public void setEventManager(IEventManager manager) {
		return;
	}

	@Override
	public void addEventListener(Object... listeners) {
		return;
	}

	@Override
	public void removeEventListener(Object... listeners) {
		return;
	}

	@Override
	public List<Object> getRegisteredListeners() {
		return null;
	}

	@Override
	public RestAction<List<Command>> retrieveCommands(boolean withLocalizations) {
		return null;
	}

	@Override
	public RestAction<Command> retrieveCommandById(String id) {
		return null;
	}

	@Override
	public RestAction<Command> upsertCommand(CommandData command) {
		return null;
	}

	@Override
	public CommandListUpdateAction updateCommands() {
		return null;
	}

	@Override
	public CommandEditAction editCommandById(String id) {
		return null;
	}

	@Override
	public RestAction<Void> deleteCommandById(String commandId) {
		return null;
	}

	@Override
	public RestAction<List<RoleConnectionMetadata>> retrieveRoleConnectionMetadata() {
		return null;
	}

	@Override
	public RestAction<List<RoleConnectionMetadata>> updateRoleConnectionMetadata(
			Collection<? extends RoleConnectionMetadata> records) {
		return null;
	}

	@Override
	public GuildAction createGuild(String name) {
		return null;
	}

	@Override
	public RestAction<Void> createGuildFromTemplate(String code, String name, Icon icon) {
		return null;
	}

	@Override
	public CacheView<AudioManager> getAudioManagerCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<User> getUserCache() {
		return null;
	}

	@Override
	public List<Guild> getMutualGuilds(User... users) {
		return null;
	}

	@Override
	public List<Guild> getMutualGuilds(Collection<User> users) {
		return null;
	}

	@Override
	public CacheRestAction<User> retrieveUserById(long id) {
		return null;
	}

	@Override
	public SnowflakeCacheView<Guild> getGuildCache() {
		return null;
	}

	@Override
	public Set<String> getUnavailableGuilds() {
		return null;
	}

	@Override
	public boolean isUnavailable(long guildId) {
		return false;
	}

	@Override
	public SnowflakeCacheView<Role> getRoleCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<ScheduledEvent> getScheduledEventCache() {
		return null;
	}

	@Override
	public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
		return null;
	}

	@Override
	public CacheRestAction<PrivateChannel> openPrivateChannelById(long userId) {
		return null;
	}

	@Override
	public SnowflakeCacheView<RichCustomEmoji> getEmojiCache() {
		return null;
	}

	@Override
	public RestAction<StickerUnion> retrieveSticker(StickerSnowflake sticker) {
		return null;
	}

	@Override
	public RestAction<List<StickerPack>> retrieveNitroStickerPacks() {
		return null;
	}

	@Override
	public IEventManager getEventManager() {
		return null;
	}

	@Override
	public SelfUser getSelfUser() {
		return null;
	}

	@Override
	public Presence getPresence() {
		return null;
	}

	@Override
	public ShardInfo getShardInfo() {
		return null;
	}

	@Override
	public String getToken() {
		return null;
	}

	@Override
	public long getResponseTotal() {
		return 0l;
	}

	@Override
	public int getMaxReconnectDelay() {
		return 0;
	}

	@Override
	public void setAutoReconnect(boolean reconnect) {
		return;
	}

	@Override
	public void setRequestTimeoutRetry(boolean retryOnTimeout) {
		return;
	}

	@Override
	public boolean isAutoReconnect() {
		return false;
	}

	@Override
	public boolean isBulkDeleteSplittingEnabled() {
		return false;
	}

	@Override
	public void shutdown() {
		return;
	}

	@Override
	public void shutdownNow() {
		return;
	}

	@Override
	public RestAction<ApplicationInfo> retrieveApplicationInfo() {
		return null;
	}

	@Override
	public JDA setRequiredScopes(Collection<String> scopes) {
		return null;
	}

	@Override
	public String getInviteUrl(Permission... permissions) {
		return null;
	}

	@Override
	public String getInviteUrl(Collection<Permission> permissions) {
		return null;
	}

	@Override
	public ShardManager getShardManager() {
		return null;
	}

	@Override
	public RestAction<Webhook> retrieveWebhookById(String webhookId) {
		return null;
	}

	@Override
	public <T extends Channel> T getChannelById(Class<T> type, String id) {
		return null;
	}

	@Override
	public GuildChannel getGuildChannelById(String id) {
		return null;
	}

	@Override
	public GuildChannel getGuildChannelById(long id) {
		return null;
	}

	@Override
	public GuildChannel getGuildChannelById(ChannelType type, String id) {
		return null;
	}

	@Override
	public GuildChannel getGuildChannelById(ChannelType type, long id) {
		return null;
	}

	@Override
	public List<StageChannel> getStageChannelsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public StageChannel getStageChannelById(String id) {
		return null;
	}

	@Override
	public StageChannel getStageChannelById(long id) {
		return null;
	}

	@Override
	public List<StageChannel> getStageChannels() {
		return null;
	}

	@Override
	public List<ThreadChannel> getThreadChannelsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public ThreadChannel getThreadChannelById(String id) {
		return null;
	}

	@Override
	public ThreadChannel getThreadChannelById(long id) {
		return null;
	}

	@Override
	public List<ThreadChannel> getThreadChannels() {
		return null;
	}

	@Override
	public List<Category> getCategoriesByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public Category getCategoryById(String id) {
		return null;
	}

	@Override
	public Category getCategoryById(long id) {
		return null;
	}

	@Override
	public List<Category> getCategories() {
		return null;
	}

	@Override
	public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public TextChannel getTextChannelById(String id) {
		return null;
	}

	@Override
	public TextChannel getTextChannelById(long id) {
		return null;
	}

	@Override
	public List<TextChannel> getTextChannels() {
		return null;
	}

	@Override
	public List<NewsChannel> getNewsChannelsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public NewsChannel getNewsChannelById(String id) {
		return null;
	}

	@Override
	public NewsChannel getNewsChannelById(long id) {
		return null;
	}

	@Override
	public List<NewsChannel> getNewsChannels() {
		return null;
	}

	@Override
	public List<VoiceChannel> getVoiceChannelsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public VoiceChannel getVoiceChannelById(String id) {
		return null;
	}

	@Override
	public VoiceChannel getVoiceChannelById(long id) {
		return null;
	}

	@Override
	public List<VoiceChannel> getVoiceChannels() {
		return null;
	}

	@Override
	public List<ForumChannel> getForumChannelsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public ForumChannel getForumChannelById(String id) {
		return null;
	}

	@Override
	public ForumChannel getForumChannelById(long id) {
		return null;
	}

	@Override
	public List<ForumChannel> getForumChannels() {
		return null;
	}

	@Override
	public RestAction<Long> getRestPing() {
		return null;
	}

	@Override
	public JDA awaitStatus(Status status) throws InterruptedException {
		return null;
	}

	@Override
	public JDA awaitReady() throws InterruptedException {
		return null;
	}

	@Override
	public boolean awaitShutdown(Duration timeout) throws InterruptedException {
		return false;
	}

	@Override
	public boolean awaitShutdown() throws InterruptedException {
		return false;
	}

	@Override
	public RestAction<List<Command>> retrieveCommands() {
		return null;
	}

	@Override
	public RestAction<Command> retrieveCommandById(long id) {
		return null;
	}

	@Override
	public CommandCreateAction upsertCommand(String name, String description) {
		return null;
	}

	@Override
	public CommandEditAction editCommandById(long id) {
		return null;
	}

	@Override
	public RestAction<Void> deleteCommandById(long commandId) {
		return null;
	}

	@Override
	public List<AudioManager> getAudioManagers() {
		return null;
	}

	@Override
	public List<User> getUsers() {
		return null;
	}

	@Override
	public User getUserById(String id) {
		return null;
	}

	@Override
	public User getUserById(long id) {
		return null;
	}

	@Override
	public User getUserByTag(String tag) {
		return null;
	}

	@Override
	public User getUserByTag(String username, String discriminator) {
		return null;
	}

	@Override
	public List<User> getUsersByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public CacheRestAction<User> retrieveUserById(String id) {
		return null;
	}

	@Override
	public List<Guild> getGuilds() {
		return null;
	}

	@Override
	public Guild getGuildById(String id) {
		return null;
	}

	@Override
	public Guild getGuildById(long id) {
		return null;
	}

	@Override
	public List<Guild> getGuildsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public List<Role> getRoles() {
		return null;
	}

	@Override
	public Role getRoleById(String id) {
		return null;
	}

	@Override
	public Role getRoleById(long id) {
		return null;
	}

	@Override
	public List<Role> getRolesByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public List<ScheduledEvent> getScheduledEvents() {
		return null;
	}

	@Override
	public ScheduledEvent getScheduledEventById(String id) {
		return null;
	}

	@Override
	public ScheduledEvent getScheduledEventById(long id) {
		return null;
	}

	@Override
	public List<ScheduledEvent> getScheduledEventsByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public <T extends Channel> T getChannelById(Class<T> type, long id) {
		return null;
	}

	@Override
	public List<PrivateChannel> getPrivateChannels() {
		return null;
	}

	@Override
	public PrivateChannel getPrivateChannelById(String id) {
		return null;
	}

	@Override
	public PrivateChannel getPrivateChannelById(long id) {
		return null;
	}

	@Override
	public RestAction<PrivateChannel> openPrivateChannelById(String userId) {
		return null;
	}

	@Override
	public List<RichCustomEmoji> getEmojis() {
		return null;
	}

	@Override
	public RichCustomEmoji getEmojiById(String id) {
		return null;
	}

	@Override
	public RichCustomEmoji getEmojiById(long id) {
		return null;
	}

	@Override
	public List<RichCustomEmoji> getEmojisByName(String name, boolean ignoreCase) {
		return null;
	}

	@Override
	public JDA setRequiredScopes(String... scopes) {
		return null;
	}

	@Override
	public RestAction<Webhook> retrieveWebhookById(long webhookId) {
		return null;
	}

	@Override
	public AuditableRestAction<Integer> installAuxiliaryPort() {
		return null;
	}

}
