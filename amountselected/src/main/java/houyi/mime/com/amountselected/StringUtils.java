/*
 *  -------------------------------------------------------------------------------------
 *  Mi-Me Confidential
 *
 *  Copyright (C) 2017.  Shanghai Mi-Me Financial Information Service Co., Ltd.
 *  All rights reserved.
 *
 *  No part of this file may be reproduced or transmitted in any form or by any means,
 *  electronic, mechanical, photocopying, recording, or otherwise, without prior
 *  written permission of Shanghai Mi-Me Financial Information Service Co., Ltd.
 *  -------------------------------------------------------------------------------------
 */

package houyi.mime.com.amountselected;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by huangchenggang on 2015/11/20.
 */
public class StringUtils {
    /**
     * 判断是否为字母.
     */
    public static boolean isLetter(char c) {
        int k = 0x80;
        return c / k == 0;
    }

    /**
     * 判断字符串是否为空.
     */
    public static boolean isNull(String str) {
        return str == null || str.length() == 0 || str.trim().equalsIgnoreCase("null");
    }

    /**
     * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为2,英文字符长度为1.
     *
     * @param s 需要得到长度的字符串
     * @return int 得到的字符串长度
     */
    public static int length(String s) {
        if (s == null) {
            return 0;
        }
        char[] c = s.toCharArray();
        int len = 0;
        for (char aC : c) {
            len++;
            if (!isLetter(aC)) {
                len++;
            }
        }
        return len;
    }


    /**
     * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为1,英文字符长度为0.5.
     *
     * @param s 需要得到长度的字符串
     * @return int 得到的字符串长度
     */
    public static double getLength(String s) {
        double valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (int i = 0; i < s.length(); i++) {
            // 获取一个字符
            String temp = s.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为1
                valueLength += 1;
            } else {
                // 其他字符长度为0.5
                valueLength += 0.5;
            }
        }
        //进位取整
        return Math.ceil(valueLength);
    }

    /**
     * 获取字符串的长度.
     */
    public static int getWordCount(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        return s.length();
    }


    /**
     * 格式化double类型的数字（添加分隔符）.
     */
    public static String formatDouble2(double number) {
        List<String> list = new ArrayList<String>();
        String numberStr = String.valueOf(number);
        String[] arrayStr = numberStr.split("\\.");
        // 小数部分
        // String decimalStr = "." + arrayStr[1];
        String decimalStr = null;
        if (arrayStr[1].length() < 2) {
            decimalStr = "." + arrayStr[1] + "0";
        } else {
            decimalStr = "." + arrayStr[1];
        }
        list.add(decimalStr);
        // 整数部分
        String integerStr = arrayStr[0];
        int integerNo = Integer.parseInt(integerStr);
        int index = integerStr.length() / 3 + 1;
        for (int i = 0; i < index; i++) {
            int commerce = integerNo / 1000;
            int remainder = integerNo % 1000;
            // 判断余数位数
            String remainderStr = String.valueOf(remainder);
            if (i != index - 1) {
                if (remainderStr.length() == 1) {
                    remainderStr = "00" + remainderStr;
                } else if (remainderStr.length() == 2) {
                    remainderStr = "0" + remainderStr;
                }
            }
            // 判断是否需要加","
            if (i == 0) {
                list.add(0, remainderStr);
            } else {
                if (remainder != 0) {
                    list.add(0, remainderStr + ",");
                }
            }
            integerNo = commerce;
        }
        StringBuilder lastStr = new StringBuilder();
        for (int i = 0; i < list.size(); ++i) {
            lastStr.append(list.get(i));
        }
        return lastStr.toString();
    }

    public static String formatDouble(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");//格式化设置
        return decimalFormat.format(number);
    }

    public static String formatSeparator(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");//格式化设置
        return decimalFormat.format(number);
    }

    public static String formatDoubleWithoutSeparator(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("##0.00");//格式化设置
        return decimalFormat.format(number);
    }

    /**
     * 格式化金额
     * 最新交互规则：接收以分为单位的金额数值，double类型；返回以元为单位String格式金额
     * 处理规则：1,千分位格式；2,整元不显示小数，非整元显示两位小数.
     *
     * @param number 以分为单位的金额数值
     * @return 以元为单元的金额
     */
    public static String formatSeparatorForMoney(double number) {
        return formatDouble(number / 100);
    }

    /**
     * @param colorStr 需要判断的颜色字符串
     * @return 是否是16进制的颜色字符串
     */
    public static boolean isHexadecimalColorStr(String colorStr) {
        String regString = "^#[0-9a-fA-F]{6,8}$";
        return Pattern.matches(regString, colorStr);
    }
}
