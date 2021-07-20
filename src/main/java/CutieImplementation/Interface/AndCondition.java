package CutieImplementation.Interface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndCondition {

    private List<Condition> conditionList = new ArrayList<>();

    /**
     * 与条件构造器，
     * @param conditions
     */
    public AndCondition(Condition... conditions) {
        conditionList.addAll(Arrays.asList(conditions));
    }


    public AndCondition addCondition(Condition condition) {
        conditionList.add(condition);
        return this;
    }

}
