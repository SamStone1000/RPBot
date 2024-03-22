package stone.rpbot.slash.commands.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LazyTag implements Tag {

    static TagFactory FACTORY;

    private final long id;
    
    private final String name;
    private final String shortDescription;
    private final String description;

    private final long superTag;
    private final Set<Long> subTags;

    private final Set<String> aliases;
    private final Map<Long, Tag.Rating> ratings;

    @Override
    public long getID() {
        return this.id;
    }
    
    @Override
    public String getName() {
        return this.name;
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
        return this.superTag == -1 ? null : FACTORY.makeTag(superTag);
    }

    @Override
    public Set<Tag> getSubTags() {
        Set<Tag> out = new HashSet<>();
        for (long sub : this.subTags) {
            out.add(FACTORY.makeTag(sub));
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tag))
            return false;
        if (!(o instanceof LazyTag))
            return false; //go by fields someday
        LazyTag other = (LazyTag) o;
        return this.id == other.id;
    }
    
    public LazyTag(long id, String name, String shortDescription, String description, long superTag, Set<Long> subTags, Set<String> aliases, Map<Long, Tag.Rating> ratings) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.superTag = superTag;
        this.subTags = subTags;
        this.aliases = aliases;
        this.ratings = ratings;
    }

    public static class Builder {
        private Long id;
        private String name = null;
        private String shortDescription = null;
        private String description = null;
        private Long superTag = null;
        private Set<Long> subTags = new HashSet<>();
        private Set<String> aliases = new HashSet<>();
        private Map<Long, Tag.Rating> ratings = new HashMap<>();

        public LazyTag build() {
            return new LazyTag(id, name, shortDescription, description, superTag, subTags, aliases, ratings);
        }

        public Builder setID(long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
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
                                                                  
