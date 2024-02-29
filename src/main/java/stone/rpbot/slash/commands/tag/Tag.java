package stone.rpbot.slash.commands.tag;

import java.util.Collection;

public interface Tag {
    public String getName();
    public String getDescription();
    public Tag getSuperTag();
    public Collection<Tag> getSubTags();
}
