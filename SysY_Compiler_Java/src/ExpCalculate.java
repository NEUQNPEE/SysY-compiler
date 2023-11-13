
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-08 15:27:25
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 00:39:09
 * @FilePath     : \Student\src\ExpCalculate.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class ExpCalculate {

    /**
     * 运算符栈和数字栈
     */
    Stack<String> opStack = new Stack<String>();
    Stack<Integer> numStack = new Stack<Integer>();

    /**
     * 运算符map，key为运算符，value为优先级
     */
    Map<String, Integer> opMap = new HashMap<String, Integer>();

    /**
     * 上一个表达式的值
     */
    private int lastExpValue = 0;

    /**
     * 是否正在读取常量表达式
     */
    private Boolean isReadingConstExp = false;

    /**
     * 启用与禁用常量表达式读取
     */
    public void enable() {
        isReadingConstExp = true;
    }

    public void disable() {
        isReadingConstExp = false;
        clearQueue();
    }

    /**
     * 是否正在读取常量表达式
     */
    public Boolean isEnable() {
        return isReadingConstExp;
    }

    /**
     * 常量表达式符号暂存队列
     */
    private Queue<String> constExpSymQueue = new LinkedList<>();

    /**
     * 向队列中添加符号
     * 
     * @param {String} sym 符号
     */
    public void addSym(String sym) {
        if (!isLegalSym(sym)) {
            calculateExpValue();
            clearQueue();
            return;
        }

        // 在这里检测正负号互相抵消和连续出现的情况
        if (constExpSymQueue.size() > 0) {
            String lastSym = constExpSymQueue.peek();

            if ("+".equals(lastSym) && "-".equals(sym)) {
                // + - 抵消
                constExpSymQueue.poll();
                return;

            } else if ("-".equals(lastSym) && "+".equals(sym)) {
                // - + 抵消
                constExpSymQueue.poll();
                return;

            } else if ("+".equals(lastSym) && "+".equals(sym)) {
                // + + 变 +
                constExpSymQueue.poll();
                return;

            } else if ("-".equals(lastSym) && "-".equals(sym)) {
                // - - 变 +
                constExpSymQueue.poll();
                constExpSymQueue.poll();
                constExpSymQueue.add("+");
                return;
            }
        }
        constExpSymQueue.add(sym);
    }

    /**
     * @description: 直接添加数字
     * @param {Integer} integer
     */
    public void addNum(Integer integer) {
        constExpSymQueue.add(integer.toString());
    }

    /**
     * 清空队列
     */
    public void clearQueue() {
        constExpSymQueue.clear();
    }

    /**
     * 合法符号集合
     */
    public static final String[] LEGAL_SYM = {
            "+", "-", "*", "/", "%", "^", "(", ")" };

    ExpCalculate() {
        // 初始化运算符map
        opMap.put("+", 1);
        opMap.put("-", 1);
        opMap.put("*", 2);
        opMap.put("/", 2);
        opMap.put("%", 2);
        opMap.put("^", 3);
        opMap.put("(", 0);
        opMap.put(")", 0);
    }

    /**
     * 计算表达式的值
     */
    private void calculateExpValue() {
        if (constExpSymQueue.isEmpty()) {
            return;
        }

        if (isAllSym()) {
            return;
        }

        if (!isBracketMatch()) {
            return;
        }

        while (!constExpSymQueue.isEmpty()) {
            String sym = constExpSymQueue.poll();

            if (opMap.containsKey(sym)) {
                // 是运算符
                if ("(".equals(sym)) {
                    // 左括号直接入栈
                    opStack.push(sym);
                } else if (")".equals(sym)) {
                    // 右括号，弹出栈顶运算符，计算，直到遇到左括号
                    while (!("(".equals(opStack.peek()))) {
                        String op = opStack.pop();
                        int num2 = numStack.pop();
                        int num1 = numStack.pop();
                        int result = calculate(num1, num2, op);
                        numStack.push(result);
                    }
                    opStack.pop(); // 弹出左括号
                } else {
                    // 是运算符
                    if (opStack.isEmpty() || opStack.peek().equals("(")) {
                        // 运算符栈为空或栈顶为左括号，直接入栈
                        opStack.push(sym);
                    } else {
                        // 运算符栈不为空，比较优先级
                        if (opMap.get(sym) > opMap.get(opStack.peek())) {
                            // 优先级比栈顶高，直接入栈
                            opStack.push(sym);
                        } else {
                            // 优先级比栈顶低或相等，弹出栈顶运算符，计算
                            handleOneOp();

                            while (!opStack.isEmpty() && !"(".equals(opStack.peek())
                                    && opMap.get(sym) <= opMap.get(opStack.peek())) {
                                handleOneOp();
                            }
                            opStack.push(sym);
                        }
                    }
                }
            } else {
                // 是数字
                // 向操作符栈中读取一到两个操作符
                // 1. 操作符栈为空，直接入栈
                // 2. 操作符栈只有一个符号，且为+或-，把符号取出给数字
                Boolean onlyOneOpIsPlusOrMinus = opStack.size() == 1 && ("+".equals(opStack.peek())
                        || "-".equals(opStack.peek()));
                // 3. 操作符栈至少有两个符号，读出两个符号，如果为左括号 +或-，把符号取出给数字

                if (opStack.isEmpty()) {
                    // 操作符栈为空，直接入栈
                    numStack.push(Integer.parseInt(sym.toString()));

                } else if (onlyOneOpIsPlusOrMinus) {
                    // 操作符栈只有一个符号，且为+或-，把符号取出给数字
                    String op = opStack.pop();
                    int result = calculate(0, Integer.parseInt(sym.toString()), op);
                    numStack.push(result);

                } else if (opStack.size() >= 2) {
                    // 操作符栈至少有两个符号，读出两个符号，如果为左括号 +或-，把符号取出给数字
                    String op2 = opStack.pop();
                    String op1 = opStack.pop();
                    Boolean isLeftBracketWithPlusOrMinus = op1.equals("(")
                            && ("+".equals(op2) || "-".equals(op2));
                            
                    if (isLeftBracketWithPlusOrMinus) {
                        int result = calculate(0, Integer.parseInt(sym.toString()), op2);
                        numStack.push(result);
                        opStack.push(op1);
                    } else {
                        opStack.push(op1);
                        opStack.push(op2);
                        numStack.push(Integer.parseInt(sym.toString()));
                    }
                }
            }
        }

        // 表达式读取完毕，计算剩余运算符
        finishOpStack();
        lastExpValue = numStack.pop();
    }

    /**
     * 获取表达式的值
     */
    public int getExpValue() {
        disable();
        return lastExpValue;
    }

    private int calculate(int num1, int num2, String sym) {
        switch (sym) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                if (num2 == 0) {
                    // 除数为0，报错
                    System.out.println("除数为0");
                    return 0;
                } else {
                    return num1 / num2;
                }
            case "%":
                if (num2 == 0) {
                    // 除数为0，报错
                    System.out.println("除数为0");
                    return 0;
                } else {
                    return num1 % num2;
                }
            case "^":
                return (int) Math.pow(num1, num2);
            default:
                return 0;
        }
    }

    /**
     * @description: 判断是否为合法符号或数字
     */
    public static boolean isLegalSym(String sym) {
        for (String legal : LEGAL_SYM) {
            if (legal.equals(sym) || isNum(sym)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @description: 判断是否为数字
     */
    private static boolean isNum(String sym) {
        try {
            Integer.parseInt(sym);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @description: 检查栈中是否全是符号
     */
    private boolean isAllSym() {
        for (String sym : constExpSymQueue) {
            if (!opMap.containsKey(sym)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @description: 检查栈中左右括号是否匹配
     */
    private boolean isBracketMatch() {
        int leftBracketCount = 0;
        int rightBracketCount = 0;
        for (String sym : constExpSymQueue) {
            if ("(".equals(sym)) {
                leftBracketCount++;
            } else if (")".equals(sym)) {
                rightBracketCount++;
            }
        }
        return leftBracketCount == rightBracketCount;
    }

    /**
     * @description: 将opStack的内容处理完毕
     */
    public void finishOpStack() {
        while (!opStack.isEmpty()) {
            handleOneOp();
        }
    }

    /**
     * @description: 处理一个运算符
     */
    public void handleOneOp() {
        String op = opStack.pop();
        int num2 = numStack.pop();
        int num1 = numStack.pop();
        int result = calculate(num1, num2, op);
        numStack.push(result);
    }
}
