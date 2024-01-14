package com.dianping.util;


public abstract class RegexPatterns {
    /**
     * 手机号正则
     * 1. 手机号码的第一位必须是1
     * 2. 手机号码的第二位或第三位：
     *      第二位：3 或者 8 开头，第三位为 0 ~ 9
     *      第二位：4 开头，第三位为 5 或 7 或者 9
     *      第二位：5 开头，第三位为 0 ~3 或 5 ~ 9
     *      第二位：6 开头，第三位为 6
     *      第二位：7 开头，第三位为 0，1，3，5，6，7，8
     *      第二位：9 开头，第三位为 8 或者 9
     * 3. 手机号码剩下需要 8 位
     */
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
    /**
     * 邮箱正则
     * 邮箱分三部分，入 aaadd@163.com
     * 1. @ 之前的部分
     * 2. dot 之前的部分
     * 3. dot 之后的部分
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    /**
     * 密码正则。4~32位的字母、数字、下划线
     */
    public static final String PASSWORD_REGEX = "^\\w{4,32}$";
    /**
     * 验证码正则, 6位数字或字母
     */
    public static final String VERIFY_CODE_REGEX = "^[a-zA-Z\\d]{6}$";

}