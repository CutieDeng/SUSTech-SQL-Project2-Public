package CutieImplementation.Interface;

import javax.annotation.Nonnull;

/**
 * Column 表示表格中的一列<br>
 * 它并不存储任何实际的表格信息，它只赋值描述表格的某种属性：该列的名字/别名，
 * 和该列对应的数据类型。<br>
 **/
public class Column extends Tuple<String, Class<?>> implements AcquisitiveEntityName {
    /**
     * Column 唯一构造器<br>
     * 传入参数不应该为空指针
     * @param value1 传入该列的名称
     * @param value2 传入该列的值的属性在 Java 中的类对象
     */
    public Column(@Nonnull String value1,@Nonnull Class<?> value2) {
        super(value1, value2);
        assert value1 != null && value2 != null;
    }

    /**
     * @return 该实体，列的列名。
     */
    @Override
    public String getSimpleName() {
        return value1;
    }

    @Override
    public String toString() {
        return value1;
    }
}
