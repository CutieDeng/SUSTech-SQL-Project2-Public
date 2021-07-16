package cn.edu.sustech.cs307.exception;

/**
 * 违反数据完整性异常<br>
 * 该错误应该在所有的 add 方法中被抛出，抛出理由：某些传入 add 的参数非法。<br>
 * 原文：In all add method, if some of parameters are invalid, throw IntegrityViolationException<br>
 */
public class IntegrityViolationException extends RuntimeException {
    public IntegrityViolationException() {
    }

    public IntegrityViolationException(String message) {
        super(message);
    }

    public IntegrityViolationException(Throwable cause) {
        super(cause);
    }
}
