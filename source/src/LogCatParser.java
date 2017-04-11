import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 *
 */

/**
 *
 */
public class LogCatParser implements ILogParser
{
    final String TOKEN_KERNEL= "{}<>[]";
    final String TOKEN_TRACE= "- ";
    final String TOKEN_SPACE = " ";
    final String TOKEN_SLASH = "/";
    final String TOKEN       = "/()";
    final String TOKEN_PID   = "/() ";
    final String TOKEN_TIME  = "{}<>[]/ ";
    final String TOKEN_MESSAGE = "'";
    final String Kernel_msg_level[] = {
      "<0> system is unusable",
      "<1> action must be taken immediately",
      "<2> critical conditions",
      "<3> error conditions",
      "<4> warning conditions",
      "<5> normal but significant condition",
      "<6> informational",
      "<7> debug-level messages"
    };

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    { sdf.setTimeZone(TimeZone.getTimeZone("GMT")); }

    private long parseTimeToLong(final String date, final String time) throws ParseException
    {
        try {
//            System.out.println("time: " + time + " 0:" + time.substring(0, 12) + " 1:" + time.substring(12, 15));
            long tm = sdf.parse("1970-" + date + " " + time.substring(0, 12)).getTime();
            return (tm*1000) + Long.parseLong(time.substring(12, 15));
        } catch (Exception e) {
            return sdf.parse("1970-01-01 00:00:00.000").getTime();
        }
    }

//    private long parseTimeToLong(final String date, final String time) throws ParseException
//    {
//        try {
//            return sdf.parse("1970-" + date + " " + time).getTime();
//        } catch (Exception e) {
//            return sdf.parse("1970-01-01 00:00:00.000").getTime();
//        }
//    }

     public Color getColor(LogInfo logInfo)
    {
        if(logInfo.m_strLogLV == null) return Color.WHITE;

        if(logInfo.m_strLogLV.equals("FATAL") || logInfo.m_strLogLV.equals("F"))
            return new Color(LogColor.COLOR_FATAL);
        if(logInfo.m_strLogLV.equals("ERROR") || logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("3"))
            return new Color(LogColor.COLOR_ERROR);
        else if(logInfo.m_strLogLV.equals("WARN") || logInfo.m_strLogLV.equals("W") || logInfo.m_strLogLV.equals("4"))
            return new Color(LogColor.COLOR_WARN);
        else if(logInfo.m_strLogLV.equals("INFO") || logInfo.m_strLogLV.equals("I") || logInfo.m_strLogLV.equals("6"))
            return new Color(LogColor.COLOR_INFO);
        else if(logInfo.m_strLogLV.equals("DEBUG") || logInfo.m_strLogLV.equals("D") || logInfo.m_strLogLV.equals("7"))
            return new Color(LogColor.COLOR_DEBUG);
        else if(logInfo.m_strLogLV.equals("VERBOSE") || logInfo.m_strLogLV.equals("V"))
            return new Color(LogColor.COLOR_VERBOSE);
        else if(logInfo.m_strLogLV.equals("0"))
            return new Color(LogColor.COLOR_0);
        else if(logInfo.m_strLogLV.equals("1"))
            return new Color(LogColor.COLOR_1);
        else if(logInfo.m_strLogLV.equals("2"))
            return new Color(LogColor.COLOR_2);
        else if(logInfo.m_strLogLV.equals("5"))
            return new Color(LogColor.COLOR_5);
        else if(logInfo.m_strLogLV.equals("BM1"))
            return new Color(LogColor.BOOKMARK_COLOR1);
        else if(logInfo.m_strLogLV.equals("BM2"))
            return new Color(LogColor.BOOKMARK_COLOR2);
        else if(logInfo.m_strLogLV.equals("BM3"))
            return new Color(LogColor.BOOKMARK_COLOR3);
        else if(logInfo.m_strLogLV.equals("BM4"))
            return new Color(LogColor.BOOKMARK_COLOR4);
        else if(logInfo.m_strLogLV.equals("BM5"))
            return new Color(LogColor.BOOKMARK_COLOR5);
//        else if(logInfo.m_strLogLV.equals("BM6"))
//            return new Color(LogColor.BOOKMARK_COLOR6);
//        else if(logInfo.m_strLogLV.equals("BM7"))
//            return new Color(LogColor.BOOKMARK_COLOR7);
//        else if(logInfo.m_strLogLV.equals("BM8"))
//            return new Color(LogColor.BOOKMARK_COLOR8);
//        else if(logInfo.m_strLogLV.equals("BM9"))
//            return new Color(LogColor.BOOKMARK_COLOR9);
        else
            return Color.WHITE;
    }

