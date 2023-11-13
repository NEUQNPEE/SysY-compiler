import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import symbol.*;

/**
 * @Author: NieFire planet_class@foxmail.com
 * @Date: 2023-09-20 19:07:02
 * @LastEditors: NieFire planet_class@foxmail.com
 * @LastEditTime: 2023-09-25 21:36:50
 * @FilePath: \Student\src\corepackage\compiler\SyntacticParser.java
 * @Description: 语法分析器.这是编译器中最重要的部分，在语法分析的过程中穿插着语法错误检查和目标代码生成。
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * 
 *    tip 1 首先说 xxx,xxx,xxx……这种格式，为什么不用do while 而用更繁多的代码实现：
 *    tip 1 如果do while，while的判断条件是遇上右边界（分号，右小括号什么的），那坏了，一旦源代码出错，这地方没法检测跳出
 *    tip 1 所以说，有时候按自然语言的语法写代码是有道理的
 * 
 *    tip 2 说数组的事，实验要求定义数组的所有（其实最多就两个）[]里面必须有值。
 *    tip 2 这根平常写代码的规则有所区别了
 * 
 *    tip 3 你知道什么时候给符号表加新符号吗？对！就是在每次遇到Ident的时候！
 * 
 *    readme 更新日志
 *    readme 2023年9月25日21:17:03：初步完成了一部分，停止编写，完善词法分析器，准备单元测试
 * 
 *    readme 2023年10月9日14:47:18：在SynP中添加读入符号暂存栈与已读符号暂存栈
 *    readme SynP获取符号时优先从符号暂存栈出栈，
 *    readme 如果符号暂存栈为空，则调用词法分析器读出下一个符号
 *    readme SynP每次读入一个符号后，将其压入已读符号暂存栈
 *    readme SynP中出现任何需要回退的状况，都是将已读符号暂存栈的栈顶元素压入符号暂存栈
 * 
 *    readme 2023年10月9日17:07:11：由于回滚的存在，现在所有分析函数的返回值都必须为Boolean
 *    readme 表示是否有意义
 * 
 *    readme 2023年10月13日19:16:07：出现最终的问题：follow集必须加入
 * 
 *    readme 2023年11月9日17:54:33：考虑lev问题。lev最适合的数据结构是树
 * 
 *    readme 2023年11月9日20:00:21：考虑以下问题：
 *    readme 1. 是否应当将单个量与数组合并，只以list的内容作为区别
 *    readme 2. 如1被否定，是否应当将单个量与数组再抽象出一层，让各种类型分别对其进行继承。
 * 
 *    readme 2023年11月9日23:37:45：先不考虑以上问题，总结当前情况：
 *    readme 1. 数组的读入、符号表登入、值引用问题已经得到解决
 * 
 * @Copyright (c) 2023 by NieFire, All Rights Reserved.
 */
public class SyntacticParser {
    /**
     * 对付多维数组的维度和维度大小两个参数在函数间的传递问题
     */
    class DimAndDimSize {
        int dim;
        Queue<Integer> dimSizeQueue;

        public DimAndDimSize(int dim) {
            this.dim = dim;
            dimSizeQueue = new LinkedList<>();
        }

        public DimAndDimSize(int dim, Queue<Integer> dimSizeQueue) {
            this.dim = dim;
            this.dimSizeQueue = dimSizeQueue;
        }
    }

    /**
     * 层次树结点
     */
    class LevTreeNode {
        private int value;
        private LevTreeNode parent;
        private List<LevTreeNode> children;

        public LevTreeNode(int value) {
            this.value = value;
            this.parent = null;
            this.children = new ArrayList<>();
        }

        public LevTreeNode(int value, LevTreeNode parent) {
            this.value = value;
            this.parent = parent;
            this.children = new ArrayList<>();
        }

        public int getValue() {
            return value;
        }

        public LevTreeNode getParent() {
            return parent;
        }

        public void setParent(LevTreeNode parent) {
            this.parent = parent;
        }

        public List<LevTreeNode> getChildren() {
            return children;
        }

        public void addChild(LevTreeNode child) {
            child.setParent(this);
            children.add(child);
        }

        // 查找某个值的节点
        public LevTreeNode find(int value) {
            if (this.value == value) {
                return this;
            } else {
                for (LevTreeNode child : children) {
                    LevTreeNode result = child.find(value);
                    if (result != null) {
                        return result;
                    }
                }
                return null;
            }
        }
    }

    /**
     * 层次树根节点
     */
    private LevTreeNode levTreeRoot = new LevTreeNode(0);

    /**
     * 层次数量
     */
    private int levNum = 0;

    /**
     * 结果记录用字符串
     */
    private String result = "";

    /**
     * 引用词法分析器、符号表、目标代码生成器
     */
    public LexicalParser lexParser;
    public SymbolTable symbolTable;

    /**
     * 语法符号数量
     */
    // private final int SYNSYM_NUM = SynSymbol.values().length;

    /**
     * 当前词法符号，由nextsym()读入
     */
    public LexSymbol lexSym;

    /**
     * 读入符号暂存栈
     */
    private Stack<LexSymbol> readSymStack = new Stack<>();

    /**
     * 已读符号暂存栈
     */
    private Stack<LexSymbol> hasReadSymStack = new Stack<>();

    /**
     * 常量表达式处理类
     */
    private ExpCalculate expCalculate = new ExpCalculate();

    /**
     * io中介者
     * 应当注意到文法分析器的输入都来自于词法分析器，因此文法分析器不要BR
     */
    private IFileWriteMediator fileWriteMediator;

    /**
     * @description: 构造并初始化
     * @param {IFileWriteMediator} fileWriteMediator
     * @param {LexicalParser}      lexParser
     */
    public SyntacticParser(IFileWriteMediator fileWriteMediator,
            LexicalParser lexParser) {
        this.fileWriteMediator = fileWriteMediator;
        this.lexParser = lexParser;
        this.symbolTable = SymbolTable.getInstance();
    }

    /**
     * @description: 输出文法分析器的结果
     * @return {*}
     */
    public void writeSynResult() throws Exception {
        fileWriteMediator.write(result);
        fileWriteMediator.writeFile();
    }

    /**
     * @description: 测试用函数
     */
    public void synParserTest() {
        // 测试
        // parseStmt(SYNSYM_NUM, new SymSet(SYNSYM_NUM));
        // parseDecl(0);
        parse();
    }

