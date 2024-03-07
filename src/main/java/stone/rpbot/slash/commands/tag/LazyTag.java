package stone.rpbot.slash.commands.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LazyTag implements Tag {

    private static final TagFactory factory = new DatabaseTagFactory();

    private final String primaryAlias;
    private final String shortDescription;
    private final String description;

    private final long superTag;
    private final Set<Long> subTags;

    private final Set<String> aliases;
    private final Map<Long, Tag.Rating> ratings;

    @Override
    public String getPrimaryAlias() {
        return this.primaryAlias;
    }

    @Override
    public String getShortDescription() {
        return this.shortDescription;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Tag getSuperTag() {
        factory.makeTag(superTag);
    }

    @Override
    public Set<Tag> getSubTags() {
        Set<Tag> out = new HashSet<>();
        for (long sub : this.subTags) {
            out.add(factory.makeTag(sub));
        }
        return out;
    }
    
    @Override
    public Set<String> getAliases() {
        return this.aliases;
    }
    
    @Override
    public Map<Long, Tag.Rating> getRatings() {
        return this.ratings;
    }
    
    public LazyTag(String primary, String shortDescription, String description, long superTag, Set<Long> subTags, Set<String> aliases, Map<Long, Tag.Rating> ratings) {
        this.primaryAlias = primary;
        this.shortDescription = shortDescription;
        this.description = description;
        this.superTag = superTag;
        this.subTags = subTags;
        this.aliases = aliases;
        this.ratings = ratings;
    }

    public static class Builder {

        private String primaryAlias;
        private String shortDescription;
        private String description;
        private long superTag;
        private Set<Long> subTags = new HashSet<>();
        private Set<String> aliases = new HashSet<>();
        private Map<Long, Tag.Rating> ratings = new HashMap<>();

        public Tag build() {
            return new LazyTag(primaryAlias, shortDescription, description, superTag, subTags, aliases, ratings);
        }

        public Builder setPrimaryAlias(String alias) {
            this.primaryAlias = alias;
            return this;
        }

        public Builder addAlias(String alias) {
            this.aliases.add(alias);
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setShortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
            return this;
        }

        public Builder setSuperTag(long tag) {
            this.superTag = tag;
            return this;
        }

        public Builder addSubTag(long tag) {
            this.subTags.add(tag);
            return this;
        }

        public Builder addRating(long user, Tag.Rating rating) {
            this.ratings.put(user, rating);
            return this;
        }
    }
}
                                                                  
