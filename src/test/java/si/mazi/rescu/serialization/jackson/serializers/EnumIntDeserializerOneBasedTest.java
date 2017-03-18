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

package si.mazi.rescu.serialization.jackson.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumIntDeserializerOneBasedTest {

    @Test
    public void shouldDeserialize1AsFirstEnum() throws Exception {
        Entity entity = new ObjectMapper().readValue("{\"type\":\"1\"}", Entity.class);
        assertThat(entity.type).isEqualTo(Entity.Type.a);
    }

    @Test
    public void shouldDeserialize2AsSecondEnum() throws Exception {
        Entity entity = new ObjectMapper().readValue("{\"type\":\"2\"}", Entity.class);
        assertThat(entity.type).isEqualTo(Entity.Type.b);
    }

    static class Entity {
        public Type type;

        @JsonDeserialize(using = Type.Deserializer.class)
        enum Type {
            a, b;

            static class Deserializer extends EnumIntDeserializerOneBased<Type> {
                protected Deserializer() { super(Type.class); }
            }
        }
    }
}