    /**
     * @description: 启动语法分析过程，此前必须先调用一次nextsym()
     * @see #nextSym()
     */
    public void parse() {
        // SymSet nxtlev = new SymSet(SYNSYM_NUM);

        nextSym();
        parseCompUnit(levTreeRoot);
    }

    /**
     * @description: 预读下一个词法符号
     * @return {LexSymbol}
     */
    public LexSymbol peekNextSym() {
        if (readSymStack.empty()) {
            lexParser.peekSym();
            return lexParser.peekSym;
        } else {
            return readSymStack.peek();
        }
    }

    /**
     * @description: 获取下一个词法符号，先从已读符号暂存栈读取，如果为空则从词法分析器读取
     */
    public void nextSym() {
        // lexParser.getsym();
        // lexSym = lexParser.sym;

        if (lexSym != null) {
            hasReadSymStack.push(lexSym);
        }

        if (readSymStack.empty()) {
            lexParser.getsym();
            readSymStack.push(lexParser.sym);
        }
        lexSym = readSymStack.pop();

        // 注意，读取常量表达式，进入栈的是符号本身而非种别码
        if (expCalculate.isEnable()) {
            // 读入的内容存在如下可能：
            // 1. 操作符，+-*/%^()
            // 2. 数字
            // 3. 已经存在的常量标识符
            // 前两种expCalculate可以自行处理，第三种需要将去读符号表把值读出来

            addExpValue();
        }
    }

    /**
     * @description: 获取可能读取到的常量表达式的值，并加入expCalculate
     * @param {SynSymbol} synSym
     */
    public void addExpValue() {
        if (lexSym == LexSymbol.IDENFR) {
            // 去符号表里查有没有这个标识符
            // 首先找到值为levNum的叶子结点，向父亲结点遍历直到根节点，确定所有需要搜查的层次值
            LevTreeNode node = levTreeRoot.find(levNum);
            List<Integer> levList = new ArrayList<>();
            while (node != null) {
                levList.add(node.getValue());
                node = node.getParent();
            }
            // 从符号表中查找
            Symbol symbol = symbolTable.getSymbol(lexParser.symId, levList);

            if (symbol == null) {
                // 没找到，报错
                Error.report(Error.ErrorType.c, lexParser.lineCounter);
            }

            // 开始分情况处理,如果其子类类型为ConStant或者Variable，直接读取值即可
            if (symbol instanceof Constant) {
                Constant<Integer> constant = (Constant<Integer>) symbol;
                expCalculate.addNum(constant.getValue());
            } else if (symbol instanceof Variable) {
                Variable<Integer> variable = (Variable<Integer>) symbol;
                // expCalculate.addNum(variable.getValue());
                // 先检查是否为空
                if (variable.getValue() == null) {
                    expCalculate.disable();
                } else {
                    expCalculate.addNum(variable.getValue());
                }
            } else if (symbol instanceof ConstantArray) {
                ConstantArray<Integer> constantArray = (ConstantArray<Integer>) symbol;
                expCalculate.addNum(constantArray.getValue(getArrayIndex()));

            } else if (symbol instanceof VariableArray) {
                VariableArray<Integer> variableArray = (VariableArray<Integer>) symbol;
                expCalculate.addNum(variableArray.getValue(getArrayIndex()));
            } else {
                // 目前来说也就是Function了，不用管
                // expCalculate.clearQueue();
                // return;
            }
        } else {
            expCalculate.addSym(lexParser.symValue);
        }
    }

    /**
     * @description: 获取数组下标。具体为向后预读符号，将遇到的exp加入计算，遇到其他符号停止，遇到[]直接跳过
     */
    public int getArrayIndex() {
        int peekNum = 0;
        int index = 1;
        while (true) {
            expCalculate.enable();
            nextSym();
            peekNum++;
            if (lexSym == LexSymbol.LBRACK) {
                // 遇到左中括号，跳过
                continue;
            } else if (lexSym == LexSymbol.RBRACK) {
                // 遇到右中括号，跳过
                continue;
            } else if (isExp()) {
                // 遇到表达式
                peekNum++;
                index *= (getExpValue() + 1);
            } else {
                // 遇到其他符号，停止
                break;
            }
        }

        // 预读完毕，回滚
        rollBackSym(peekNum);
        return index - 1;
    }

    /**
     * @description: 回滚一个符号，将已读符号暂存栈的栈顶元素压入读入符号暂存栈
     */
    public void rollBackSym() {
        readSymStack.push(lexSym);
        lexSym = hasReadSymStack.pop();
    }

    /**
     * @description: 回滚n个符号，将已读符号暂存栈的栈顶元素压入读入符号暂存栈
     * @param {int} n
     */
    public void rollBackSym(int n) {
        for (int i = 0; i < n; i++) {
            rollBackSym();
        }
    }

    /**
     * @description: 向结果添加新token
     * @param {SynSymbol} synSym
     */
    public void addResult(SynSymbol synSym) {
        result += "<" + synSym.toString() + ">\n";
        fileWriteMediator.write("<" + synSym.toString() + ">\n");
    }

    /**
     * @description: 分析<编译单元>
     * @param {LevTreeNode} levTreeNode 层次树根节点
     */
    private void parseCompUnit(LevTreeNode levTreeNode) {
        // <编译单元(CompUnit)> = {声明(Decl)} {函数定义(FuncDef)} <主函数定义(MainFuncDef)>
        parseDecl();
        while (parseFuncDef(levTreeNode)) {
        }
        parseMainFuncDef(levTreeNode);
        addResult(SynSymbol.CompUnit);
    }

    /**
     * @description: 分析<声明>
     * @return {Boolean} 是否有意义
     */
    private Boolean parseDecl() {
        // <声明(Decl)> = <常量声明(ConstDecl)> | <变量声明(VarDecl)>
        Boolean isMeaningful = false;
        while (parseConstDecl() || parseVarDecl()) {
            isMeaningful = true;
        }

        return isMeaningful;
    }

