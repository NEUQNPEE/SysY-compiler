import java.io.BufferedReader;
/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 05:08:19
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 00:15:24
 * @FilePath     : \Student\src\FileReadMediator.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class FileReadMediator implements IFileReadMediator {
    private BufferedReader br;

    public FileReadMediator(String source) throws Exception {
        br = new BufferedReader(new java.io.FileReader(source));
    }

    @Override
    public String readFile() throws Exception {
        String line = "";
        String result = "";

        while ((line = br.readLine()) != null) {
            result += line;
            result += '\n';
        }

        result += '\0';

        return result;
    }

    @Override
    public void close() throws Exception {
        br.close();
    }

    
}
