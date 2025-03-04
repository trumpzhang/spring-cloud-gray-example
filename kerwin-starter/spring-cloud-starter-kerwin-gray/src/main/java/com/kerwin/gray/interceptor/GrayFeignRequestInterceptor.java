package com.kerwin.gray.interceptor;

import com.kerwin.gray.constant.GrayConstant;
import com.kerwin.gray.enums.GrayStatusEnum;
import com.kerwin.gray.holder.GrayFlagRequestHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Collections;

public class GrayFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 如果灰度标记存在，将灰度标记通过HttpHeader传递下去
        GrayStatusEnum grayStatusEnum = GrayFlagRequestHolder.getGrayTag();
        if (grayStatusEnum != null ) {
            template.header(GrayConstant.GRAY_HEADER, Collections.singleton(grayStatusEnum.getVal()));
        }
    }
}