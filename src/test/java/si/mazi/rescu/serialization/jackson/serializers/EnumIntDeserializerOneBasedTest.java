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