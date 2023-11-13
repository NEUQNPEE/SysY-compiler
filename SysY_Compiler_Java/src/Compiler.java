import java.io.FileInputStream;
import java.io.IOException;

/**
 * @Author : NieFire planet_class@foxmail.com
 * @Date : 2023-09-20 20:29:43
 * @LastEditors : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 16:52:45
 * @FilePath : \Student\src\Compiler.java
 * @Description :
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 *    @Copyright (c) 2023 by NieFire, All Rights Reserved.
 * 
 *    readme 更新日志
 *    readme 2023年10月7日00:30:59 完成了词法分析器的修正，可以投入使用；语法分析器已经开始测试，函数形参已经可以识别
 *    readme 接下来开始写语法分析器
 * 
 *    readme 2023年10月7日16:29:57 正在编写词法分析器，发现应该将词法分析器和语法分析器写成单例模式，因为他们都是全局唯一的
 *    readme 这样可以都只用一个函数启用词法分析器和语法分析器，而不用先初始化各种参数
 * 
 */
public class Compiler {
    // 编译器配置参数

    /**
     * @description : 是否输出词法分析结果
     */
    public static boolean isOutputLexResult = false;

    /**
     * @description : 是否输出语法分析结果
     */
    public static boolean isOutputSynResult = false;

    /**
     * @description : 词法分析器与语法分析器
     */
    public static LexicalParser lexParser;
    public static SyntacticParser synParser;

    /**
     * @description : IO文件路径
     */
    private static String source;
    private static String target;

    public static void main(String[] args) throws Exception {
        // 实验需求，打开输出
        isOutputLexResult = true;
        isOutputSynResult = true;

        // testAll();

        // 文件在当前目录下
        // 在vscode中使用这个路径
        source = "SysY_Compiler_Java/src/testfile/testfile0.txt";
        target = "SysY_Compiler_Java/src/output/output0.txt";
        // source = "testfile.txt";
        // target = "output.txt";

        // 初始化io中介者
        IFileReadMediator fileReadMediator = new FileReadMediator(source);
        IFileWriteMediator fileWriteMediator = new FileWriteMediator(target);

        // 初始化词法分析器
        lexParser = new LexicalParser(fileReadMediator, fileWriteMediator);

        // 单独启用词法分析器
        // lexParser.lexsParse();
        // if (isOutputLexResult) {
        //     fileWriteMediator.write("202112123 宋子墨");
        //     fileWriteMediator.writeFile();
        // } else {
        //     fileWriteMediator.clear();
        // }

        // 初始化语法分析器
        synParser = new SyntacticParser(fileWriteMediator, lexParser);

        synParser.parse();

        if (isOutputSynResult) {
            fileWriteMediator.write("202112123 宋子墨");
            fileWriteMediator.writeFile();
        } else {
            fileWriteMediator.clear();
        }

        fileReadMediator.close();
        fileWriteMediator.close();

    }

    /**
     * @description : 测试所有文件,并且检查输出结果是否与标准输出相同,从文件1读到文件15
     * @throws Exception
     */
    public static void testAll() throws Exception {
        for (int i = 1; i <= 15; i++) {

            source = "SysY_Compiler_Java/src/testfile/testfile" + i + ".txt";
            target = "SysY_Compiler_Java/src/output/output" + i + ".txt";

            IFileReadMediator fileReadMediator = new FileReadMediator(source);
            IFileWriteMediator fileWriteMediator = new FileWriteMediator(target);

            lexParser = new LexicalParser(fileReadMediator, fileWriteMediator);
            synParser = new SyntacticParser(fileWriteMediator, lexParser);

            // 由于符号表已经写成单例模式，因此每个文件都要初始化一次
            SymbolTable.getInstance().init();

            synParser.parse();
            if (isOutputSynResult) {
                fileWriteMediator.writeFile();
            } else {
                fileWriteMediator.clear();
            }
            fileReadMediator.close();
            fileWriteMediator.close();
        }

        // 检查每一个output文件是否与standardOutput文件相同
        for (int i = 1; i <= 15; i++) {
            // 读取文件testfile.txt，文件在当前目录下
            source = "SysY_Compiler_Java/src/output/output" + i + ".txt";
            // 目标文件output.txt，文件在当前目录下
            target = "SysY_Compiler_Java/src/standardOutput/standardOutput" + i + ".txt";

            // 检测两个文件是否相同
            try {
                byte[] file1Content = readFileContent(source);
                byte[] file2Content = readFileContent(target);

                String file1ContentStr = new String(file1Content);
                String file2ContentStr = new String(file2Content);

                if (file1ContentStr.equals(file2ContentStr)) {
                    System.out.println("文件" + i + "相同");
                } else {
                    System.out.println("警告！文件" + i + "不相同！");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static byte[] readFileContent(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }
}