    /**
     * @description: 分析<常量声明>
     * @return {Boolean} 是否有意义
     */
    private Boolean parseConstDecl() {
        // <常量声明(ConstDecl)> = 'const' BType ConstDef { ',' ConstDef } ';'

        if (lexSym != LexSymbol.CONSTTK) {
            // 开头不是const，直接返回
            return false;
        }
        nextSym();

        // tip 一旦读入const，必然为常量声明

        if (!isBType(lexParser.sym)) {
            // TODO 错误处理，常量声明缺少基本类型
        }
        nextSym();

        parseConstDef();

        while (lexSym == LexSymbol.COMMA) {
            nextSym();
            parseConstDef();
        }

        if (lexSym != LexSymbol.SEMICN) {
            // 报缺少分号错误
            Error.report(Error.ErrorType.i, lexParser.lineCounter);
        }
        nextSym();

        // tip 到此，完成了一条常量声明的分析
        addResult(SynSymbol.ConstDecl);
        return true;
    }

    /**
     * @description: 分析<常量定义>
     * @return {Boolean} 是否有意义
     *         tip 注意以下几点：
     *         tip 1.SysY 在声明数组时各维⻓度都需要显式给出，而不允许是未知的。
     *         tip 2.ConstDef 中表示各维⻓度的 ConstExp 都必须能在编译时求值到非负整数。
     *         tip 3.当 ConstDef 定义的是数组时，‘=’ 右边的
     *         ConstInitVal必须为与多维数组中数组维数和各维⻓度完全对应的初始值
     *         tip 如{{1,2},{3,4},{5,6}}为 a[3][2]的初始值。
     *         tip 4.常量定义与变量定义表区分开的关键在于常量定义必须在定义时赋初值
     */
    private Boolean parseConstDef() {
        // <常量定义(ConstDef)> = Ident { '[' Exp ']' } '=' InitVal
        // 注意有Ident，要添加符号表

        if (lexSym != LexSymbol.IDENFR) {
            // 开头不是标识符，直接返回
            return false;
        }
        nextSym();

        // tip 一旦读入标识符，必然为常量定义

        DimAndDimSize dds = new DimAndDimSize(0);
        while (lexSym == LexSymbol.LBRACK) {
            expCalculate.enable();

            nextSym();

            if (isConstExp()) {
                int value = getExpValue();
                if (value <= 0) {
                    // TODO 错误处理，数组维度不能为负数或零
                }
                dds.dimSizeQueue.add(value);
            } else {
                // TODO 错误处理，数组定义时未给出某维度的长度或给出的不是表达式
            }

            if (lexSym != LexSymbol.RBRACK) {
                Error.report(Error.ErrorType.k, lexParser.lineCounter);
            }
            nextSym();

            dds.dim++;
        }

        // 到这里，标识符与类型已经读入，可以查重了，符号表的添加在parseConstInitVal中进行
        if (symbolTable.isInTable(lexParser.symId, levNum)) {
            // 错误处理，重复定义
            Error.report(Error.ErrorType.b, lexParser.lineCounter);
        }

        if (lexSym != LexSymbol.ASSIGN) {
            // ? 常量定义缺少等号，未定义此错误
            System.out.println("出现错误：常量定义缺少等号");
        }

        expCalculate.enable();
        nextSym();

        List<Integer> valList = new ArrayList<>();
        parseConstInitVal(dds, valList);

        // 根据dim大小，判断是常量还是常量数组，添加符号表
        if (dds.dim == 0) {
            // 常量
            symbolTable.enter(SymbolTable.SymbolFactory.createSymbol(
                    new Constant<Integer>().new ConstantBuilder()
                            .basicInfo(lexParser.symId, SymbolType.constant, levNum)
                            .value(valList.get(0))));
        } else {
            // 常量数组
            Integer[] dimSizeArray = new Integer[dds.dimSizeQueue.size()];
            dds.dimSizeQueue.toArray(dimSizeArray);
            Integer[] valArray = new Integer[valList.size()];
            valList.toArray(valArray);

            symbolTable.enter(SymbolTable.SymbolFactory.createSymbol(
                    new ConstantArray<Integer>().new ConstantArrayBuilder()
                            .basicInfo(lexParser.symId, SymbolType.constant, levNum)
                            .value(dds.dim, dimSizeArray, valArray)));
        }

        // tip 到此，完成了一条常量定义的分析
        addResult(SynSymbol.ConstDef);
        return true;
    }

    /**
     * @description: 分析<常量初值>
     * @param {DimAndDimSize} dds
     * @param {List<Integer>} valList
     * @return {Boolean} 是否有意义
     *         tip 按规范来应该还有个lev参数，但定义个常量或者变量根本不可能使得代码层次发生变化，出于两参数原则就不要了
     */
    private Boolean parseConstInitVal(DimAndDimSize dds, List<Integer> valList) {
        // <常量初值(ConstInitVal)> = ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ]
        // '}'

        int dim = dds.dim;
        Queue<Integer> dimSizeQueue = dds.dimSizeQueue;

        if (dim == 0) {
            if (!isConstExp()) {
                // TODO 错误处理，常量缺少初值
            }
            valList.add(getExpValue());
        } else {
            // dim >= 1，数组型，
            int thisDimSize = dimSizeQueue.poll();

            if (lexSym == LexSymbol.LBRACE) {

                nextSym();
                expCalculate.enable();
                // tip 注意这里为什么传一个新的队列，因为这个队列要在递归中被修改，但上层的队列不能被修改
                parseConstInitVal(new DimAndDimSize(dim - 1, new LinkedList<>(dimSizeQueue)), valList);
                thisDimSize--;

                while (lexSym == LexSymbol.COMMA) {

                    nextSym();
                    expCalculate.enable();
                    parseConstInitVal(new DimAndDimSize(dim - 1, new LinkedList<>(dimSizeQueue)), valList);
                    thisDimSize--;
                }

                if (lexSym != LexSymbol.RBRACE) {
                    // TODO 错误处理，缺少右大括号
                }
                nextSym();

                if (thisDimSize != 0) {
                    // TODO 错误处理，数组元素个数与维度不匹配
                }

            } else {
                // TODO 错误处理，常量缺少初值
            }
        }

        // 到此，完成了一条常量初值的分析
        addResult(SynSymbol.ConstInitVal);
        return true;
    }

