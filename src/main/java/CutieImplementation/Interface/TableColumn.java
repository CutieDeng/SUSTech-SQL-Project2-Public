package CutieImplementation.Interface;

/**
 * TableColumn 描述具体某个表格的某一列，是一种高度抽象的描述方式，它并不真正创建原始的数据结构描述信息<br>
 */
public class TableColumn extends Tuple<Table, Column> implements AcquisitiveEntityName{
    /**
     * TableColumn 构造器，描述具体的列归属
     * @param value1 列归属的表格
     * @param value2 列本身
     */
    public TableColumn(Table value1, Column value2) {
        super(value1, value2);
    }

    /**
     * 获取该列的具体名称描述<br>
     * @return 这个方法不建议被直接调用
     */
    @Override
    public String getSimpleName() {
        return value2.getName(value1.getName());
    }

    /**
     * 返回该列的名称描述，其实与 getSimpleName() 等价，不过降低了类的耦合度。
     * @return 该列的全名
     */
    @Override
    public String getName() {
        return getSimpleName();
    }
}
