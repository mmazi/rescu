/*
 * Copyright (C) 2013 Matija Mazi
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
 */
package si.mazi.rescu;

import org.testng.annotations.Test;
import si.mazi.rescu.dto.DummyAccountInfo;

import jakarta.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matija Mazi <br>
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class AnnotationUtilsTest {

    @Test
    public void testGetFromMethodOrClass() throws Exception {
        Path path = AnnotationUtils.getFromMethodOrClass(ExampleService.class.getMethod("getTicker", String.class, String.class), Path.class);
        assertThat(path).isNotNull();
        assertThat(path.value()).isEqualTo("{ident: [a-Z]+}_{currency}/ticker");

        Path pathFromIntf = AnnotationUtils.getFromMethodOrClass(ExampleService.class.getMethod("getInfo", Long.class, Long.class), Path.class);
        assertThat(pathFromIntf).isNotNull();
        assertThat(pathFromIntf.value()).isEqualTo("api/{version}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetMethodAnnotations() throws Exception {
        Method method = ExampleService.class.getMethod("testJsonBody", DummyAccountInfo.class);
        testForAnns(method, Arrays.asList(POST.class, Consumes.class));
        testForAnns(method, Arrays.<Class<? extends Annotation>>asList(Path.class));
        testForAnns(method, Arrays.<Class<? extends Annotation>>asList());

        List<Class<? extends Annotation>> classes = Arrays.asList(POST.class, GET.class, DELETE.class, PUT.class, HEAD.class);
        Map<Class<? extends Annotation>,Annotation> map = AnnotationUtils.getMethodAnnotationMap(method, classes);
        assertThat(map.keySet()).isEqualTo(sorted(Arrays.<Class<? extends Annotation>>asList(POST.class)));
        assertThat(map.get(POST.class)).isInstanceOf(POST.class);
    }

    private void testForAnns(Method method, List<Class<? extends Annotation>> classes) {
        Map<Class<? extends Annotation>,Annotation> map = AnnotationUtils.getMethodAnnotationMap(method, classes);
        assertThat(sorted(map.keySet())).isEqualTo(sorted(classes));
        for (Class<? extends Annotation> annClass : classes) {
            assertThat(map.get(annClass)).isInstanceOf(annClass);
        }
    }

    private static <T> Set<Class<? extends T>> sorted(Collection<Class<? extends T>> classes) {
        Comparator<Class<? extends T>> comparator = new Comparator<Class<? extends T>>() {
            public int compare(Class<? extends T> o1, Class<? extends T> o2) {
                return o1.getCanonicalName().compareTo(o2.getCanonicalName());
            }
        };
        SortedSet<Class<? extends T>> sortedSet = new TreeSet<>(comparator);
        sortedSet.addAll(classes);
        return sortedSet;
    }
}