    /**
     * @description: 分析<变量声明>
     * @return {Boolean} 是否有意义
     */
    private Boolean parseVarDecl() {
        // <变量声明(VarDecl)> = BType VarDef { ',' VarDef } ';'

        // 记录本函数已经读入的词法符号数，便于回滚
        int symNum = 0;

        if (!isBType(lexParser.sym)) {
            // 开头不是基本类型，直接返回
            return false;
        }
        symNum++;

        // 预读一个，检查是否为MAINTK
        if (peekNextSym() == LexSymbol.MAINTK) {
            // 这是主函数，返回
            lexParser.iniPeekSym();
            return false;
        }

        // 再预读一个，检查是否为左小括号
        if (peekNextSym() == LexSymbol.LPARENT) {
            // 这是函数定义，返回
            lexParser.iniPeekSym();
            return false;
        }

        nextSym();

        if (!parseVarDef()) {
            rollBackSym(symNum);
            return false;
        }

        while (lexSym == LexSymbol.COMMA) {
            nextSym();
            parseVarDef();
        }

        if (lexSym != LexSymbol.SEMICN) {
            // 报缺少分号错误
            Error.report(Error.ErrorType.i, lexParser.lineCounter);
        }
        nextSym();

        addResult(SynSymbol.VarDecl);
        return true;
    }

    /**
     * @description: 分析<变量定义>
     * @return {Boolean} 是否有意义
     */
    private Boolean parseVarDef() {
        // <变量定义(VarDef)>
        // = Ident { '[' ConstExp ']' }
        // | Ident { '[' ConstExp ']' } '=' InitVal

        if (lexSym != LexSymbol.IDENFR) {
            // TODO 错误处理，变量定义缺少标识符
        }
        nextSym();

        DimAndDimSize dds = new DimAndDimSize(0);
        while (lexSym == LexSymbol.LBRACK) {
            expCalculate.enable();
            nextSym();

            if (isConstExp()) {
                int value = getExpValue();
                if (value <= 0) {
                    // TODO 错误处理，数组维度不能为负数或零
                }
                dds.dimSizeQueue.add(value);
            } else {
                // TODO 错误处理，数组定义时未给出某维度的长度或给出的不是表达式
            }

            if (lexSym != LexSymbol.RBRACK) {
                Error.report(Error.ErrorType.k, lexParser.lineCounter);
            }
            nextSym();

            dds.dim++;
        }

        if (symbolTable.isInTable(lexParser.symId, levNum)) {
            // 错误处理，重复定义
            Error.report(Error.ErrorType.b, lexParser.lineCounter);
        }

        // 根据dim大小，判断是变量还是变量数组，添加符号表
        String id = lexParser.symId;

        if (dds.dim == 0) {
            // 变量
            symbolTable.enter(SymbolTable.SymbolFactory.createSymbol(
                    new Variable<Integer>().new VariableBuilder()
                            .basicInfo(id, SymbolType.variable, levNum)));
        } else {
            // 变量数组
            Integer[] dimSizeArray = new Integer[dds.dimSizeQueue.size()];
            dds.dimSizeQueue.toArray(dimSizeArray);

            symbolTable.enter(SymbolTable.SymbolFactory.createSymbol(
                    new VariableArray<Integer>().new VariableArrayBuilder()
                            .basicInfo(id, SymbolType.constant, levNum)
                            .allocate(dds.dim, dimSizeArray)));
        }

        if (lexSym == LexSymbol.ASSIGN) {
            expCalculate.enable();
            nextSym();

            List<Integer> valList = new ArrayList<>();
            parseInitVal(dds, valList);

            Symbol symbol = symbolTable.getSymbol(id, levNum);
            if (dds.dim == 0) {
                if (symbol instanceof Variable) {
                    ((Variable<Integer>) symbol).setValue(valList.get(0));
                }
            } else {
                if (symbol instanceof VariableArray) {
                    Integer[] valArray = new Integer[valList.size()];
                    valList.toArray(valArray);
                    ;
                    ((VariableArray<Integer>) symbol).setValue(valArray);
                }
            }
        }

        addResult(SynSymbol.VarDef);
        return true;
    }

    /**
     * @description: 分析<变量初值>
     * @param {DimAndDimSize} dds
     * @param {List<Integer>} valList
     * @return {Boolean} 是否有意义
     */
    private Boolean parseInitVal(DimAndDimSize dds, List<Integer> valList) {
        // <变量初值(InitVal)> = Exp | '{' [ InitVal { ',' InitVal } ] '}'
        int dim = dds.dim;
        Queue<Integer> dimSizeQueue = dds.dimSizeQueue;

        if (dim == 0) {
            if (!isExp()) {
                // TODO 错误处理，变量缺少初值
            }
            valList.add(getExpValue());
        } else {
            // dim >= 1，数组型，
            int thisDimSize = dimSizeQueue.poll();

            if (lexSym == LexSymbol.LBRACE) {
                nextSym();
                expCalculate.enable();

                // tip 注意这里为什么传一个新的队列，因为这个队列要在递归中被修改，但上层的队列不能被修改
                parseInitVal(new DimAndDimSize(dim - 1, new LinkedList<>(dimSizeQueue)), valList);
                thisDimSize--;

                while (lexSym == LexSymbol.COMMA) {
                    nextSym();
                    expCalculate.enable();

                    parseInitVal(new DimAndDimSize(dim - 1, new LinkedList<>(dimSizeQueue)), valList);
                    thisDimSize--;
                }

                if (lexSym != LexSymbol.RBRACE) {
                    // TODO 错误处理，缺少右大括号
                }
                nextSym();

                if (thisDimSize != 0) {
                    // TODO 错误处理，数组元素个数与维度不匹配
                }

            } else {
                // TODO 错误处理，常量缺少初值
            }
        }

        addResult(SynSymbol.InitVal);
        return true;
    }

