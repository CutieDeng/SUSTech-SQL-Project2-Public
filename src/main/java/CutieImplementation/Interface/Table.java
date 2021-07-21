package CutieImplementation.Interface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Table implements AcquisitiveEntityName{

    private String name;
    private List<Column> columnList = new ArrayList<>();

    public Table(String name, Column... columns) {
        this.name = name;
        Arrays.stream(columns).filter(Objects::nonNull).forEach(
                col -> {
                    columnList.add(col);
                    col.setTable(this);
                }
        );
    }

    /**
     * 获取表格的简称<br>
     * 当然，事实上就是表格的全名
     * @return 表格全名
     */
    @Override
    public String getSimpleName() {
        return name;
    }

    /**
     * 我们暂且把表格默认设置为抽象层级的顶层。<br>
     * 因此表格没有容器<br>
     * 当然，实际上其实是有的，但我不想再研究 Schema 的相关内容了
     * @return NULL
     */
    @Override
    public AcquisitiveEntityName getParentEntity() {
        return null;
    }
}
