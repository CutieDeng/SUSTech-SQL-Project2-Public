package cn.edu.sustech.cs307.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务工厂类，抽象类。<br>
 * 存储了相关服务类对应的具体实例。<br>
 * 提供了两个公开方法：<br>
 * {@link ServiceFactory#createService(Class 服务类)} 泛型访问器方法，可以获取对应的服务类的具体实例。<br>
 * {@link ServiceFactory#registerService(Class 服务类接口, Object 具体服务实例)} 受保护的方法。
 * 用于注册相关的服务具体实现。<br>
 * 该方法希望在被具体实现(继承)的 service factory 中的某个过程被调用(即注册相关的所有服务), 笔者
 * 建议可以在初始化的时候便将所有服务类进行注册。<br>
 */
public abstract class ServiceFactory {
    /**
     * 描述相关服务接口和实例的映射关系，用于回应访问器方法 {@link ServiceFactory#createService(Class)} 的询问。
     */
    private final Map<Class<?>, Object> services = new HashMap<>();

    /**
     * 原文有误：该方法并没有真正创建一个相关的服务实例，它实际上仅仅只是从注册表中获取相关的实例内容罢了。<br>
     * 特标注[原文有误导倾向].<br>
     * 原文：
     * Create a service instance of the given service class.
     *
     * @param serviceClass the requested service class.
     * @return an instance of the service.
     */
    public <T> T createService(Class<T> serviceClass) {
        try {
            return (T) services.get(serviceClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注册一个服务接口，并给出它的具体实现实例。
     * <br>
     * 原文：
     * Register a service implementation class.
     *
     * @param serviceClass the service interface.
     * @param implementationInstance the service implementation instance.
     */
    protected <T> void registerService(Class<T> serviceClass, T implementationInstance) {
        services.put(serviceClass, implementationInstance);
    }
}
