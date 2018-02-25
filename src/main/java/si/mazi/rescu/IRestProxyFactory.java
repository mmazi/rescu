package si.mazi.rescu;

/**
 * @see RestProxyFactory
 */
public interface IRestProxyFactory {
  /**
   * @see RestProxyFactory#createProxy(Class, String, ClientConfig, Interceptor...)
   */
  <I> I createProxy(Class<I> restInterface, String baseUrl, ClientConfig config, Interceptor... interceptors);

  /**
   * @see RestProxyFactory#createProxy(Class, String)
   */
  <I> I createProxy(Class<I> restInterface, String baseUrl);
}
