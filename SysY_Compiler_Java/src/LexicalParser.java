import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @Author : NieFire planet_class@foxmail.com
 * @Date : 2023-11-11 05:08:19
 * @LastEditors : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-12 23:57:00
 * @FilePath : \Student\src\LexicalParser.java
 * @Description : 词法分析器负责的工作是从源代码里面读取文法符号
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 *    @Copyright (c) 2023 by NieFire, All Rights Reserved.
 */
public class LexicalParser {
    class Token {
        String category;
        LexSymbol symbol;

        Token(LexSymbol symbol, String category) {
            this.symbol = symbol;
            this.category = category;
        }
    }

    /**
     * 符号所允许的最大长度
     */
    private final int MAX_ID_LEN = 10;

    /**
     * 数值所允许的最大位数
     */
    private final int MAX_NUM_LEN = 14;

    /**
     * 刚刚读入的字符
     */
    private char ch = ' ';

    /**
     * 当前读入文件的字符数组
     */
    private char[] lineCharArr;

    /**
     * 当前读入文件字符串的长度
     */
    public int lineLength = 0;

    /**
     * 当前字符在当前读入文件字符串中的位置
     */
    public int charCounter = 0;

    /**
     * 当前字符所在行数
     */
    public int lineCounter = 1;

    /**
     * 当前读入的符号的种别码
     */
    public LexSymbol sym;

    /**
     * 预读符号的种别码
     */
    public LexSymbol peekSym;

    /**
     * 如当前读入为数字，记录其值
     */
    public String symValue = "";

    /**
     * 如当前读入为标识符，记录其名称
     */
    public String symId = "";

    /**
     * 数值
     */
    public int num = 0;

    /**
     * 将词与词的种别码对应起来，使用map数据结构
     */
    private final Map<String, LexSymbol> KEYWORD_OR_IDENT = new HashMap<String, LexSymbol>();
    private final Map<String, LexSymbol> NUMBER = new HashMap<String, LexSymbol>();
    private final Map<String, LexSymbol> OPERATOR = new HashMap<String, LexSymbol>();

    /**
     * 所得结果键值对
     */
    public List<Token> result = new ArrayList<>();

    /**
     * 根据实验需求，读入下一个字符后，才将当前字符传给中介，因此需要两个Str作为缓存
     */
    private String tempKey = "";
    private String tempStr = "";

    /**
     * 预读sym队列，队列里存放种别码和值
     */
    private class SymQueue {
        private String sym;
        private String value;

        SymQueue(String sym, String value) {
            this.sym = sym;
            this.value = value;
        }
    }

    private Queue<SymQueue> peekSymQueue = new LinkedList<>();
    private int peekSymQueuePointer = 0;

    /**
     * 读中介者
     */
    private IFileWriteMediator fileWriteMediator;

    public LexicalParser(IFileReadMediator fileReadMediator, IFileWriteMediator fileWriteMediator) throws Exception {
        // 初始化各种词法符号
        initKeywordOrIdent();
        initNumber();
        initOperator();

        this.fileWriteMediator = fileWriteMediator;

        // 加载文件
        String line = fileReadMediator.readFile();
        lineLength = line.length();
        lineCharArr = line.toCharArray();
        charCounter = 0;

        getch();
    }

    /**
     * @description: 输出词法分析器的结果
     * @throws Exception
     */
    public void writeLexResult() throws Exception {
        for (Token token : result) {
            fileWriteMediator.write(token.symbol + " " + token.category + "\n");
        }
        fileWriteMediator.writeFile();
    }

    /**
     * @description: 词法分析函数
     * @throws Exception
     */
    public void lexsParse() throws Exception {
        while (ch != '\0') {
            getsym();
        }
    }

