package stone.rpbot.slash.commands.tag;

/**
 * The entity that controls all the tags
 * Can make new tags, delete tags, create new root tags, etc.
 */
public interface TagManager<T extends Tag> {
    public T createTag(String name, String shortDescription, String description, T superTag);
    public boolean addAliases(T tag, String alias...);
    public boolean removeAliases(T tag, String alias...);
    public boolean rateTag(T tag, long user, Tag.Rating rating);

    public T getTag(String tag);
}
