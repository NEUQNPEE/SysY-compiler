package symbol;
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 00:05:04
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 01:11:43
 * @FilePath     : \SysYCompiler\src\Symbol\Constant.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class Constant<T> extends Symbol {
    private T value;

    public Constant() {
    }

    public class ConstantBuilder extends SymbolBuilder {
        Constant<T> constant = new Constant<T>();

        public ConstantBuilder basicInfo(String name, SymbolType type, int level) {
            this.constant.name = name;
            this.constant.type = type;
            this.constant.level = level;
            return this;
        }

        public ConstantBuilder value(T value) {
            this.constant.value = value;
            return this;
        }

        public Constant<T> build() {
            return this.constant;
        }
    }

    public T getValue() {
        return this.value;
    }
}