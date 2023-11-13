package symbol;
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 00:05:04
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 01:08:43
 * @FilePath     : \SysYCompiler\src\Symbol\Symbol.java
 * @Description  : 符号基类
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class Symbol {
    String name; 
    SymbolType type; 
    int level; 
    // int adr;

    public Symbol() {
    }

    public static class SymbolBuilder {
        Symbol symbol = new Symbol();

        public Symbol build() {
            return this.symbol;
        }
    }

    public String getName() {
        return name;
    }
    
    public SymbolType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }
}
