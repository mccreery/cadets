package cadets1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/** A person and their known network */
public class Person {
	/** iSolutions ID */
	public final String id;
	/** Display name */
	public final String name;

	/** Every person this person has a direct link to */
	private List<Person> related = new ArrayList<Person>();

	/** @param id iSolutions ID
	 * @param name Display name
	 * @param related Shortcut for {@link #addRelated(Person...)}
	 * @see Person */
	public Person(String id, String name, Person... related) {
		this.id = id;
		this.name = name;
		addRelated(related);
	}

	/** @see #addRelated(List) */
	public void addRelated(Person... related) {
		addRelated(Arrays.asList(related));
	}
	/** Merges the list of people into this person's related people */
	public void addRelated(List<Person> related) {
		for(Person p : related) {
			if(!related.equals(this) && !this.related.contains(p))
				this.related.add(p);
		}
	}
	/** @return {@code true} if this person knows {@code person} */
	public boolean knows(Person person) {
		return related.contains(person);
	}

	@Override
	public String toString() {
		// Draw a tree for this network
		StringBuilder builder = new StringBuilder();
		List<Person> unique = new ArrayList<Person>(); // Allow everyone on the tree
		this.construct(builder, "", unique); // Start at no indent, hence ""
		return builder.toString();
	}

	/** Used in tree graphic generation */
	private static final String WALL = "|", FOOT = "`", BRANCH = "-";
	/** Generates a tree ASCII art for this node in the tree and its children, recursively
	 * @param builder The target of the ASCII art
	 * @param prefix The indent inherited from this person's parent and the decoration character
	 * @param unique Keeps track of who has been seen in this tree so no person is repeated */
	protected void construct(StringBuilder builder, String prefix, List<Person> unique) {
		unique.add(this);
		// Generates display name e.g. John A Smith (jas)
		builder.append(prefix).append(BRANCH).append(name).append(" (").append(id).append(")\n");

		/* Calculates the prefix that should be used for the leaves of this node
		 * If the prefix indicates a foot, the children of this element should have a space
		 * instead of | as the parent has ended */
		String deeper = prefix.endsWith(FOOT) ? prefix.substring(0, prefix.length() - 1) + ' ' : prefix;

		for(Iterator<Person> it = related.iterator(); it.hasNext();) {
			Person person = it.next();

			if(!unique.contains(person)) { // Ignore duplicates
				// Puts `- on the final node in a branch instead of |- to make it look pretty
				person.construct(builder, deeper + (it.hasNext() ? WALL : FOOT), unique);
			}
		}
	}

	@Override
	public boolean equals(Object other) {
		// Tests against IDs, not references
		return other instanceof Person && ((Person)other).id.equals(id);
	}
}
