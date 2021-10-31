import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.Procedure;
import net.dv8tion.jda.internal.entities.MemberImpl;

public class Counting implements Procedure<Message> {

	private int asyncCount;
	private String[] terms;
	private TreeMap<String, TreeMap<Long, MutableInt>> maps = new TreeMap<>();
	private Pattern[] regex;
	
	public Counting(String[] terms, Pattern[] regex, TreeMap<String, TreeMap<Long, MutableInt>> maps) {
		this.terms = terms;
		this.maps = maps;
		this.regex = regex;
	}
	@Override
	public boolean execute(Message msg) {
		String content = msg.getContentRaw().toLowerCase();
		User author = msg.getAuthor();
		if (author.isBot()) return true;
		Long id = author.getIdLong();
		for (int i = 0; i < terms.length; i++) {
		String term = terms[i];
		Matcher matcher = regex[i].matcher(content);
		int count = 0;
		while (matcher.find()) count++;
		if (count > 0) {
			TreeMap<Long, MutableInt> map = maps.get(term);
			MutableInt currentCount = map.get(id);
			if (currentCount != null)
			{
				currentCount.add(count);
			}
		}
		}
		return true;
	}
	
	public String toString() {
		return maps.toString();
	}
	
	public TreeMap<String, TreeMap<Long, MutableInt>> getMaps() {
		return maps;
	}

}
