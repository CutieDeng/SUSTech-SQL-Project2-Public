package CutieImplementation;

import CutieImplementation.Interface.*;

import java.util.Arrays;

public class test {
    public static void main(String[] args) {

        Column name = new Column("名字", String.class);
        Column ID = new Column("学号", int.class);

        Table studentTable = new Table("学生表格", name, ID);

        Constant<String> constant = new Constant<>("Cutie", String.class);
        Constant<Integer> vaConstant = new Constant<>(123, int.class);


        EqualCondition equalCondition = new EqualCondition(new TableColumn(studentTable, name), constant);
        System.out.println(equalCondition.getConditionExpression());
    }
}
