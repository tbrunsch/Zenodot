package dd.kms.zenodotx;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterStream
{
	private final String	s;
	private int				position;

	public CharacterStream(String s) {
		this.s = s;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Optional<String> readRegex(Pattern regex) {
		Matcher matcher = regex.matcher(s.substring(position));
		return matcher.matches()
			? Optional.of(matcher.group())
			: Optional.empty();
	}
}
