package CutieImplementation.Interface;

/**
 * 条件子句<br>
 * 当一个类被说成是条件子句的时候，它不得不面临以下几种情况：它是一个根条件/一个
 */
public interface Condition {
    /**
     * 获取条件子句的 SQL 表达式
     * @return 条件子句 SQL 表达式。
     */
    String getConditionExpression();
}