    public int getLogLV(String strLogLV)
    {
        if(strLogLV == null) return LogInfo.LOG_LV_VERBOSE;

        if(strLogLV.equals("FATAL") || strLogLV.equals("F"))
            return LogInfo.LOG_LV_FATAL;
        if(strLogLV.equals("ERROR") || strLogLV.equals("E") || strLogLV.equals("3"))
            return LogInfo.LOG_LV_ERROR;
        else if(strLogLV.equals("WARN") || strLogLV.equals("W")  || strLogLV.equals("4"))
            return LogInfo.LOG_LV_WARN;
        else if(strLogLV.equals("INFO") || strLogLV.equals("I")  || strLogLV.equals("6"))
            return LogInfo.LOG_LV_INFO;
        else if(strLogLV.equals("DEBUG") || strLogLV.equals("D") || strLogLV.equals("7"))
            return LogInfo.LOG_LV_DEBUG;
        else if(strLogLV.equals("VERBOSE") || strLogLV.equals("V"))
            return LogInfo.LOG_LV_VERBOSE;
        else
            return LogInfo.LOG_LV_VERBOSE;
    }

    public String getLogLVStr(String strLogLV)
    {
        int level = getLogLV(strLogLV);
        switch(level)
        {
            case LogInfo.LOG_LV_FATAL:  return "F";
            case LogInfo.LOG_LV_ERROR:  return "E";
            case LogInfo.LOG_LV_WARN:   return "W";
            case LogInfo.LOG_LV_INFO:   return "I";
            case LogInfo.LOG_LV_DEBUG:  return "D";
            default: return "V";
        }
    }

// 04-17 09:01:18.910 D/LightsService(  139): BKL : 106
    public boolean isNormal(String strText)
    {
//        if(strText.length() < 21) return false;

        String strLevel = (String)strText.substring(19, 21);
        if(strText.substring(2, 3).equals("-")) {
            if(strLevel.equals("D/")
                    || strLevel.equals("V/")
                    || strLevel.equals("I/")
                    || strLevel.equals("W/")
                    || strLevel.equals("E/")
                    || strLevel.equals("F/")
                    )
                return true;
        }
        return false;
    }

// 04-20 12:06:02.125   146   179 D BatteryService: update start
    public boolean isThreadTime(String strText)
    {
//        if(strText.length() < 33) return false;

        String strLevel = (String)strText.substring(31, 33);
        if(strText.substring(2, 3).equals("-")) {
            if(strLevel.equals("D ")
                    || strLevel.equals("V ")
                    || strLevel.equals("I ")
                    || strLevel.equals("W ")
                    || strLevel.equals("E ")
                    || strLevel.equals("F ")
                    )
                return true;
        }
        return false;
    }

//04-20 12:06:02.125456   146   179 D BatteryService: update start
    public boolean isThreadUSTime(String strText)
    {
//        if(strText.length() < 33) return false;

        String strLevel = (String)strText.substring(34, 36);
        if(strText.substring(2, 3).equals("-")) {
            if(strLevel.equals("D ")
                    || strLevel.equals("V ")
                    || strLevel.equals("I ")
                    || strLevel.equals("W ")
                    || strLevel.equals("E ")
                    || strLevel.equals("F ")
                    )
                return true;
        }
        return false;
    }

//    <4>[19553.494855] [DEBUG] USB_SEL(1) HIGH set USB mode
    public boolean isKernel_normal(String strText)
    {
//        if(strText.length() < 17) return false;

//        if(strText.substring(0, 1).equals("<"))
        if(((strText.substring(0, 1).equals("<")) && (strText.substring(2, 4).equals(">[")))
            || ((strText.substring(0, 1).equals("{")) && (strText.substring(2, 4).equals("}[")))
            )
        {
//          String strLevel = (String)strText.substring(1, 2);
//          if(strLevel.equals("0")
//                  || strLevel.equals("1")
//                  || strLevel.equals("2")
//                  || strLevel.equals("3")
//                  || strLevel.equals("4")
//                  || strLevel.equals("5")
//                  || strLevel.equals("6")
//                  || strLevel.equals("7")
//                  )
              return true;
        }

        return false;
    }

//  <0>[  658.586876 / 01-01 14:01:49.420]
    public boolean isKernel_other_0(String strText)
    {
//        if(strText.length() < 38) return false;

        if(strText.substring(17, 18).equals("/"))
        {
          if((strText.substring(0, 1).equals("<")) && (strText.substring(2, 4).equals(">["))){
//            String strLevel = (String)strText.substring(1, 2);
//            if(strLevel.equals("0")
//                    || strLevel.equals("1")
//                    || strLevel.equals("2")
//                    || strLevel.equals("3")
//                    || strLevel.equals("4")
//                    || strLevel.equals("5")
//                    || strLevel.equals("6")
//                    || strLevel.equals("7")
//                    )
                return true;

          }
        }

        return false;
    }

// {1}<3><4>[   15.908851 / 04-18 00:39:54.650]
    public boolean isKernel_other_1(String strText)
    {
//        if(strText.length() < 44) return false;

        if((strText.substring(0, 1).equals("{")) && (strText.substring(2, 4).equals("}<")))
        {
//          String strLevel = (String)strText.substring(1, 2);
//          if(strLevel.equals("0")
//                  || strLevel.equals("1")
//                  || strLevel.equals("2")
//                  || strLevel.equals("3")
//                  || strLevel.equals("4")
//                  || strLevel.equals("5")
//                  || strLevel.equals("6")
//                  || strLevel.equals("7")
//                  )
              return true;
        }

        return false;
    }

// {2}<2>[  127.231261 / 01-06 16:55:22.410]
    public boolean isKernel_other_2(String strText)
    {
//        if(strText.length() < 41) return false;

        if(strText.substring(20, 21).equals("/"))
        {
          if(strText.substring(0, 1).equals("{")) {
//            String strLevel = (String)strText.substring(1, 2);
//            if(strLevel.equals("0")
//                    || strLevel.equals("1")
//                    || strLevel.equals("2")
//                    || strLevel.equals("3")
//                    || strLevel.equals("4")
//                    || strLevel.equals("5")
//                    || strLevel.equals("6")
//                    || strLevel.equals("7")
//                    )
                return true;

          }
        }

        return false;
    }

// {3} kk<0>[32234.278976 / 02-01 05:56:07.320] kobject:
    public boolean isKernel_other_3(String strText)
    {
//        if(strText.length() < 44) return false;

        if(strText.substring(4, 6).equals("kk"))
        {
          if(strText.substring(0, 1).equals("{")) {
//            String strLevel = (String)strText.substring(1, 2);
//            if(strLevel.equals("0")
//                    || strLevel.equals("1")
//                    || strLevel.equals("2")
//                    || strLevel.equals("3")
//                    || strLevel.equals("4")
//                    || strLevel.equals("5")
//                    || strLevel.equals("6")
//                    || strLevel.equals("7")
//                    )
                return true;

          }
        }

        return false;
    }


//     mediaserver-26306 [003] 31271.616784: softirq_exit:         vec=1
    public boolean isEventTrace(String strText)
    {
//        if(strText.length() < 36) return false;

        if(strText.substring(23, 24).equals("[") && strText.substring(27, 28).equals("]"))
        {
              return true;
        }

        return false;
    }

//----- pid 7723 at 2016-05-24 10:06:29 -----
    public boolean isTrace(String strText)
    {
        if(strText.substring(0, 9).equals("----- pid"))
        {
          return true;
        }

        return false;
    }

//     [Wed May 18 10:07:36.833 2016] {3}<3>
    public boolean isTeraTerm(String strText)
    {
//        if(strText.length() < 31) return false;

        if(strText.substring(0, 1).equals("[") && strText.substring(29, 30).equals("]"))
        {
          return true;
        }

        return false;
    }

