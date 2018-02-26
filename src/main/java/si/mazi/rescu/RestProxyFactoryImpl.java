package si.mazi.rescu;

/**
 * The default implementation of {@link IRestProxyFactory} that calls {@link RestProxyFactory}
 */
public class RestProxyFactoryImpl implements IRestProxyFactory {
  @Override
  public <I> I createProxy(Class<I> restInterface, String baseUrl, ClientConfig config, Interceptor... interceptors) {
    return RestProxyFactory.createProxy(restInterface, baseUrl, config, interceptors);
  }

  @Override
  public <I> I createProxy(Class<I> restInterface, String baseUrl) {
    return RestProxyFactory.createProxy(restInterface, baseUrl);
  }
}
