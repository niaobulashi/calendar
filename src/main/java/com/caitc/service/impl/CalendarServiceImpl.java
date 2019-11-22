package com.caitc.service.impl;

import com.caitc.entity.Tcalendar;
import com.caitc.mapper.TcalendarMapper;
import com.caitc.service.CalendarService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @version V1.0
 * @Description: 工作日历获取实现类
 * @author: 胡浪
 * @date: 2019/11/22
 */
@Service
public class CalendarServiceImpl implements CalendarService {

    private Logger logger = LoggerFactory.getLogger(CalendarServiceImpl.class);

    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static String userAgent =  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

    //配置您申请的KEY
    public static final String APPKEY ="37057";
    //密钥
    public static final String SIGN = "ca019eba94260a1098520e138a914e62";
    //报文格式
    public static final String FORMAT = "json";
    //
    public static final String APP = "life.workday";
    //
    public static final String INFO = "1";

    @Autowired
    private TcalendarMapper calendarMapper;

    /**
     * 查询获取日历信息并入库
     * @param year  开始年份
     * @param month 开始月份-1
     * @param days
     * @param n     结束天数
     * @throws Exception
     */
    @Override
    public void saveCalendar(int year, int month, int days, int n) throws Exception {
        logger.error("saveCalendar-start");
        try {
            //循环获取未来的日期便利
            Date date = new Date();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            //默认从20170101开始，获取2017至2018两年间的数据
            cal.set(year, month, days);
            for (int i = 1; i < n+1; i++) {
                cal.add(java.util.Calendar.DATE, 1);
                String day = sdf.format(cal.getTime());
                //
                logger.info("日期：" + day);
                //获取日期详情
                getRequest(day);
            }

            logger.error("saveCalendar-end");
        } catch (Exception e) {
            logger.error("saveCalendar-error");
            logger.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    //1.获取当天的详细信息
    public void getRequest(String dateStr){
        Tcalendar cal = new Tcalendar();
        String result =null;
        String url ="http://tool.bitefu.net/jiari/";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("info", INFO);//您申请的appKey
        params.put("d", dateStr);//指定日期,格式为YYYYMMDD,如月份和日期小于10,则取个位,如:20120101
        try {
            // 发送请求
            result = net(url, params, "GET");
            // 获取
            JSONObject object = JSONObject.fromObject(result);
            if(object.getString("status").equals("1")){

                //根据日期和日期类型查询数据库中是否存在
                Tcalendar calendar = calendarMapper.selectByPrimaryKey(dateStr);
                //若存在
                if (calendar != null) {
                    //判断接口返回的日期类型和数据中保存的是否一致，若不一致，则更新
                    if (calendar.getcType().equals(object.get("type"))) {
                        //说明国家法定节假日有变化，之前存入的数据，需要变更
                        cal.setdDate(dateStr);
                        cal.setcType(object.getString("type"));
                        cal.setcTypename(object.getString("typename"));
                        cal.setcYearname(object.getString("yearname"));
                        cal.setcNonglicn(object.getString("nonglicn"));
                        cal.setcNongli(object.getString("nongli"));
                        cal.setcAnimalsYear(object.getString("shengxiao"));
                        cal.setcThrottle(object.getString("jieqi"));
                        cal.setcWeekcn(object.getString("weekcn"));
                        cal.setcWeek1(object.getString("week1"));
                        cal.setcWeek2(object.getString("week2"));
                        cal.setcWeek3(object.getString("week3"));
                        cal.setcDaynum(object.getString("daynum"));
                        cal.setcWeeknum(object.getString("weeknum"));
                        if (object.containsKey("avoid")) {
                            cal.setcAvoid(object.getString("avoid"));
                        }
                        if (object.containsKey("suit")) {
                            cal.setcSuit(object.getString("suit"));
                        }

                        calendarMapper.updateByPrimaryKeySelective(cal);
                    }

                    //若为空，则直接新增日期
                } else {

                    cal.setdDate(dateStr);
                    cal.setcType(object.getString("type"));
                    cal.setcTypename(object.getString("typename"));
                    cal.setcYearname(object.getString("yearname"));
                    cal.setcNonglicn(object.getString("nonglicn"));
                    cal.setcNongli(object.getString("nongli"));
                    cal.setcAnimalsYear(object.getString("shengxiao"));
                    cal.setcThrottle(object.getString("jieqi"));
                    cal.setcWeekcn(object.getString("weekcn"));
                    cal.setcWeek1(object.getString("week1"));
                    cal.setcWeek2(object.getString("week2"));
                    cal.setcWeek3(object.getString("week3"));
                    cal.setcDaynum(object.getString("daynum"));
                    cal.setcWeeknum(object.getString("weeknum"));
                    if (object.containsKey("avoid")) {
                        cal.setcAvoid(object.getString("avoid"));
                    }
                    if (object.containsKey("suit")) {
                        cal.setcSuit(object.getString("suit"));
                    }

                    calendarMapper.insertSelective(cal);
                }

            } else {
                logger.info(object.get("success") + ":" + object.get("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送请求
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return  网络请求字符串
     * @throws Exception
     */
    public static String net(String strUrl, Map params, String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if(method==null || method.equals("GET")){
                strUrl = strUrl+"?"+urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if(method==null || method.equals("GET")){
                conn.setRequestMethod("GET");
            }else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params!= null && method.equals("POST")) {
                try {
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes(urlencode(params));
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    // 获取日期
    public static void getDay(String dateStr, String numStr) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(Integer.parseInt(dateStr.substring(0,4)), Integer.parseInt(dateStr.substring(4,6))-1, Integer.parseInt(dateStr.substring(6,8)));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        for (int i = 1; i < Integer.parseInt(numStr) + 1; i++) {
            cal.add(java.util.Calendar.DATE, 1);
            String day = sdf.format(cal.getTime());
            System.out.println("日期：" + day);
        }
    }


    public static void main(String[] args) throws ParseException {
        getDay("20201105", "56");
        /*String dateStr = "20181230";
        //循环获取未来的日期便利
        Date date = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(2017, 00, 00);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        for (int i = 1; i < 731; i++) {
            cal.add(java.util.Calendar.DATE, 1);
            String day = sdf.format(cal.getTime());
            System.out.println("日期：" + day);
        }*/
        //getRequest2(dateStr);
    }

}
