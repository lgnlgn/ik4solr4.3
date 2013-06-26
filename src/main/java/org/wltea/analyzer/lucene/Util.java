package org.wltea.analyzer.lucene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

	/*
	 * 切分多条文件路径，以逗号分隔
	 */
	public static List<String> SplitFileNames(String fileNames) {
		if (fileNames == null)
			return Collections.<String> emptyList();

		List<String> result = new ArrayList<String>();
		for (String file : fileNames.split("(?<!\\\\),")) {
			result.add(file.replaceAll("\\\\(?=,)", ""));
		}

		return result;
	}
}
