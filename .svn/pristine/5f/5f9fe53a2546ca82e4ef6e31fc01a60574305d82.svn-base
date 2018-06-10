package com.metarnet.eomeem.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2016/8/11.
 */
public class StringUtils {
    public static String toUtf8String(String agent, String s) {
        try {
            boolean isFireFox = (agent != null && agent.toLowerCase().indexOf("firefox") != -1);
            if (isFireFox) {
                s = new String(s.getBytes("UTF-8"), "ISO8859-1");
            } else {
                s = toUtf8String(s);
                if ((agent != null && agent.indexOf("MSIE") != -1)) {
                    if (s.length() > 150) {
                        s = new String(s.getBytes("UTF-8"), "ISO8859-1");
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = Character.toString(c).getBytes("utf-8");
                } catch (Exception ex) {

                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    public static int fromStringToInt(String str) {
        int res = 0;
        try {
            if (str != null && !"".equals(str)) {
                res = Integer.parseInt(str);
            }
        } catch (Exception e) {
            System.out.println("从字符串转换成int时出错");
            // e.printStackTrace();
        }
        return res;
    }
}
