package CutieImplementation.Interface;

/**
 * 简单泛型二元组<br>
 * @param <K> value1 的类型
 * @param <V> value2 的类型
 */
public class Tuple <K, V> {
    public final K value1;
    public final V value2;

    /**
     * 简单二元组的唯一构造器
     * @param value1 传入的第一个值
     * @param value2 传入的第二个值
     */
    public Tuple(K value1, V value2) {
        this.value1 = value1;
        this.value2 = value2;
    }
}
