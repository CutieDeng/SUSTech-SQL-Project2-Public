package CutieImplementation.Interface;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 与条件集<br>
 * 一种条件描述方式<br>
 * 高层次的条件模块负责管理控制低层次的条件模块的关系
 */
public class AndCondition implements Condition{

    private final List<Condition> conditionList = new ArrayList<>();

    /**
     * 与条件构造器 <br>
     * 里面的所有条件都将相与形成条件集<br>
     * NULL 参数将会自动缺省
     * @param conditions 与条件组
     */
    public AndCondition(Condition... conditions) {
        Arrays.stream(conditions).filter(Objects::nonNull).forEach(conditionList::add);
    }


    /**
     * 更改器方法，返回条件句本身。<br>
     * 不是成构造器方法。<br>
     * 请不要随意使用。<br>
     * 方法过滤 null 参数，传入 NULL 将不会导致与条件集发生任何变化。<br>
     * @param condition 条件
     * @return 被修改后的与条件集.
     */
    public AndCondition addCondition(Condition condition) {
        if (condition != null) {
            conditionList.add(condition);
        }
        return this;
    }

    /**
     * 与条件是一种制约型条件集，如果没有任何条件，那么我们将默认它返回 TRUE. <br>
     * 如果与条件集中只有一个条件，那么我们将直接返回该条件。
     * @return 与条件集的 SQL 表达
     */
    @Override
    public String getConditionExpression() {
        if (conditionList.size() == 0) {
            return "TRUE";
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
