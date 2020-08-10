package dd.kms.zenodot.tests.tokenstream;

abstract class TokenDescription
{
	private final int	numSpacesBefore;

	TokenDescription(int numSpacesBefore) {
		this.numSpacesBefore = numSpacesBefore;
	}

	abstract String getTokenText();

	int getNumSpacesBefore() {
		return numSpacesBefore;
	}

	int getTokenLength() {
		return numSpacesBefore + getTokenText().length();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < numSpacesBefore; i++) {
			builder.append(" ");
		}
		builder.append(getTokenText());
		return builder.toString();
	}
}
