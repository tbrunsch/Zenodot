package dd.kms.zenodotx.java.rule;

public class SubPackageRule extends AbstractPackageRule<String>
{
	@Override
	protected String getParentPackageName(String parentPackageName) {
		return parentPackageName;
	}
}
