package CutieImplementation.Interface;

/**
 * AcquisitiveEntityName 可读实体，实体名存在<br>
 * 实现该接口意味着我们将会调用相关实体的获取名称方法，
 * 以便于自动构建 SQL 表达式。<br>
 * SQL 表达式将在之后被构造。
 */
public interface AcquisitiveEntityName {

    /**
     * 获取该实体的名称（可被数据库识别的名称）<br>
     * 可以通过重写 getSimpleName() 来达到调用它的目的。<br>
     * 传入参数为比该实体高一层次的实体名，以便于形成索引。<br>
     * @param supName 上一层次的实体名
     * @return 该实体的名称描述，包含对上层实体的描述
     */
    default String getName(String supName) {
        String shownName = getSimpleName();
        if (
                (CutieConfig.getInstance().isDoubleQuotationPreferred()) ||
                        (!shownName.toLowerCase().equals(shownName))
        ) {
            shownName = "\"" + getSimpleName() + "\"";
        }
        return supName != null ?
                String.format("%s.%s", supName, shownName) :
                shownName;
    }

    /**
     * 获取该实体的全名<br>
     * 使用该方法必须先重写 getParentEntity() 方法<br>
     * 该方法将会递归调用所有内容相关内容
     * @return 该实体的全名具体描述
     */
    default String getName() {
        AcquisitiveEntityName parent = getParentEntity();
        return getName(parent != null ? parent.getName() : null);
    }

    /**
     * 获得该实体的简单名称<br>
     * 该方法必须被重写，所有的名称调用都将依赖于该方法<br>
     * @return 该实体的简单名称
     */
    String getSimpleName();

    /**
     * 获取该实体的父实体，或容器<br>
     * @return 返回该实体的容器
     */
    default AcquisitiveEntityName getParentEntity () {
        throw new RuntimeException("Method getParentEntity() 未被重写");
    }
}
