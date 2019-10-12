package com.huiway.activiti.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.IOUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * 字符串工具。
 */
public class StringUtils {

    private static final char[] HEX_CHARSET = "0123456789abcdef".toCharArray();

    /**
     * 检查字符串是否为空白。
     * @param string 检查对象字符串
     * @param trim 是否去除首尾空白字符
     * @return 字符串是否为空白
     */
    public static boolean isBlank(String string, boolean trim) {
        return string != null && "".equals(trim ? string.trim() : string);
    }

    /**
     * 检查字符串是否为空白。
     * @param string 检查对象字符串
     * @return 字符串是否为空白
     */
    public static boolean isBlank(String string) {
        return isBlank(string, false);
    }

    /**
     * 检查字符串是否为空。
     * @param string 检查对象字符串
     * @param trim 是否去除首尾空白字符
     * @return 字符串是否为空
     */
    public static boolean isEmpty(String string, boolean trim) {
        return string == null || isBlank(string, trim);
    }

    /**
     * 检查字符串是否为空。
     * @param string 检查对象字符串
     * @return 字符串是否为空
     */
    public static boolean isEmpty(String string) {
        return isEmpty(string, false);
    }

    /**
     * 去除首尾空白字符。
     * @param string 输入字符串
     * @return 去除首尾空白字符后的字符串
     */
    public static String trim(String string) {
        return trim(string, "");
    }

    /**
     * 去除首尾空白字符。
     * @param string       输入字符串
     * @param defaultValue 当为空指针或空字符串时的默认值
     * @return 去除首尾空白字符后的字符串
     */
    public static String trim(String string, String defaultValue) {

        if (isEmpty(string, true)) {
            return defaultValue;
        }

        return string.trim();
    }

    /**
     * 重复字符串。
     * @param string 字符串
     * @param times 重复次数
     * @return 新的字符串
     */
    public static String repeat(String string, int times) {
        return (new String(new char[times])).replace("\0", string);
    }

    /**
     * 整数补零。
     * @param integer 整数
     * @param length 位数
     * @return 补位后的字符串
     */
    public static String pad(int integer, int length) {
        return padLeft("" + integer, length, '0');
    }

    /**
     * 整数补位。
     * @param integer 整数
     * @param length 位数
     * @param padding 补位字符
     * @return 补位后的字符串
     */
    public static String pad(int integer, int length, char padding) {
        return padLeft("" + integer, length, padding);
    }

    /**
     * 字符串补位。
     * @param string 字符串
     * @param length 位数
     * @return 补位后的字符串
     */
    public static String padLeft(String string, int length) {
        return padLeft(string, length, ' ');
    }

    /**
     * 字符串补位。
     * @param string 字符串
     * @param length 位数
     * @param padding 补位字符
     * @return 补位后的字符串
     */
    public static String padLeft(String string, int length, char padding) {

        if (length <= string.length()) {
            return string;
        }

        return last(repeat("" + padding, length) + string, length);
    }

    /**
     * 截取字符串中最后指定个数的字符。
     * @param string 输入字符串
     * @param chars 截取字符数
     * @return 截取后的字符串
     */
    public static String last(String string, int chars) {

        int startAt = string.length() - chars;

        if (startAt < 0) {
            startAt = 0;
        }

        return string.substring(startAt, startAt + chars);
    }

