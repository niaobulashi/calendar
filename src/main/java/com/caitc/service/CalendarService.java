package com.caitc.service;

/**
 * @program: calendar
 * @description: 工作日历获取接口类
 * @author: hulang  hulang6666@qq.com
 * @create: 2019-11-22 15:13
 */
public interface CalendarService {

    /**
     *
     * @param year  开始年份
     * @param month 开始月份-1
     * @param day   开始日期-1
     * @param n     结束天数
     * @throws Exception
     */
    void saveCalendar(int year, int month, int day, int n) throws Exception;
}