    public LogInfo getNormal(String strText)
    {
        LogInfo logInfo = new LogInfo();

//        System.out.println("getNormal");

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_PID, false);
            if(stk.hasMoreElements())
                logInfo.m_strDate = stk.nextToken();
            if(stk.hasMoreElements()) {
                logInfo.m_strTime = stk.nextToken();
                try {
    				logInfo.m_TimeStamp = parseTimeToLong(logInfo.m_strDate, logInfo.m_strTime); //ms
    			} catch (ParseException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            }
            if(stk.hasMoreElements())
                logInfo.m_strLogLV = getLogLVStr(stk.nextToken().trim());
            if(stk.hasMoreElements())
                logInfo.m_strTag = stk.nextToken();
            if(stk.hasMoreElements())
                logInfo.m_strPid = stk.nextToken().trim();
            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(TOKEN_MESSAGE);
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += stk.nextToken(TOKEN_MESSAGE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("\\): ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

    public LogInfo getThreadTime(String strText)
    {
        LogInfo logInfo = new LogInfo();

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_SPACE, false);
            if(stk.hasMoreElements())
                logInfo.m_strDate = stk.nextToken();
            if(stk.hasMoreElements()) {
                logInfo.m_strTime = stk.nextToken();
                try {
                  logInfo.m_TimeStamp = parseTimeToLong(logInfo.m_strDate, logInfo.m_strTime);
                } catch (ParseException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  T.e(e);
                }
            }
            if(stk.hasMoreElements())
                logInfo.m_strPid = stk.nextToken().trim();
            if(stk.hasMoreElements())
                logInfo.m_strThread = stk.nextToken().trim();
            if(stk.hasMoreElements())
                logInfo.m_strLogLV = getLogLVStr(stk.nextToken().trim());
            if(stk.hasMoreElements())
                logInfo.m_strTag = stk.nextToken();
            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(TOKEN_MESSAGE);
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += stk.nextToken(TOKEN_MESSAGE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("\\): ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

//    <4>[19553.494855] [DEBUG] USB_SEL(1) HIGH set USB mode
    public LogInfo getKernel_normal(String strText)
    {
//        System.out.println("getKernel_normal");
        LogInfo logInfo = new LogInfo();

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_KERNEL, false);
            if(stk.hasMoreElements())
                logInfo.m_strLogLV = getLogLVStr(stk.nextToken().trim());
            if(stk.hasMoreElements())
                logInfo.m_strTime = stk.nextToken();
            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(TOKEN_KERNEL);
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += " " + stk.nextToken(TOKEN_SPACE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("  ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

//  <0>[  658.586876 / 01-01 14:01:49.420]
    public LogInfo getKernel_other_0(String strText)
    {
//        System.out.println("getKernel_other_0");
        LogInfo logInfo = new LogInfo();

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_KERNEL, false);

            if(stk.hasMoreElements()) {
                logInfo.m_strPid = stk.nextToken().trim(); // smp_processor_id
            }

    //        if(stk.hasMoreElements())
    //            logInfo.m_strLogLV = getLogLVStr(stk.nextToken());
    //        if(stk.hasMoreElements())
    //            logInfo.m_strThread = stk.nextToken();

            if(stk.hasMoreElements()) {
                logInfo.m_TimeUs = (long)(Double.parseDouble(stk.nextToken(TOKEN_TIME))*1000000);
            }

            if(stk.hasMoreElements())
                logInfo.m_strDate = stk.nextToken();

            if(stk.hasMoreElements())
                logInfo.m_strTime = stk.nextToken();

            try {
              logInfo.m_TimeStamp = parseTimeToLong(logInfo.m_strDate, logInfo.m_strTime);
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
              T.e(e);
            }

            logInfo.m_strTag = "KL0";

            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(TOKEN_KERNEL);
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += " " + stk.nextToken(TOKEN_SPACE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("  ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

//{1}<13><4>[   15.908851 / 04-18 00:39:54.650]
    public LogInfo getKernel_other_1(String strText)
    {
//        System.out.println("getKernel_other_1");
        LogInfo logInfo = new LogInfo();

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_KERNEL, false);

            if(stk.hasMoreElements()) // cpu_id
                logInfo.m_strThread = stk.nextToken().trim();

            if(stk.hasMoreElements()) { //(msg->facility << 3) | msg->level
                logInfo.m_strLogLV = getLogLVStr(stk.nextToken().trim());
    //            logInfo.m_strLogLV = getLogLVStr(Integer.toString(Integer.parseInt(stk.nextToken())&0x07));
    //            logInfo.m_strTag   = Integer.toString(Integer.parseInt(stk.nextToken())>>3);
            }

            if(stk.hasMoreElements()) //smp_processor_id
                logInfo.m_strPid = stk.nextToken().trim();

            if(stk.hasMoreElements()) {
                logInfo.m_TimeUs = (long)(Double.parseDouble(stk.nextToken(TOKEN_TIME))*1000000);
            }

            if(stk.hasMoreElements())
                logInfo.m_strDate = stk.nextToken();

            if(stk.hasMoreElements())
                logInfo.m_strTime = stk.nextToken();

            try {
              logInfo.m_TimeStamp = parseTimeToLong(logInfo.m_strDate, logInfo.m_strTime);
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
              T.e(e);
            }

            logInfo.m_strTag = "KL1";

            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(TOKEN_KERNEL);
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += " " + stk.nextToken(TOKEN_SPACE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("  ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

// {2}<2>[  127.231261 / 01-06 16:55:22.410]
    public LogInfo getKernel_other_2(String strText)
    {
//        System.out.println("getKernel_other_2");

        LogInfo logInfo = new LogInfo();

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_KERNEL, false);

            if(stk.hasMoreElements()) // cpu_id
                logInfo.m_strThread = stk.nextToken().trim();

    //        if(stk.hasMoreElements()) //(msg->facility << 3) | msg->level
    //            logInfo.m_strLogLV = getLogLVStr(Integer.toString(Integer.parseInt(stk.nextToken())&0x07));

            if(stk.hasMoreElements()) //smp_processor_id
                logInfo.m_strPid = stk.nextToken().trim();

            if(stk.hasMoreElements()) {
                logInfo.m_TimeUs = (long)(Double.parseDouble(stk.nextToken(TOKEN_TIME))*1000000);
            }

            if(stk.hasMoreElements())
                logInfo.m_strDate = stk.nextToken();

            if(stk.hasMoreElements())
                logInfo.m_strTime = stk.nextToken();

            try {
              logInfo.m_TimeStamp = parseTimeToLong(logInfo.m_strDate, logInfo.m_strTime);
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
              T.e(e);
            }

            logInfo.m_strTag = "KL2";

            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(TOKEN_KERNEL);
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += " " + stk.nextToken(TOKEN_SPACE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("  ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

// {2}<2>[  127.231261 / 01-06 16:55:22.410]
// {3} kk<0>[32234.278976 / 02-01 05:56:07.320] kobject:
    public LogInfo getKernel_other_3(String strText)
    {
//        System.out.println("getKernel_other_3");
        LogInfo logInfo = new LogInfo();

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_KERNEL, false);

            if(stk.hasMoreElements()) // cpu_id
                logInfo.m_strThread = stk.nextToken().trim();

            if(stk.hasMoreElements()) // kk
                logInfo.m_strTag = stk.nextToken();

            if(stk.hasMoreElements()) //smp_processor_id
                logInfo.m_strPid = stk.nextToken().trim();

            if(stk.hasMoreElements()) {
                logInfo.m_TimeUs = (long)(Double.parseDouble(stk.nextToken(TOKEN_TIME))*1000000);
            }

            if(stk.hasMoreElements())
                logInfo.m_strDate = stk.nextToken();

            if(stk.hasMoreElements())
                logInfo.m_strTime = stk.nextToken();

            try {
              logInfo.m_TimeStamp = parseTimeToLong(logInfo.m_strDate, logInfo.m_strTime);
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
              T.e(e);
            }

            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(TOKEN_KERNEL);
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += " " + stk.nextToken(TOKEN_SPACE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("  ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

//     mediaserver-26306 [003] 31271.616784: softirq_exit:         vec=1
    public LogInfo getEventTrace(String strText)
    {
//        System.out.println("getEventTrace");
        LogInfo logInfo = new LogInfo();

        try {
            logInfo.m_strThread = strText.substring(0, 23).trim();

            strText = strText.substring(24);
            StringTokenizer stk = new StringTokenizer(strText, "[]", false);

            if(stk.hasMoreElements()) {
                logInfo.m_strPid = stk.nextToken("[] ");
                if(logInfo.m_strPid.equals("000"))
                  logInfo.m_strLogLV = "F";
                else if(logInfo.m_strPid.equals("001"))
                  logInfo.m_strLogLV = "E";
                else if(logInfo.m_strPid.equals("002"))
                  logInfo.m_strLogLV = "W";
                else if(logInfo.m_strPid.equals("003"))
                  logInfo.m_strLogLV = "I";
                else if(logInfo.m_strPid.equals("004"))
                  logInfo.m_strLogLV = "D";
                else
                  logInfo.m_strLogLV = "V";
            }

            if(stk.hasMoreElements()) {
                logInfo.m_strTime = stk.nextToken("[]: ");
                logInfo.m_TimeUs = (long)(Double.parseDouble(logInfo.m_strTime)*1000000);
            }

            if(stk.hasMoreElements())
                logInfo.m_strTag = stk.nextToken(": ");

            if(stk.hasMoreElements())
            {
                logInfo.m_strMessage = stk.nextToken(": ");
                while(stk.hasMoreElements())
                {
                    logInfo.m_strMessage += " " + stk.nextToken(TOKEN_SPACE);
                }
                logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("  ", "");
            }
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

//----- pid 7723 at 2016-05-24 10:06:29 -----
    public LogInfo getTrace(String strText)
    {
        LogInfo logInfo = new LogInfo();

        try {
            StringTokenizer stk = new StringTokenizer(strText, TOKEN_TRACE, false);

            stk.nextToken(); //pid

            if(stk.hasMoreElements()) //smp_processor_id
                logInfo.m_strPid = stk.nextToken().trim();

            stk.nextToken(); //at
            stk.nextToken(); //year

            if(stk.hasMoreElements()) {
                logInfo.m_strDate = stk.nextToken().trim();
                logInfo.m_strDate += "-";
                logInfo.m_strDate += stk.nextToken().trim();
            }

            if(stk.hasMoreElements()) {
                logInfo.m_strTime = stk.nextToken().trim();
                logInfo.m_strTime += ".000000";
            }

            try {
              logInfo.m_TimeStamp = parseTimeToLong(logInfo.m_strDate, logInfo.m_strTime);
//              System.out.println(logInfo.m_strDate + " : " + logInfo.m_strTime + " = " + logInfo.m_TimeStamp);
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
              T.e(e);
            }
            logInfo.m_strTag = "Trace";
            logInfo.m_strMessage = strText;
        } catch(Exception e) {
            T.e("e = " + e);
            logInfo.m_strMessage = strText;
        }

        logInfo.m_TextColor = getColor(logInfo);
        return logInfo;
    }

//     [Wed May 18 10:07:36.833 2016] {3}<3>
    public LogInfo getTeraTerm(String strText)
    {
//        System.out.println("getTeraTerm");
        String str_text = strText.substring(31, strText.length());
        return parseLog(str_text);
    }

    public LogInfo parseLog(String strText)
    {
        int len = strText.length();

        if(strText.equals(" "))
            len = 0;

        if((len > 32) && isThreadTime(strText))
            return getThreadTime(strText);
        else if((len > 35) && isThreadUSTime(strText))
            return getThreadTime(strText);
        else if((len > 20) && isNormal(strText))
            return getNormal(strText);
        else if((len > 43) && isKernel_other_3(strText))
            return getKernel_other_3(strText);
        else if((len > 40) && isKernel_other_2(strText))
            return getKernel_other_2(strText);
        else if((len > 43) && isKernel_other_1(strText))
            return getKernel_other_1(strText);
        else if((len > 37) && isKernel_other_0(strText))
            return getKernel_other_0(strText);
        else if((len > 16) && isKernel_normal(strText))
            return getKernel_normal(strText);
        else if((len > 35) && isEventTrace(strText))
            return getEventTrace(strText);
        else if((len > 42) && isTrace(strText))
            return getTrace(strText);
        else if((len > 30) && isTeraTerm(strText))
            return getTeraTerm(strText);
        else
        {
            LogInfo logInfo = new LogInfo();
            logInfo.m_strMessage = strText.trim();
            logInfo.m_strTag = "Unknown";

//            System.out.println("unknown type : Len[ " + len + " ] "+ strText.trim());

            return logInfo;
        }
    }
}