    /**
     * @description: 分析<函数定义>
     * @param {LevTreeNode} levTreeNode 层次树根节点
     * @return {Boolean} 是否有意义
     */
    private Boolean parseFuncDef(LevTreeNode levTreeNode) {
        // <函数定义(FuncDef)> = <函数类型(FuncType)> Ident '(' [FuncFParams] ')' Block

        if (!parseFuncType()) {
            // 不是函数定义,返回
            return false;
        }

        if (lexSym == LexSymbol.MAINTK) {
            // 这是主函数，返回
            return false;
        }

        // tip 至此，确定为函数定义

        if (lexSym != LexSymbol.IDENFR) {
            // TODO 错误处理，函数定义缺少标识符
        }

        // 添加符号表
        String id = lexParser.symId;
        if (symbolTable.isInTable(id, levNum)) {
            // 错误处理，重复定义
            Error.report(Error.ErrorType.b, lexParser.lineCounter);
        }
        symbolTable.enter(SymbolTable.SymbolFactory.createSymbol(
                new Function().new FunctionBuilder()
                        .basicInfo(id, SymbolType.function, levNum)));

        nextSym();

        if (lexSym != LexSymbol.LPARENT) {
            // TODO 错误处理，函数定义缺少左小括号
        }
        nextSym();

        // 为层次树添加子节点（这是为参数列表加的）
        levNum++;
        LevTreeNode child = new LevTreeNode(levNum, levTreeNode);
        levTreeNode.addChild(child);

        parseFuncFParams(child);

        if (lexSym != LexSymbol.RPARENT) {
            // 错误处理，函数定义缺少右小括号
            Error.report(Error.ErrorType.j, lexParser.lineCounter);
        }
        nextSym();

        parseBlock(child);

        addResult(SynSymbol.FuncDef);
        return true;
    }

    /**
     * @description: 分析<主函数定义(MainFuncDef)>
     * @param {LevTreeNode} levTreeNode 层次树根节点
     * @return {Boolean} 是否有主函数
     */
    private Boolean parseMainFuncDef(LevTreeNode levTreeNode) {
        // <主函数定义(MainFuncDef)> = 'int' 'main' '(' ')' Block

        // 根据语法定义，进入此函数必为主函数定义，层次加一
        levNum++;
        LevTreeNode child = new LevTreeNode(levNum, levTreeNode);
        levTreeNode.addChild(child);

        if (lexSym != LexSymbol.INTTK) {
            // TODO 错误处理，主函数定义缺少int
        }
        nextSym();

        if (lexSym != LexSymbol.MAINTK) {
            // TODO 错误处理，主函数定义缺少main
        }
        nextSym();

        if (lexSym != LexSymbol.LPARENT) {
            // TODO 错误处理，主函数定义缺少左小括号
        }
        nextSym();

        if (lexSym != LexSymbol.RPARENT) {
            // 错误处理，主函数定义缺少右小括号
            Error.report(Error.ErrorType.j, lexParser.lineCounter);
        }
        nextSym();

        parseBlock(child);

        addResult(SynSymbol.MainFuncDef);
        return true;
    }

    /**
     * @description: 分析<函数类型>
     * @return {Boolean} 是否有意义
     */
    private Boolean parseFuncType() {
        // <函数类型(FuncType)> = 'int' | 'void'
        if (lexSym == LexSymbol.INTTK || lexSym == LexSymbol.VOIDTK) {
            // 不清楚是主函数还是普通函数，预读一个标识符
            if (peekNextSym() == LexSymbol.MAINTK) {
                lexParser.iniPeekSym();
                return false;
            }

            addResult(SynSymbol.FuncType);
            nextSym();
            return true;
        }

        return false;
    }

    /**
     * @description: 分析<函数形参表>
     * @param {LevTreeNode} levTreeNode 层次树节点
     * @return {Boolean} 是否有形参
     */
    private Boolean parseFuncFParams(LevTreeNode levTreeNode) {
        // <函数形参表(FuncFParams)> = <函数形参(FuncFParam)>{','<函数形参(FuncFParam)>}
        if (!parseFuncFParam(levTreeNode)) {
            if (lexSym == LexSymbol.RPARENT) {
                // 没有形参，直接返回
                return false;
            }
        }

        while (lexSym == LexSymbol.COMMA) {
            nextSym();
            parseFuncFParam(levTreeNode);
        }

        addResult(SynSymbol.FuncFParams);
        return true;
    }

    /**
     * @description: 分析<函数实参>
     * @return {Boolean} 是否有意义
     */
    private Boolean parseFuncRParams() {
        // FuncRParams = Exp {',' Exp}

        if (!isExp()) {
            if (lexSym == LexSymbol.RPARENT) {
                // 没有形参，直接返回
                return false;
            }
        }

        while (lexSym == LexSymbol.COMMA) {
            nextSym();
            if (!isExp()) {
                // TODO 错误处理，函数实参缺少表达式
            }
        }

        addResult(SynSymbol.FuncRParams);
        return true;
    }

    /**
     * @description: 分析<语句块>
     * @param {LevTreeNode} levTreeNode 层次树根节点
     * @return
     */
    private Boolean parseBlock(LevTreeNode levTreeNode) {
        // <语句块(Block)> = '{' { <语句块项(BlockItem)> } '}'

        if (lexSym != LexSymbol.LBRACE) {
            // 不是语句块，返回
            return false;
        }

        // tip 读取到左大括号，确定进入语句块，由于语句块项可以重复0次，无需判断是否有意义，结果中必然加入SynSymbol.Block
        levNum++;
        LevTreeNode child = new LevTreeNode(levNum, levTreeNode);
        levTreeNode.addChild(child);

        nextSym();

        while (parseBlockItem(child)) {
        }

        if (lexSym != LexSymbol.RBRACE) {
            // TODO 错误处理，语句块缺少右大括号
        }
        nextSym();

        addResult(SynSymbol.Block);
        return true;
    }

    /**
     * @description: 分析<语句块项>
     * @param {LevTreeNode} levTreeNode 层次树根节点
     * @return {Boolean} 是否有意义
     */
    private Boolean parseBlockItem(LevTreeNode levTreeNode) {
        // <语句块项(BlockItem)> = <声明(Decl)> | <语句(Stmt)>
        // 由于支持重复0次，因此需要确定是否存在有意义的语句块项
        Boolean isMeaningful = false;

        while (parseDecl() || parseStmt(levTreeNode)) {
            isMeaningful = true;
        }

        return isMeaningful;
    }

