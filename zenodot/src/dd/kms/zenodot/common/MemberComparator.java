package dd.kms.zenodot.common;

import java.lang.reflect.Member;
import java.util.Comparator;

/**
 * Compares members by name with the following exceptions:
 * <ul>
 *     <li>Members of the root class get a higher priority.</li>
 *     <li>Members of Object get a lower priority.</li>
 * </ul>
 */
class MemberComparator implements Comparator<Member>
{
	/**
	 * Members of the root class will be prioritized.
	 */
	private final Class<?> rootClass;

	MemberComparator(Class<?> rootClass) {
		this.rootClass = rootClass;
	}

	@Override
	public int compare(Member member1, Member member2) {
		Class<?> declaringClass1 = member1.getDeclaringClass();
		Class<?> declaringClass2 = member2.getDeclaringClass();

		if (declaringClass1 == rootClass) {
			if (declaringClass2 != rootClass) {
				return -1;
			}
		} else {
			if (declaringClass2 == rootClass) {
				return 1;
			}
		}
		if (declaringClass1 == Object.class) {
			if (declaringClass2 != Object.class) {
				return 1;
			}
		} else {
			if (declaringClass2 == Object.class) {
				return -1;
			}
		}

		String name1 = member1.getName();
		String name2 = member2.getName();
		return name1.toLowerCase().compareTo(name2.toLowerCase());
	}
}
