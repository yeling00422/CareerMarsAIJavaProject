/**
 */
package com.example.careermarsaiproject.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultEnum {
    // 成功
    SUCCESS(200, "成功"),

    // token异常
    TOKEN_PAST(301, "token过期"), TOKEN_ERROR(302, "token异常"),
    // 登录异常
    LOGIN_ERROR(303, "登录异常"), REMOTE_ERROR(304, "异地登录"),
    MENU_PAST(305, "菜单过期"), MENU_NO(306, "没此权限，请联系管理员！"),
    LOGIN_FAIL(307, "账号或密码不对"), UPDATE_PASSWORD_FAIL(308, "原密码错误，修改失败"),
    LOGIN_EMAIL_FAIL(307, "邮箱或密码不对"),
    LOGIN_PHONE_OR_CODE_FAIL(307, "手机号或密码不对"),
    PARSE_TOKEN(401,"token error"),
    //    PARSE_TOKEN(401, "登录已失效，请重新登陆"),
    USER_DISABLE(402, "用户已禁用，请联系管理员开通"),
    USER_UNREG(401, "该手机号未注册，请微信扫码注册"),
    USER_NOT_HAS(401, "该账号未注册，请联系管理员注册"),

    // 课程异常，4开头
    COURSE_SAVE_FAIL(403, "添加失败"), COURSE_UPDATE_FAIL(404, "更新失败"), COURSE_DELETE_FAIL(405, "删除失败"),
    //
    COLLECTION(406, "已收藏"), USER_ADVICE(406, "保存建议失败,不能重复提建议"), COURSE_AUDIT_FAIL(407, "审核失败"),

    // 用户异常，5开头
    LECTURER_REQUISITION_REGISTERED(501, "申请失败！该手机没注册，请先注册账号"), LECTURER_REQUISITION_WAIT(502, "申请失败！该账号已提交申请入驻成为讲师，待审核中，在7个工作日内会有相关人员与您联系确认"), LECTURER_REQUISITION_YET(503, "申请失败！该账号已成为讲师，请直接登录"),
    //
    USER_SAVE_FAIL(504, "添加失败"), USER_UPDATE_FAIL(505, "更新失败"), LECTURER_REQUISITION_FAIL(506, "申请失败！该账号已提交申请入驻成为讲师，审核不通过，请联系平台管理员"), USER_LECTURER_AUDIT(507, "审核失败"), USER_SEND_FAIL(508, "发送失败"),
    USER_DELETE_FAIL(509, "删除失败"),

    // 系統异常，6开头
    SYSTEM_SAVE_FAIL(601, "添加失败"), SYSTEM_UPDATE_FAIL(602, "更新失败"), SYSTEM_DELETE_FAIL(603, "删除失败"),


    SMS_CODE_EXCEPTION(500, "验证码60秒内无法重复发送"),
    SMS_CODE_EXPIRE(500, "验证码已过期，请重新发送"),
    SMS_CODE_ERROR(500, "验证码错误，请核实"),
    SMS_SEND_ERROR(500, "发送失败，请检查手机号稍后重试"),
    EMAIL_SEND_ERROR(500, "发送失败，请检查邮箱号稍后重试"),
    MENTOR_SAVE_PHONE_FAIL(601, "手机号被占用，请更换别的手机号"),
    MENTOR_SAVE_EMAIL_CODE_FAIL(602, "邮箱号被占用，请更换别的邮箱"),
    EMAIL_NOT_REGISTER(603, "该邮箱未注册过账号！"),
    PHONE_NOT_REGISTER(604, "该手机号未注册过账号！"),
    THE_PASSWORD_IS_INCONSISTENT(500, "密码和确认密码不一致，请重新输入！"),

    PAY_ERROR(701, "支付失败，请稍后重试"),

    // 错误
    ERROR(999, "执行失败，请稍后重试");

    private Integer code;

    private String desc;

}
