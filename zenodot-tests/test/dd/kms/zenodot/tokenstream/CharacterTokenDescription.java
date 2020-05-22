package dd.kms.zenodot.tokenstream;

class CharacterTokenDescription extends TokenDescription
{
	private final char	c;

	public CharacterTokenDescription(char c, int numSpacesBefore) {
		super(numSpacesBefore);
		this.c = c;
	}

	char getCharacter() {
		return c;
	}

	@Override
	String getTokenText() {
		return String.valueOf(c);
	}
}
