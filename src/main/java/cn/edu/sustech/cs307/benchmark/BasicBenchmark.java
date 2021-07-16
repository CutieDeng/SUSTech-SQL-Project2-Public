package cn.edu.sustech.cs307.benchmark;

import cn.edu.sustech.cs307.config.Config;
import cn.edu.sustech.cs307.factory.ServiceFactory;

/**
 * BasicBenchMark 是一个基准的检测的基本类，同时也是一个抽象类——不要轻易对它进行实例化。<br>
 * 它里面有一个可以被子类看到的参数，同时这个参数也会在 new 的瞬间一起被初始化：serviceFactory. <br>
 * 在它的初始化过程，会直接调用静态方法 {@link Config#getServiceFactory()}. <br>
 * 在原代码中，它仅仅被设置成受保护的，但为了更加安全起见的目的，我把它设置成 final, 以避免
 * 意料之外的更改或删除。
 */
public abstract class BasicBenchmark {
    /**
     * service factory 核心参数，来自 {@link Config#getServiceFactory()} 方法调用的结果。<br>
     * 该参数被设置为 final, 请不要尝试修改它。<br>
     * 尽管它的描述是一个接口，但是在我们的 project 中它应该被希望是一个已经被重新完全好的具体实现。
     */
    protected final ServiceFactory serviceFactory = Config.getServiceFactory();
}
