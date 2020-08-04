package dd.kms.zenodot.classesForTest.visibility;

import dd.kms.zenodot.impl.utils.ClassUtils;

public class PublicOuterClass
{

	public static String	publicField			= "public";
	protected static String	protectedField		= "protected";
	static String			packagePrivateField	= "package private";
	private static String	privateField		= "private";

	public static String publicMethod() { return "public"; }
	protected static String protectedMethod() { return "protected"; }
	static String packagePrivateMethod() { return "package private"; }
	private static String privateMethod() { return "private"; }

	public static class PublicInnerClass {}
	protected static class ProtectedInnerClass {}
	static class PackagePrivateInnerClass {}
	private static class PrivateInnerClass {}
}
