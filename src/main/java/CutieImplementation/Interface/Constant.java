package CutieImplementation.Interface;

/**
 * 常量描述<br>
 */
public class Constant<Va> extends Tuple<Va, Class<Va>> implements AcquisitiveEntityName{

    /**
     * 常量描述
     *
     * @param value1 常量值
     * @param value2 常量类型
     */
    public Constant(Va value1, Class<Va> value2) {
        super(value1, value2);
    }

    /**
     * 获得常量的名称<br>
     * @return 常量名
     */
    @Override
    public String getSimpleName() {
        if (!value2.isArray()) {
            return String.valueOf(value1);
        }
        throw new RuntimeException("暂时未完成的字符串解析：" + value1 + "\t" + value2);
    }

    /**
     * 常量没有上层抽象结构，返回空指针。
     * @return NULL
     */
    @Override
    public AcquisitiveEntityName getParentEntity() {
        return null;
    }

    @Override
    public String getName(String supName) {
        if (value2 == String.class) {
            return String.format("'%s'", this.getSimpleName());
        }
        return this.getSimpleName();
    }
}