    private void initKeywordOrIdent() {
        KEYWORD_OR_IDENT.put("main", LexSymbol.MAINTK);
        KEYWORD_OR_IDENT.put("const", LexSymbol.CONSTTK);
        KEYWORD_OR_IDENT.put("int", LexSymbol.INTTK);
        KEYWORD_OR_IDENT.put("break", LexSymbol.BREAKTK);
        KEYWORD_OR_IDENT.put("continue", LexSymbol.CONTINUETK);
        KEYWORD_OR_IDENT.put("if", LexSymbol.IFTK);
        KEYWORD_OR_IDENT.put("else", LexSymbol.ELSETK);
        KEYWORD_OR_IDENT.put("while", LexSymbol.WHILETK);
        KEYWORD_OR_IDENT.put("getint", LexSymbol.GETINTTK);
        KEYWORD_OR_IDENT.put("printf", LexSymbol.PRINTFTK);
        KEYWORD_OR_IDENT.put("return", LexSymbol.RETURNTK);
        KEYWORD_OR_IDENT.put("void", LexSymbol.VOIDTK);
        KEYWORD_OR_IDENT.put("Ident", LexSymbol.IDENFR);
        KEYWORD_OR_IDENT.put("FormatString", LexSymbol.STRCON);
    }

    private void initNumber() {
        NUMBER.put("IntConst", LexSymbol.INTCON);
    }

    private void initOperator() {
        OPERATOR.put("!", LexSymbol.NOT);
        OPERATOR.put("&&", LexSymbol.AND);
        OPERATOR.put("||", LexSymbol.OR);
        OPERATOR.put(">=", LexSymbol.GEQ);
        OPERATOR.put("==", LexSymbol.EQL);
        OPERATOR.put("<=", LexSymbol.LEQ);
        OPERATOR.put("+", LexSymbol.PLUS);
        OPERATOR.put("-", LexSymbol.MINU);
        OPERATOR.put("!=", LexSymbol.NEQ);
        OPERATOR.put("*", LexSymbol.MULT);
        OPERATOR.put("=", LexSymbol.ASSIGN);
        OPERATOR.put("/", LexSymbol.DIV);
        OPERATOR.put(";", LexSymbol.SEMICN);
        OPERATOR.put("%", LexSymbol.MOD);
        OPERATOR.put(",", LexSymbol.COMMA);
        OPERATOR.put("<", LexSymbol.LSS);
        OPERATOR.put("(", LexSymbol.LPARENT);
        OPERATOR.put(")", LexSymbol.RPARENT);
        OPERATOR.put(">", LexSymbol.GRE);
        OPERATOR.put("[", LexSymbol.LBRACK);
        OPERATOR.put("]", LexSymbol.RBRACK);
        OPERATOR.put("{", LexSymbol.LBRACE);
        OPERATOR.put("}", LexSymbol.RBRACE);
    }

    /**
     * @description: 读取下一个字符
     */
    private void getch() {

        // 如果当前字符串已经读完，说明已经读完文件，返回
        if (charCounter == lineLength) {
            return;
        }

        ch = lineCharArr[charCounter];
        if (ch == '\0') {
            return;
        }
        charCounter++;

        if (ch == '\n') {
            lineCounter++;
        }
    }

    /**
     * @description: 预读队列初始化
     */
    public void iniPeekSym() {
        peekSymQueuePointer = 0;
    }

    /**
     * @description: 预读到下一个分号 检查这一语句中是否有与传入的sym相同的符号
     * @param {LexSymbol} lexSymbol
     * @return {Boolean} 是否有相同符号
     */
    public Boolean peekMatchSym(LexSymbol lexSymbol) {
        iniPeekSym();
        peekSym();
        while (peekSym != LexSymbol.SEMICN) {
            if (peekSym == lexSymbol) {
                iniPeekSym();
                return true;
            }
            peekSym();
        }
        iniPeekSym();
        return false;
    }

