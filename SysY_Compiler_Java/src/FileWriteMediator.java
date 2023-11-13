import java.io.BufferedWriter;
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 05:08:19
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 16:32:06
 * @FilePath     : \SysYCompiler\src\FileWriteMediator.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class FileWriteMediator implements IFileWriteMediator {
    private BufferedWriter bw;
    private String str;

    public FileWriteMediator(String target) throws Exception {
        // 如果文件不存在则创建
        if(!new java.io.File(target).exists()) {
            new java.io.File(target).createNewFile();
        }
        // bw = new BufferedWriter(new java.io.FileWriter(target));
        bw = new BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(target), "utf-8"));
        str = "";
    }
    
    @Override
    public void writeFile() throws Exception {
        bw.write(str);
        str = "";
    }

    @Override
    public void write(String str) {
        this.str += str;
    }

    @Override
    public void clear() {
        str = "";
    }

    @Override
    public void close() throws Exception {
        bw.close();
    }

}