    /**
     * @description: 分析<语句>
     * @param {LevTreeNode} levTreeNode 层次树根节点
     * @return {Boolean} 是否有意义
     *         tip 1. Stmt 中的 if 类型语句遵循就近匹配。
     *         tip 2. 单个 Exp 可以作为 Stmt。Exp 会被求值，所求的值会被丢弃。
     */
    private Boolean parseStmt(LevTreeNode levTreeNode) {
        // <语句(Stmt)> = LVal '=' Exp ';'
        // | LVal '=' 'getint''('')'';'
        // | [Exp] ';' // 有无Exp两种情况
        // | Block // 这是把循环条件那些玩意拆开了
        // | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        // | 'while' '(' Cond ')' Stmt
        // | 'break' ';'
        // | 'continue' ';'
        // | 'return' [Exp] ';' // 1.有Exp 2.无Exp
        // | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp

        if (lexParser.peekMatchSym(LexSymbol.ASSIGN)) {
            // 有等号
            if (isLVal()) {
                return parseStmtLval();
            }
        }

        if (isExp()) {
            if (lexSym != LexSymbol.SEMICN) {
                // 缺少分号，错误处理
                Error.report(Error.ErrorType.i, lexParser.lineCounter);
            }
            nextSym();
            addResult(SynSymbol.Stmt);
            return true;
        } else if (lexSym == LexSymbol.SEMICN) {
            // 空语句
            nextSym();
            addResult(SynSymbol.Stmt);
            return true;
        }

        if (lexSym == LexSymbol.LBRACE) {
            parseBlock(levTreeNode);
            addResult(SynSymbol.Stmt);
            return true;
        }

        if (lexSym == LexSymbol.IFTK) {
            return parseStmtIf(levTreeNode);
        }

        if (lexSym == LexSymbol.WHILETK) {
            return parseStmtWhile(levTreeNode);
        }

        if (lexSym == LexSymbol.BREAKTK) {
            nextSym();
            if (lexSym != LexSymbol.SEMICN) {
                // 错误处理，缺少分号
                Error.report(Error.ErrorType.i, lexParser.lineCounter);
            }
            nextSym();

            addResult(SynSymbol.Stmt);
            return true;
        }

        if (lexSym == LexSymbol.CONTINUETK) {
            nextSym();
            if (lexSym != LexSymbol.SEMICN) {
                // 错误处理，缺少分号
                Error.report(Error.ErrorType.i, lexParser.lineCounter);
            }
            nextSym();

            addResult(SynSymbol.Stmt);
            return true;
        }

        if (lexSym == LexSymbol.RETURNTK) {
            nextSym();
            isExp();
            if (lexSym != LexSymbol.SEMICN) {
                // 错误处理，缺少分号
                Error.report(Error.ErrorType.i, lexParser.lineCounter);
            }
            nextSym();
            addResult(SynSymbol.Stmt);
            return true;
        }

        if (lexSym == LexSymbol.PRINTFTK) {
            return parseStmtPrintf();
        }

        return false;
    }

    /**
     * @description: 分析语句左值表达式分支：
     *               LVal '=' 'getint''('')'';'
     *               LVal '=' Exp ';'
     * @return {Boolean} 是否是语句的左值表达式分支
     */
    private Boolean parseStmtLval() {
        // 有两种情况
        if (lexSym == LexSymbol.ASSIGN) {
            nextSym();

            if (lexSym == LexSymbol.GETINTTK) {
                nextSym();
                if (lexSym != LexSymbol.LPARENT) {
                    // TODO 错误处理，缺少左小括号
                }
                nextSym();
                if (lexSym != LexSymbol.RPARENT) {
                    // 缺少右小括号，错误处理
                    Error.report(Error.ErrorType.l, lexParser.lineCounter);
                }
                nextSym();
                if (lexSym != LexSymbol.SEMICN) {
                    // 缺少分号，错误处理
                    Error.report(Error.ErrorType.i, lexParser.lineCounter);
                }
                nextSym();
                addResult(SynSymbol.Stmt);
                return true;
            }

            if (isExp()) {
                if (lexSym != LexSymbol.SEMICN) {
                    // 缺少分号，错误处理
                    Error.report(Error.ErrorType.i, lexParser.lineCounter);
                }
                nextSym();
                addResult(SynSymbol.Stmt);
                return true;
            }
        }

        return false;
    }

    /**
     * @description: 分析语句if分支：
     *               'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
     * @return
     */
    private Boolean parseStmtIf(LevTreeNode levTreeNode) {
        // tip 读入if，确定为if语句,层次加一
        levNum++;
        LevTreeNode child = new LevTreeNode(levNum, levTreeNode);
        levTreeNode.addChild(child);

        nextSym();
        if (lexSym != LexSymbol.LPARENT) {
            // TODO 错误处理，缺少左小括号
        }
        nextSym();

        if (!isCond()) {
            // TODO 错误处理，缺少条件表达式
        }

        if (lexSym != LexSymbol.RPARENT) {
            // 错误处理，缺少右小括号
            Error.report(Error.ErrorType.j, lexParser.lineCounter);
        }
        nextSym();

        parseStmt(child);

        if (lexSym == LexSymbol.ELSETK) {
            nextSym();
            parseStmt(child);
        }

        addResult(SynSymbol.Stmt);
        return true;
    }

    /**
     * @description: 分析语句while分支：
     *               'while' '(' Cond ')' Stmt
     * @return
     */
    private Boolean parseStmtWhile(LevTreeNode levTreeNode) {
        // tip 读入while，确定为while语句,层次加一
        levNum++;
        LevTreeNode child = new LevTreeNode(levNum, levTreeNode);
        levTreeNode.addChild(child);

        nextSym();
        if (lexSym != LexSymbol.LPARENT) {
            // TODO 错误处理，缺少左小括号
        }
        nextSym();

        if (!isCond()) {
            // TODO 错误处理，缺少条件表达式
        }

        if (lexSym != LexSymbol.RPARENT) {
            // 错误处理，缺少右小括号
            Error.report(Error.ErrorType.j, lexParser.lineCounter);
        }
        nextSym();

        parseStmt(child);

        addResult(SynSymbol.Stmt);
        return true;
    }

    /**
     * @description: 分析语句printf分支：
     *               'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
     * @return
     */
    private Boolean parseStmtPrintf() {
        nextSym();

        if (lexSym != LexSymbol.LPARENT) {
            // TODO 错误处理，缺少左小括号
        }
        nextSym();

        if (lexSym != LexSymbol.STRCON) {
            // TODO 错误处理，缺少字符串常量
        }
        nextSym();

        while (lexSym == LexSymbol.COMMA) {
            nextSym();
            if (!isExp()) {
                // TODO 错误处理，printf缺少某个Exp部分
            }
        }

        if (lexSym != LexSymbol.RPARENT) {
            // 错误处理，缺少右小括号
            Error.report(Error.ErrorType.j, lexParser.lineCounter);
        }
        nextSym();

        if (lexSym != LexSymbol.SEMICN) {
            // 错误处理，缺少分号
            Error.report(Error.ErrorType.i, lexParser.lineCounter);
        }
        nextSym();

        addResult(SynSymbol.Stmt);
        return true;
    }