    /**
     * @description: 预读下一个sym，但不读入
     */
    public void peekSym() {
        // 如果此时tempKey和tempStr不是""，说明上一个sym还没有写入，需要先写入
        if (!"".equals(tempKey) && !"".equals(tempStr)) {
            if (OPERATOR.containsKey(tempKey)) {
                fileWriteMediator.write(OPERATOR.get(tempKey) + " " + tempStr + "\n");
            } else if (KEYWORD_OR_IDENT.containsKey(tempKey)) {
                fileWriteMediator.write(KEYWORD_OR_IDENT.get(tempKey) + " " + tempStr + "\n");
            } else if (NUMBER.containsKey(tempKey)) {
                fileWriteMediator.write(NUMBER.get(tempKey) + " " + tempStr + "\n");
            } else {
                // 错误处理
            }

            tempKey = "";
            tempStr = "";
        }

        if (peekSymQueue != null && !peekSymQueue.isEmpty() && peekSymQueuePointer < peekSymQueue.size()) {
            // 根据指针读取
            peekSym = OPERATOR.get(((LinkedList<SymQueue>) peekSymQueue).get(peekSymQueuePointer).sym);
            if (peekSym == null) {
                peekSym = KEYWORD_OR_IDENT.get(((LinkedList<SymQueue>) peekSymQueue).get(peekSymQueuePointer).sym);
            }
            if (peekSym == null) {
                peekSym = NUMBER.get(((LinkedList<SymQueue>) peekSymQueue).get(peekSymQueuePointer).sym);
            }
            peekSymQueuePointer++;
            return;
        }

        while (Character.isWhitespace(ch)) {
            // 跳过所有空白字符
            getch();
        }

        if (ch == '\0') {
            return;
        }

        while (ch == '/') {
            getch();
            switch (ch) {
                case '/':
                    // 读到下一个\n
                    while (true) {
                        getch();
                        if (ch == '\n') {
                            break;
                        }
                    }
                    getch();

                    while (Character.isWhitespace(ch)) {
                        getch();
                    }
                    break;
                case '*':
                    while (true) {
                        getch();
                        if (ch == '*') {
                            getch();
                            if (ch == '/') {
                                getch();
                                break;
                            }
                        }
                    }
                    break;
                default:
                    peekSymQueue.add(new SymQueue("/", "/"));
                    peekSym = OPERATOR.get("/");
                    return;
            }
        }

        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
            // 关键字或者一般标识符
            matchKeywordOrIdentifier("T");
        } else if (ch == '"') {
            matchString("T");
        } else if (ch >= '0' && ch <= '9') {
            // 数字
            matchNumber("T");
        } else {
            // 操作符
            matchOperator("T");
        }

