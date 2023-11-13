package symbol;
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 00:05:04
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 01:11:06
 * @FilePath     : \SysYCompiler\src\Symbol\ConstantArray.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class ConstantArray<T> extends Symbol {
    private int dim;
    private Integer[] dimSize;
    private T[] val;
    
    public ConstantArray() {
    }

    public class ConstantArrayBuilder extends SymbolBuilder {
        ConstantArray<T> constantArray = new ConstantArray<T>();

        public ConstantArrayBuilder basicInfo(String name, SymbolType type, int level) {
            this.constantArray.name = name;
            this.constantArray.type = type;
            this.constantArray.level = level;
            return this;
        }

        public ConstantArrayBuilder value(int dim, Integer[] dimSize, T[] val) {
            this.constantArray.dim = dim;
            this.constantArray.dimSize = dimSize;
            this.constantArray.val = val;
            return this;
        }

        public ConstantArray<T> build() {
            return this.constantArray;
        }
    }

    public T[] getArr() {
        return this.val;
    }

    public T getValue(int index) {
        return this.val[index];
    }

    public int getDim() {
        return this.dim;
    }

    public Integer[] getDimSize() {
        return this.dimSize;
    }
}
