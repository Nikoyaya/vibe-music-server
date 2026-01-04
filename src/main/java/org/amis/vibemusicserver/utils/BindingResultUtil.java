package org.amis.vibemusicserver.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * @author : KwokChichung
 * @description : BindingResult 工具类
 * @createDate : 2026/1/3 17:39
 */
public class BindingResultUtil {

    /**
     * 处理校验失败的BindingResult，返回错误信息
     *
     * @param bindingResult 校验结果
     * @return 错误信息字符串
     */
    public static String handleBindingResultErrors(BindingResult bindingResult) {
        // 检查校验结果中是否存在错误
        if (bindingResult.hasErrors()) {
            // 初始化一个StringBuilder对象，用于拼接错误信息字符串
            StringBuilder errorMessage = new StringBuilder("输入参数校验失败: ");

            // 遍历校验结果中的所有错误
            for (ObjectError error : bindingResult.getAllErrors()) {
                // 将每个错误的默认消息追加到错误信息字符串中，并在后面添加分号和空格
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }

            // 返回拼接好的错误信息字符串
            return errorMessage.toString();
        }
        // 如果没有错误，返回null
        return null;
    }

}

