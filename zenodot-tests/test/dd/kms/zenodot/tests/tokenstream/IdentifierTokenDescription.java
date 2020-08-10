package dd.kms.zenodot.tests.tokenstream;

class IdentifierTokenDescription extends TokenDescription
{
	private final String	identifier;

	IdentifierTokenDescription(String identifier, int numSpacesBefore) {
		super(numSpacesBefore);
		this.identifier = identifier;
	}

	String getIdentifier() {
		return identifier;
	}

	@Override
	String getTokenText() {
		return identifier;
	}
}
