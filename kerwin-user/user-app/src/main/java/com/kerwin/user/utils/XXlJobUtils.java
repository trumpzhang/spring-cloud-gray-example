package com.kerwin.user.utils;

import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Classname XXlJobUtils
 * @Author pu.li
 * @Date 2021/9/15 14:28
 * @Version V1.0
 */
@Slf4j
public class XXlJobUtils {


    /**
     * @Description 解析xxlJob管理平台的参数
     * 参数格式, key1=value1,key2=values
     */
    public static Map<String, String> getParam() {
        HashMap<String, String> map = new HashMap<>();
        String param = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(param)) {
            String[] arr = param.split(",");
            for (String string : arr) {
                String[] split = string.split("=");
                map.put(split[0].trim(), split.length > 1 ? split[1].trim() : null);
            }
        }
        return map;
    }


    public static <T> T getParam(String key, Class<T> clz, Object defalultValue) {
        Map<String, String> params = getParam();
        String value = params.get(key);
        value = StringUtils.isNotBlank(value) ? value : defalultValue.toString();
        return JSONObject.parseObject(value, clz);
    }


    public static <T> T getParamNew(String key, Class<T> clz, Object defalultValue) {
        Map<String, String> params = getParam();
        String valueStr = params.get(key);
        T value = StringUtils.isNotBlank(valueStr) ? (T) valueStr : (T) defalultValue;
        return value;
    }

    public static void main(String[] args) {
//        Integer executeNodeNum = 4;
//        int shardTotal=4;
//        int shardIndex=1;
//        System.out.println(executeNodeNum <= 0 || executeNodeNum > shardTotal);
//        System.out.println(shardIndex > (executeNodeNum - 1));

//        Map<String, String> params = new HashMap<>();
//        String key = "today";
//        Class<LocalDate> clz = LocalDate.class;
//        LocalDate defalultValue = LocalDate.now();
//        String valueStr = params.get(key);
//        Object value = null;
//        value = StringUtils.isNotBlank(valueStr) ? (LocalDate) value : defalultValue;
//        System.out.println(value);


//        System.out.println(getParamNew(new HashMap<>(), "today", LocalDate.class, LocalDate.now()));
    }

    public static int getShardTotal(int shardIndex) {
        Integer executeNodeNum = XXlJobUtils.getParam("executeNodeNum", Integer.class, 0);
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("发票清理--分片序号:{},分片总数:{},执行任务节点数{}", shardIndex, shardTotal, executeNodeNum);

        if (executeNodeNum <= 0 || executeNodeNum > shardTotal) {
            XxlJobHelper.log("执行任务节点数量取值范围[1,节点总数]");
            XxlJobHelper.handleFail();
            return 0;
        }
        if (shardIndex > (executeNodeNum - 1)) {
            XxlJobHelper.log("当前分片 {} 无需执行", shardIndex);
            XxlJobHelper.handleSuccess();
            return 0;
        }
        return executeNodeNum;
    }


}
