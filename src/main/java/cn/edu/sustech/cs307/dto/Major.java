package cn.edu.sustech.cs307.dto;

import java.util.Objects;

public class Major {
    public int id;
    public String name;
    public Department department;

    private static final long meaninglessValue = 202107171754L;

    @Override
    public int hashCode() {
        return Objects.hash(id, meaninglessValue);
    }

    /**
     * 重写相等方法，只判定 ID 相等。
     * @param obj 对比对象
     * @return True 表示相等。
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Major &&
                this.id == ((Major) obj).id;
    }
}
