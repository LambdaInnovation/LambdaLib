/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under  
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.liutils.util.generic;

/**
 * Debug-related utility functions.
 * @author WeAthFolD
 */
public class DebugUtils {
	
	public static String formatArray(Object... arr) {
		StringBuilder b = new StringBuilder("(");
		for(int i = 0; i < arr.length; ++i) {
			b.append(arr[i]).append(i == arr.length - 1 ? "" : ", "); 
		}
		return b.append(")").toString();
	}
	
	public static String formatArray(boolean[] arr) {
		StringBuilder b = new StringBuilder("(");
		for(int i = 0; i < arr.length; ++i) {
			b.append(arr[i]).append(i == arr.length - 1 ? "" : ", "); 
		}
		return b.append(")").toString();
	}
	
	public static String formatArray(float[] arr) {
		StringBuilder b = new StringBuilder("(");
		for(int i = 0; i < arr.length; ++i) {
			b.append(arr[i]).append(i == arr.length - 1 ? "" : ", "); 
		}
		return b.append(")").toString();
	}
	
	public static String formatArray(int[] arr) {
		StringBuilder b = new StringBuilder("(");
		for(int i = 0; i < arr.length; ++i) {
			b.append(arr[i]).append(i == arr.length - 1 ? "" : ", "); 
		}
		return b.append(")").toString();
	}
	
	public static String formatArray(double[] arr) {
		StringBuilder b = new StringBuilder("(");
		for(int i = 0; i < arr.length; ++i) {
			b.append(arr[i]).append(i == arr.length - 1 ? "" : ", "); 
		}
		return b.append(")").toString();
	}

}
