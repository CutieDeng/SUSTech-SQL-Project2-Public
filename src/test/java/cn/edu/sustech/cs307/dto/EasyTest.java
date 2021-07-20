package cn.edu.sustech.cs307.dto;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

/**
 * Cutie 同学的无聊测试类，请不要实例化它。
 */
public class EasyTest {

    @Test
    public void testParrelTest() {
        IntStream.range(0, 56).parallel().forEach(i -> System.out.println(Thread.currentThread().getName()));
    }
}
