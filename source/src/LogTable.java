import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class LogTable extends JTable implements FocusListener, ActionListener
{
    private static final long             serialVersionUID = 1L;

    LogFilterMain                         m_LogFilterMain;
    ILogParser                            m_iLogParser;
    String                                m_strAutoPause;
    String                                m_strTidShow;
    String                                m_strTagShow;
    String                                m_strTagRemove;
    String                                m_strFilterRemove;
    String                                m_strFilterFind;
    float                                 m_fFontSize;
    boolean                               m_bAltPressed;
    int                                   m_nTagLength;
    boolean[]                             m_arbShow;
    int                                   m_lastSelectRow = -1;
    int                                   m_nFilterLogLV = LogInfo.LOG_LV_ALL;;

    String                                  m_keywordString[]        = {"",     "",    "",     "",    "",    "",    "",    "",    "",    ""};
    boolean                                 m_keywordCaseSensitive[] = {false, false, false, false, false, false, false, false, false, false};

    public LogTable(LogFilterTableModel tablemodel, LogFilterMain filterMain)
    {
        super(tablemodel);
        m_LogFilterMain = filterMain;

        m_nTagLength         = 0;
        m_arbShow            = new boolean[LogFilterTableModel.COMUMN_MAX];
        init();
        setColumnWidth();
    }

    public void changeSelection( int rowIndex)
    {
        if(rowIndex < 0 ) rowIndex = 0;
        if(rowIndex > getRowCount() - 1) rowIndex = getRowCount() - 1;
        super.changeSelection(rowIndex, 0, false, false);
//        m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_CHANGESELECTION));
//        if(getAutoscrolls())
        showRow(rowIndex);
    }

    public void changeSelection( int rowIndex, int columnIndex, boolean toggle, boolean extend )
    {
        if(rowIndex < 0 ) rowIndex = 0;
        if(rowIndex > getRowCount() - 1) rowIndex = getRowCount() - 1;
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
//        if(getAutoscrolls())
        showRow(rowIndex);
    }

    public void changeSelection( int rowIndex, int columnIndex, boolean toggle, boolean extend, boolean bMove )
    {
        if(rowIndex < 0 ) rowIndex = 0;
        if(rowIndex > getRowCount() - 1) rowIndex = getRowCount() - 1;
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
//        if(getAutoscrolls())
        if(bMove)
            showRow(rowIndex);
    }

    private void init() {
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
        registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);

        addFocusListener( this );
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        setTableHeader(createTableHeader());
//        getTableHeader().setReorderingAllowed(false);
        m_fFontSize = 12;
        setOpaque(false);
        setAutoscrolls(false);
//        setRequestFocusEnabled(false);

//        setGridColor(TABLE_GRID_COLOR);
        setIntercellSpacing(new Dimension(0, 0));
        // turn off grid painting as we'll handle this manually in order to paint
        // grid lines over the entire viewport.
        setShowGrid(false);

        for(int iIndex = 0; iIndex < getColumnCount(); iIndex++)
        {
            getColumnModel().getColumn(iIndex).setCellRenderer(new LogCellRenderer());
        }

        addMouseListener(new MouseAdapter()
        {
//            public void mouseEntered( MouseEvent e ) {
//                Point p = e.getPoint();
//                int row = rowAtPoint( p );
//
//                LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(row);
//
//                setToolTipText(logInfo.m_strMessage);
//            }

            public void mouseClicked( MouseEvent e )
            {
                Point p = e.getPoint();
                int row = rowAtPoint( p );
                int colum = columnAtPoint(p);
                m_lastSelectRow = row;
                if ( SwingUtilities.isLeftMouseButton( e ) )
                {
                    if(m_bAltPressed)
                    {
                        int column = columnAtPoint(p);
                        if(column == LogFilterTableModel.COMUMN_LINE)
                        {
                            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_ADD_GOTO_LINE));
                        }
                        else if(column == LogFilterTableModel.COMUMN_PID)
                        {
                            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_ADD_PID));
                        }
                        else if(column == LogFilterTableModel.COMUMN_THREAD)
                        {
                            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_ADD_TID));
                        }
                        else if(column == LogFilterTableModel.COMUMN_TAG)
                        {
                            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_CHANGE_FILTER_SHOW_TAG));
                        }
                    }
                    else if (e.getClickCount() == 2)
                    {
                        //jinube mouse double click

                        LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(row);
                        int[] arSelectedRow = getSelectedRows();
                        for(int nIndex : arSelectedRow)
                        {
                            logInfo = ((LogFilterTableModel)getModel()).getRow(nIndex);
                            if(colum == LogFilterTableModel.COMUMN_LINE)
                                logInfo.m_bMarked = true;
                            else
                                logInfo.m_bMarked = !logInfo.m_bMarked;
                            logInfo.m_MarkColor = LogColor.COLOR_BOOKMARK;  //jinube
                            m_LogFilterMain.bookmarkItem(nIndex, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked);
                        }
                        repaint();
                        m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_ADD_GOTO_LINE));
                    }
                    else
                    {
                        m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_CHANGESELECTION));

                        if(colum == LogFilterTableModel.COMUMN_PID)
                        {
                            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_GET_PID_INFO));
                        } else if(colum == LogFilterTableModel.COMUMN_THREAD) {
                            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_GET_TID_INFO));
                        } else {
                            try {
                                String strThread = ((LogFilterTableModel)getModel()).getRow(row).m_strThread;
                                if(strThread == null || strThread.isEmpty() || ("".equals(strThread.trim())))
                                    m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_GET_PID_INFO));
                                else
                                    m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_GET_TID_INFO));
                            } catch (Exception ee) {
//                              System.out.printf("mouseClicked row = %d\n", row);
//                              ee.printStackTrace();
                              T.e(ee);
                            }
                        }
                    }
                }
                else if ( SwingUtilities.isRightMouseButton( e ))
                {
//                    T.d("m_bAltPressed = " + m_bAltPressed);
                    if(m_bAltPressed)
                    {
                        if(colum == LogFilterTableModel.COMUMN_TAG)
                        {
                            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_CHANGE_FILTER_REMOVE_TAG));
                        }
                    }
                    else
                    {
//                        T.d();
                        LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(row);
                        StringSelection data = new StringSelection(((String)logInfo.getData(colum)).trim());
                        getToolkit();
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(data, data);
                    }
                }
            }
        });
        getTableHeader().addMouseListener(new ColumnHeaderListener());
    }

    public boolean isCellEditable(int row, int column)
    {
        if(column == LogFilterTableModel.COMUMN_BOOKMARK)
            return true;
        return false;
    }

    boolean isInnerRect(Rectangle parent, Rectangle child)
    {
        if(parent.y <= child.y && (parent.y + parent.height) >= (child.y + child.height))
            return true;
        else
            return false;
    }

    boolean checkLogLVFilter(LogInfo logInfo)
    {
        if(m_nFilterLogLV == LogInfo.LOG_LV_ALL)
            return true;
        if((m_nFilterLogLV & LogInfo.LOG_LV_VERBOSE) != 0 && (logInfo.m_strLogLV.equals("V") || logInfo.m_strLogLV.equals("VERBOSE")))
            return true;
        if((m_nFilterLogLV & LogInfo.LOG_LV_DEBUG) != 0 && (logInfo.m_strLogLV.equals("D") || logInfo.m_strLogLV.equals("DEBUG")))
            return true;
        if((m_nFilterLogLV & LogInfo.LOG_LV_INFO) != 0 && (logInfo.m_strLogLV.equals("I") || logInfo.m_strLogLV.equals("INFO")))
            return true;
        if((m_nFilterLogLV & LogInfo.LOG_LV_WARN) != 0 && (logInfo.m_strLogLV.equals("W") || logInfo.m_strLogLV.equals("WARN")))
            return true;
        if((m_nFilterLogLV & LogInfo.LOG_LV_ERROR) != 0 && (logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("ERROR")))
            return true;
        if((m_nFilterLogLV & LogInfo.LOG_LV_FATAL) != 0 && (logInfo.m_strLogLV.equals("F") || logInfo.m_strLogLV.equals("FATAL")))
            return true;

        return false;
    }

    boolean CheckFilter(LogInfo logInfo)
    {
        if(checkLogLVFilter(logInfo)
            && CheckFilterKeyword(m_LogFilterMain.FILTER_0, logInfo)
            && CheckFilterKeyword(m_LogFilterMain.FILTER_2, logInfo)
            && CheckFilterKeyword(m_LogFilterMain.FILTER_3, logInfo)
            && CheckFilterKeyword(m_LogFilterMain.FILTER_4, logInfo)
            && CheckFilterKeyword(m_LogFilterMain.FILTER_1, logInfo)
            && CheckFilterKeyword(m_LogFilterMain.FILTER_5, logInfo)
          )
            return true;

        return false;
    }

    LogInfo GetCurrentLog()
    {
        int row = getSelectedRow();
        if(row < 0 || getSelectedRowCount() > 1)
            row = m_lastSelectRow;
        LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(row);
        return logInfo;
    }

    LogInfo GetAtLog(int row)
    {
        if(row < 0 || getSelectedRowCount() > 1)
            row = m_lastSelectRow;
        LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(row);
        return logInfo;
    }

    int getVisibleRowCount()
    {
        return getVisibleRect().height/getRowHeight();
    }

    boolean gotoSelectBookmark(int nIndex)
    {
        LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(nIndex);
        if(logInfo.m_bMarked)
        {
            Rectangle parent = getVisibleRect();

            changeSelection(nIndex);
            int nVisible = nIndex;
            if(!isInnerRect(parent, getCellRect(nIndex, 0, true)))
                nVisible = nIndex + getVisibleRowCount() / 2;
            showRow(nVisible);
            return true;
        }
        return false;
    }

    void gotoNextPreBookmark(boolean next)
    {
        int nSeletectRow = getSelectedRow();

        if(next) {
            for(int nIndex = nSeletectRow + 1; nIndex < getRowCount(); nIndex++)
            {
                if(gotoSelectBookmark(nIndex)) return;
            }

            for(int nIndex = 0; nIndex < nSeletectRow; nIndex++)
            {
                if(gotoSelectBookmark(nIndex)) return;
            }
        } else {
            for(int nIndex = nSeletectRow - 1; nIndex >= 0; nIndex--)
            {
                if(gotoSelectBookmark(nIndex)) return;
            }

            for(int nIndex = getRowCount() - 1; nIndex > nSeletectRow; nIndex--)
            {
                if(gotoSelectBookmark(nIndex)) return;
            }
        }
    }

    boolean gotoSelectHighlight(int nIndex)
    {
        LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(nIndex);
        if(CheckFilterKeyword(LogFilterMain.FILTER_6, logInfo))
        {
            Rectangle parent = getVisibleRect();

            changeSelection(nIndex);
            int nVisible = nIndex;
            if(!isInnerRect(parent, getCellRect(nIndex, 0, true)))
                nVisible = nIndex + getVisibleRowCount() / 2;
            showRow(nVisible);
            return true;
        }
        return false;
    }

    void gotoNextPreHighlight(boolean next)
    {
        int nSeletectRow = getSelectedRow();

        if(next) {
            for(int nIndex = nSeletectRow + 1; nIndex < getRowCount(); nIndex++)
            {
                if(gotoSelectHighlight(nIndex)) return;
            }

            for(int nIndex = 0; nIndex < nSeletectRow; nIndex++)
            {
                if(gotoSelectHighlight(nIndex)) return;
            }
        } else {
            for(int nIndex = nSeletectRow - 1; nIndex >= 0; nIndex--)
            {
                if(gotoSelectHighlight(nIndex)) return;
            }

            for(int nIndex = getRowCount() - 1; nIndex > nSeletectRow; nIndex--)
            {
                if(gotoSelectHighlight(nIndex)) return;
            }
        }
    }

    public void hideColumn(int nColumn)
    {
        getColumnModel().getColumn(nColumn).setWidth(0);
        getColumnModel().getColumn(nColumn).setMinWidth(0);
        getColumnModel().getColumn(nColumn).setMaxWidth(0);
        getColumnModel().getColumn(nColumn).setPreferredWidth(0);
        getColumnModel().getColumn(nColumn).setResizable(false);
    }

    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed)
    {
        m_bAltPressed = e.isAltDown();
//        if(e.getID() == KeyEvent.KEY_RELEASED)
        {
            switch(e.getKeyCode())
            {
                case KeyEvent.VK_END: //END
                    changeSelection(getRowCount() - 1);
                    return true;

                case KeyEvent.VK_HOME: // HOME
                    changeSelection(0);
                    return true;

                case KeyEvent.VK_F2: // CTRL + F2
                    if(e.isControlDown() && e.getID() == KeyEvent.KEY_PRESSED)
                    {
                        int[] arSelectedRow = getSelectedRows();
                        for(int nIndex : arSelectedRow)
                        {
                            LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(nIndex);
                            logInfo.m_bMarked = !logInfo.m_bMarked;
                            logInfo.m_MarkColor = LogColor.COLOR_BOOKMARK;  //jinube
                            m_LogFilterMain.bookmarkItem(nIndex, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked);
                        }
                        repaint();
                    }
                    else if(!e.isControlDown() && e.getID() == KeyEvent.KEY_PRESSED) {
                        gotoNextPreBookmark(false);
                    }
                    return true;

                case KeyEvent.VK_F3: // F3
                    if(e.getID() == KeyEvent.KEY_PRESSED) {
                        gotoNextPreBookmark(true);
                    }
                    return true;

                case KeyEvent.VK_COMMA: // , <
                    if(e.getID() == KeyEvent.KEY_PRESSED) {
                        gotoNextPreHighlight(false);
                    }
                    return true;

                case KeyEvent.VK_PERIOD: // . >
                    if(e.getID() == KeyEvent.KEY_PRESSED) {
                        gotoNextPreHighlight(true);
                    }
                    return true;

                case KeyEvent.VK_R: // jinube CTRL + R
                    if(e.isControlDown() && e.getID() == KeyEvent.KEY_PRESSED)
                    {
                        unmark_all();
                    }
                    return true;

                case KeyEvent.VK_F: // F
                    if(e.getID() == KeyEvent.KEY_PRESSED && ( (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK))
                    {
                        m_LogFilterMain.setFindFocus();
                        return true;
                    }
                    break;

                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    if(e.getID() == KeyEvent.KEY_PRESSED) {
                        m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_CHANGESELECTION));
                    }
                    break;

//                case KeyEvent.VK_O:
//                    if(e.getID() == KeyEvent.KEY_RELEASED)
//                    {
//                        m_LogFilterMain.openFileBrowser();
//                        return true;
//                    }
            }
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    public void packColumn(int vColIndex, int margin) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel)getColumnModel();
        TableColumn col = colModel.getColumn(vColIndex);
        int width = 0;

        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = getTableHeader().getDefaultRenderer();
        }
        Component comp;
