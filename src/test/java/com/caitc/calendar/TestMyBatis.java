package com.caitc.calendar;

import com.caitc.service.CalendarService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * File: TestMyBatis
 *
 * @Author 胡浪
 * @Since 2019/11/22 14:20
 * @Ver 1.0
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
//------------如果加入以下代码，所有继承该类的测试类都会遵循该配置，也可以不加，在测试类的方法上///控制事务，参见下一个实例
//这个非常关键，如果不加入这个注解配置，事务控制就会完全失效！
//@Transactional
//这里的事务关联到配置文件中的事务控制器（transactionManager = "transactionManager"）
//指定@Rollback(value = false)  默认不加为true，需要指定为false说明该测试不做回滚处理，对数据进行操作
@Rollback(value = false)
public class TestMyBatis {

    @Autowired
    private CalendarService calendarService;

    /**
     * 初始化工作日历表数据
     * @throws Exception
     */
    @Test
    public void getCalendarInfo() throws Exception {
        /**
         * 从20170101开始，获取2017至2018两年间的数据
         * year  开始年份
         * month 开始月份-1
         * day   开始日期-1
         * n     结束天数，366天表示从20200101到20201231，一共有366天
         */
        calendarService.saveCalendar(2020, 11,30, 2);
    }

}
