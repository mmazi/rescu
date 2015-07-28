package si.mazi.rescu.serialization.jackson;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.util.regex.Pattern;

/**
 * Deserializing {@link Throwable} objects has caused problems on some restricted platforms
 * (in particular, GAE). This class is used to prevent these problems by making Jackson
 * ignore (some) properties declared on class Throwable.
 */
public class IgnoreThrowableProperties extends JacksonAnnotationIntrospector {

    private final Pattern IGNORED_THROWABLE_MEMBER = Pattern.compile("(cause)|(stacktrace)");

    @Override public boolean hasIgnoreMarker(final AnnotatedMember m) {
        final String memberName = m.getName();
        return super.hasIgnoreMarker(m) ||
                (m.getDeclaringClass() == Throwable.class && IGNORED_THROWABLE_MEMBER.matcher(memberName).matches());
    }
}