package cn.edu.sustech.cs307.dto;

import java.util.Objects;

public class Department {
    public int id;

    public String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Department that = (Department) o;
        // return id == that.id && name.equals(that.name); 笑死，居然不判断空指针。 -- Cutie Deng.
        return id == that.id &&
                Objects.equals(name, that.name);
    }

    public static void main(String[] args) {
        // test.
        Department department = new Department();
        Department department1 = new Department();
        department.id = 1;
        department1.id = 1;
        System.out.println(department.equals(department1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
