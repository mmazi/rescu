/*
 * Copyright (C) 2016 Matija Mazi
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

package si.mazi.rescu;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static si.mazi.rescu.RestInvocation.appendPath;
import static si.mazi.rescu.RestInvocation.getInvocationUrl;

public class InvocationUrlTest {

    @Test
    public void shouldHandleSlashesWhenAppendingPaths1() throws Exception {
        assertThat(appendPath("one", "two")).isEqualTo("one/two");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingPaths2() throws Exception {
        assertThat(appendPath("one/", "/two")).isEqualTo("one/two");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingPaths3() throws Exception {
        assertThat(appendPath("one/", "two")).isEqualTo("one/two");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingPaths4() throws Exception {
        assertThat(appendPath("one", "/two")).isEqualTo("one/two");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingEmptyPath1() throws Exception {
        assertThat(appendPath("one", "")).isEqualTo("one");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingEmptyPath2() throws Exception {
        assertThat(appendPath("one/", "/")).isEqualTo("one/");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingEmptyPath3() throws Exception {
        assertThat(appendPath("one/", "")).isEqualTo("one/");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingEmptyPath4() throws Exception {
        assertThat(appendPath("one", "/")).isEqualTo("one/");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingToEmptyPath1() throws Exception {
        assertThat(appendPath("", "two")).isEqualTo("two");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingToEmptyPath2() throws Exception {
        assertThat(appendPath("/", "/two")).isEqualTo("/two");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingToEmptyPath3() throws Exception {
        assertThat(appendPath("/", "two")).isEqualTo("/two");
    }

    @Test
    public void shouldHandleSlashesWhenAppendingToEmptyPath4() throws Exception {
        assertThat(appendPath("", "/two")).isEqualTo("/two");
    }

    @Test
    public void shouldWorkWithNormalStuff() throws Exception {
        String url = getInvocationUrl("http://www.example.com", "/apiPath/", "param=value");

        assertUrl(url);
        assertThat(url).isEqualTo("http://www.example.com/apiPath/?param=value");
    }

    @Test
    public void shouldComposeNonEmptyElementsWithFullSlashes() throws Exception {
        String url = getInvocationUrl("http://www.example.com/", "/apiPath/", "param=value");

        assertUrl(url);
        assertThat(url).isEqualTo("http://www.example.com/apiPath/?param=value");
    }

    @Test
    public void shouldComposeEmptyElementsWithFullSlashes() throws Exception {
        String url = getInvocationUrl("http://www.example.com/", "/", "param=value");

        assertUrl(url);
        assertThat(url).isEqualTo("http://www.example.com/?param=value");
    }

    @Test
    public void shouldComposeNonEmptyElementsWithNoSlashes() throws Exception {
        String url = getInvocationUrl("http://www.example.com", "apiPath", "param=value");

        assertUrl(url);
        assertThat(url).isEqualTo("http://www.example.com/apiPath?param=value");
    }

    @Test
    public void shouldSupportEmptyPathWithNoSlash() throws Exception {
        String url = getInvocationUrl("http://www.example.com", "", "param=value");

        assertUrl(url);
        assertThat(url).isEqualTo("http://www.example.com?param=value");
    }

    @Test
    public void shouldSupportEmptyPathAsSlash() throws Exception {
        String url = getInvocationUrl("http://www.example.com", "/", "param=value");

        assertUrl(url);
        assertThat(url).isEqualTo("http://www.example.com/?param=value");
    }

    @Test
    public void shouldKeepFinalSlash() throws Exception {
        String url = getInvocationUrl("http://www.example.com/", "", "param=value");

        assertUrl(url);
        assertThat(url).isEqualTo("http://www.example.com/?param=value");
    }

    private void assertUrl(String url) {
        assertThat(url).startsWith("http");
        assertThat(url).matches("^https?://[a-z].+$");
        assertThat(url.substring(7)).doesNotContain("//");
    }
}