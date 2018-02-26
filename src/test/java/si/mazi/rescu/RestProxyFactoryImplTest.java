package si.mazi.rescu;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RestProxyFactoryImplTest {

    @Test
    public void testCreateProxy() {
        IRestProxyFactory f = new RestProxyFactoryImpl();
        ExampleService proxy = f.createProxy(ExampleService.class, "http://example.com/api");

        assertThat(proxy).isNotNull();
    }

    @Test
    public void testCreateProxy1() {
        IRestProxyFactory f = new RestProxyFactoryImpl();
        ExampleService proxy = f.createProxy(ExampleService.class, "http://example.com/api", new ClientConfig());

        assertThat(proxy).isNotNull();
    }
}
