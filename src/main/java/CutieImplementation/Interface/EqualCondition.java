package CutieImplementation.Interface;

import javax.annotation.Nonnull;

/**
 * 全等简单条件<br>
 */
public class EqualCondition implements Condition{
    private final AcquisitiveEntityName leftValue;
    private final AcquisitiveEntityName rightValue;

    public EqualCondition(@Nonnull AcquisitiveEntityName leftValue, @Nonnull AcquisitiveEntityName rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    /**
     * 等值条件表述
     * @return 等值条件的 SQL 语法描述
     */
    @Override
    public String getConditionExpression() {
        return String.format("%s = %s", leftValue.getName(), rightValue.getName());
    }
}