    /**
     * 将对象转为 JSON 字符串。
     * @param object 对象
     * @param pretty 是否格式化
     * @return JSON 字符串
     */
    public static String toJSON(Object object, boolean pretty) {

        ObjectWriter writer = (new ObjectMapper()).writer();

        if (pretty) {
            writer = writer.withDefaultPrettyPrinter();
        }

        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    /**
     * 将输入流转为字符串。
     * @param stream 输入流
     * @return 字符串
     */
    public static String fromInputStream(InputStream stream) {
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");
            return writer.toString();
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 将对象转为 JSON 字符串。
     * @param object 对象
     * @return JSON 字符串
     */
    public static String toJSON(Object object) {
        return toJSON(object, false);
    }

    /**
     * 将 JSON 转为对象。
     * @param <T> 范型
     * @param json JSON 字符串
     * @param type 类型
     * @return 转换后的对象
     */
    public static <T> T fromJSON(
        String json,
        Class<T> type
    ) throws IOException {

        if (json == null) {
            return null;
        }

        return (new ObjectMapper())
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(json, type);
    }

    /**
     * 将 JSON 转为对象。
     * @param <T> 范型
     * @param json JSON 字符串
     * @param type 类型
     * @return 转换后的对象
     */
    public static <T> T fromJSON(
        String json,
        TypeReference<T> type,
        T defaultValue
    ) {

        if (isEmpty(json)) {
            return defaultValue;
        }

        try {
            return (new ObjectMapper())
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(json, type);
        } catch (IOException e) {
            return defaultValue;
        }

    }

    /**
     * 将json array反序列化为对象
     *
     * @param json
     * @param jsonTypeReference
     * @return
     */
    public static <T> T decode(String json, TypeReference<T> jsonTypeReference) {
        try {
            return (T) new ObjectMapper().readValue(json, jsonTypeReference);
        } catch (JsonParseException e) {
        } catch (JsonMappingException e) {
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * 将 JSON 转为对象。
     * @param <T> 范型
     * @param stream 输入流
     * @param type 类型
     * @return 转换后的对象
     */
    public static <T> T fromJSON(
        InputStream stream,
        Class<T> type
    ) throws IOException {
        return (new ObjectMapper())
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(stream, type);
    }

    /**
     * 根据字节数组生成十六进制字符串。
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String toHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARSET[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARSET[v & 0x0F];
        }

        return new String(hexChars);
    }

    /**
     * 将对象转为 Map。
     * @param object 输入值
     * @return 转换后的值
     */
    public static Object toMap(Object object) {

        if (DataTypeUtils.isPrimitive(object)) {
            return object;
        }

        if (object.getClass().isArray()) {
            object = Arrays.asList((Object[]) object);
        }

        if (object instanceof Iterable) {

            List<Object> list = new ArrayList<>();

            for (Object o : (Iterable) object) {
                list.add(toMap(o));
            }

            return list;
        }

        if (object instanceof Map) {

            Map source = (Map) object;
            Map<String, Object> map = new HashMap<>();

            Set keys = source.keySet();

            for (Object key : keys) {
                map.put(key.toString(), toMap(source.get(key)));
            }

            return map;
        }

        Map<String, Object> map = new HashMap<>();

        BeanInfo info;
        Method reader;
        String propertyName;
        Object propertyValue;

        try {
            info = Introspector.getBeanInfo(object.getClass());
        } catch (IntrospectionException e) {
            return null;
        }

        for (PropertyDescriptor property : info.getPropertyDescriptors()) {

            reader =  property.getReadMethod();

            if (reader == null) {
                continue;
            }

            try {
                propertyValue = reader.invoke(object);
            } catch (ReflectiveOperationException e) {
                continue;
            }

            propertyName = property.getName();

            if ("class".equals(propertyName)) {
                continue;
            }

            map.put(propertyName, toMap(propertyValue));
        }

        return map;
    }

    /**
     * URL 内容编码。
     * @param string 输入字符串
     * @return 编码后的字符串
     */
    public static String encodeURIComponent(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    /**
     * 取得字符串中所有符合给定格式的片段。
     * @param pattern 格式
     * @param input   输入字符串
     * @return 符合给定格式的片段列表
     */
    public static List<String> findAll(final Pattern pattern, final String input) {

        Matcher matcher = pattern.matcher(input);

        List<String> matched = new ArrayList<>();

        while (matcher.find()) {
            matched.add(matcher.group(0));
        }

        return matched;
    }

    /**
     * 将对象转为 URL Encoded 字符串。
     * @param object 输入值
     * @return URL Encoded 字符串
     */
    public static String toURLEncoded(Object object) {
        return String.join("&", toNameValuePairs(null, object));
    }

    public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z" };

    /**
     * 生成短的8位uuid。二维码用
     * @return
     */
    public static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }

    /**
     * 将对象转为键值对列表。
     * @param parentKey 上级名称
     * @param object 输入值
     * @return 键值对列表
     */
    private static List<String> toNameValuePairs(
        String parentKey,
        Object object
    ) {

        List<String> kvps = new ArrayList<>();

        if (object == null) {
            return kvps;
        }

        if (DataTypeUtils.isPrimitive(object)) {
            kvps.add(parentKey + "=" + encodeURIComponent(object.toString()));
            return kvps;
        }

        Map map = (Map) toMap(object);

        if (map == null) {
            return kvps;
        }

        if (parentKey != null && !"".equals(parentKey)) {
            parentKey += ".";
        } else {
            parentKey = "";
        }

        Set keys = map.keySet();
        String name;
        Object value;

        for (Object key : keys) {

            name = parentKey + key.toString();
            value = map.get(key);

            if (value == null) {
                continue;
            }

            if (value instanceof Map) {
                kvps.addAll(toNameValuePairs(name, value));
            } else if (value instanceof Iterable) {
                for (Object item : (Iterable) value) {
                    kvps.addAll(toNameValuePairs(name, item));
                }
            } else {
                kvps.add(
                    key.toString()
                    + "="
                    + encodeURIComponent(value.toString())
                );
            }

        }

        return kvps;
    }

    /**
     * 根据位数取字符串
     * @param str
     * @param count
     * @return
     */
    public static String substringByCount(String str, int byteCount) {
		StringBuffer buff = new StringBuffer();
		if (str != null && !"".equals(str)) {
			if (byteCount > 0) {
				char c;
				int sumByteCount = 0;
				for (int i = 0; i < str.length(); i++) {
					c = str.charAt(i);
					sumByteCount += String.valueOf(c).getBytes(Charset.forName("UTF-8")).length;
					if (sumByteCount > byteCount) {
						break;
					}
					buff.append(c);
				}
			}
		}
		return buff.toString();
	}

    /**
     * 根据字节位数取字符串
     * @param str
     * @param start
     * @param byteCount
     * @return
     */
    public static String substringByCount(String str, int start, int byteCount) {
		StringBuffer buff = new StringBuffer();
		if (str != null && !"".equals(str)) {
			start = substringByCount(str, start).length();
			if (byteCount > 0) {
				char c;
				int sumByteCount = 0;
				for (int i = start; i < str.length(); i++) {
					c = str.charAt(i);
					sumByteCount += String.valueOf(c).getBytes(Charset.forName("UTF-8")).length;
					if (sumByteCount > byteCount) {
						break;
					}
					buff.append(c);
				}
			}
		}
		return buff.toString();
	}

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param keyword
     * @return
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (!StringUtils.isEmpty(keyword)) {
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    public static boolean isIDNumber(String IDNumber) {
        if (IDNumber == null || "".equals(IDNumber)) {
            return false;
        }
        // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
        String regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" +
                "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
        //假设18位身份证号码:41000119910101123X  410001 19910101 123X
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //(18|19|20)                19（现阶段可能取值范围18xx-20xx年）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十七位奇数代表男，偶数代表女）
        //[0-9Xx] 0123456789Xx其中的一个 X（第十八位为校验值）
        //$结尾

        //假设15位身份证号码:410001910101123  410001 910101 123
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十五位奇数代表男，偶数代表女），15位身份证不含X
        //$结尾


        boolean matches = IDNumber.matches(regularExpression);

        //判断第18位校验值
        if (matches) {

            if (IDNumber.length() == 18) {
                try {
                    char[] charArray = IDNumber.toCharArray();
                    //前十七位加权因子
                    int[] idCardWi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                    //这是除以11后，可能产生的11位余数对应的验证码
                    String[] idCardY = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
                    int sum = 0;
                    for (int i = 0; i < idCardWi.length; i++) {
                        int current = Integer.parseInt(String.valueOf(charArray[i]));
                        int count = current * idCardWi[i];
                        sum += count;
                    }
                    char idCardLast = charArray[17];
                    int idCardMod = sum % 11;
                    if (idCardY[idCardMod].toUpperCase().equals(String.valueOf(idCardLast).toUpperCase())) {
                        return true;
                    } else {
                        System.out.println("身份证最后一位:" + String.valueOf(idCardLast).toUpperCase() +
                                "错误,正确的应该是:" + idCardY[idCardMod].toUpperCase());
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("异常:" + IDNumber);
                    return false;
                }
            }

        }
        return matches;
    }

}
