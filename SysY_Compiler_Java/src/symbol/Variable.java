package symbol;
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 00:05:04
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 01:07:22
 * @FilePath     : \SysYCompiler\src\Symbol\Variable.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class Variable<T> extends Symbol {
    private T value;

    public Variable() {
    }

    public class VariableBuilder extends SymbolBuilder {
        Variable<T> variable = new Variable<T>();

        public VariableBuilder basicInfo(String name, SymbolType type, int level) {
            this.variable.name = name;
            this.variable.type = type;
            this.variable.level = level;
            return this;
        }

        public VariableBuilder value(T value) {
            this.variable.value = value;
            return this;
        }

        public Variable<T> build() {
            return this.variable;
        }
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }
}