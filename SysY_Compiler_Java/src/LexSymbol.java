//各种词法符号的种别码
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 05:08:19
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 00:02:46
 * @FilePath     : \Student\src\LexSymbol.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public enum LexSymbol {
    // 标识符
    IDENFR,
    // 字符串
    STRCON,
    // 整数
    INTCON,

    // 关键字
    // main
    MAINTK,
    // const
    CONSTTK,
    // int
    INTTK,
    // break
    BREAKTK,
    // continue
    CONTINUETK,
    // if
    IFTK,
    // else
    ELSETK,
    // while
    WHILETK,
    // getint
    GETINTTK,
    // printf
    PRINTFTK,
    // return
    RETURNTK,
    // void
    VOIDTK,

    // 操作符
    // ！
    NOT,
    // &&
    AND,
    // ||
    OR,
    // >=
    GEQ,
    // ==
    EQL,
    // <=
    LEQ,
    // +
    PLUS,
    // -
    MINU,
    // ！=
    NEQ,
    // *
    MULT,
    // =
    ASSIGN,
    // /
    DIV,
    // ;
    SEMICN,
    // %
    MOD,
    // ,
    COMMA,
    // <
    LSS,
    // (
    LPARENT,
    // )
    RPARENT,
    // >
    GRE,
    // [
    LBRACK,
    // ]
    RBRACK,
    // {
    LBRACE,
    // }
    RBRACE

}