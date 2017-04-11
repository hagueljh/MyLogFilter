import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class LogFilterTableModel extends AbstractTableModel
{
    static final int        COMUMN_LINE         = 0;
    static final int        COMUMN_DATE         = 1;
    static final int        COMUMN_TIME         = 2;
    static final int        COMUMN_TIMEUS       = 3;
    static final int        COMUMN_TIMESTAMP    = 4;
    static final int        COMUMN_GAP          = 5;
    static final int        COMUMN_LOGLV        = 6;
    static final int        COMUMN_PID          = 7;
    static final int        COMUMN_THREAD       = 8;
    static final int        COMUMN_TAG          = 9;
    static final int        COMUMN_BOOKMARK     = 10;
    static final int        COMUMN_MESSAGE      = 11;
    public static final int COMUMN_MAX          = 12;

    private static final long serialVersionUID = 1L;

    public static String  ColName[]     = { "Line", "Date", "Time", "TimeUs", "TimeStamp", "Gap", "LogLV", "Pid(smp_processor_id)", "Thread(cpu_id)", "Tag", "Bookmark", "Message"};
    public static int     ColWidth[]    = { 50,     50,     100,      50,        50,        50,       20,          50,                        50,       100,   100,         600};
    public static int     DEFULT_WIDTH[]= { 50,     50,     100,      50,        50,        50,       20,          50,                        50,       100,   100,         600};

    ArrayList<LogInfo> m_arData;

    public static void setColumnWidth(int nColumn, int nWidth)
    {
//        T.d("nWidth = " + nWidth);
        if(nWidth >= DEFULT_WIDTH[nColumn])
            ColWidth[nColumn] = nWidth;
    }

    public int getColumnCount()
    {
        return ColName.length;
    }

    public int getRowCount()
    {
        if(m_arData != null)
            return m_arData.size();
        System.out.printf("getRowCount : m_arData null");
        return 0;
    }

    public String getColumnName(int col) {
        return ColName[col];
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if(rowIndex < 0 || columnIndex < 0) {
            System.out.printf("getValueAt err : row[%d], col[%d]\n", rowIndex, columnIndex);
            return null;
        }

        try {
            LogInfo log = m_arData.get(rowIndex);
            if(log == null) {
                System.out.printf("getValueAt null : row[%d], col[%d]\n", rowIndex, columnIndex);
                return null;
            }
            return log.getData(columnIndex);
        } catch (Exception e) {
            System.out.printf("getValueAt Exception : row[%d], col[%d]\n", rowIndex, columnIndex);
            T.e("e = " + e);
//            e.printStackTrace();
            return null;
        }
    }

    public LogInfo getRow(int row) {
        if(row < 0) {
            System.out.printf("getRow err : row[%d]\n", row);
            return null;
        }
        try {
            return m_arData.get(row);
        } catch (Exception e) {
            System.out.printf("getRow Exception : row[%d]\n", row);
            T.e("e = " + e);
//            e.printStackTrace();
            return null;
        }
    }

    public void setData(ArrayList<LogInfo> arData)
    {
        m_arData = arData;
    }
}
