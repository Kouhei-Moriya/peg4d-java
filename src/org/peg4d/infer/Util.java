package org.peg4d.infer;

import java.util.ArrayList;
import java.util.List;

public class Util {
	public static <T> List<T> newList(int size) {
		List<T> ret = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			ret.add(null);
		}
		return ret;
	}
	
	public static <T> List<List<T>> newListOfList(int size) {
		List<List<T>> ret = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			ret.add(new ArrayList<T>());
		}
		return ret;
	}
	
	public static List<Branch> newListOfBranch(int size) {
		List<Branch> ret = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			ret.add(new Branch());
		}
		return ret;
	}
}
