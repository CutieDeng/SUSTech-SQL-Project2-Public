package CutieImplementation;

import cn.edu.sustech.cs307.factory.ServiceFactory;

public class CutieFactory extends ServiceFactory {

    private volatile static CutieFactory INSTANCE = null;

    public static final ServiceFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (INSTANCE) {
                if (INSTANCE == null) {
                    INSTANCE = new CutieFactory();
                }
            }
        }
        return INSTANCE;
    }

    private CutieFactory() {
        // todo: 创建一个相关的工厂类，里面提供相关的所有方法实现。
    }

}
