package dd.kms.zenodotx;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterStream
{
	private final String	text;
	private int				position;

	public CharacterStream(String text) {
		this.text = text;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Optional<String> readRegex(Pattern regex) {
		Matcher matcher = regex.matcher(text.substring(position));
		if (matcher.find() && matcher.start() == 0) {
			String parsedString = matcher.group();
			position += parsedString.length();
			return Optional.of(parsedString);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return text.substring(0, position)
			+ "^"
			+ text.substring(position);
	}
}
