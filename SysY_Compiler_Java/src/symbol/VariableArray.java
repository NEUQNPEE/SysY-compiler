package symbol;

/**
 * @Author : NieFire planet_class@foxmail.com
 * @Date : 2023-11-11 00:05:04
 * @LastEditors : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 01:06:48
 * @FilePath : \SysYCompiler\src\Symbol\VariableArray.java
 * @Description : 变量数组
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 *    @Copyright (c) 2023 by NieFire, All Rights Reserved.
 */
public class VariableArray<T> extends Symbol {
    /**
     * dim: 维数,单个值为0，数组参照维数
     * dimSize 各维长度。大小为dim
     * val 数值, 大小为各纬长度的乘积
     */
    private int dim;
    private Integer[] dimSize;
    private T[] val;

    public VariableArray() {
    }

    /**
     * 变量数组构造器
     */
    public class VariableArrayBuilder extends SymbolBuilder {
        VariableArray<T> variableArray = new VariableArray<T>();

        public VariableArrayBuilder basicInfo(String name, SymbolType type, int level) {
            this.variableArray.name = name;
            this.variableArray.type = type;
            this.variableArray.level = level;
            return this;
        }

        // 开辟数组空间
        public VariableArrayBuilder allocate(int dim, Integer[] dimSize) {
            this.variableArray.dim = dim;
            this.variableArray.dimSize = dimSize;
            return this;
        }

        // 给数组赋值
        public VariableArrayBuilder value(T[] val) {
            this.variableArray.val = val;
            return this;
        }

        public VariableArrayBuilder value(int dim, Integer[] dimSize, T[] val) {
            this.variableArray.dim = dim;
            this.variableArray.dimSize = dimSize;
            this.variableArray.val = val;
            return this;
        }

        public VariableArray<T> build() {
            return this.variableArray;
        }
    }

    /**
     * @description: 给数组赋值
     * @param val
     */
    public void setValue(T[] val) {
        this.val = val;
    }

    /**
     * @description: 获取整个数组
     * @return T[]
     */
    public T[] getArr() {
        return this.val;
    }

    /**
     * @description: 给数组的某个元素赋值
     * @param {int} index
     * @param {T}   value
     */
    public void setValue(int index, T value) {
        this.val[index] = value;
    }

    /**
     * @description: 获取数组的某个元素
     * @param {int} index
     * @return T
     */
    public T getValue(int index) {
        return this.val[index];
    }

    /**
     * @description: 获取数组的维数
     * @return {int} dim
     */
    public int getDim() {
        return this.dim;
    }

    /**
     * @description: 获取数组的各维长度
     * @return {Integer[]} dimSize
     */
    public Integer[] getDimSize() {
        return this.dimSize;
    }

}
