package com.jinninghui.newspiral.common.entity.common.logmodule;

import com.alibaba.fastjson.JSONObject;

/**
 * @version V1.0
 * @Title: LogClient
 * @Package com.jinninghui.newspiral.common.entity.common.logmodule
 * @Description:
 * @author: xuxm
 * @date: 2020/4/7 10:22
 */
public class LogClient {
    /* 日志级别 - 进入 */
    private final static Integer ENTER = 1;
    /* 日志级别 - TRACE */
    private final static Integer TRACE = 2;
    /* 日志级别 - DEBUG */
    private final static Integer DEBUG = 3;
    /* 日志级别 - INFO */
    private final static Integer INFO = 4;
    /* 日志级别 - WARN */
    private final static Integer WARN = 5;
    /* 日志级别 - ERROR */
    private final static Integer ERROR = 6;
    /* 日志级别 - FATAL */
    private final static Integer FATAL = 7;
    /* 日志级别 - 退出 */
    private final static Integer EXIT = 0;

    /**
     * 记录ENTER级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void enter(JSONObject log) {
        log(ENTER, log);
    }

    /**
     * 记录TRACE级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void trace(JSONObject log) {
        log(TRACE, log);
    }

    /**
     * 记录DEBUG级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void debug(JSONObject log) {
        log(DEBUG, log);
    }

    /**
     * 记录INFO级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void info(JSONObject log) {
        log(INFO, log);
    }

    /**
     * 记录WARN级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void warn(JSONObject log) {
        log(WARN, log);
    }

    /**
     * 记录ERROR级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void error(JSONObject log) {
        log(ERROR, log);
    }

    /**
     * 记录FATAL级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void fatal(JSONObject log) {
        log(FATAL, log);
    }

    /**
     * 记录EXIT级别的日志
     *
     * @param log 日志信息
     */
    public synchronized static void exit(JSONObject log) {
        log(EXIT, log);
    }

    /**
     * 向服务器传送日志信息
     *
     * @param levelFlag 日志等级
     * @param log       日志信息
     */
    private synchronized static void log(Integer levelFlag, JSONObject log) {
        if (log == null || log.keySet().isEmpty()) {
            return;
        }
        try {
            // 保存中

            /*
             * {"name": "clientCode", "type": "string"}, {"name": "levelFlag",
             * "type": "int"}, {"name": "logTime", "type": "long"}, {"name":
             * "jsonData", "type": "string"}
             */
            // initiate the request data
            log.put("LevelFlag", levelFlag);
            log.put("LogTime", System.currentTimeMillis());

            // 转换为字符串，保存到Redis中
            //jedis.lpush(REDIS_LOG_KEY, log.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 异步返回日志信息
     *
     * @param levelFlag 日志等级
     * @param log       日志信息
     */
    private synchronized static JSONObject returnLog(Integer levelFlag, JSONObject log) {
        if (log == null || log.keySet().isEmpty()) {
            return null;
        }
        try {
            // 保存中

            /*
             * {"name": "clientCode", "type": "string"}, {"name": "levelFlag",
             * "type": "int"}, {"name": "logTime", "type": "long"}, {"name":
             * "jsonData", "type": "string"}
             */
            // initiate the request data
            log.put("LevelFlag", levelFlag);
            log.put("LogTime", System.currentTimeMillis());

           return log;

        } catch (Exception ex) {
            ex.printStackTrace();
            return log;
        }
    }
}
