/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-10-06 18:03:44
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-13 00:13:00
 * @FilePath     : \Student\src\IFileReadMediator.java
 * @Description  : 文件读取中介者的接口
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public interface IFileReadMediator {
    /**
     * 读文件函数，返回整个文件的字符串，去除所有换行符
     * @return {String} 整个文件的字符串
     * @throws Exception
     */
    public String readFile() throws Exception;

    /**
     * 关闭文件流
     * @throws Exception
     */
    public void close() throws Exception;
}
