package dd.kms.zenodot.impl.wrappers;

import com.google.common.reflect.TypeToken;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeInfoImpl implements TypeInfo
{
	private static final Pattern	SIMPLE_TYPE_NAME_EXTRACTION_PATTERN	= Pattern.compile("([\\w]+\\.)+([\\w]+)");
	private static final String		SIMPLE_TYPE_NAME_REPLACEMENT		= "$2";

	private final TypeToken<?> typeToken;

	public TypeInfoImpl(@Nullable TypeToken<?> typeToken) {
		this.typeToken = typeToken;
	}

	@Override
	public Class<?> getRawType() {
		return typeToken == null ? null : typeToken.getRawType();
	}

	@Override
	public Type getType() {
		return typeToken == null ? null : typeToken.getType();
	}

	@Override
	public TypeInfo getComponentType() {
		if (typeToken == null) {
			return InfoProvider.NO_TYPE;
		}
		TypeToken<?> componentType = typeToken.getComponentType();
		return componentType == null ? InfoProvider.NO_TYPE : new TypeInfoImpl(componentType);
	}

	@Override
	public boolean isPrimitive() {
		return typeToken != null && typeToken.isPrimitive();
	}

	@Override
	public boolean isArray() {
		return typeToken.isArray();
	}

	@Override
	public boolean isSupertypeOf(TypeInfo obj) {
		if (typeToken == null) {
			return false;
		}
		if (!(obj instanceof TypeInfoImpl)) {
			return false;
		}
		TypeInfoImpl that = (TypeInfoImpl) obj;
		return that.typeToken != null && this.typeToken.isSupertypeOf(that.typeToken);
	}

	@Override
	public TypeInfo getSubtype(Class<?> subclass) {
		return typeToken == null ? InfoProvider.NO_TYPE : new TypeInfoImpl(typeToken.getSubtype(subclass));
	}

	@Override
	public TypeInfo resolveType(Type type) {
		return typeToken == null ? InfoProvider.NO_TYPE : new TypeInfoImpl(typeToken.resolveType(type));
	}

	@Override
	public String getName() {
		return	typeToken == null	? null :
				isArray()			? getComponentType().getName() + "[]"
									: typeToken.toString();
	}

	@Override
	public String getSimpleName() {
		Matcher matcher = SIMPLE_TYPE_NAME_EXTRACTION_PATTERN.matcher(getName());
		return matcher.replaceAll(SIMPLE_TYPE_NAME_REPLACEMENT);
	}

	@Override
	public String toString() {
		if (typeToken == null) {
			return this == InfoProvider.NO_TYPE ? "none" : null;
		}
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TypeInfoImpl typeInfo = (TypeInfoImpl) o;
		return Objects.equals(typeToken, typeInfo.typeToken);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeToken);
	}
}
