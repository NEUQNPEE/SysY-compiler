import java.util.ArrayList;
import java.util.List;

import symbol.Symbol;

/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 05:08:19
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-12 18:04:35
 * @FilePath     : \Student\src\SymbolTable.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class SymbolTable {
    /** 
     * 唯一实例
     */    
    private static SymbolTable instance = new SymbolTable();

    /** 
     * 私有构造函数
     */    
    private SymbolTable() {
    }

    /** 
     * @description: 获取唯一实例
     * @return 
     */    
    public static SymbolTable getInstance() {
        if (instance == null) {
            instance = new SymbolTable();
        }
        return instance;
    }

    /** 
     * 符号名称表容量
     */    
    public static final int STMAX = 1000; 

    /** 
     * @description: 符号工厂
     */    
    public static class SymbolFactory {
        public static Symbol createSymbol(Symbol.SymbolBuilder builder) {
            return builder.build();
        }
    }

    /** 
     * @description: 符号表（此时若是能写C#的set和get将是绝杀，可惜绝不得）
     */    
    List<Symbol> symbolTable = new ArrayList<Symbol>();

    /** 
     * @description: 符号表指针
     */    
    public int symbolTableIndex = 0;

    /** 
     * @description: 根据index获取符号，这玩意应该完全没用
     * @param {int} index
     * @return {Symbol}
     */    
    public Symbol getSymbol(int index) {
        return symbolTable.get(index);
    }

    /** 
     * @description: 把某个符号登陆到符号名称表中
     * @param {Symbol} symbol
     */    
    public void enter(Symbol symbol) {
        symbolTable.add(symbol);
        symbolTableIndex++;
    }

    /** 
     * @description: 检测某个符号是否在符号表中
     * @param {String} id
     * @param {int} lev
     * @return {boolean}
     */    
    public boolean isInTable(String id, int lev) {
        for (Symbol symbol : symbolTable) {
            if (symbol.getName().equals(id) && symbol.getLevel() == lev) {
                return true;
            }
        }
        return false;
    }

    /** 
     * @description: 根据id和lev获取符号
     * @param {String} id
     * @param {int} lev
     * @return {Symbol}
     */    
    public Symbol getSymbol(String id, int lev) {
        for (Symbol symbol : symbolTable) {
            if (symbol.getName().equals(id) && symbol.getLevel() == lev) {
                return symbol;
            }
        }
        return null;
    }

    /** 
     * @description: 根据id和levList获取符号
     * @param {String} id
     * @param {List<Integer>} levList
     * @return {Symbol}
     */    
    public Symbol getSymbol(String id, List<Integer> levList) {
        for (Symbol symbol : symbolTable) {
            if (symbol.getName().equals(id) && levList.contains(symbol.getLevel())) {
                return symbol;
            }
        }
        return null;
    }

    /** 
     * @description: 初始化符号表
     */    
    public void init() {
        symbolTable.clear();
        symbolTableIndex = 0;
    }
}
