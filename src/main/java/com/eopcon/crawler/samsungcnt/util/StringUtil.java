package com.eopcon.crawler.samsungcnt.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {

	/**
	 * 입력된 문자열에서 패턴에 맞지 않는 특수문자는 빈문자열로 대체된 문자열을 리턴한다.
	 * 
	 * @param input
	 * @return
	 */
	public static String replaceSymbols(String input) {

		String pattern = "^[\\p{Punct}\\p{javaWhitespace}0-9a-zA-Zㄱ-ㅎ가-힣]*$";

		List<StringBuffer> inputList = new ArrayList<>();

		for (char c : input.toCharArray()) {
			char[] ca = new char[1];
			ca[0] = c;
			inputList.add(new StringBuffer(new String(ca)));
		}

		StringBuffer buf = new StringBuffer();
		inputList.stream().forEach(sbuf -> {
			if (!Pattern.matches(pattern, sbuf)) {
				// 패턴에 맞지 않는 캐릭터는 빈문자열로 대체
				sbuf.replace(0, 1, "");
			}
			buf.append(sbuf);
		});

		return buf.toString();
	}

}