//        Component comp = renderer.getTableCellRendererComponent(
//            this, col.getHeaderValue(), false, false, 0, 0);
//        width = comp.getPreferredSize().width;

        JViewport viewport = (JViewport)m_LogFilterMain.m_scrollVBar.getViewport();
        Rectangle viewRect = viewport.getViewRect();
        int nFirst = m_LogFilterMain.m_tbLogTable.rowAtPoint(new Point(0, viewRect.y));
        int nLast = m_LogFilterMain.m_tbLogTable.rowAtPoint(new Point(0, viewRect.height - 1));

        if(nLast < 0)
            nLast = m_LogFilterMain.m_tbLogTable.getRowCount();
        // Get maximum width of column data
        for (int r=nFirst; r<nFirst + nLast; r++) {
            renderer = getCellRenderer(r, vColIndex);
            comp = renderer.getTableCellRendererComponent(
                this, getValueAt(r, vColIndex), false, false, r, vColIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // Add margin
        width += 2*margin;

        // Set the width
        col.setPreferredWidth(width);
    }

    public float getFontSize()
    {
        return m_fFontSize;
    }

    public int getColumnWidth(int nColumn)
    {
        return getColumnModel().getColumn(nColumn).getWidth();
    }

    public void showColumn(int nColumn, boolean bShow)
    {
        m_arbShow[nColumn] = bShow;
        if(bShow)
        {
            getColumnModel().getColumn(nColumn).setResizable(true);
            getColumnModel().getColumn(nColumn).setMaxWidth(LogFilterTableModel.ColWidth[nColumn] * 1000);
            getColumnModel().getColumn(nColumn).setMinWidth(1);
            getColumnModel().getColumn(nColumn).setWidth(LogFilterTableModel.ColWidth[nColumn]);
            getColumnModel().getColumn(nColumn).setPreferredWidth(LogFilterTableModel.ColWidth[nColumn]);
        }
        else
        {
            hideColumn(nColumn);
        }
    }

    public void setColumnWidth()
    {
        for(int iIndex = 0; iIndex < getColumnCount(); iIndex++)
        {
            showColumn(iIndex, true);
        }
        showColumn(LogFilterTableModel.COMUMN_GAP, false);
        showColumn(LogFilterTableModel.COMUMN_BOOKMARK, false);
        showColumn(LogFilterTableModel.COMUMN_TIMEUS, false);
        showColumn(LogFilterTableModel.COMUMN_TIMESTAMP, false);
//        showColumn(LogFilterTableModel.COMUMN_THREAD, false);
    }

    void SetFilterKeyword(int filter, String keyword, boolean bCaseSensitive)
    {
        m_keywordString[filter]         = keyword;
        m_keywordCaseSensitive[filter]  = bCaseSensitive;
    }

    String GetFilterKeywordString(int filter)
    {
        return m_keywordString[filter];
    }

    boolean GetFilterKeywordCaseSensitive(int filter)
    {
        return m_keywordCaseSensitive[filter];
    }

    boolean CheckFilterKeyword(int filter, LogInfo logInfo)
    {
        if(GetFilterKeywordString(filter).length() <= 0) return true;

        String comp = null;

        switch(filter)
        {
            case LogFilterMain.FILTER_0:  comp = logInfo.m_strMessage; break;
            case LogFilterMain.FILTER_1:  comp = logInfo.m_strMessage; break;
            case LogFilterMain.FILTER_2:  comp = logInfo.m_strPid;     break;
            case LogFilterMain.FILTER_3:  comp = logInfo.m_strThread;  break;
            case LogFilterMain.FILTER_4:  comp = logInfo.m_strTag;     break;
            case LogFilterMain.FILTER_5:  comp = logInfo.m_strTag;     break;
            default:                      comp = logInfo.m_strMessage; break;
        }
        if(comp == null || comp.isEmpty() || ("".equals(comp.trim()))) return false;

        StringTokenizer stk = new StringTokenizer(m_keywordString[filter], "|", false);

        boolean contain = false;

        if(GetFilterKeywordCaseSensitive(filter))
        {
            while(stk.hasMoreElements())
            {
                if(comp.contains(stk.nextToken())) {
                    contain = true;
                    break;
                }
            }
        }
        else
        {
            comp = comp.toLowerCase();
            while(stk.hasMoreElements())
            {
                if(comp.contains(stk.nextToken().toLowerCase())) {
                    contain = true;
                    break;
                }
            }
        }

        // Remove Case
        switch(filter)
        {
            case LogFilterMain.FILTER_1:
            case LogFilterMain.FILTER_5:
                return !contain;
        }
        return contain;
    }

    void SetLogLevel(int level)
    {
        m_nFilterLogLV = level;
    }

    public void setFontSize(int nFontSize)
    {
        m_fFontSize = nFontSize;
        setRowHeight(nFontSize + 4);
    }

    public void setLogParser(ILogParser iLogParser)
    {
        m_iLogParser = iLogParser;
    }

    public void setValueAt(Object aValue, int row, int column)
    {
        LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(row);
        if(column == LogFilterTableModel.COMUMN_BOOKMARK)
        {
            //logInfo.m_strBookmark = (String)aValue;
            m_LogFilterMain.setBookmark(Integer.parseInt(logInfo.m_strLine) - 1, (String)aValue);
        }
    }

    public class LogCellRenderer extends DefaultTableCellRenderer
    {
        private static final long serialVersionUID = 1L;
        boolean m_bChanged;

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            try {
                if(value != null)
                    value = remakeData(column, (String)value);
                Component c = super.getTableCellRendererComponent(table,
                                                                  value,
                                                                  isSelected,
                                                                  hasFocus,
                                                                  row,
                                                                  column);
                LogInfo logInfo = ((LogFilterTableModel)getModel()).getRow(row);

                if(logInfo == null) {
                    System.out.printf("getTableCellRendererComponent null : row = %d\n", row);
                    return c;
                }

                if(row == 0) {
                  logInfo.m_Timegap = 0;
//                  logInfo.m_strTimegap = "0";
                } else {
                  LogInfo blogInfo = ((LogFilterTableModel)getModel()).getRow(row-1);
                  if(blogInfo != null) {
                      if((logInfo.m_TimeUs > 0) && (blogInfo.m_TimeUs > 0))
                          logInfo.m_Timegap = logInfo.m_TimeUs - blogInfo.m_TimeUs;
                      else
                          logInfo.m_Timegap = logInfo.m_TimeStamp - blogInfo.m_TimeStamp;
//                      logInfo.m_strTimegap = String.format("%,d", logInfo.m_Timegap);
                  }
                }

                c.setFont(getFont().deriveFont(m_fFontSize));
                c.setForeground(logInfo.m_TextColor);
                if(isSelected)
                {
                    if(logInfo.m_bMarked)
                    {
                        c.setBackground(new Color((int)(logInfo.m_MarkColor*1.8)));
                    }
                    else
                    {
                        c.setBackground(new Color(LogColor.COLOR_BOOKMARK2));
                    }
                }
                else if(logInfo.m_bMarked)
                {
    //                c.setBackground(new Color(LogColor.COLOR_BOOKMARK));
                    c.setBackground(new Color(logInfo.m_MarkColor));
                }
                else
                {
                    c.setBackground(Color.BLACK);
    //                c.setBackground(Color.WHITE);
                }

                return c;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        String remakeData(int nIndex, String strText)
        {
            switch(nIndex)
            {
                case LogFilterTableModel.COMUMN_MESSAGE:
                case LogFilterTableModel.COMUMN_TAG:
                    break;
                default:
                    return strText;
            }

            String strKeyword = GetFilterKeywordString(LogFilterMain.FILTER_6);
            boolean bCheck    = GetFilterKeywordCaseSensitive(LogFilterMain.FILTER_6);

            m_bChanged = false;
            strText = strText.replace( " ", "\u00A0" );

            if(strKeyword == null || strKeyword.isEmpty() || ("".equals(strKeyword.trim()))) {
                ;
            } else {
                if(LogColor.COLOR_HIGHLIGHT != null && LogColor.COLOR_HIGHLIGHT.length > 0)
                    strText = remakeFind(strText, strKeyword, LogColor.COLOR_HIGHLIGHT, true, bCheck);
                else
                    strText = remakeFind(strText, strKeyword, "#00FF00", true, bCheck);
            }

            switch(nIndex)
            {
                case LogFilterTableModel.COMUMN_MESSAGE:
                    strKeyword = GetFilterKeywordString(LogFilterMain.FILTER_0);
                    bCheck = GetFilterKeywordCaseSensitive(LogFilterMain.FILTER_0);
                    break;

                case LogFilterTableModel.COMUMN_TAG:
                    strKeyword = GetFilterKeywordString(LogFilterMain.FILTER_4);
                    bCheck = GetFilterKeywordCaseSensitive(LogFilterMain.FILTER_4);
                    break;
                default:
                    strKeyword = null;
                    break;
            }

            if(strKeyword == null || strKeyword.isEmpty() || ("".equals(strKeyword.trim()))) {
                ;
            } else {
                strText = remakeFind(strText, strKeyword, "#FF0000", false, bCheck);
            }

            if(m_bChanged) {
                strText = "<html><nobr>" + strText + "</nobr></html>";
            }

            return strText.replace("\t", "    ");
        }

        String remakeFind(String strText, String strFind, String[] arColor, boolean bUseSpan, boolean bCaseSensitive)
        {
            if(strFind == null || strFind.length() <= 0) {
//                System.out.printf("remakeFind : strFind null");
                return strText;
            }

            strFind = strFind.replace( " ", "\u00A0" );
            StringTokenizer stk = new StringTokenizer(strFind, "|");
            String newText;
            String strToken;
            int nIndex = 0;

            while (stk.hasMoreElements())
            {
                boolean bfind = false;
                if(nIndex >= arColor.length)
                    nIndex = 0;
                strToken = stk.nextToken();

                if(bCaseSensitive)
                    bfind = strText.contains(strToken);
                else
                    bfind = strText.toLowerCase().contains(strToken.toLowerCase());

                if(bfind)
                {
                    if(bUseSpan)
                        newText = "<span style=\"background-color:#" + arColor[nIndex] + "\"><b>";
                    else
                        newText = "<font color=#" + arColor[nIndex] + "><b>";
                    newText += strToken;
                    if(bUseSpan)
                        newText += "</b></span>";
                    else
                        newText += "</b></font>";
//                    if(bCaseSensitive)
                        strText = strText.replace(strToken, newText);
//                    else
//                        strText = strText.replaceAll("(?i)" + strToken, newText);
                    m_bChanged = true;
                    nIndex++;
                }
            }
            return strText;
        }

        String remakeFind(String strText, String strFind, String strColor, boolean bUseSpan, boolean bCaseSensitive)
        {
            if(strFind == null || strFind.length() <= 0) return strText;

            strFind = strFind.replace( " ", "\u00A0" );
            StringTokenizer stk = new StringTokenizer(strFind, "|");
            String newText;
            String strToken;

            while (stk.hasMoreElements())
            {
                boolean bfind = false;
                strToken = stk.nextToken();

                if(bCaseSensitive)
                    bfind = strText.contains(strToken);
                else
                    bfind = strText.toLowerCase().contains(strToken.toLowerCase());

                if(bfind)
                {
                    if(bUseSpan)
                        newText = "<span style=\"background-color:" + strColor + "\"><b>";
                    else
                        newText = "<font color=" + strColor + "><b>";
                    newText += strToken;
                    if(bUseSpan)
                        newText += "</b></span>";
                    else
                        newText += "</b></font>";
//                    if(bCaseSensitive)
                        strText = strText.replace(strToken, newText);
//                    else
//                        strText = strText.replaceAll("(?i)" + strToken, newText);
                    m_bChanged = true;
                }
            }
            return strText;
        }
    }

    public void showRow(int row)
    {
        if(row < 0 ) row = 0;
        if(row > getRowCount() - 1) row = getRowCount() - 1;

        Rectangle rList = getVisibleRect();
        Rectangle rCell = getCellRect(row, 0, true);
        if(rList != null && rCell != null)
        {
            Rectangle scrollToRect = new Rectangle((int)rList.getX(), (int)rCell.getY(), (int)(rList.getWidth()), (int)rCell.getHeight());
            scrollRectToVisible(scrollToRect);
        }
    }

    public int getRowIndexesOfValue(int row) {
        if(row < 0) {
            System.out.println("getRowIndexesOfValue : row = " + row);
            return -1;
        }

        LogInfo logInfo;
        boolean bfind = false;
        String find_row_string = Integer.toString(row+1);
        int row_count = 0;

        try {
            row_count = getRowCount();
        } catch (Exception e) {
            System.out.printf("getRowCount Exception : row = " + row);
            return -1;
        }

        if(row < row_count) {
          logInfo = ((LogFilterTableModel)getModel()).getRow(row);
          if(logInfo != null && logInfo.m_strLine != null && logInfo.m_strLine.equals(find_row_string)) {
            bfind = true;
          } else {
//            System.out.println("row = " + row + " / m_strLine = " + Integer.parseInt(logInfo.m_strLine));
//            System.out.println("getRowIndexesOfValue : row = " + row);
          }
        }

        if(bfind == false) {
            for(int rowCount = 0; rowCount < row_count; rowCount++) {
                logInfo = ((LogFilterTableModel)getModel()).getRow(rowCount);
                if(logInfo.m_strLine.equals(find_row_string)) {
//                    System.out.println("row = " + row + " / rowCount = " + rowCount);
                    return rowCount;
                }
            }
        }
//        System.out.println("row = " + row);
        return row;
    }

    public void showRow(int tbl_row, boolean bCenter)
    {
        int nLastSelectedIndex = getSelectedRow();
        int row = getRowIndexesOfValue(tbl_row);

        if(row < 0) {
            System.out.println("tbl_row = " + tbl_row + " / row = " + row);
            return;
        }

        changeSelection(row);
        int nVisible = row;
        if(nLastSelectedIndex <= row || nLastSelectedIndex == -1)
            nVisible = row + getVisibleRowCount() / 2;
        else
            nVisible = row - getVisibleRowCount() / 2;
        if(nVisible < 0) nVisible = 0;
        else if(nVisible > getRowCount() - 1) nVisible = getRowCount() - 1;
        showRow(nVisible);
    }

    public class ColumnHeaderListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {

            if ( SwingUtilities.isLeftMouseButton( evt ) && evt.getClickCount() == 2 )
            {
                JTable table = ((JTableHeader)evt.getSource()).getTable();
                TableColumnModel colModel = table.getColumnModel();

                // The index of the column whose header was clicked
                int vColIndex = colModel.getColumnIndexAtX(evt.getX());

                if (vColIndex == -1) {
                    T.d("vColIndex == -1");
                    return;
                }
                packColumn(vColIndex, 1);
            }
        }
    }

    @Override
    public void focusGained( FocusEvent arg0 )
    {
    }

    @Override
    public void focusLost( FocusEvent arg0 )
    {
        m_bAltPressed = false;
    }

    @Override
    public void actionPerformed( ActionEvent arg0 )
    {
        Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();;
        StringBuffer sbf = new StringBuffer();
        int numrows = getSelectedRowCount();
        int[] rowsselected = getSelectedRows();
//        if ( !( ( numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length )
//                && ( numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] && numcols == colsselected.length ) ) )
//        {
//            JOptionPane.showMessageDialog( null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE );
//            return;
//        }

        for ( int i = 0; i < numrows; i++ )
        {
            for ( int j = 0; j < m_arbShow.length; j++ )
            {
                if(!(j == LogFilterTableModel.COMUMN_LINE) && m_arbShow[j])
                {
                    StringBuffer strTemp = new StringBuffer((String)getValueAt( rowsselected[i], j ));
                    if(j == LogFilterTableModel.COMUMN_TAG)
                    {
                        String strTag = strTemp.toString();
                        for(int k = 0; k < m_nTagLength - strTag.length(); k++)
                            strTemp.append(" ");
                    }
                    else if(j == LogFilterTableModel.COMUMN_THREAD || j == LogFilterTableModel.COMUMN_PID)
                    {
                        String strTag = strTemp.toString();
                        for(int k = 0; k < 8 - strTag.length(); k++)
                            strTemp.append(" ");
                    }
                    strTemp.append(" ");
                    sbf.append( strTemp );
                }
            }
            sbf.append( "\n" );
        }
        StringSelection stsel = new StringSelection( sbf.toString() );
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(stsel,stsel);
    }

    public void setTagLength(int nLength)
    {
        if(m_nTagLength < nLength)
        {
            m_nTagLength = nLength;
//            T.d("m_nTagLength = " + m_nTagLength);
        }
    }

    public void mark(boolean mark)
    {
      LogInfo logInfo;
      int[] arSelectedRow = getSelectedRows();
      for(int nIndex : arSelectedRow)
      {
          logInfo = ((LogFilterTableModel)getModel()).getRow(nIndex);
          logInfo.m_bMarked = mark;
          logInfo.m_MarkColor = LogColor.COLOR_BOOKMARK;  //jinube
          m_LogFilterMain.bookmarkItem(nIndex, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked);
      }
      repaint();
    }

    public void unmark_all()
    {
        LogInfo logInfo;
        for(int nIndex = 0; nIndex < getRowCount(); nIndex++)
        {
            logInfo = ((LogFilterTableModel)getModel()).getRow(nIndex);
            if(logInfo.m_bMarked)
            {
              logInfo.m_bMarked = false;
              logInfo.m_MarkColor = LogColor.COLOR_BOOKMARK;
              m_LogFilterMain.bookmarkItem(nIndex, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked);
            }
        }
        repaint();
    }

    public void TimeGapMark(boolean more, long time)
    {
        LogInfo logInfo, blogInfo;

        if(more) {
            for(int nIndex = 1; nIndex < getRowCount(); nIndex++)
            {
                logInfo  = ((LogFilterTableModel)getModel()).getRow(nIndex);
                blogInfo = ((LogFilterTableModel)getModel()).getRow(nIndex-1);

                if((logInfo.m_TimeUs > 0) && (blogInfo.m_TimeUs > 0))
                    logInfo.m_Timegap = logInfo.m_TimeUs - blogInfo.m_TimeUs;
                else
                    logInfo.m_Timegap = logInfo.m_TimeStamp - blogInfo.m_TimeStamp;
//                logInfo.m_strTimegap = String.format("%,d", logInfo.m_Timegap);

                if(logInfo.m_Timegap > time)
                {
                    logInfo.m_bMarked = true;
                    logInfo.m_MarkColor = LogColor.COLOR_BOOKMARK;
                    m_LogFilterMain.bookmarkItem(nIndex, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked);
                }
            }
        }
        else
        {
            for(int nIndex = 1; nIndex < getRowCount(); nIndex++)
            {
                logInfo  = ((LogFilterTableModel)getModel()).getRow(nIndex);
                blogInfo = ((LogFilterTableModel)getModel()).getRow(nIndex-1);

                if((logInfo.m_TimeUs > 0) && (blogInfo.m_TimeUs > 0))
                    logInfo.m_Timegap = logInfo.m_TimeUs - blogInfo.m_TimeUs;
                else
                    logInfo.m_Timegap = logInfo.m_TimeStamp - blogInfo.m_TimeStamp;
//                logInfo.m_strTimegap = String.format("%,d", logInfo.m_Timegap);

                if(logInfo.m_Timegap < time)
                {
                    logInfo.m_bMarked = true;
                    logInfo.m_MarkColor = LogColor.COLOR_BOOKMARK;
                    m_LogFilterMain.bookmarkItem(nIndex, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked);
                }
            }
        }
        repaint();
    }
}

