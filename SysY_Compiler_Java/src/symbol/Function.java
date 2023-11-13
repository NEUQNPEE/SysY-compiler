package symbol;
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 00:05:04
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 01:10:32
 * @FilePath     : \SysYCompiler\src\Symbol\Function.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class Function extends Symbol {

    public class ParameterItem {
        String name; // 名称
        SymbolType type; // 类型：const, var

        public ParameterItem(String name, SymbolType type) {
            this.name = name;
            this.type = type;
        }
    }

    ParameterItem[] parameters; 
    // int size; // 需要分配的数据区空间

    public Function() {
    }

    public class FunctionBuilder extends SymbolBuilder {
        Function function = new Function();

        public FunctionBuilder basicInfo(String name, SymbolType type, int level) {
            this.function.name = name;
            this.function.type = type;
            this.function.level = level;
            return this;
        }

        public FunctionBuilder parameters(ParameterItem[] parameters) {
            this.function.parameters = parameters;
            return this;
        }

        // public FunctionBuilder size(int size) {
        // this.function.size = size;
        // }

        public Function build() {
            return this.function;
        }
    }
}
