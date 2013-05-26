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

import org.testng.Assert;
import org.testng.annotations.Test;
import si.mazi.rescu.dto.DummyAccountInfo;
import si.mazi.rescu.utils.AssertUtil;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Matija Mazi <br/>
 */
public class AnnotationUtilsTest {

    public static final Comparator<Class<? extends Annotation>> CLASS_NAME_COMPARATOR = new Comparator<Class<? extends Annotation>>() {
        public int compare(Class<? extends Annotation> o1, Class<? extends Annotation> o2) {
            return o1.getCanonicalName().compareTo(o2.getCanonicalName());
        }
    };

    @Test
    public void testGetFromMethodOrClass() throws Exception {

        Path path = AnnotationUtils.getFromMethodOrClass(ExampleService.class.getMethod("getTicker", String.class, String.class), Path.class);
        AssertUtil.isTrue(path.value().equals("{ident}_{currency}/ticker"), "Wrong path.");

        Path pathFromIntf = AnnotationUtils.getFromMethodOrClass(ExampleService.class.getMethod("getInfo", Long.class, Long.class), Path.class);
        AssertUtil.isTrue(pathFromIntf.value().equals("api/2"), "Wrong path: " + pathFromIntf.value());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetMethodAnnotations() throws Exception {
        Method method = ExampleService.class.getMethod("testJsonBody", DummyAccountInfo.class);
        testForAnns(method, Arrays.<Class<? extends Annotation>>asList(POST.class, Consumes.class));
        testForAnns(method, Arrays.<Class<? extends Annotation>>asList(Path.class));
        testForAnns(method, Arrays.<Class<? extends Annotation>>asList());

        List<Class<? extends Annotation>> classes = Arrays.asList(POST.class, GET.class, DELETE.class, PUT.class, HEAD.class);
        Map<Class<? extends Annotation>,Annotation> map = AnnotationUtils.getMethodAnnotationMap(method, classes);
        Assert.assertEquals(map.keySet(), Arrays.asList(POST.class));
        Assert.assertTrue(POST.class.isInstance(map.get(POST.class)));
    }

    private void testForAnns(Method method, List<Class<? extends Annotation>> classes) {
        Map<Class<? extends Annotation>,Annotation> map = AnnotationUtils.getMethodAnnotationMap(method, classes);
        Assert.assertEquals(sort(map.keySet()), sort(classes));
        for (Class<? extends Annotation> annClass : classes) {
            Assert.assertTrue(annClass.isInstance(map.get(annClass)));
        }
    }

    private static Set<Class<? extends Annotation>> sort(Collection<Class<? extends Annotation>> classes) {
        SortedSet<Class<? extends Annotation>> sortedSet = new TreeSet<Class<? extends Annotation>>(CLASS_NAME_COMPARATOR);
        sortedSet.addAll(classes);
        return sortedSet;
    }
}
