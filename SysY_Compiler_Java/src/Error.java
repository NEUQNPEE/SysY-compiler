/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-09-20 21:52:12
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-12 23:44:48
 * @FilePath     : \Student\src\Error.java
 * @Description  : 
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class Error {
	/**
	 * tip1 符号表在词法分析器中，语法分析器中只有一个符号表的引用
	 */
	public static enum ErrorType {
		/**
		 * 非法符号 // 在词法分析器中处理
		 */
		a, 
		/**
		 * 名字重定义，函数名或者变量名在当前作用域下重复定义 // 定义时检查符号表，无则加入，有则报错
		 */
		b, 
		/**
		 * 未定义的名字，使用了未定义的标识符 // 引用时检查符号表，无则报错
		 */
		c, 
		/**
		 * 函数参数个数不匹配
		 */
		d, 
		/**
		 * 函数参数类型不匹配
		 */
		e, 
		/**
		 * 无返回值的函数存在不匹配的return语句 
		 */
		f, 
		/**
		 * 有返回值的函数缺少return语句
		 */
		g, 

		/*
		* d、e、f、g综合考虑：
		* 首先，文法分析器中分析函数定义时，读入函数名之后立刻将其加入符号表，接下来分析函数形参表时，将形参表加入该函数中
		* 之后，出现函数调用的部分在一元表达式中，分析到函数名时解决c错误，之后会调用分析函数形参表的函数
		* 因此，分析函数形参表的函数中，首先检查函数名是否存在，若不存在说明是定义，将形参一一加入符号表（层次为0）
		* 若存在，说明是调用，一边读入形参一边检查形参是否匹配，若不匹配则报错，若匹配则将形参加入符号表（层次为函数层次+1）
		* 以上，解决了d、e错误

		* 再考虑分析函数定义分析，在全局定义函数栈，在函数定义分析的开头，将函数的层次与返回值类型加入函数栈
		* 语句分析时，遇到return语句时，检查函数栈栈顶。
		* 1. 如层次相同，返回值类型为void，说明是无返回值函数中出现return语句，报错 f
		* 2. 如层次相同，返回值类型不为void，说明是有返回值函数中出现return语句，检查返回值类型是否匹配（实验未要求）。如匹配，则将函数栈栈顶弹出
		* 3. 如层次不同，说明是函数更深层出现return语句，情况较复杂，暂时不考虑
		* 4. 函数定义分析结束时，检查函数栈顶，如栈不空，且返回值类型不为void，说明是有返回值函数，但是没有return语句，报错 g、
		* 以上，解决了f、g错误
		*/

		/**
		 * 不能改变常量的值 // 在语句分析时的两个赋值语句中检查符号表
		 */ 
		h, 
		/**
		 * 缺少分号 // 到处都要检查
		 */ 
		i, 
		/**
		 * 缺少右小括号 
		 */ 
		j, 
		/**
		 * 缺少右中括号 // 到处都要检查
		 */ 
		k, 
		/**
		 * printf语句中，格式字符与表达式个数不匹配 // 在语句分析的printf语句中检查
		 */
		l, 
		/**
		 * 在非循环语句中使用了break 和 continue // 在语句分析的break和continue语句中检查
		 */
		m, 
		
		/*
		 * m错误指出了一个非常重大的问题：各分析函数之间需要传递follow集。
		 * 在分析循环语句时，进入其语句块分析时传入follow集（其实也就只有break和continue）
		 * 语句分析到break和continue时，检查follow集，如不存在则报错
		 */
	}

	/**
	 * @description: 错误报告函数
	 * @param {ErrorType} errorType
	 * @param {int} line
	 */ 
	public static void report(ErrorType errorType, int line){
		switch (errorType) {
			case a:
				System.out.println("非法符号");
				System.out.println("在第" + line + "行");
				break;
			case b:
				System.out.println("名字重定义");
				System.out.println("在第" + line + "行");
				break;
			case c:
				System.out.println("未定义的名字");
				System.out.println("在第" + line + "行");
				break;
			case d:
				System.out.println("函数参数个数不匹配");
				System.out.println("在第" + line + "行");

				break;
			case e:
				System.out.println("函数参数类型不匹配");
				System.out.println("在第" + line + "行");
				break;
			case f:
				System.out.println("无返回值的函数存在不匹配的return语句");
				System.out.println("在第" + line + "行");
				break;
			case g:
				System.out.println("有返回值的函数缺少return语句");
				System.out.println("在第" + line + "行");
				break;
			case h:
				System.out.println("不能改变常量的值");
				System.out.println("在第" + line + "行");
				break;
			case i:
				System.out.println("缺少分号");
				System.out.println("在第" + line + "行");
				break;
			case j:
				System.out.println("缺少右小括号')'");
				System.out.println("在第" + line + "行");
				break;
			case k:
				System.out.println("缺少右中括号']'");
				System.out.println("在第" + line + "行");
				break;
			case l:
				System.out.println("printf中格式字符与表达式个数不匹配");
				System.out.println("在第" + line + "行");
				break;
			case m:
				System.out.println("在非循环块中使用break和continue语句");
				System.out.println("在第" + line + "行");
				break;
			default:
				break;
		}
		

	}

}
