package com.michael.qrcode.utils;


import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户基本信息校验
 */
public class UserInfoValidator {
    public static void main(String[] args) {

        String str = "qqqqqqqqqqwwwwwwwwwweeeeeeeeeerrrrrrrwyr";
        String str2 = "afdfafd@163.com";
//        boolean tag = emailValidate(str);
        System.out.println("tag is: " + getEmail(str2));
    }

    // 密码框字符限制
    public static final char[] PASSWORDCHAR = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '!', '\"', '#', '$', '%', '&', ',', '(', ')', '*', '+', '\'', '-', '.', '\\', ':', ';', '<',
            '=', '>', '?', '@', '[', ']', '/', '^', '_', '`', '{', '|', '}', '~',};

    // 邮箱
//    private static final String EMAIL_PATTERN = "^([a-zA-Z0-9]+[_|_|.-]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|_|.-]?)*[a-zA-Z0-9]+.[a-zA-Z]{2,4}$";

    private static final String EMAIL_PATTERN = "^([a-z0-9A-Z]+[-|\\\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\\\.)+[a-zA-Z]{2,}$";

    // 用户名
    private static final String NAME_PATTERN = "^([\u4e00-\u9fa5]|[a-zA-Z\\s]){2,40}$";

    // 手机号
    private static final String MOBILE_PATTERN = "^[1][3-8][0-9]{9}$";

    // 密码
//    private static final String PASSWORD_PATTERN = "^[!@#$%^&*a-zA-Z0-9]{4,20}$";

    // 微卡号
    private static final String WIKA_ID_PATTERN = "^[a-zA-z][a-zA-z0-9_.]{3,19}$";

    // 公司
    private static final String CORP_PATTERN = "^([\u4e00-\u9fa5]|[a-zA-Z\\s]){2,40}$";

    // 职位
    private static final String POSITION_PATTERN = "^([\u4e00-\u9fa5]|[a-zA-Z\\s]){2,40}$";

    // 部门
    private static final String DEPARTMENT_PATTERN = "^([\u4e00-\u9fa5]|[a-zA-Z\\s]){2,40}$";

    // 行业
    private static final String INDUSTRY_PATTERN = "^([\u4e00-\u9fa5]|[a-zA-Z\\s]){2,40}$";

    //地区
    private static final String LOCATION_PATTERN = "^([\u4e00-\u9fa5]|[a-zA-Z\\s]){2,40}$";


    private static final int DESCRIPTION_LEN = 160;

    public static boolean emailValidate(String email) {
//        return validate(email, EMAIL_PATTERN);
        return getEmail(email);
    }

    public static boolean nameValidate(String name) {
        return validate(name, NAME_PATTERN);
    }

    public static boolean mobileValidate(String mobile) {
        return validate(mobile, MOBILE_PATTERN);
    }

//    public static boolean passwordValidate(String password) {
//        return validate(password, PASSWORD_PATTERN);
//    }

    public static boolean wikaIdValadate(String wikaId) {
        return validate(wikaId, WIKA_ID_PATTERN);
    }

    public static boolean descriptionValidate(String description) {
        if (description != null && description.length() > DESCRIPTION_LEN) {
            return false;
        }
        return true;
    }

    public static boolean corpValidate(String corp) {
        return validate(corp, CORP_PATTERN);
    }

    public static boolean industryValidate(String industry) {
        return validate(industry, INDUSTRY_PATTERN);
    }

    public static boolean positionValidate(String position) {
        return validate(position, POSITION_PATTERN);
    }

    public static boolean departmentValidate(String department) {
        return validate(department, DEPARTMENT_PATTERN);
    }

    public static boolean locationValidate(String location) {
        return validate(location, LOCATION_PATTERN);
    }

    public static boolean validate(String input, String regex) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        boolean tag = true;
        Pattern pattern = Pattern.compile(regex);
        Matcher mat = pattern.matcher(input);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    private static boolean getEmail(String line) {
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher m = p.matcher(line);
        return m.find();
    }
}