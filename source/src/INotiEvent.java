/**
 *
 */

/**
 *
 */
public interface INotiEvent
{
    public static int EVENT_CLICK_BOOKMARK              = 0;
    public static int EVENT_CLICK_ERROR                 = 1;
    public static int EVENT_CHANGE_FILTER_SHOW_TAG      = 2;
    public static int EVENT_CHANGE_FILTER_REMOVE_TAG    = 3;
    public static int EVENT_CHANGE_FILTER_FIND_WORD     = 4;
    public static int EVENT_CHANGE_FILTER_REMOVE_WORD   = 5;
    public static int EVENT_ADD_GOTO_LINE               = 6;
    public static int EVENT_ADD_PID                     = 7;
    public static int EVENT_ADD_TID                     = 8;
    public static int EVENT_CHANGESELECTION             = 9;
    public static int EVENT_GET_PID_INFO                = 10;
    public static int EVENT_GET_TID_INFO                = 11;
    public static int EVENT_EXIT_PROC_INFO              = 12;
    public static int EVENT_GOTO_TIMESTAMP              = 13;
    public static int EVENT_GOTO_LINENUMBER             = 14;

    void notiEvent(EventParam param);

    class EventParam
    {
        int nEventId;
        Object param1;
        Object param2;
        Object param3;

        public EventParam(int nEventId)
        {
            this.nEventId = nEventId;
        }
    }
}
