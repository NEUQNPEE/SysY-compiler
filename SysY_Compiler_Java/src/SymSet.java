import java.util.BitSet;

/**
 * @Author       : NieFire planet_class@foxmail.com
 * @Date         : 2023-11-11 05:08:19
 * @LastEditors  : NieFire planet_class@foxmail.com
 * @LastEditTime : 2023-11-12 17:53:20
 * @FilePath     : \Student\src\SymSet.java
 * @Description  : 把 java.util.BitSet 包装一下，以便于编写代码
 * ????虽然你源代码这么说，但我连这轮子干什么的都不知道，干脆拿来主义了
 * @( ﾟ∀。)只要加满注释一切都会好起来的( ﾟ∀。)
 * @Copyright (c) 2023 by NieFire, All Rights Reserved. 
 */
public class SymSet extends BitSet{

    /**
	 * 这个域没有特别意义 
	 */
	private static final long serialVersionUID = 8236959240158320958L;

	/**
	 * 构造一个符号集合
	 * @param nbits 这个集合的容量
	 */
	public SymSet(int nbits) {
		super(nbits);
	}

	/**
	 * 把一个符号放到集合中
	 * @param s 要放置的符号
	 */
	public void set(LexSymbol s) {
		set(s.ordinal());
	}
	
	/**
	 * 检查一个符号是否在集合中
	 * @param s 要检查的符号
	 * @return 若符号在集合中，则返回true，否则返回false
	 */
	public boolean get(LexSymbol s) {
		return get(s.ordinal());
	}
    
}
