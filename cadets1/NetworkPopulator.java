package cadets1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Handles searching through related people pages to expand a network */
public class NetworkPopulator {
	/** Matches on things that look like links to people, yielding ID and name groups */
	private static final Pattern LINK_PATTERN = Pattern.compile("<a href='https://secure.ecs.soton.ac.uk/people/([a-z0-9]+)'>([^<]+)");

	/** Holds all the previously searched people, so they will not be searched again */
	private final List<Person> cache;
	/** The maximum recursion depth
	 * @see #populate(Person, int) */
	private final int maxDepth;
	/** The first person in the network to search */
	private Person root;

	/** @see NetworkPopulator
	 * @param root The first person in the network to search
	 * @param maxDepth The maximum recursion depth
	 * @see #populate() */
	public NetworkPopulator(Person root, int maxDepth) {
		cache = new ArrayList<Person>();
		this.maxDepth = maxDepth;
		this.root = root;
	}

	/** As {@link #populate(Person, int)} but using the root node */
	public void populate() {populate(root, 0);}
	/** Scrapes the site for related people recursively. {@code node} will be linked to other members of the network
	 * @param node The person to start from
	 * @param depth The current recursion depth the algorithm has reached. This should not exceed {@link #maxDepth} */
	private void populate(Person node, int depth) {
		cache.add(node); // Make sure this person is never searched again

		try {
			// People branching off this person that haven't been discovered yet
			List<Person> toExplore = new ArrayList<Person>();

			for(Matcher m = NetworkSearch.searcher.search(LINK_PATTERN, "https://secure.ecs.soton.ac.uk/people/%s/related_people", node.id); m.find();) {
				Person person = new Person(m.group(1), m.group(2)); // Deserialize person

				if(cache.contains(person)) {
					// Use existing cached reference
					node.addRelated(cache.get(cache.indexOf(person)));
				} else {
					// Search and cache this new person later
					node.addRelated(person);
					toExplore.add(person);
				}
			}
			System.out.println("Found " + toExplore.size() + " new people at level " + depth);

			// If we can go deeper, explore the children
			if(depth < maxDepth) {
				for(Person leaf : toExplore)
					populate(leaf, depth + 1); // New person to explore
			}
		} catch(IOException e) {
			System.err.println("Failed to build network for " + node.id);
		}
	}
}