        peekSymQueuePointer++;
    }

    public void getsym() {

        if (!peekSymQueue.isEmpty()) {
            SymQueue symQueue = peekSymQueue.poll();
            updateResultAndSym(symQueue.sym, symQueue.value);
            return;
        }

        while (Character.isWhitespace(ch)) {
            // 跳过所有空白字符
            getch();
        }

        if (ch == '\0') {
            writeTempResult();
            return;
        }

        while (ch == '/') {
            getch();
            switch (ch) {
                case '/':
                    // 读到下一个\n
                    while (true) {
                        getch();
                        if (ch == '\n') {
                            break;
                        }
                    }
                    getch();

                    while (Character.isWhitespace(ch)) {
                        getch();
                    }
                    break;
                case '*':
                    while (true) {
                        getch();
                        if (ch == '*') {
                            getch();
                            if (ch == '/') {
                                getch();
                                break;
                            }
                        }
                    }
                    getch();

                    while (Character.isWhitespace(ch)) {
                        getch();
                    }
                    break;
                default:
                    updateResultAndSym("/", "/");
                    return;
            }
        }

        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
            // 关键字或者一般标识符
            matchKeywordOrIdentifier();
        } else if (ch == '"') {
            matchString();
        } else if (ch >= '0' && ch <= '9') {
            // 数字
            matchNumber();
        } else {
            // 操作符
            matchOperator();
        }

    }

    private void matchString() {
        // 首先，将"读入
        String str = "";
        str += ch;
        ch = lineCharArr[charCounter];
        charCounter++;
        while (ch != '"') {

            str += ch;
            ch = lineCharArr[charCounter];
            charCounter++;

            if (ch == '\\') {
                str += ch;
                ch = lineCharArr[charCounter];
                charCounter++;

                str += ch;
                ch = lineCharArr[charCounter];
                charCounter++;

            }
        }
        str += ch;
        ch = lineCharArr[charCounter];
        charCounter++;

        updateResultAndSym("FormatString", str);
    }

    private void matchString(String str) {
        if (!"T".equals(str)) {
            return;
        }

        // 首先，将"读入
        String tempStr = "";
        tempStr += ch;
        ch = lineCharArr[charCounter];
        charCounter++;
        while (ch != '"') {

            tempStr += ch;
            ch = lineCharArr[charCounter];
            charCounter++;

            if (ch == '\\') {
                tempStr += ch;
                ch = lineCharArr[charCounter];
                charCounter++;

                tempStr += ch;
                ch = lineCharArr[charCounter];
                charCounter++;

            }
        }
        tempStr += ch;
        ch = lineCharArr[charCounter];
        charCounter++;

        peekSymQueue.add(new SymQueue("FormatString", tempStr));
        peekSym = KEYWORD_OR_IDENT.get("FormatString");
    }

    private void matchKeywordOrIdentifier() {
        StringBuilder sb = new StringBuilder(MAX_ID_LEN);

        // 首先把整个单词读出来
        do {
            sb.append(ch);
            getch();
        } while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9');

        symId = sb.toString();

        // 搜索是否是保留字
        if (KEYWORD_OR_IDENT.containsKey(symId)) {
            updateResultAndSym(symId, symId);
        } else {
            // 是一般标识符
            updateResultAndSym("Ident", symId);
        }
    }

    private void matchKeywordOrIdentifier(String str) {
        if (!"T".equals(str)) {
            return;
        }

        StringBuilder sb = new StringBuilder(MAX_ID_LEN);

        do {
            sb.append(ch);
            getch();
        } while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9');

        symId = sb.toString();

        if (KEYWORD_OR_IDENT.containsKey(symId)) {
            peekSymQueue.add(new SymQueue(symId, symId));
            peekSym = KEYWORD_OR_IDENT.get(symId);
        } else {
            peekSymQueue.add(new SymQueue("Ident", symId));
            peekSym = KEYWORD_OR_IDENT.get("Ident");
        }
    }

    private void matchNumber() {
        int i = 0;
        num = 0;
        do {
            num = num * 10 + ch - '0';
            i++;
            getch();
        } while (ch >= '0' && ch <= '9');
        if (i > MAX_NUM_LEN) {
            // 数值位数过多
        }
        updateResultAndSym("IntConst", String.valueOf(num));
    }

    private void matchNumber(String str) {
        if (!"T".equals(str)) {
            return;
        }

        int i = 0;
        num = 0;
        do {
            num = num * 10 + ch - '0';
            i++;
            getch();
        } while (ch >= '0' && ch <= '9');
        if (i > MAX_NUM_LEN) {
            // 数值位数过多
        }
        peekSymQueue.add(new SymQueue("IntConst", String.valueOf(num)));
        peekSym = NUMBER.get("IntConst");
    }

    private void matchOperator() {
        switch (ch) {
            // 处理双字符运算符
            case '=':
                getch();
                if (ch == '=') {
                    updateResultAndSym("==", "==");
                    getch();
                } else {
                    updateResultAndSym("=", "=");
                }
                break;
            case '!':
                getch();
                if (ch == '=') {
                    updateResultAndSym("!=", "!=");
                    getch();
                } else {
                    updateResultAndSym("!", "!");
                }
                break;
            case '<':
                getch();
                if (ch == '=') {
                    updateResultAndSym("<=", "<=");
                    getch();
                } else {
                    updateResultAndSym("<", "<");
                }
                break;
            case '>':
                getch();
                if (ch == '=') {
                    updateResultAndSym(">=", ">=");
                    getch();
                } else {
                    updateResultAndSym(">", ">");
                }
                break;
            case '&':
                getch();
                if (ch == '&') {
                    updateResultAndSym("&&", "&&");
                    getch();
                } else {
                    // 错误处理,非法字符
                    Error.report(Error.ErrorType.a, lineCounter);
                }
                break;
            case '|':
                getch();
                if (ch == '|') {
                    updateResultAndSym("||", "||");
                    getch();
                } else {
                    // 错误处理,非法字符
                    Error.report(Error.ErrorType.a, lineCounter);
                }
                break;
            default:
                // 处理单字符运算符
                if (OPERATOR.containsKey(String.valueOf(ch))) {
                    updateResultAndSym(String.valueOf(ch), String.valueOf(ch));
                    getch();
                } else {
                    // 错误处理
                    Error.report(Error.ErrorType.a, lineCounter);
                }
                break;
        }
    }

    private void matchOperator(String str) {
        if (!"T".equals(str)) {
            return;
        }

        switch (ch) {
            // 处理双字符运算符
            case '=':
                getch();
                if (ch == '=') {
                    peekSymQueue.add(new SymQueue("==", "=="));
                    peekSym = OPERATOR.get("==");
                    getch();
                } else {
                    peekSymQueue.add(new SymQueue("=", "="));
                    peekSym = OPERATOR.get("=");
                }
                break;
            case '!':
                getch();
                if (ch == '=') {
                    peekSymQueue.add(new SymQueue("!=", "!="));
                    peekSym = OPERATOR.get("!=");
                    getch();
                } else {
                    peekSymQueue.add(new SymQueue("!", "!"));
                    peekSym = OPERATOR.get("!");
                }
                break;
            case '<':
                getch();
                if (ch == '=') {
                    peekSymQueue.add(new SymQueue("<=", "<="));
                    peekSym = OPERATOR.get("<=");
                    getch();
                } else {
                    peekSymQueue.add(new SymQueue("<", "<"));
                    peekSym = OPERATOR.get("<");
                }
                break;
            case '>':
                getch();
                if (ch == '=') {
                    peekSymQueue.add(new SymQueue(">=", ">="));
                    peekSym = OPERATOR.get(">=");
                    getch();
                } else {
                    peekSymQueue.add(new SymQueue(">", ">"));
                    peekSym = OPERATOR.get(">");
                }
                break;
            case '&':
                getch();
                if (ch == '&') {
                    peekSymQueue.add(new SymQueue("&&", "&&"));
                    peekSym = OPERATOR.get("&&");
                    getch();
                } else {
                    // 错误处理,非法字符
                    Error.report(Error.ErrorType.a, lineCounter);
                }
                break;
            case '|':
                getch();
                if (ch == '|') {
                    peekSymQueue.add(new SymQueue("||", "||"));
                    peekSym = OPERATOR.get("||");
                    getch();
                } else {
                    // 错误处理,非法字符
                    Error.report(Error.ErrorType.a, lineCounter);
                }
                break;
            default:
                // 处理单字符运算符
                if (OPERATOR.containsKey(String.valueOf(ch))) {
                    peekSymQueue.add(new SymQueue(String.valueOf(ch), String.valueOf(ch)));
                    peekSym = OPERATOR.get(String.valueOf(ch));
                    getch();
                } else {
                    // 错误处理,非法字符
                    Error.report(Error.ErrorType.a, lineCounter);
                }
                break;
        }
    }

    /**
     * @description: 记录词及其种别码，并更新sym
     * @param {String} key
     * @param {String} str
     */
    private void updateResultAndSym(String key, String str) {
        if (OPERATOR.containsKey(key)) {
            result.add(new Token(OPERATOR.get(key), str));
            sym = OPERATOR.get(key);
        } else if (KEYWORD_OR_IDENT.containsKey(key)) {
            result.add(new Token(KEYWORD_OR_IDENT.get(key), str));
            sym = KEYWORD_OR_IDENT.get(key);

            if ("Ident".equals(key)) {
                symId = str;
            }

        } else if (NUMBER.containsKey(key)) {
            result.add(new Token(NUMBER.get(key), str));
            sym = NUMBER.get(key);
            symValue = str;
        }

        writeTempResult(key, str);
    }

    private void writeTempResult(String key, String str) {
        if (!"".equals(tempKey) && !"".equals(tempStr)) {
            if (OPERATOR.containsKey(tempKey)) {
                fileWriteMediator.write(OPERATOR.get(tempKey) + " " + tempStr + "\n");
            } else if (KEYWORD_OR_IDENT.containsKey(tempKey)) {
                fileWriteMediator.write(KEYWORD_OR_IDENT.get(tempKey) + " " + tempStr + "\n");
            } else if (NUMBER.containsKey(tempKey)) {
                fileWriteMediator.write(NUMBER.get(tempKey) + " " + tempStr + "\n");
            }
        }
        tempKey = key;
        tempStr = str;
    }

    private void writeTempResult() {
        if (!"".equals(tempKey) && !"".equals(tempStr)) {
            if (OPERATOR.containsKey(tempKey)) {
                fileWriteMediator.write(OPERATOR.get(tempKey) + " " + tempStr + "\n");
            } else if (KEYWORD_OR_IDENT.containsKey(tempKey)) {
                fileWriteMediator.write(KEYWORD_OR_IDENT.get(tempKey) + " " + tempStr + "\n");
            } else if (NUMBER.containsKey(tempKey)) {
                fileWriteMediator.write(NUMBER.get(tempKey) + " " + tempStr + "\n");
            } else {
                // 错误处理
            }
        }
    }
}
