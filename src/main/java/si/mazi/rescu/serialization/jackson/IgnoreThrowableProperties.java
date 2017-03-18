/*
 * Copyright (C) 2015 Matija Mazi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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