package cn.edu.sustech.cs307.exception;

/**
 * 实体未找到异常<br>
 * 在删除方法中，如果没能找到相关的实体；或在 get 方法中，没有特定 id 的实体，则抛出该异常。<br>
 * 原文：
 * in remove method, if there is no Entity about specific, throw EntityNotFoundException.<br>
 * in get method, if there is no Entity about specific id, throw EntityNotFoundException.<br>
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException() {
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