    /**
     * @description: 分析<条件表达式>
     * @return {Boolean} 是否是条件表达式
     */
    private boolean isCond() {
        // Cond = LOrExp
        if (isLOrExp()) {
            addResult(SynSymbol.Cond);
            return true;
        }
        return false;
    }

    /**
     * @description: 分析<逻辑或表达式>
     * @return {Boolean} 是否是逻辑或表达式
     */
    private boolean isLOrExp() {
        // LOrExp = LAndExp | LOrExp '||' LAndExp

        if (isLAndExp()) {
            addResult(SynSymbol.LOrExp);

            while (lexSym == LexSymbol.OR) {
                nextSym();
                if (!isLAndExp()) {
                    // TODO 错误处理，逻辑或表达式缺少逻辑与表达式
                }
                addResult(SynSymbol.LOrExp);
            }
            return true;
        }

        return false;
    }

    /**
     * @description: 分析<逻辑与表达式>
     * @return {Boolean} 是否是逻辑与表达式
     */
    private boolean isLAndExp() {
        // LAndExp = EqExp | LAndExp '&&' EqExp

        if (isEqExp()) {
            addResult(SynSymbol.LAndExp);

            while (lexSym == LexSymbol.AND) {
                nextSym();
                if (!isEqExp()) {
                    // TODO 错误处理，逻辑与表达式缺少相等表达式
                }
                addResult(SynSymbol.LAndExp);
            }
            return true;
        }

        return false;
    }

    /**
     * @description: 分析<相等表达式(EqExp)>
     * @return {Boolean} 是否是相等表达式
     */
    private boolean isEqExp() {
        // EqExp = RelExp | EqExp ('==' | '!=') RelExp

        if (isRelExp()) {
            addResult(SynSymbol.EqExp);

            while (lexSym == LexSymbol.EQL || lexSym == LexSymbol.NEQ) {
                nextSym();
                if (!isRelExp()) {
                    // TODO 错误处理，相等表达式缺少关系表达式
                }
                addResult(SynSymbol.EqExp);
            }

            return true;
        }

        return false;
    }

    /**
     * @description: 分析<关系表达式(RelExp)>
     * @return {Boolean} 是否是关系表达式
     */
    private boolean isRelExp() {
        // RelExp = AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp

        if (isAddExp()) {
            addResult(SynSymbol.RelExp);

            while (lexSym == LexSymbol.LSS || lexSym == LexSymbol.LEQ || lexSym == LexSymbol.GRE
                    || lexSym == LexSymbol.GEQ) {
                nextSym();

                if (!isAddExp()) {
                    // TODO 错误处理，关系表达式缺少加减表达式
                }
                addResult(SynSymbol.RelExp);
            }
            return true;
        }

        return false;
    }

    /**
     * @description: 分析<左值表达式(LVal)>
     * @return {Boolean} 是否是左值表达式
     */
    private boolean isLVal() {
        // LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组

        // follow集不能为左小括号（因为可能是函数调用）

        if (lexSym == LexSymbol.IDENFR) {
            nextSym();
        } else {
            // 开头不是标识符，直接返回
            return false;
        }

        if (lexSym == LexSymbol.LPARENT) {
            // 这是函数调用，回滚
            rollBackSym();
            return false;
        }

        while (lexSym == LexSymbol.LBRACK) {
            nextSym();
            if (isExp()) {
            } else {
                // TODO 错误处理，数组元素有维度没写或不是表达式
            }
            if (lexSym == LexSymbol.RBRACK) {
                nextSym();
            } else {
                // 错误处理，缺少右中括号
                Error.report(Error.ErrorType.k, lexParser.lineCounter);
            }
        }

        addResult(SynSymbol.LVal);
        return true;
    }

    /**
     * @description: 是否是基本类型(BType)
     * @param {LexSymbol} sym
     * @return {Boolean} 是否是基本类型
     */
    private boolean isBType(LexSymbol sym) {
        // tip 神经病般的编译原理实验，基本类型就个int
        if (sym == LexSymbol.INTTK) {
            return true;
        }
        return false;
    }

    /**
     * @description: 分析<表达式(Exp)>
     * @return {Boolean} 是否是表达式
     */
    private boolean isExp() {
        // Exp = AddExp
        if (isAddExp()) {
            addResult(SynSymbol.Exp);
            return true;
        }
        return false;
    }

    /**
     * @description: 分析<加减表达式(AddExp)>
     * @return {Boolean} 是否是加减表达式
     */
    private boolean isAddExp() {
        // AddExp = MulExp | AddExp ('+' | '−') MulExp

        if (isMulExp()) {
            addResult(SynSymbol.AddExp);

            while (lexSym == LexSymbol.PLUS || lexSym == LexSymbol.MINU) {
                nextSym();

                if (!isMulExp()) {
                    // TODO 错误处理，加减表达式缺少乘除模表达式
                }
                addResult(SynSymbol.AddExp);
            }

            return true;
        }
        return false;
    }

    /**
     * @description: 分析<乘除模表达式(MulExp)>
     * @return {Boolean} 是否是乘除模表达式
     */
    private boolean isMulExp() {
        // MulExp = UnaryExp | MulExp ('*' | '/' | '%') UnaryExp

        if (isUnaryExp()) {
            addResult(SynSymbol.MulExp);

            while (lexSym == LexSymbol.MULT || lexSym == LexSymbol.DIV || lexSym == LexSymbol.MOD) {
                nextSym();

                if (!isUnaryExp()) {
                    // TODO 错误处理，乘除模表达式缺少一元表达式
                }
                addResult(SynSymbol.MulExp);
            }

            return true;
        }

        return false;
    }

