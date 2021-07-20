package CutieImplementation.Interface;

import java.util.*;

/**
 * 或条件集<br>
 * 是一种条件类型，属于扩展性的条件集合<br>
 */
public class OrCondition implements Condition{

    /**
     * 或条件集数据结构
     */
    private final List<Condition> conditionList = new ArrayList<>();

    /**
     * 或条件集构造器
     * @param conditions 或条件类型情况
     */
    public OrCondition(Condition... conditions) {
        Arrays.stream(conditions).filter(Objects::nonNull).forEach(conditionList::add);
    }

    /**
     * 或条件添加器，更改器方法<br>
     * 包含对 NULL 和自身引用的判断，禁止在集合中添加它本身作为其中元素，
     * 预防可能导致的栈溢出问题。<br>
     * @param condition 或条件成员
     * @return 该或条件集
     */
    public OrCondition addCondition(Condition condition) {
        if (condition != null && condition != this) {
            conditionList.add(condition);
        }
        return this;
    }

    /**
     * 或条件集是一种拓展性的条件集<br>
     * 如果里面没有任何元素，默认值为 FALSE.<br>
     * 否则以 OR 的形式把条件展开。
     */
    @Override
    public String getConditionExpression() {
        if (conditionList.size() == 0) {
            return "FALSE";
        }
        if (conditionList.size() == 1) {
            return conditionList.get(0).getConditionExpression();
        }
        Iterator<Condition> conditionIterator = conditionList.iterator();
        boolean andFlag = false;
        StringBuilder builder = new StringBuilder();
        while (conditionIterator.hasNext()) {
            String conditionExpression = conditionIterator.next().getConditionExpression();
            if (andFlag) {
                builder.append("AND ");
            }
            builder.append("(").append(conditionExpression).append(")\n");
            andFlag = true;
        }
        return builder.toString();
    }
}