    /**
     * @description: 分析<一元表达式(UnaryExp)>
     * @return
     */
    private boolean isUnaryExp() {
        // UnaryExp = PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp

        if (isPrimaryExp()) {
            addResult(SynSymbol.UnaryExp);
            return true;
        }

        if (lexSym == LexSymbol.IDENFR) {
            nextSym();
            if (lexSym == LexSymbol.LPARENT) {
                nextSym();
                parseFuncRParams();
                if (lexSym == LexSymbol.RPARENT) {
                    nextSym();
                } else {
                    // 错误处理，缺少右小括号
                    Error.report(Error.ErrorType.j, lexParser.lineCounter);
                }
            } else {
                // TODO 错误处理，缺少左小括号
            }
            addResult(SynSymbol.UnaryExp);
            return true;
        }

        if (isUnaryOp()) {
            nextSym();
            addResult(SynSymbol.UnaryOp);
            if (!isUnaryExp()) {
                // TODO 错误处理，一元表达式缺少一元表达式
            }
            addResult(SynSymbol.UnaryExp);
            return true;
        }

        return false;
    }

    /**
     * @description: 分析单目运算符(UnaryOp)
     * @return {Boolean} 是否是单目运算符
     */
    private boolean isUnaryOp() {
        // UnaryOp = '+' | '-' | '!'

        if (lexSym == LexSymbol.PLUS || lexSym == LexSymbol.MINU || lexSym == LexSymbol.NOT) {
            return true;
        }
        return false;
    }

    /**
     * @description: 分析基本表达式(PrimaryExp)
     * @return {Boolean} 是否是基本表达式
     */
    private boolean isPrimaryExp() {
        // PrimaryExp = '('<Exp>')' | LVal | Number

        if (lexSym == LexSymbol.LPARENT) {
            nextSym();
            if (isExp()) {
                if (lexSym == LexSymbol.RPARENT) {
                    nextSym();
                } else {
                    // 错误处理，缺少右小括号
                    Error.report(Error.ErrorType.j, lexParser.lineCounter);
                }
            } else {
                // TODO 错误处理，缺少表达式
            }

            addResult(SynSymbol.PrimaryExp);
            return true;
        }

        if (isLVal()) {
            addResult(SynSymbol.PrimaryExp);
            return true;
        }

        if (isNumber()) {
            addResult(SynSymbol.PrimaryExp);
            return true;
        }
        return false;
    }

    /**
     * @description: 分析数值(Number)
     * @return {Boolean} 是否是数值
     */
    private boolean isNumber() {
        // Number = IntConst
        if (lexSym == LexSymbol.INTCON) {
            nextSym();

            addResult(SynSymbol.Number);
            return true;
        }
        return false;
    }

    /**
     * @description: 分析常量表达式(ConstExp)
     * @return {Boolean} 是否是常量表达式
     */
    private boolean isConstExp() {
        // ConstExp = AddExp
        if (isAddExp()) {
            addResult(SynSymbol.ConstExp);
            return true;
        }
        return false;
    }

    /**
     * @description: 获得表达式的值,具体实现在ExpCalculate中
     * @return {int} 表达式的值
     */
    private int getExpValue() {
        return expCalculate.getExpValue();
    }

    /**
     * @description: 分析<函数形参>
     * @param {LevTreeNode} levTreeNode 层次树节点
     * @return {Boolean} 是否有形参
     * 
     * 函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
     * 1. FuncFParam 定义一个函数的一个形式参数。当 Ident 后面的可选部分存在时， 表示数组定义。
     * 2. 当 FuncFParam 为数组定义时，其第一维的⻓度省去（用方括号[ ]表示），而后面的各维则需要
     * 用表达式指明⻓度，⻓度是常量。
     * 3. 函数实参的语法是 Exp。对于 int 类型的参数，遵循按值传递；对于数组类型的参数，则形参接收
     * 的是实参数组的地址，并通过地址间接访问实参数组中的元素。
     * 4. 对于多维数组，可以传递其中的一部分到形参数组中。例如，若 int a[4][3], 则 a[1] 是包含三个元
     * 素的一维数组，a[1] 可以作为参数传递给类型为 int[] 的形参。
     * 5. 常量数组如 const int arr[3] = {1,2,3} ，常量数组 arr 不能作为参数传入到函数中
     */
    private Boolean parseFuncFParam(LevTreeNode levTreeNode) {
        if (!isBType(lexParser.sym)) {
            // 开头不是基本类型，直接返回，返回后测试是否是右小括号，如果是说明没参数，否则在那里报错
            return false;
        }
        nextSym();

        if (lexSym == LexSymbol.IDENFR) {
            nextSym();
        } else {
            // TODO 错误处理，函数形参缺少标识符
        }

        int dim = 0;
        Queue<Integer> dimSizeQueue = new LinkedList<>();

        // 先解决第一个维度
        if (lexSym == LexSymbol.LBRACK) {
            nextSym();
            if (lexSym == LexSymbol.RBRACK) {
                nextSym();
            } else {
                // 错误处理，缺少右中括号
                Error.report(Error.ErrorType.k, lexParser.lineCounter);
            }
            dim++;
            dimSizeQueue.add(0);
        }

        // 再解决后面的维度
        while (lexSym == LexSymbol.LBRACK) {
            nextSym();

            expCalculate.enable();
            if (isConstExp()) {
                int value = getExpValue();
                if (value <= 0) {
                    // TODO 错误处理，数组维度不能为负数或零
                }
                dimSizeQueue.add(value);
            } else {
                // TODO 错误处理，数组元素有维度没写或不是表达式
            }
            if (lexSym == LexSymbol.RBRACK) {
                nextSym();
            } else {
                // 错误处理，缺少右中括号
                Error.report(Error.ErrorType.k, lexParser.lineCounter);
            }
            dim++;
        }

        String id = lexParser.symId;

        if (dim == 0) {
            // 变量
            symbolTable.enter(SymbolTable.SymbolFactory.createSymbol(
                    new Variable<Integer>().new VariableBuilder()
                            .basicInfo(id, SymbolType.variable, levNum)));
        } else {
            // 变量数组
            Integer[] dimSizeArray = new Integer[dimSizeQueue.size()];
            dimSizeQueue.toArray(dimSizeArray);

            symbolTable.enter(SymbolTable.SymbolFactory.createSymbol(
                    new VariableArray<Integer>().new VariableArrayBuilder()
                            .basicInfo(id, SymbolType.constant, levNum)
                            .allocate(dim, dimSizeArray)));
        }

        addResult(SynSymbol.FuncFParam);
        return true;
    }
}
