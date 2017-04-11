import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Point;

import java.io.*;
import java.text.SimpleDateFormat;
import java.net.*;
import java.util.*;

import javax.swing.Action;
import javax.swing.AbstractAction;
//import javax.comm.*;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

class AMProcInfo
{
    int             m_tid;
    int             m_line;
    long            m_TimeStamp;
}

class lc extends JDialog
{
    public lc() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            long limit   = sdf.parse("2017-01-01 00:00:00.000").getTime();
            long current = System.currentTimeMillis();

            if(current > limit) {
//                System.out.printf("%s:%d > %s:%d\r\n", sdf.format(new Date(current)), current, sdf.format(new Date(limit)), limit);
                JOptionPane.showMessageDialog(this, "계약 기간 종료", "Message", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        }
        catch (Exception e)
        {
          e.printStackTrace();
          System.exit(0);
        }
    }
}

public class LogFilterMain extends JFrame implements INotiEvent
{
    private static final long serialVersionUID           = 1L;
    public static String ENDL = System.getProperty("line.separator");

    static final String       LOGFILTER                  = "LogFilter";
    static final String       VERSION                    = "Version 1.8";
    final String              COMBO_ANDROID              = "Android";
    final String              COMBO_UART                 = "UART";
    final String              COMBO_IOS                  = "ios";
    final String              COMBO_CUSTOM_COMMAND       = "custom command";

//    final String              IOS_DEFAULT_CMD            = "adb logcat -v time ";
//    final String              IOS_SELECTED_CMD_FIRST     = "adb -s ";
//    final String              IOS_SELECTED_CMD_LAST      = " logcat -v time ";
//    final String              ANDROID_DEFAULT_CMD        = "logcat -v time ";
//    final String              ANDROID_THREAD_CMD         = "logcat -v threadtime ";
//    final String              ANDROID_EVENT_CMD          = "logcat -b events -v time ";
//    final String              ANDROID_RADIO_CMD          = "logcat -b radio -v time          ";
//    final String              ANDROID_CUSTOM_CMD         = "logcat ";
    String              ANDROID_DEFAULT_CMD_FIRST;
    String              ANDROID_SELECTED_CMD_FIRST;
//    final String              ANDROID_SELECTED_CMD_LAST  = " logcat -v time ";
    String[]            DEVICES_CMD={"","","","","","","","","",""};

    static final int          DEFAULT_WIDTH              = 1200;
    static final int          DEFAULT_HEIGHT             = 720;
    static final int          MIN_WIDTH                  = 500;
    static final int          MIN_HEIGHT                 = 300;

    static int                DEVICES_CUSTOM             = 0xFF;

    static final int          STATUS_CHANGE              = 1;
    static final int          STATUS_PARSING             = 2;
    static final int          STATUS_READY               = 4;

    final int                 L                          = SwingConstants.LEFT;
    final int                 C                          = SwingConstants.CENTER;
    final int                 R                          = SwingConstants.RIGHT;

    static LogFilterMain mainFrame;

    JTabbedPane               m_tpTab;
    JTextField                m_tfStatus;
    JTextArea                 m_taSourceInfo;
    IndicatorPanel            m_ipIndicator;
//    ArrayList<TagInfo>        m_arTagInfo;
    ArrayList<AMProcInfo>     m_arAMProcInfo;
    ArrayList<LogInfo>        m_arLogInfoAll;
    ArrayList<LogInfo>        m_arLogInfoFiltered;
    HashMap<Integer, Integer> m_hmBookmarkAll;
    HashMap<Integer, Integer> m_hmBookmarkFiltered;
    HashMap<Integer, Integer> m_hmErrorAll;
    HashMap<Integer, Integer> m_hmErrorFiltered;
    ILogParser                m_iLogParser;
    LogTable                  m_tbLogTable;
//    TagTable                    m_tbTagTable;
    JScrollPane               m_scrollVBar;
//    JScrollPane                 m_scrollVTagBar;
    LogFilterTableModel       m_tmLogTableModel;
//    TagFilterTableModel         m_tmTagTableModel;
    boolean                   m_bUserFilter;

    JDialog                   m_jdFilterDialog = null;
    JDialog                   m_jdSettingDialog = null;
    JDialog                   m_jdDeviceSelectDialog = null;
    JDialog                   m_jdSendCommandDialog = null;

    boolean                   m_bfFilterDialog = true;
    boolean                   m_bfSettingDialog = true;
    boolean                   m_bfDeviceSelectDialog = true;
    boolean                   m_bfSendCommandDialog = true;

    boolean                   m_bFilterDialog = true;
    boolean                   m_bSettingDialog = true;
    boolean                   m_bDeviceSelectDialog = true;
    boolean                   m_bSendCommandDialog = true;
    //Device
    JButton                   m_btnDevice;
    JList                     m_lDeviceList;
    JComboBox                 m_jcDeviceType;
    JComboBox                 m_jcCommand;
    JComboBox                 m_comboSendCommand;
//    JButton                   m_btnSetFont;

    JCheckBox                 m_chkAppendMode;
    JCheckBox                 m_chkSyncRcv;
    JCheckBox                 m_chkSyncReq;
    JCheckBox                 m_chkSyncLine;

    JCheckBox                 m_chkEnableAutoClear;
    //Log filter
    JCheckBox                 m_chkVerbose;
    JCheckBox                 m_chkDebug;
    JCheckBox                 m_chkInfo;
    JCheckBox                 m_chkWarn;
    JCheckBox                 m_chkError;
    JCheckBox                 m_chkFatal;
    JCheckBox                 m_chkSendCommandClear;

    //Show column
    JCheckBox                 m_chkClmBookmark;
    JCheckBox                 m_chkClmLine;
    JCheckBox                 m_chkClmDate;
    JCheckBox                 m_chkClmTime;
    JCheckBox                 m_chkClmGap;
    JCheckBox                 m_chkClmLogLV;
    JCheckBox                 m_chkClmPid;
    JCheckBox                 m_chkClmThread;
    JCheckBox                 m_chkClmTag;
    JCheckBox                 m_chkClmMessage;
    JCheckBox                 m_chkClmTimeUs;
    JCheckBox                 m_chkClmTimeStamp;

    JTextField                m_tfFontSize;
//    JTextField                  m_tfProcessCmd;
    JComboBox                 m_comboEncode;
    JComboBox                 m_jcFontType;
    JComboBox                 m_jcBookmarkColor; //jinube
    JButton                   m_btnGetPS;
    JButton                   m_btnClear;
    JButton                   m_btnRun;
    JToggleButton             m_tbtnPause;
    JButton                   m_btnStop;

    String                    m_strLogFileName;
    String                    m_strLogDirName;

    String                    m_strSelectedDevice;
//    String                      m_strProcessCmd;
    Process                   m_Process;
    Thread                    m_thProcess;
    Thread                    m_thWatchFile;
    Thread                    m_thFilterParse;
    boolean                   m_bPauseADB;
    long                      m_nSetStatusTime;

    //TimeGap Mark
    JButton                   m_btnTimeGapLess;
    JButton                   m_btnTimeGapMore;
    JTextField                m_tfTimeGap;

    JButton                   m_tbtnExplorer;

    JButton                   m_btnFilterOption;
    JButton                   m_btnSettingOption;
    JButton                   m_btnDeviceSelect;
    JButton                   m_btnCommand;
    JButton                   m_btnMark;
    JButton                   m_btnUnmark;
    JButton                   m_btnUnmarkAll;
    JButton                   m_btnScroll;
    JButton                   m_btnSync;
    JButton                   m_btnSave;

    Component                   m_comFilterPanel;
    Component                   m_comSettingPanel;
    Component                   m_comDeviceSelectPanel;
    Component                   m_comSendCommandPanel;

    Component                   m_comComportSetPanel;

    Object                    FILE_LOCK;
    Object                    FILTER_LOCK;
    volatile int              m_nChangedFilter;
    int                       m_nFilterLogLV = LogInfo.LOG_LV_ALL;
    static int                m_nWinWidth  = DEFAULT_WIDTH;
    static int                m_nWinHeight = DEFAULT_HEIGHT;
    int                       m_nLastWidth;
    int                       m_nLastHeight;
    static int                m_nWindState;
    static RecentFileMenu     m_recentMenu;
    static RecentFileMenu     m_st_recentMenu;
//    String                    m_strLastDir;

    int                       m_nAddCount=0;
    static int                m_nAutoClearNum;
    JComboBox                 m_jcGoto;

    JComboBox                 m_jcAutoClearNum;
    ProcessInfoDialog          m_thPSDialog;
    BroadcastServer           m_broadcastServer;
    SerialPort                m_serialPort;

    static int                m_nComportSetSpeed;
    JComboBox                 m_jcComportSetSpeed;

    RecentFileMenu            m_rfmSendCommand;
    InputStream               m_serialInput;
    OutputStream              m_serialOutput;

    static final int FILTER_0 = 0; //FILTER_FINDWORD    = 0;
    static final int FILTER_1 = 1; //FILTER_REMOVEWORD  = 1;
    static final int FILTER_2 = 2; //FILTER_SHOWPID     = 2;
    static final int FILTER_3 = 3; //FILTER_SHOWTID     = 3;
    static final int FILTER_4 = 4; //FILTER_SHOWTAG     = 4;
    static final int FILTER_5 = 5; //FILTER_REMOVETAG   = 5;

    static final int FILTER_6 = 6; //FILTER_HIGHLIGHT   = 6;
    static final int FILTER_7 = 7; //FILTER_AUTOPAUSE   = 7;

    static final int MAX_DEF_FILTER    = 6;
    static final int MAX_FILTER        = 8;

    String  keywordStr[] = {
        "FindWord",
        "RemoveWord",
        "ShowPid",
        "ShowTid",
        "ShowTag",
        "RemoveTag",
        "Highlight",
        "AutoPause"
    };

    //Log filter enable/disable
    JPanel          keywordPanel[]      = {null, null, null, null, null, null, null, null, null, null};
    JLabel          keywordLabel[]      = {null, null, null, null, null, null, null, null, null, null};
    JCheckBox       keywordCheck[]      = {null, null, null, null, null, null, null, null, null, null};
    JCheckBox       keywordCSCheck[]    = {null, null, null, null, null, null, null, null, null, null};
//    JTextField      keywordText[]     = {null, null, null, null, null, null, null, null, null, null};
    JComboBox       keywordCombo[]      = {null, null, null, null, null, null, null, null, null, null};
    RecentFileMenu  keywordRecent[]     = {null, null, null, null, null, null, null, null, null, null};

    String          keywordString[] = {
        "        Find : ",
        "Remove : ",
        "         Pid : ",
        "         Tid : ",
        "     Show : ",
        "Remove : ",
        "Highlight : ",
        "AutoPause : ",
    };

    public static void main(final String args[])
    {
        new lc();

//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        final LogFilterMain mainFrame = new LogFilterMain();
        mainFrame = new LogFilterMain();
        mainFrame.setTitle(LOGFILTER + " " + VERSION);
//        mainFrame.addWindowListener(new WindowEventHandler());

        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem fileOpen = new JMenuItem("Open");
        fileOpen.setMnemonic(KeyEvent.VK_O);
        fileOpen.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.ALT_MASK) );
        fileOpen.setToolTipText("Open log file");
        fileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mainFrame.openFileBrowser();
            }
        });

        m_recentMenu = new RecentFileMenu("RecentFile",30, null, 0){
            public void onSelectFile(int type, String filePath){
                mainFrame.parseFile(new File(filePath), 0);
            }
        };

        JMenuItem fileStreamOpen = new JMenuItem("Open Stream");
//        fileStreamOpen.setMnemonic(KeyEvent.VK_S);
//        fileStreamOpen.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S,
//                ActionEvent.ALT_MASK) );
        fileStreamOpen.setToolTipText("Open stream log file");
        fileStreamOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mainFrame.openStreamFileBrowser();
            }
        });

        m_st_recentMenu = new RecentFileMenu("RecentStreamFile",30, null, 0){
            public void onSelectFile(int type, String filePath){
                mainFrame.openStreamFileBrowser(filePath);
            }
        };

        JMenuItem fileExplorer = new JMenuItem("Explorer");
        fileExplorer.setMnemonic(KeyEvent.VK_E);
        fileExplorer.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_E,
                ActionEvent.ALT_MASK) );
        fileExplorer.setToolTipText("Log File 경로에서 탐색기 실행");
        fileExplorer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mainFrame.openExplorer();
            }
        });

        JMenuItem fileSave = new JMenuItem("Save");
        fileSave.setMnemonic(KeyEvent.VK_S);
        fileSave.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.ALT_MASK) );
        fileSave.setToolTipText("Marked Log를 파일로 저장");
        fileSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mainFrame.saveFileBrowser();
            }
        });

        file.add(fileOpen);
        file.add(m_recentMenu);
        file.add(fileStreamOpen);
        file.add(m_st_recentMenu);
        file.add(fileExplorer);
        file.add(fileSave);

        menubar.add(file);

        JMenu help = new JMenu("Help");

        JMenuItem helpOpen = new JMenuItem("Help");
        helpOpen.setMnemonic(KeyEvent.VK_H);
        helpOpen.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_H,
                ActionEvent.ALT_MASK) );

        helpOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              new Help();
            }
        });

        help.add(helpOpen);
        menubar.add(help);

        JMenu option = new JMenu("Option");

        JMenuItem optFilter = new JMenuItem("Filter");
        optFilter.setMnemonic(KeyEvent.VK_F);
        optFilter.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_F,
                ActionEvent.CTRL_MASK) );

        optFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              mainFrame.setFindFocus();
            }
        });
        option.add(optFilter);

        JMenuItem optSetting = new JMenuItem("Setting");
        optSetting.setMnemonic(KeyEvent.VK_S);
        optSetting.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK) );

        optSetting.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              mainFrame.openOptionDlgSetting();
            }
        });
        option.add(optSetting);

        JMenuItem optDevice = new JMenuItem("Device");
        optDevice.setMnemonic(KeyEvent.VK_D);
        optDevice.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_D,
                ActionEvent.CTRL_MASK) );

        optDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              mainFrame.openOptionDlgDevice();
            }
        });
        option.add(optDevice);
        menubar.add(option);

        mainFrame.setJMenuBar(menubar);

//        if(m_nWindState == JFrame.MAXIMIZED_BOTH)
//        else
            mainFrame.setSize(m_nWinWidth, m_nWinHeight);
            mainFrame.setExtendedState( m_nWindState );
        mainFrame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

        if(args != null && args.length > 0)
        {
            EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    if(args.length > 1)
                      mainFrame.parseFile(new File(args[0]), Integer.parseInt(args[1]));
                    else
                      mainFrame.parseFile(new File(args[0]), 0);
                }
            });
        }
    }

    String makeFilename()
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "LogFilter_" + format.format(now) + ".txt";
    }

    String makeFilename(String prefix)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return prefix.trim() + "_" + format.format(now) + ".txt";
    }

    void exit()
    {
        if(m_Process != null) m_Process.destroy();
        if(m_thProcess != null) m_thProcess.interrupt();
        if(m_thWatchFile != null) m_thWatchFile.interrupt();
        if(m_thFilterParse != null) m_thFilterParse.interrupt();

        saveFilter();
        saveColor();
        System.exit(0);
    }

    /**
     * @throws HeadlessException
     */
    public LogFilterMain()
    {
        super();
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                exit();
            }

            public void windowClosed(WindowEvent e) {
                //This will only be seen on standard output.
//                System.out.println("WindowListener method called: windowClosed.");
            }

            public void windowOpened(WindowEvent e) {
//                System.out.println("WindowListener method called: windowOpened.");
            }

            public void windowIconified(WindowEvent e) {
//                System.out.println("WindowListener method called: windowIconified.");
                if(m_jdFilterDialog != null) {
                    m_bFilterDialog = m_jdFilterDialog.isVisible();
                    m_jdFilterDialog.setVisible(false);
                }
                if(m_jdSettingDialog != null) {
                    m_bSettingDialog = m_jdSettingDialog.isVisible();
                    m_jdSettingDialog.setVisible(false);
                }
                if(m_jdDeviceSelectDialog != null) {
                    m_bDeviceSelectDialog = m_jdDeviceSelectDialog.isVisible();
                    m_jdDeviceSelectDialog.setVisible(false);
                }
                if(m_jdSendCommandDialog != null) {
                    m_bSendCommandDialog = m_jdSendCommandDialog.isVisible();
                    m_jdSendCommandDialog.setVisible(false);
                }
            }

            public void windowDeiconified(WindowEvent e) {
//                System.out.println("WindowListener method called: windowDeiconified.");
                if(m_jdFilterDialog != null) {
                    if(m_bFilterDialog)
                        m_jdFilterDialog.setVisible(true);
                }
                if(m_jdSettingDialog != null) {
                    if(m_bSettingDialog)
                        m_jdSettingDialog.setVisible(true);
                }
                if(m_jdDeviceSelectDialog != null) {
                    if(m_bDeviceSelectDialog)
                        m_jdDeviceSelectDialog.setVisible(true);
                }
                if(m_jdSendCommandDialog != null) {
                    if(m_bSendCommandDialog)
                        m_jdSendCommandDialog.setVisible(true);
                }
            }

            public void windowActivated(WindowEvent e) {
//                System.out.println("WindowListener method called: windowActivated.");
            }

            public void windowDeactivated(WindowEvent e) {
//                System.out.println("WindowListener method called: windowDeactivated.");
            }

            public void windowGainedFocus(WindowEvent e) {
//                System.out.println("WindowFocusListener method called: windowGainedFocus.");
            }

            public void windowLostFocus(WindowEvent e) {
//                System.out.println("WindowFocusListener method called: windowLostFocus.");
            }

            public void windowStateChanged(WindowEvent e) {
//                System.out.println("WindowStateListener method called: windowStateChanged." + e);
            }
        });
        initValue();
        createComponent();

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());

//        pane.add(getControlPanel(), BorderLayout.NORTH);
        pane.add(getCommandPanel(), BorderLayout.NORTH);
        pane.add(getBookmarkPanel(), BorderLayout.WEST);
        pane.add(getTabPanel(), BorderLayout.CENTER);
        pane.add(getInfoPanel(), BorderLayout.SOUTH);

        try {
            m_comComportSetPanel = getComportSetPanel();

            m_jdFilterDialog = newOptionDialog("Filter", m_comFilterPanel);
            m_jdFilterDialog.setVisible(false);
            m_bFilterDialog = m_jdFilterDialog.isVisible();

            m_jdSettingDialog = newOptionDialog("Setting", m_comSettingPanel);
            m_jdSettingDialog.setVisible(false);
            m_bSettingDialog = m_jdSettingDialog.isVisible();

            m_jdDeviceSelectDialog = newOptionDialog("Device Select", m_comDeviceSelectPanel);
            m_jdDeviceSelectDialog.setVisible(false);
            m_bDeviceSelectDialog = m_jdDeviceSelectDialog.isVisible();

            m_jdSendCommandDialog = newOptionDialog("Command", m_comSendCommandPanel);
            m_jdSendCommandDialog.setVisible(false);
            m_bSendCommandDialog = m_jdSendCommandDialog.isVisible();
        } catch(Exception ee) {
            T.e("e = " + ee);
        }

        setDnDListener();
        addChangeListener();
        startFilterParse();

        setVisible(true);
        addDesc();
        loadFilter();
        loadColor();
        loadCmd();

        m_tbLogTable.setColumnWidth();

        runMultiCastThread();
    }

    final String INI_FILE           = "LogFilter.ini";
    final String INI_FILE_CMD       = "LogFilterCmd.ini";
    final String INI_FILE_COLOR     = "LogFilterColor.ini";
    final String INI_LAST_DIR       = "LAST_DIR";
    final String INI_CMD_COUNT      = "CMD_COUNT";
    final String INI_CMD_APP        = "CMD_APP";
    final String INI_CMD            = "CMD_";
    final String INI_FONT_TYPE      = "FONT_TYPE";
    final String INI_COLOR_0        = "INI_COLOR_0";
    final String INI_COLOR_1        = "INI_COLOR_1";
    final String INI_COLOR_2        = "INI_COLOR_2";
    final String INI_COLOR_3        = "INI_COLOR_3(E)";
    final String INI_COLOR_4        = "INI_COLOR_4(W)";
    final String INI_COLOR_5        = "INI_COLOR_5";
    final String INI_COLOR_6        = "INI_COLOR_6(I)";
    final String INI_COLOR_7        = "INI_COLOR_7(D)";
    final String INI_COLOR_8        = "INI_COLOR_8(F)";
    final String INI_COLOR_9        = "INI_COLOR_9(V)";
    final String INI_HIGHLIGHT_COUNT= "INI_HIGHLIGHT_COUNT";
    final String INI_HIGHLIGHT_=    "INI_HIGHLIGHT_";
    final String INI_WIDTH          = "INI_WIDTH";
    final String INI_HEIGHT         = "INI_HEIGHT";
    final String INI_WINDOW_STATE   = "INI_WINDOW_STATE";

    final String BOOKMARK_COLOR1   = "BOOKMARK_STYLE1";
    final String BOOKMARK_COLOR2   = "BOOKMARK_STYLE2";
    final String BOOKMARK_COLOR3   = "BOOKMARK_STYLE3";
    final String BOOKMARK_COLOR4   = "BOOKMARK_STYLE4";
    final String BOOKMARK_COLOR5   = "BOOKMARK_STYLE5";
//    final String BOOKMARK_COLOR6   = "BOOKMARK_STYLE6";
//    final String BOOKMARK_COLOR7   = "BOOKMARK_STYLE7";
//    final String BOOKMARK_COLOR8   = "BOOKMARK_STYLE8";
//    final String BOOKMARK_COLOR9   = "BOOKMARK_STYLE9";

    final String INI_COMUMN         = "INI_COMUMN_";

    void loadCmd()
    {
        try
        {
            Properties p = new Properties();

            // ini 파일 읽기
            p.load(new FileInputStream(INI_FILE_CMD));

//            T.d("p.getProperty(INI_CMD_COUNT) = " + p.getProperty(INI_CMD_COUNT));
            String cmd_app = p.getProperty(INI_CMD_APP);

            ANDROID_DEFAULT_CMD_FIRST   = cmd_app + " ";            
            ANDROID_SELECTED_CMD_FIRST  = ANDROID_DEFAULT_CMD_FIRST + "-s ";
            DEVICES_CMD[0]              = ANDROID_DEFAULT_CMD_FIRST + "devices";

//            System.out.println("APP = " + ANDROID_DEFAULT_CMD_FIRST);
//            System.out.println("SEL = " + ANDROID_SELECTED_CMD_FIRST);
//            System.out.println("DEV = " + DEVICES_CMD[0]);
            
            int nCount = Integer.parseInt(p.getProperty(INI_CMD_COUNT));
//            T.d("nCount = " + nCount);
            for(int nIndex = 0; nIndex < nCount; nIndex++)
            {
//                T.d("CMD = " + INI_CMD + nIndex);
                m_jcCommand.addItem(p.getProperty(INI_CMD + nIndex));
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    void loadColor()
    {
        try
        {
            Properties p = new Properties();

            p.load(new FileInputStream(getINIPath(INI_FILE_COLOR)));

            LogColor.COLOR_0 = Integer.parseInt(p.getProperty(INI_COLOR_0).replace("0x", ""), 16);
            LogColor.COLOR_1 = Integer.parseInt(p.getProperty(INI_COLOR_1).replace("0x", ""), 16);
            LogColor.COLOR_2 = Integer.parseInt(p.getProperty(INI_COLOR_2).replace("0x", ""), 16);
            LogColor.COLOR_ERROR = LogColor.COLOR_3 = Integer.parseInt(p.getProperty(INI_COLOR_3).replace("0x", ""), 16);
            LogColor.COLOR_WARN  = LogColor.COLOR_4 = Integer.parseInt(p.getProperty(INI_COLOR_4).replace("0x", ""), 16);
            LogColor.COLOR_5 = Integer.parseInt(p.getProperty(INI_COLOR_5).replace("0x", ""), 16);
            LogColor.COLOR_INFO     = LogColor.COLOR_6 = Integer.parseInt(p.getProperty(INI_COLOR_6).replace("0x", ""), 16);
            LogColor.COLOR_DEBUG    = LogColor.COLOR_7 = Integer.parseInt(p.getProperty(INI_COLOR_7).replace("0x", ""), 16);
            LogColor.COLOR_FATAL    = LogColor.COLOR_8 = Integer.parseInt(p.getProperty(INI_COLOR_8).replace("0x", ""), 16);
            LogColor.COLOR_VERBOSE  = LogColor.COLOR_9 = Integer.parseInt(p.getProperty(INI_COLOR_9).replace("0x", ""), 16);

            LogColor.BOOKMARK_COLOR1 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR1).replace("0x", ""), 16);
            LogColor.BOOKMARK_COLOR2 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR2).replace("0x", ""), 16);
            LogColor.BOOKMARK_COLOR3 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR3).replace("0x", ""), 16);
            LogColor.BOOKMARK_COLOR4 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR4).replace("0x", ""), 16);
            LogColor.BOOKMARK_COLOR5 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR5).replace("0x", ""), 16);
//            LogColor.BOOKMARK_COLOR6 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR6).replace("0x", ""), 16);
//            LogColor.BOOKMARK_COLOR7 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR7).replace("0x", ""), 16);
//            LogColor.BOOKMARK_COLOR8 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR8).replace("0x", ""), 16);
//            LogColor.BOOKMARK_COLOR9 = Integer.parseInt(p.getProperty(BOOKMARK_COLOR9).replace("0x", ""), 16);

            LogColor.COLOR_BOOKMARK = LogColor.BOOKMARK_COLOR1;

            int nCount = Integer.parseInt(p.getProperty( INI_HIGHLIGHT_COUNT, "0" ));
            if(nCount > 0)
            {
                LogColor.COLOR_HIGHLIGHT = new String[nCount];
                for(int nIndex = 0; nIndex < nCount; nIndex++)
                    LogColor.COLOR_HIGHLIGHT[nIndex] = p.getProperty(INI_HIGHLIGHT_ + nIndex).replace("0x", "");
            }
            else
            {
                LogColor.COLOR_HIGHLIGHT = new String[1];
                LogColor.COLOR_HIGHLIGHT[0] = "00BF00";
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    void saveColor()
    {
        try
        {
            Properties p = new Properties();

            p.setProperty(INI_COLOR_0, "0x" + Integer.toHexString(LogColor.COLOR_0).toUpperCase());
            p.setProperty(INI_COLOR_1, "0x" + Integer.toHexString(LogColor.COLOR_1).toUpperCase());
            p.setProperty(INI_COLOR_2, "0x" + Integer.toHexString(LogColor.COLOR_2).toUpperCase());
            p.setProperty(INI_COLOR_3, "0x" + Integer.toHexString(LogColor.COLOR_3).toUpperCase());
            p.setProperty(INI_COLOR_4, "0x" + Integer.toHexString(LogColor.COLOR_4).toUpperCase());
            p.setProperty(INI_COLOR_5, "0x" + Integer.toHexString(LogColor.COLOR_5).toUpperCase());
            p.setProperty(INI_COLOR_6, "0x" + Integer.toHexString(LogColor.COLOR_6).toUpperCase());
            p.setProperty(INI_COLOR_7, "0x" + Integer.toHexString(LogColor.COLOR_7).toUpperCase());
            p.setProperty(INI_COLOR_8, "0x" + Integer.toHexString(LogColor.COLOR_8).toUpperCase());
            p.setProperty(INI_COLOR_9, "0x" + Integer.toHexString(LogColor.COLOR_9).toUpperCase());

            p.setProperty(BOOKMARK_COLOR1, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR1).toUpperCase());
            p.setProperty(BOOKMARK_COLOR2, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR2).toUpperCase());
            p.setProperty(BOOKMARK_COLOR3, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR3).toUpperCase());
            p.setProperty(BOOKMARK_COLOR4, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR4).toUpperCase());
            p.setProperty(BOOKMARK_COLOR5, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR5).toUpperCase());
//            p.setProperty(BOOKMARK_COLOR6, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR6).toUpperCase());
//            p.setProperty(BOOKMARK_COLOR7, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR7).toUpperCase());
//            p.setProperty(BOOKMARK_COLOR8, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR8).toUpperCase());
//            p.setProperty(BOOKMARK_COLOR9, "0x" + Integer.toHexString(LogColor.BOOKMARK_COLOR9).toUpperCase());

            if(LogColor.COLOR_HIGHLIGHT != null)
            {
                p.setProperty(INI_HIGHLIGHT_COUNT, "" + LogColor.COLOR_HIGHLIGHT.length);
                for(int nIndex = 0; nIndex < LogColor.COLOR_HIGHLIGHT.length; nIndex++)
                    p.setProperty(INI_HIGHLIGHT_ + nIndex, "0x" + LogColor.COLOR_HIGHLIGHT[nIndex].toUpperCase());
            }

            p.store( new FileOutputStream(getINIPath(INI_FILE_COLOR)), "done.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    String getINIPath(String name)
    {
        String saveDir = "ini";
		//figure out the name of the recent file
		String pathToSavedFile=System.getProperty("user.dir");
		if((pathToSavedFile==null)||(pathToSavedFile.length()<=0)){
			pathToSavedFile = name;
		} else {
            if(pathToSavedFile.endsWith(File.separator)) {
    			pathToSavedFile=pathToSavedFile+saveDir;
    		} else{
    			pathToSavedFile=pathToSavedFile+File.separator+saveDir;
    		}
//            System.out.println("dirPath : " + pathToSavedFile);

            File theDir = new File(pathToSavedFile);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                try{
                    theDir.mkdir();
                }
                catch(SecurityException se){
                  se.printStackTrace();
                  T.e(se);
                    //handle it
                }
            }

            pathToSavedFile=pathToSavedFile+File.separator+name;
        }
        return pathToSavedFile;
    }

    void loadFilter()
    {
        try
        {
            Properties p = new Properties();

            // ini 파일 읽기
            p.load(new FileInputStream(getINIPath(INI_FILE)));

            // Key 값 읽기
            String strFontType = p.getProperty(INI_FONT_TYPE);
            if(strFontType != null && strFontType.length() > 0)
                m_jcFontType.setSelectedItem(p.getProperty(INI_FONT_TYPE));

            m_nWinWidth  = Integer.parseInt( p.getProperty( INI_WIDTH ));
            m_nWinHeight = Integer.parseInt( p.getProperty( INI_HEIGHT ));
            m_nWindState = Integer.parseInt( p.getProperty( INI_WINDOW_STATE ));

            for(int nIndex = 0; nIndex < LogFilterTableModel.COMUMN_MAX; nIndex++)
            {
                LogFilterTableModel.setColumnWidth( nIndex, Integer.parseInt( p.getProperty( INI_COMUMN + nIndex) ) );
            }

            for(int filter=0; filter < MAX_FILTER; filter++)
            {
              if(keywordCombo[filter].getItemCount() > 0)
              {
//                System.out.println("idx= " + filter + " cnt= " + keywordCombo[filter].getItemCount());
                keywordCombo[filter].setSelectedIndex(0);
//                keywordText[filter].setText  ((String)keywordCombo[filter].getItemAt(0));
              }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    void saveFilter()
    {
        try
        {
            m_nWinWidth  = m_nLastWidth;
            m_nWinHeight = m_nLastHeight;
            m_nWindState = getExtendedState();
//            T.d("m_nWindState = " + m_nWindState);

            Properties p = new Properties();
//            p.setProperty( INI_LAST_DIR, m_strLastDir );
            p.setProperty(INI_FONT_TYPE,   (String)m_jcFontType.getSelectedItem());

            p.setProperty(INI_WIDTH,       "" + m_nWinWidth);
            p.setProperty(INI_HEIGHT,      "" + m_nWinHeight);
            p.setProperty(INI_WINDOW_STATE,"" + m_nWindState);

            for(int nIndex = 0; nIndex < LogFilterTableModel.COMUMN_MAX; nIndex++)
            {
                p.setProperty(INI_COMUMN + nIndex, "" + m_tbLogTable.getColumnWidth(nIndex));
            }
            p.store( new FileOutputStream(getINIPath(INI_FILE)), "done.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    void addDesc(String strMessage)
    {
        LogInfo logInfo = new LogInfo();
        logInfo.m_strLine = "" + (m_arLogInfoAll.size() + 1);
        logInfo.m_strMessage = strMessage;
        m_arLogInfoAll.add(logInfo);
    }

    void addDesc()
    {
        addDesc(VERSION);
        addDesc("");
        addDesc("Version 1.8 : java -jar LogFilter_xx.jar [filename] [line]추가");
        addDesc("Version 1.7 : copy시 보이는 column만 clipboard에 복사(Line 제외)");
        addDesc("Version 1.6 : cmd콤보박스 길이 고정");
        addDesc("Version 1.5 : Highlight color list추가()");
        addDesc("   - LogFilterColor.ini 에 카운트와 값 넣어 주시면 됩니다.");
        addDesc("   - ex)INI_HIGHLIGHT_COUNT=2");
        addDesc("   -    INI_COLOR_HIGHLIGHT_0=0xFFFF");
        addDesc("   -    INI_COLOR_HIGHLIGHT_1=0x00FF");
        addDesc("Version 1.4 : 창크기 저장");
        addDesc("Version 1.3 : recent file 및 open메뉴추가");
        addDesc("Version 1.2 : Tid 필터 추가");
        addDesc("Version 1.1 : Level F 추가");
        addDesc("Version 1.0 : Pid filter 추가");
        addDesc("Version 0.9 : Font type 추가");
        addDesc("Version 0.8 : 필터체크 박스 추가");
        addDesc("Version 0.7 : 커널로그 파싱/LogFilter.ini에 컬러정의(0~7)");
        addDesc("Version 0.6 : 필터 대소문 무시");
        addDesc("Version 0.5 : 명령어 ini파일로 저장");
        addDesc("Version 0.4 : add thread option, filter 저장");
        addDesc("Version 0.3 : 단말 선택 안되는 문제 수정");
        addDesc("");
        addDesc("[Tag]");
        addDesc("Alt+L/R Click : Show/Remove tag");
        addDesc("");
        addDesc("[Bookmark]");
        addDesc("Ctrl+F2/double click: bookmark toggle");
        addDesc("F2 : pre bookmark");
        addDesc("F3 : next bookmark");
        addDesc("");
        addDesc("[Copy]");
        addDesc("Ctrl+c : row copy");
        addDesc("right click : cloumn copy");
        addDesc("");
        addDesc("[New version]");
        addDesc("http://blog.naver.com/iookill/140135139931");
    }

    /**
     * @param nIndex    실제 리스트의 인덱스
     * @param nLine     m_strLine
     * @param bBookmark
     */
    void bookmarkItem(int nIndex, int nLine, boolean bBookmark)
    {
        synchronized(FILTER_LOCK)
        {
            LogInfo logInfo = m_arLogInfoAll.get(nLine);
            logInfo.m_bMarked = bBookmark;
            m_arLogInfoAll.set(nLine, logInfo);

            if(logInfo.m_bMarked)
            {
                m_hmBookmarkAll.put(nLine, nLine);
                if(m_bUserFilter)
                    m_hmBookmarkFiltered.put(nLine, nIndex);
            }
            else
            {
                m_hmBookmarkAll.remove(nLine);
                if(m_bUserFilter)
                    m_hmBookmarkFiltered.remove(nLine);
            }
        }
        m_ipIndicator.repaint();
    }

    void clearData()
    {
//        m_arTagInfo.clear();
        m_arAMProcInfo.clear();
        m_arLogInfoAll.clear();
        m_arLogInfoFiltered.clear();
        m_hmBookmarkAll.clear();
        m_hmBookmarkFiltered.clear();
        m_hmErrorAll.clear();
        m_hmErrorFiltered.clear();
        System.gc();
    }

    void createComponent()
    {
    }

    Component getPanelDeviceSelect()
    {
        JPanel jpOptionDevice = new JPanel();
        jpOptionDevice.setBorder(BorderFactory.createTitledBorder("Device select"));
        jpOptionDevice.setLayout(new BorderLayout());
//        jpOptionDevice.setPreferredSize(new Dimension(200, 100));

        JPanel jpCmd = new JPanel();
        m_jcDeviceType = new JComboBox();
        m_jcDeviceType.addItem(COMBO_ANDROID);
        m_jcDeviceType.addItem(COMBO_UART);
        DEVICES_CUSTOM = m_jcDeviceType.getItemCount();
        m_jcDeviceType.setRenderer(new ComboBoxRenderer());
//        m_jcDeviceType.addItem(COMBO_IOS);
//        m_jcDeviceType.addItem(CUSTOM_COMMAND);
        m_jcDeviceType.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() != ItemEvent.SELECTED) return;

                DefaultListModel listModel = (DefaultListModel)m_lDeviceList.getModel();
                listModel.clear();
                if (e.getItem().equals(COMBO_CUSTOM_COMMAND)) {
                    m_jcDeviceType.setEditable(true);
                } else {
                    m_jcDeviceType.setEditable(false);
                }
                setProcessCmd((String)m_jcDeviceType.getSelectedItem(), m_strSelectedDevice);
            }
        });

        final DefaultListModel listModel = new DefaultListModel();
        m_btnDevice = new JButton("Find");
        m_btnDevice.setMargin(new Insets(0, 0, 0, 0));
        m_btnDevice.addActionListener(m_alButtonListener);

        jpCmd.add(m_jcDeviceType);
        jpCmd.add(m_btnDevice);

        jpOptionDevice.add(jpCmd, BorderLayout.NORTH);

        m_lDeviceList = new JList(listModel);
        JScrollPane vbar = new JScrollPane(m_lDeviceList);
        vbar.setPreferredSize(new Dimension(100,50));
        m_lDeviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_lDeviceList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                JList deviceList = (JList)e.getSource();
                Object selectedItem = (Object)deviceList.getSelectedValue();
                m_strSelectedDevice = "";
                if(selectedItem != null)
                {
                    m_strSelectedDevice = selectedItem.toString();
                    m_strSelectedDevice = m_strSelectedDevice.replace("\t", " ").replace("device", "").replace("offline", "");
                    setProcessCmd((String)m_jcDeviceType.getSelectedItem(), m_strSelectedDevice);
                }
            }
        });
        jpOptionDevice.add(vbar);

        return jpOptionDevice;
    }

    Component getPanelSendCommand()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));
//        jpMain.setBorder(BorderFactory.createTitledBorder("Command"));

        m_comboSendCommand    = new JComboBox();
        m_comboSendCommand.setEditable(true);
        m_comboSendCommand.addActionListener(m_alComboListener);
        m_comboSendCommand.setPreferredSize( new Dimension(300, 25) );

        m_comboSendCommand.setMaximumRowCount(30);
        m_comboSendCommand.setAutoscrolls(true);
        m_comboSendCommand.setRenderer(new ComboBoxRenderer());
        ((JTextComponent)(m_comboSendCommand.getEditor().getEditorComponent())).addKeyListener(m_klCommandCodeListener);

        m_rfmSendCommand  = new RecentFileMenu("Recent" + "SendCommand", 30, m_comboSendCommand, 0xFF) {
            public void onSelectFile(int type, String filePath){
//                    keywordText[type].setText(filePath);
            }
        };

        m_comboSendCommand.insertItemAt("pkill mediaserver & pkill audioserver", 0);
        m_comboSendCommand.insertItemAt("ps | grep logcat",  0);
        m_comboSendCommand.insertItemAt("logcat -b radio -v threadtime -v usec & logcat -b events -v threadtime -v usec & logcat -b main -v threadtime -v usec", 0);
//        m_comboSendCommand.setSelectedIndex(0);

        JButton btnSendCommand = new JButton("SEND");
        btnSendCommand.setMargin(new Insets(0, 0, 0, 0));
        btnSendCommand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                m_tbLogTable.changeSelection(m_tbLogTable.getRowCount(), 0, false, false);
                uartSendCommand((String)m_comboSendCommand.getSelectedItem());
                if(m_chkSendCommandClear.isSelected()) {
                    m_comboSendCommand.setSelectedItem("");
                }
            }
        });

        JButton btnETX = new JButton("ETX");
        btnETX.setMargin(new Insets(0, 0, 0, 0));
        btnETX.setToolTipText("End of text");
        btnETX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                uartSendCommand((byte)0x03); //End of text
            }
        });

        JButton btnCAN = new JButton("CAN");
        btnCAN.setMargin(new Insets(0, 0, 0, 0));
        btnCAN.setToolTipText("Cancel");
        btnCAN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                uartSendCommand((byte)0x18); //Cancel
            }
        });

        m_chkSendCommandClear   = new JCheckBox();
        m_chkSendCommandClear.setSelected(true);
        m_chkSendCommandClear.setEnabled(true);
        m_chkSendCommandClear.setToolTipText("전송 후 입력 창 초기화");

        jpMain.add(btnETX);
        jpMain.add(btnCAN);

        jpMain.add(m_chkSendCommandClear);
        jpMain.add(m_comboSendCommand);
        jpMain.add(btnSendCommand);

        return jpMain;
    }

    Component getPanelLogLevelSet()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.Y_AXIS));
//        jpMain.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        jpMain.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        jpMain.setBorder(BorderFactory.createTitledBorder("Level"));
        jpMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        m_chkVerbose    = new JCheckBox();
        m_chkDebug      = new JCheckBox();
        m_chkInfo       = new JCheckBox();
        m_chkWarn       = new JCheckBox();
        m_chkError      = new JCheckBox();
        m_chkFatal      = new JCheckBox();

        m_chkVerbose.setText("Verbose");
        m_chkVerbose.setSelected(true);
        m_chkDebug.setText("Debug");
        m_chkDebug.setSelected(true);
        m_chkInfo.setText("Info");
        m_chkInfo.setSelected(true);
        m_chkWarn.setText("Warn");
        m_chkWarn.setSelected(true);
        m_chkError.setText("Error");
        m_chkError.setSelected(true);
        m_chkFatal.setText("Fatal");
        m_chkFatal.setSelected(true);

        m_chkVerbose.addItemListener(m_itemListener);
        m_chkDebug.addItemListener(m_itemListener);
        m_chkInfo.addItemListener(m_itemListener);
        m_chkWarn.addItemListener(m_itemListener);
        m_chkError.addItemListener(m_itemListener);
        m_chkFatal.addItemListener(m_itemListener);

        JPanel jpLine0 = new JPanel(new BorderLayout());
        jpLine0.setLayout(new BoxLayout(jpLine0, BoxLayout.X_AXIS));
        jpLine0.setAlignmentX(Component.LEFT_ALIGNMENT);
        jpLine0.add(m_chkVerbose);
        jpLine0.add(m_chkDebug);
        jpLine0.add(m_chkInfo);

        JPanel jpLine1 = new JPanel(new BorderLayout());
        jpLine1.setLayout(new BoxLayout(jpLine1, BoxLayout.X_AXIS));
        jpLine1.setAlignmentX(Component.LEFT_ALIGNMENT);
        jpLine1.add(m_chkWarn);
        jpLine1.add(m_chkError);
        jpLine1.add(m_chkFatal);

        jpMain.add(jpLine0);
        jpMain.add(jpLine1);

        return jpMain;
    }

    Component getPanelColumnSet()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.Y_AXIS));
//        jpMain.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        jpMain.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        jpMain.setBorder(BorderFactory.createTitledBorder("Column"));
        jpMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        m_chkClmBookmark  = new JCheckBox();
        m_chkClmGap       = new JCheckBox();
        m_chkClmLine      = new JCheckBox();
        m_chkClmDate      = new JCheckBox();
        m_chkClmTime      = new JCheckBox();
        m_chkClmLogLV     = new JCheckBox();
        m_chkClmPid       = new JCheckBox();
        m_chkClmThread    = new JCheckBox();
        m_chkClmTag       = new JCheckBox();
        m_chkClmMessage   = new JCheckBox();
        m_chkClmTimeUs    = new JCheckBox();
        m_chkClmTimeStamp = new JCheckBox();

        m_chkClmBookmark.setText("Comment");
        m_chkClmBookmark.setToolTipText("User Comment");
        m_chkClmGap.setText("TimeGap");
        m_chkClmGap.setToolTipText("TimeGap");
        m_chkClmLine.setText("Line");
        m_chkClmLine.setSelected(true);
        m_chkClmDate.setText("Date");
        m_chkClmDate.setSelected(true);
        m_chkClmTime.setText("Time");
        m_chkClmTime.setSelected(true);
        m_chkClmLogLV.setText("LogLV");
        m_chkClmLogLV.setSelected(true);
        m_chkClmPid.setText("Pid");
        m_chkClmPid.setSelected(true);
        m_chkClmThread.setText("Thread");
        m_chkClmThread.setSelected(true);
        m_chkClmTag.setText("Tag");
        m_chkClmTag.setSelected(true);
        m_chkClmMessage.setText("Msg");
        m_chkClmMessage.setSelected(true);
        m_chkClmTimeUs.setText("TimeUs");
        m_chkClmTimeStamp.setText("TimeStamp");

        m_chkClmBookmark.addItemListener(m_itemListener);
        m_chkClmGap.addItemListener(m_itemListener);
        m_chkClmLine.addItemListener(m_itemListener);
        m_chkClmDate.addItemListener(m_itemListener);
        m_chkClmTime.addItemListener(m_itemListener);
        m_chkClmLogLV.addItemListener(m_itemListener);
        m_chkClmPid.addItemListener(m_itemListener);
        m_chkClmThread.addItemListener(m_itemListener);
        m_chkClmTag.addItemListener(m_itemListener);
        m_chkClmMessage.addItemListener(m_itemListener);
        m_chkClmTimeUs.addItemListener(m_itemListener);
        m_chkClmTimeStamp.addItemListener(m_itemListener);

        JPanel jpLine0 = new JPanel(new BorderLayout());
        jpLine0.setLayout(new BoxLayout(jpLine0, BoxLayout.X_AXIS));
        jpLine0.setAlignmentX(Component.LEFT_ALIGNMENT);
        jpLine0.add(m_chkClmLine);
        jpLine0.add(m_chkClmDate);
        jpLine0.add(m_chkClmTime);
        jpLine0.add(m_chkClmTimeUs);

        JPanel jpLine1 = new JPanel(new BorderLayout());
        jpLine1.setLayout(new BoxLayout(jpLine1, BoxLayout.X_AXIS));
        jpLine1.setAlignmentX(Component.LEFT_ALIGNMENT);
        jpLine1.add(m_chkClmTimeStamp);
        jpLine1.add(m_chkClmGap);
        jpLine1.add(m_chkClmLogLV);
        jpLine1.add(m_chkClmPid);

        JPanel jpLine2 = new JPanel(new BorderLayout());
        jpLine2.setLayout(new BoxLayout(jpLine2, BoxLayout.X_AXIS));
        jpLine2.setAlignmentX(Component.LEFT_ALIGNMENT);
        jpLine2.add(m_chkClmThread);
        jpLine2.add(m_chkClmTag);
        jpLine2.add(m_chkClmBookmark);
        jpLine2.add(m_chkClmMessage);

        jpMain.add(jpLine0);
        jpMain.add(jpLine1);
        jpMain.add(jpLine2);

        return jpMain;
    }

    Component getPanelTextSet()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));
//        jpMain.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        jpMain.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        jpMain.setBorder(BorderFactory.createTitledBorder("Font Setting"));
        jpMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel jlFontType = new JLabel("Font : ");
        m_jcFontType = new JComboBox();
        m_jcFontType.setPreferredSize( new Dimension( 80, 25) );
        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        m_jcFontType.addItem("monospaced");
        for ( int i = 0; i < fonts.length; i++ )
        {
            m_jcFontType.addItem(fonts[i]);
        }
        m_jcFontType.addActionListener(m_alComboListener);
        m_jcFontType.setRenderer(new ComboBoxRenderer());

        JLabel jlFont = new JLabel("Size : ");
        m_tfFontSize = new JTextField(2);
        m_tfFontSize.setHorizontalAlignment(SwingConstants.RIGHT);
        m_tfFontSize.setText("12");
        m_tfFontSize.addActionListener(m_alButtonListener);
//        m_btnSetFont = new JButton("OK");
//        m_btnSetFont.setMargin(new Insets(0, 0, 0, 0));
//        m_btnSetFont.addActionListener(m_alButtonListener);

        JLabel jlEncode = new JLabel("Encode : ");
        m_comboEncode = new JComboBox();
        m_comboEncode.addItem("UTF-8");
        m_comboEncode.addItem("Local");

        jpMain.add(jlFontType);
        jpMain.add(m_jcFontType);
        jpMain.add(jlFont);
        jpMain.add(m_tfFontSize);
        jpMain.add(jlEncode);
        jpMain.add(m_comboEncode);

        return jpMain;
    }

    Component getPanelFileSet()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));
//        jpMain.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        jpMain.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        jpMain.setBorder(BorderFactory.createTitledBorder("File Setting"));
        jpMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("Append : ");
        m_chkAppendMode   = new JCheckBox();
        m_chkAppendMode.setSelected(false);
        m_chkAppendMode.setEnabled(false);
        m_chkAppendMode.setToolTipText("파일 Open시 이전 Data Clear 하지 않음");

        jpMain.add(label);
        jpMain.add(m_chkAppendMode);

        return jpMain;
    }

    Component getPanelSyncSet()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));
//        jpMain.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        jpMain.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        jpMain.setBorder(BorderFactory.createTitledBorder("Sync"));
        jpMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel jpRcv = new JPanel(new BorderLayout());
        jpRcv.setLayout(new BoxLayout(jpRcv, BoxLayout.X_AXIS));
        JLabel labelRcv = new JLabel("Receive : ");
        m_chkSyncRcv = new JCheckBox();
        m_chkSyncRcv.setEnabled(true);
        m_chkSyncRcv.setSelected(true);
        m_chkSyncRcv.setToolTipText("Sync 요청시 해당 위치로 이동");
        jpRcv.add(labelRcv);
        jpRcv.add(m_chkSyncRcv);

        JPanel jpReq = new JPanel(new BorderLayout());
        jpReq.setLayout(new BoxLayout(jpReq, BoxLayout.X_AXIS));
        JLabel labelReq = new JLabel("Auto : ");
        m_chkSyncReq = new JCheckBox();
        m_chkSyncReq.setEnabled(true);
        m_chkSyncReq.setSelected(false);
        m_chkSyncReq.setToolTipText("선택한 로그 선택 할 때 마다  Sync 요청");
        jpReq.add(labelReq);
        jpReq.add(m_chkSyncReq);

        JPanel jpLine = new JPanel(new BorderLayout());
        jpLine.setLayout(new BoxLayout(jpLine, BoxLayout.X_AXIS));
        JLabel labelLine = new JLabel("Line Sync : ");
        m_chkSyncLine = new JCheckBox();
        m_chkSyncLine.setEnabled(true);
        m_chkSyncLine.setSelected(false);
        m_chkSyncLine.setToolTipText("Time Stamp 대신에 Line  number로 Sync 요청");
        jpLine.add(labelLine);
        jpLine.add(m_chkSyncLine);

        jpMain.add(jpRcv);
        jpMain.add(jpReq);
        jpMain.add(jpLine);

        return jpMain;
    }


    Component getPanelBookmarkStyle()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));

//        JLabel label = new JLabel("Style : ");
        m_jcBookmarkColor = new JComboBox();
        m_jcBookmarkColor.addItem("Style 1");
        m_jcBookmarkColor.addItem("Style 2");
        m_jcBookmarkColor.addItem("Style 3");
        m_jcBookmarkColor.addItem("Style 4");
        m_jcBookmarkColor.addItem("Style 5");
        m_jcBookmarkColor.addActionListener(m_alComboListener);
        m_jcBookmarkColor.setPreferredSize( new Dimension(65, 25) );
        m_jcBookmarkColor.setMinimumSize( m_jcBookmarkColor.getPreferredSize()  );
        m_jcBookmarkColor.setMaximumSize( m_jcBookmarkColor.getPreferredSize()  );

//        jpMain.add(label);
        jpMain.add(m_jcBookmarkColor);

        return jpMain;
    }

    Component getPanelGoto()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));

        JLabel label = new JLabel("Goto : ");
        m_jcGoto = new JComboBox();
        m_jcGoto.setEditable(true);
        m_jcGoto.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
              char c = e.getKeyChar();
              if (!((c >= '0') && (c <= '9') ||
                 (c == KeyEvent.VK_ENTER) ||
                 (c == KeyEvent.VK_BACK_SPACE) ||
                 (c == KeyEvent.VK_DELETE))) {
                getToolkit().beep();
                e.consume();
              }
            }
          });
        m_jcGoto.addActionListener(m_alComboListener);
        m_jcGoto.setRenderer(new ComboBoxRenderer());
        m_jcGoto.setPreferredSize( new Dimension(75, 25) );
        m_jcGoto.setMinimumSize( m_jcGoto.getPreferredSize()  );
        m_jcGoto.setMaximumSize( m_jcGoto.getPreferredSize()  );

        jpMain.add(label);
        jpMain.add(m_jcGoto);

        return jpMain;
    }

    Component getPanelCommand()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));

//        JLabel label = new JLabel("Cmd : ");

        m_btnDeviceSelect = new JButton("Device");
        m_btnDeviceSelect.setMargin(new Insets(0, 0, 0, 0));
        m_btnDeviceSelect.setEnabled(true);
        m_btnDeviceSelect.addActionListener(m_alButtonListener);
        m_btnDeviceSelect.setToolTipText("CTRL + D");

        m_comDeviceSelectPanel = getPanelDeviceSelect();

        m_comSendCommandPanel = getPanelSendCommand();

        m_jcCommand = new JComboBox();
        m_jcCommand.setPreferredSize( new Dimension( 100, 25) );
        m_jcCommand.setMinimumSize( m_jcCommand.getPreferredSize()  );
        m_jcCommand.setMaximumSize( m_jcCommand.getPreferredSize()  );
        m_jcCommand.setRenderer(new ComboBoxRenderer());

        m_btnCommand = new JButton("CMD");
        m_btnCommand.setMargin(new Insets(0, 0, 0, 0));
        m_btnCommand.setEnabled(false);
        m_btnCommand.addActionListener(m_alButtonListener);

//        m_jcCommand.setMaximumSize( m_jcCommand.getPreferredSize()  );
//        m_jcCommand.setSize( 20000, m_jcCommand.getHeight() );
//        m_jcCommand.addItem(ANDROID_THREAD_CMD);
//        m_jcCommand.addItem(ANDROID_DEFAULT_CMD);
//        m_jcCommand.addItem(ANDROID_RADIO_CMD);
//        m_jcCommand.addItem(ANDROID_EVENT_CMD);
//        m_jcCommand.addItem(ANDROID_CUSTOM_CMD);
//        m_jcCommand.addItemListener(new ItemListener()
//        {
//            public void itemStateChanged(ItemEvent e)
//            {
//                if(e.getStateChange() != ItemEvent.SELECTED) return;
//
//                if (e.getItem().equals(ANDROID_CUSTOM_CMD)) {
//                    m_jcCommand.setEditable(true);
//                } else {
//                    m_jcCommand.setEditable(false);
//                }
////                setProcessCmd((String)m_jcDeviceType.getSelectedItem(), m_strSelectedDevice);
//            }
//        });

//        jpMain.add(label);
        jpMain.add(m_btnDeviceSelect);
        jpMain.add(m_btnCommand);
        jpMain.add(m_jcCommand);

        return jpMain;
    }

    Component getPanelControl()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));

        m_btnClear = new JButton("Clear");
        m_btnClear.setMargin(new Insets(0, 0, 0, 0));
//        m_btnClear.setEnabled(true);

        m_btnScroll = new JButton("Scroll");
        m_btnScroll.setMargin(new Insets(0, 0, 0, 0));
        m_btnScroll.setEnabled(true);

        m_btnRun = new JButton("Run");
        m_btnRun.setMargin(new Insets(0, 0, 0, 0));

        m_tbtnPause = new JToggleButton("Pause");
        m_tbtnPause.setMargin(new Insets(0, 0, 0, 0));
        m_tbtnPause.setEnabled(false);

        m_btnStop = new JButton("Stop");
        m_btnStop.setMargin(new Insets(0, 0, 0, 0));
        m_btnStop.setEnabled(false);

        m_btnGetPS = new JButton("PS");
        m_btnGetPS.setMargin(new Insets(0, 0, 0, 0));
        m_btnGetPS.setEnabled(false);

        m_btnClear.addActionListener(m_alButtonListener);
        m_btnScroll.addActionListener(m_alButtonListener);
        m_btnRun.addActionListener(m_alButtonListener);
        m_tbtnPause.addActionListener(m_alButtonListener);
        m_btnStop.addActionListener(m_alButtonListener);
        m_btnGetPS.addActionListener(m_alButtonListener);

        jpMain.add(m_btnClear);
        jpMain.add(m_btnScroll);
        jpMain.add(m_btnRun);
        jpMain.add(m_tbtnPause);
        jpMain.add(m_btnStop);
        jpMain.add(m_btnGetPS);

        return jpMain;
    }

    Component getPanelFilter()
    {
        for(int filter=0; filter < MAX_DEF_FILTER; filter++) {
            keywordCheck[filter]    = new JCheckBox();
            keywordCheck[filter].setSelected(false);
            keywordCheck[filter].addItemListener(m_itemListener);

            keywordCSCheck[filter]    = new JCheckBox();
            keywordCSCheck[filter].setSelected(false);
            keywordCSCheck[filter].setToolTipText("Case Sensitive");
            keywordCSCheck[filter].addItemListener(m_itemListener);

//            keywordText[filter]     = new JTextField();
//            keywordText[filter].addActionListener(m_alButtonListener);

            keywordCombo[filter]    = new JComboBox();
            keywordCombo[filter].setEditable(true);
            keywordCombo[filter].addActionListener(m_alComboListener);
            keywordCombo[filter].setPreferredSize( new Dimension(150, 25) );

            keywordCombo[filter].setMaximumRowCount(30);
            keywordCombo[filter].setAutoscrolls(true);

            keywordCombo[filter].setRenderer(new ComboBoxRenderer());
            ((JTextComponent)(keywordCombo[filter].getEditor().getEditorComponent())).getDocument().addDocumentListener(m_doclFilterListener);
            ((JTextComponent)(keywordCombo[filter].getEditor().getEditorComponent())).addKeyListener(m_keylFilterListener);

//            JScrollPane scrollPane = (JScrollPane) keywordCombo[filter].getComponent(0);
//            Dimension size = scrollPane.getPreferredSize();
//            // +20, as the vertical scroll bar occupy space too.
//            size.width = 120+20;
//            scrollPane.setPreferredSize(size);
//            scrollPane.setMaximumSize(size);

            keywordRecent[filter]   = new RecentFileMenu("Recent" + keywordStr[filter], 30, keywordCombo[filter], filter) {
                public void onSelectFile(int type, String filePath){
//                    keywordText[type].setText(filePath);
                }
            };

            keywordPanel[filter] = new JPanel();
            keywordPanel[filter].setLayout(new BoxLayout(keywordPanel[filter], BoxLayout.LINE_AXIS ));

            keywordLabel[filter] = new JLabel();
            keywordLabel[filter].setText(keywordString[filter]);

            keywordPanel[filter].add(keywordLabel[filter]);
            keywordPanel[filter].add(keywordCSCheck[filter]);
            keywordPanel[filter].add(keywordCombo[filter]);
            keywordPanel[filter].add(keywordCheck[filter]);
        }

        JPanel jpWordFilter = new JPanel(new BorderLayout());
        jpWordFilter.setBorder(BorderFactory.createTitledBorder("Word filter"));

//        keywordCheck[FILTER_0].setToolTipText("<html>"
//                                    + "Go To Previous the F2 key 'F2'"
//                                    +"<br>"
//                                    + "Go To Next the F3 key 'F3'"
//                                    + "</html>");


        jpWordFilter.add(keywordPanel[FILTER_0], BorderLayout.NORTH);
        jpWordFilter.add(keywordPanel[FILTER_1]);


        JPanel jpTagFilter = new JPanel(new GridLayout(4, 1));
        jpTagFilter.setBorder(BorderFactory.createTitledBorder("Tag filter"));

        keywordCheck[FILTER_2].setToolTipText("<html>"
                                    + "Add/Del Alt+Left Click on the PID Column"
                                    + "</html>");

        keywordCheck[FILTER_3].setToolTipText("<html>"
                                    + "Add/Del Alt+Left Click on the TID Column"
                                    + "</html>");

        keywordCheck[FILTER_4].setToolTipText("<html>"
                                    + "Add/Del Alt+Left Click on the Tag Column"
                                    + "</html>");

        keywordCheck[FILTER_5].setToolTipText("<html>"
                                    + "Add/Del Alt+Right Click on the Tag Column"
                                    + "</html>");

        jpTagFilter.add(keywordPanel[FILTER_2]);
        jpTagFilter.add(keywordPanel[FILTER_3]);
        jpTagFilter.add(keywordPanel[FILTER_4]);
        jpTagFilter.add(keywordPanel[FILTER_5]);

        JPanel jpOtherFilter = new JPanel(new GridLayout(3, 1));

        jpOtherFilter.add(getPaenlHighlight());
        jpOtherFilter.add(getPanelAutoPause());
        jpOtherFilter.add(getPanelTimeGapCheck());

        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.add(jpWordFilter,  BorderLayout.NORTH);
        jpMain.add(jpTagFilter,   BorderLayout.CENTER);
        jpMain.add(jpOtherFilter, BorderLayout.SOUTH);

        return jpMain;
    }

    Component getPaenlHighlight()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain,BoxLayout.X_AXIS));
        jpMain.setBorder(BorderFactory.createTitledBorder("Highlight"));

        try
        {
            keywordCheck[FILTER_6]    = new JCheckBox();
            keywordCheck[FILTER_6].setSelected(false);
            keywordCheck[FILTER_6].addItemListener(m_itemListener);

            keywordCSCheck[FILTER_6]    = new JCheckBox();
            keywordCSCheck[FILTER_6].setSelected(true);
            keywordCSCheck[FILTER_6].setToolTipText("Case Sensitive");
            keywordCSCheck[FILTER_6].addItemListener(m_itemListener);
//            keywordCSCheck[FILTER_6].setEnabled(false); //

            keywordCombo[FILTER_6]    = new JComboBox();
            keywordCombo[FILTER_6].setEditable(true);
            keywordCombo[FILTER_6].addActionListener(m_alComboListener);
            keywordCombo[FILTER_6].setPreferredSize( new Dimension(150, 25) );

            keywordCombo[FILTER_6].setMaximumRowCount(30);
            keywordCombo[FILTER_6].setAutoscrolls(true);

            keywordCombo[FILTER_6].setRenderer(new ComboBoxRenderer());
            ((JTextComponent)(keywordCombo[FILTER_6].getEditor().getEditorComponent())).getDocument().addDocumentListener(m_doclFilterListener);
            ((JTextComponent)(keywordCombo[FILTER_6].getEditor().getEditorComponent())).addKeyListener(m_keylFilterListener);

            keywordRecent[FILTER_6]   = new RecentFileMenu("Recent" + keywordStr[FILTER_6], 30, keywordCombo[FILTER_6], FILTER_6) {
                public void onSelectFile(int type, String filePath){
//                        keywordText[type].setText(filePath);
                }
            };

            keywordCheck[FILTER_6].setToolTipText("<html>"
                                    + "Go To Previous the comma key ',<'"
                                    +"<br>"
                                    + "Go To Next the period key '.>'"
                                    + "</html>");

            jpMain.add(keywordCSCheck[FILTER_6], BorderLayout.WEST);
            jpMain.add(keywordCombo[FILTER_6], BorderLayout.CENTER);
            jpMain.add(keywordCheck[FILTER_6], BorderLayout.EAST);
        }
        catch(Exception err)
        {
            err.printStackTrace();
        }

        return jpMain;
    }

    Component getPanelAutoPause()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain,BoxLayout.X_AXIS));
        jpMain.setBorder(BorderFactory.createTitledBorder("Auto Pause"));

        try
        {
            keywordCheck[FILTER_7]    = new JCheckBox();
            keywordCheck[FILTER_7].setSelected(false);
            keywordCheck[FILTER_7].setToolTipText("설정된 Keyword의 Log가 검출 되면 자동으로 Pause 버튼 처럼 동작");
            keywordCheck[FILTER_7].addItemListener(m_itemListener);

            keywordCSCheck[FILTER_7]    = new JCheckBox();
            keywordCSCheck[FILTER_7].setSelected(true);
            keywordCSCheck[FILTER_7].setToolTipText("Case Sensitive");
            keywordCSCheck[FILTER_7].addItemListener(m_itemListener);

            keywordCombo[FILTER_7]    = new JComboBox();
            keywordCombo[FILTER_7].setEditable(true);
            keywordCombo[FILTER_7].addActionListener(m_alComboListener);
            keywordCombo[FILTER_7].setPreferredSize( new Dimension(150, 25) );

            keywordCombo[FILTER_7].setMaximumRowCount(30);
            keywordCombo[FILTER_7].setAutoscrolls(true);

            keywordCombo[FILTER_7].setRenderer(new ComboBoxRenderer());
            ((JTextComponent)(keywordCombo[FILTER_7].getEditor().getEditorComponent())).getDocument().addDocumentListener(m_doclFilterListener);
            ((JTextComponent)(keywordCombo[FILTER_7].getEditor().getEditorComponent())).addKeyListener(m_keylFilterListener);

            keywordRecent[FILTER_7]   = new RecentFileMenu("Recent" + keywordStr[FILTER_7], 30, keywordCombo[FILTER_7], FILTER_7) {
                public void onSelectFile(int type, String filePath){
//                        keywordText[type].setText(filePath);
                }
            };

            jpMain.add(keywordCSCheck[FILTER_7], BorderLayout.WEST);
            jpMain.add(keywordCombo[FILTER_7], BorderLayout.CENTER);
            jpMain.add(keywordCheck[FILTER_7], BorderLayout.EAST);
        }
        catch(Exception err)
        {
            err.printStackTrace();
        }

        return jpMain;
    }

    Component getPanelAutoClearSet()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setBorder(BorderFactory.createTitledBorder("Auto Clear"));
        jpMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        m_chkEnableAutoClear   = new JCheckBox();
        m_chkEnableAutoClear.setSelected(false);
        m_chkEnableAutoClear.setToolTipText("설정된 라인 만큼 로그가 저장되면 자동으로 Clear 버튼 처럼 동작");

        m_jcAutoClearNum = new JComboBox();
        m_jcAutoClearNum.setEditable(false);
        m_jcAutoClearNum.addActionListener(m_alComboListener);
        m_jcAutoClearNum.setPreferredSize( new Dimension(60, 25) );

        m_jcAutoClearNum.addItem("500000");
        m_jcAutoClearNum.addItem("100000");
        m_jcAutoClearNum.addItem("50000");
        m_jcAutoClearNum.addItem("10000");
        m_jcAutoClearNum.addItem("5000");
        m_jcAutoClearNum.addItem("1000");
        m_jcAutoClearNum.addItem("500");
        m_jcAutoClearNum.addItem("100");
        m_nAutoClearNum = 500000;

        jpMain.add(m_jcAutoClearNum);
        jpMain.add(m_chkEnableAutoClear, BorderLayout.EAST);

        return jpMain;
    }

    Component getPanelTimeGapCheck()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setBorder(BorderFactory.createTitledBorder("Time Gap"));

        m_btnTimeGapLess = new JButton("Gap < ");
        m_btnTimeGapLess.setMargin(new Insets(0, 0, 0, 0));
        m_btnTimeGapLess.setEnabled(true);
        m_btnTimeGapLess.addActionListener(m_alButtonListener);

        m_tfTimeGap   = new JTextField();
        m_tfTimeGap.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
              char c = e.getKeyChar();
              if (!((c >= '0') && (c <= '9') ||
                 (c == KeyEvent.VK_ENTER) ||
                 (c == KeyEvent.VK_BACK_SPACE) ||
                 (c == KeyEvent.VK_DELETE))) {
                getToolkit().beep();
                e.consume();
              }
            }
          });
        m_tfTimeGap.setPreferredSize( new Dimension( 80, 25) );

        m_btnTimeGapMore = new JButton(" < Gap");
        m_btnTimeGapMore.setMargin(new Insets(0, 0, 0, 0));
        m_btnTimeGapMore.setEnabled(true);
        m_btnTimeGapMore.addActionListener(m_alButtonListener);

        jpMain.add(m_btnTimeGapLess, BorderLayout.WEST);
        jpMain.add(m_tfTimeGap);
        jpMain.add(m_btnTimeGapMore, BorderLayout.EAST);

        return jpMain;
    }

    Component getPanelBookMarkButton()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain,BoxLayout.X_AXIS));
//        jpMain.setBorder(BorderFactory.createTitledBorder("Select"));

        m_btnFilterOption = new JButton("Filter");
        m_btnFilterOption.setMargin(new Insets(0, 0, 0, 0));
        m_btnFilterOption.setEnabled(true);
        m_btnFilterOption.setToolTipText("CTRL + F");

        m_comFilterPanel = getFilterPanel();

        m_btnSettingOption = new JButton("Setting");
        m_btnSettingOption.setMargin(new Insets(0, 0, 0, 0));
        m_btnSettingOption.setEnabled(true);
        m_btnSettingOption.setToolTipText("CTRL + S");

        m_comSettingPanel = getSettingPanel();

        m_btnMark = new JButton("Mark");
        m_btnMark.setMargin(new Insets(0, 0, 0, 0));
        m_btnMark.setEnabled(true);

        m_btnUnmark = new JButton("Unmark");
        m_btnUnmark.setMargin(new Insets(0, 0, 0, 0));
        m_btnUnmark.setEnabled(true);

        m_btnUnmarkAll = new JButton("Unmark All");
        m_btnUnmarkAll.setMargin(new Insets(0, 0, 0, 0));
        m_btnUnmarkAll.setEnabled(true);

        m_btnSync = new JButton("Sync");
        m_btnSync.setMargin(new Insets(0, 0, 0, 0));
        m_btnSync.setEnabled(true);

        m_btnSave = new JButton("Save");
        m_btnSave.setMargin(new Insets(0, 0, 0, 0));
        m_btnSave.setEnabled(true);

        m_tbtnExplorer = new JButton("Explorer");
        m_tbtnExplorer.setMargin(new Insets(0, 0, 0, 0));
        m_tbtnExplorer.setEnabled(true);

        m_btnFilterOption.addActionListener(m_alButtonListener);
        m_btnSettingOption.addActionListener(m_alButtonListener);
        m_btnMark.addActionListener(m_alButtonListener);
        m_btnUnmark.addActionListener(m_alButtonListener);
        m_btnUnmarkAll.addActionListener(m_alButtonListener);
        m_btnSync.addActionListener(m_alButtonListener);
        m_btnSave.addActionListener(m_alButtonListener);
        m_tbtnExplorer.addActionListener(m_alButtonListener);

        jpMain.add(m_btnFilterOption);
//        jpMain.add(m_btnSettingOption);
        jpMain.add(m_btnMark);
        jpMain.add(m_btnUnmark);
        jpMain.add(m_btnUnmarkAll);
//        jpMain.add(m_tbtnExplorer);
        jpMain.add(m_btnSync);
//        jpMain.add(m_btnSave);

        return jpMain;
    }

    Component getPanelComportSet()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setBorder(BorderFactory.createTitledBorder("Speed"));

        m_jcComportSetSpeed = new JComboBox();
        m_jcComportSetSpeed.setEditable(true);
        m_jcComportSetSpeed.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
              char c = e.getKeyChar();
              if (!((c >= '0') && (c <= '9') ||
                 (c == KeyEvent.VK_ENTER) ||
                 (c == KeyEvent.VK_BACK_SPACE) ||
                 (c == KeyEvent.VK_DELETE))) {
                getToolkit().beep();
                e.consume();
              }
            }
          });
        m_jcComportSetSpeed.addActionListener(m_alComboListener);
        m_jcComportSetSpeed.setPreferredSize( new Dimension(120, 25) );

        m_jcComportSetSpeed.addItem("115200");
        m_jcComportSetSpeed.addItem("9600");
        m_nComportSetSpeed = 115200;

        jpMain.add(m_jcComportSetSpeed);
        return jpMain;
    }

    Component getFilterPanel()
    {
//        JPanel jpFilter = new JPanel(new BorderLayout());

//        jpFilter.add(getPanelDeviceSelect(), BorderLayout.WEST);
//        jpFilter.add(getPanelFilter(), BorderLayout.CENTER);
//        jpFilter.add(getPanelSetting(), BorderLayout.EAST);

        return getPanelFilter();
    }

    Component getSettingPanel()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.Y_AXIS)); //jinube
        jpMain.setAlignmentX(Component.LEFT_ALIGNMENT);

        jpMain.add(getPanelLogLevelSet());
        jpMain.add(getPanelColumnSet());

        jpMain.add(getPanelTextSet());
        jpMain.add(getPanelAutoClearSet());

        JPanel jpOther = new JPanel(new BorderLayout());
        jpOther.setLayout(new BoxLayout(jpOther, BoxLayout.X_AXIS)); //jinube
        jpOther.setAlignmentX(Component.LEFT_ALIGNMENT);
        jpOther.add(getPanelFileSet());
        jpOther.add(getPanelSyncSet());

        jpMain.add(jpOther);

        return jpMain;
    }

    Component getCommandPanel()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS)); //LINE_AXIS

        Component comBookMark = getPanelBookMarkButton();

        jpMain.add(getPanelBookmarkStyle());
//        jpMain.add(keywordCombo[FILTER_0]);
//        jpMain.add(keywordCheck[FILTER_0]);
        jpMain.add(getPanelGoto());
        jpMain.add(comBookMark);
        jpMain.add(getPanelCommand());
        jpMain.add(getPanelControl());

        return jpMain;
    }

    Component getComportSetPanel()
    {
        JPanel jpMain = new JPanel(new BorderLayout());
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS)); //LINE_AXIS

        jpMain.add(getPanelComportSet());
        return jpMain;
    }


//    Component getControlPanel()
//    {
//        JPanel jpMain = new JPanel(new BorderLayout());
//
//        jpMain.add(getFilterPanel(), BorderLayout.CENTER);
//        jpMain.add(getCommandPanel(), BorderLayout.SOUTH);
//
//        return optionMain;
//    }

    Component getBookmarkPanel()
    {
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());

//        //iookill
//        m_tmTagTableModel = new TagFilterTableModel();
//        m_tmTagTableModel.setData(m_arTagInfo);
//        m_tbTagTable = new TagTable(m_tmTagTableModel, this);
//
//        m_scrollVTagBar = new JScrollPane(m_tbTagTable);
//        m_scrollVTagBar.setPreferredSize(new Dimension(182,50));
//        // show list
//        jp.add(m_scrollVTagBar, BorderLayout.WEST);

        m_ipIndicator = new IndicatorPanel(this);
        m_ipIndicator.setData(m_arLogInfoAll, m_hmBookmarkAll, m_hmErrorAll);
        jp.add(m_ipIndicator, BorderLayout.CENTER);
        return jp;
    }

    Component getTabPanel()
    {
        m_tpTab = new JTabbedPane();
        m_tmLogTableModel = new LogFilterTableModel();
        m_tmLogTableModel.setData(m_arLogInfoAll);
        m_tbLogTable = new LogTable(m_tmLogTableModel, this);
        m_iLogParser = new LogCatParser();
        m_tbLogTable.setLogParser(m_iLogParser);

        m_scrollVBar = new JScrollPane(m_tbLogTable);

        m_tpTab.addTab("Log", m_scrollVBar);

        return m_scrollVBar;
    }

    Component getInfoPanel()
    {
        JPanel infoPanel = new JPanel(new BorderLayout());

        m_tfStatus = new JTextField("ready");
        m_tfStatus.setEditable(false);

        m_taSourceInfo = new JTextArea("");
        m_taSourceInfo.setRows(3);
        m_taSourceInfo.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(m_taSourceInfo);

        infoPanel.add(scrollPane, BorderLayout.NORTH);
        infoPanel.add(m_tfStatus, BorderLayout.SOUTH);

        return infoPanel;
    }

    void openOptionDlgFilter()
    {
        try {
            if(m_jdFilterDialog.isVisible()) {
                m_jdFilterDialog.setVisible(false);
            } else {
                if(m_bfFilterDialog) {
                    Point pos       = m_btnFilterOption.getLocationOnScreen();
                    Dimension size  = m_btnFilterOption.getSize();
                    m_jdFilterDialog.setLocation(pos.x, pos.y + size.height);
                    m_bfFilterDialog = false;
                }

                m_jdFilterDialog.setVisible(true);
            }
            m_bFilterDialog = m_jdFilterDialog.isVisible();
        } catch(Exception ee) {
            T.e("e = " + ee);
        }
    }

    void openOptionDlgSetting()
    {
        try {
            if(m_jdSettingDialog.isVisible()) {
                m_jdSettingDialog.setVisible(false);
            } else {
                if(m_bfSettingDialog) {
    //                Point pos       = m_btnSettingOption.getLocationOnScreen();
    //                Dimension size  = m_btnSettingOption.getSize();
                    Point pos       = m_btnFilterOption.getLocationOnScreen();
                    Dimension size  = m_btnFilterOption.getSize();

                    m_jdSettingDialog.setLocation(pos.x, pos.y + size.height);
                    m_bfSettingDialog = false;
                }

                m_jdSettingDialog.setVisible(true);
            }
            m_bSettingDialog = m_jdSettingDialog.isVisible();
        } catch(Exception ee) {
            T.e("e = " + ee);
        }
    }

    void openOptionDlgDevice()
    {
        try {
            if(m_jdDeviceSelectDialog.isVisible()) {
                m_jdDeviceSelectDialog.setVisible(false);
            } else {
                if(m_bfDeviceSelectDialog) {
                    Point pos       = m_btnDeviceSelect.getLocationOnScreen();
                    Dimension size  = m_btnDeviceSelect.getSize();
                    m_jdDeviceSelectDialog.setLocation(pos.x, pos.y + size.height);
                    m_bfDeviceSelectDialog = false;
                }

                m_jdDeviceSelectDialog.setVisible(true);
            }
            m_bDeviceSelectDialog = m_jdDeviceSelectDialog.isVisible();
        } catch(Exception ee) {
            T.e("e = " + ee);
        }
    }

    void openOptionDlgSendCommand()
    {
        try {
            if(m_jdSendCommandDialog.isVisible()) {
                m_jdSendCommandDialog.setVisible(false);
            } else {
                if(m_bfSendCommandDialog) {
                    Point pos       = m_btnCommand.getLocationOnScreen();
                    Dimension size  = m_btnCommand.getSize();
                    m_jdSendCommandDialog.setLocation(pos.x, pos.y + size.height);
                    m_bfSendCommandDialog = false;
                }

                m_comboSendCommand.requestFocus();

                m_jdSendCommandDialog.setVisible(true);
            }
            m_bSendCommandDialog = m_jdSendCommandDialog.isVisible();
        } catch(Exception ee) {
            T.e("e = " + ee);
        }
    }

    void initValue()
    {
        m_bPauseADB         = false;
        FILE_LOCK           = new Object();
        FILTER_LOCK         = new Object();
        m_nChangedFilter    = STATUS_READY;

//        m_arTagInfo         = new ArrayList<TagInfo>();
        m_arAMProcInfo      = new ArrayList<AMProcInfo>();
        m_arLogInfoAll      = new ArrayList<LogInfo>();
        m_arLogInfoFiltered = new ArrayList<LogInfo>();
        m_hmBookmarkAll     = new HashMap<Integer, Integer>();
        m_hmBookmarkFiltered= new HashMap<Integer, Integer>();
        m_hmErrorAll        = new HashMap<Integer, Integer>();
        m_hmErrorFiltered   = new HashMap<Integer, Integer>();

        m_strLogFileName = makeFilename();
        m_strLogDirName  = System.getProperty("user.dir");
//        m_strProcessCmd     = ANDROID_DEFAULT_CMD + m_strLogFileName;
        m_nSetStatusTime = System.currentTimeMillis();
    }

    void parseFile(final File file, final int line)
    {
        System.gc();
        if(file == null)
        {
            JOptionPane.showMessageDialog(this, "파일이 없습니다", "Message", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if(file.exists() == false)
        {
          JOptionPane.showMessageDialog(this, "파일이 없습니다", "Message", JOptionPane.WARNING_MESSAGE);
          return;
        }

        m_chkAppendMode.setEnabled(true);

        m_strLogFileName = file.getPath();
        m_strLogDirName  = file.getParent();
        new Thread(new Runnable()
        {
            public void run()
            {
                FileInputStream fstream = null;
                DataInputStream in = null;
                BufferedReader br = null;
                String strLine = null;
                LogInfo logInfo;

                try {
                    synchronized(FILE_LOCK)
                    {
                        setTitle(m_strLogFileName);
                        fstream = new FileInputStream(file);
                        in = new DataInputStream(fstream);
                        if(m_comboEncode.getSelectedItem().equals("UTF-8"))
                            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        else
                            br = new BufferedReader(new InputStreamReader(in));

                        if(m_chkAppendMode.isSelected() && m_arLogInfoAll.size() > 0) {
                            m_nAddCount = m_arLogInfoAll.size() + 1;

                            logInfo = new LogInfo();

                            logInfo.m_strLine   = "" + m_nAddCount;

                            logInfo.m_strTag    = "Append File";
                            logInfo.m_strMessage = m_strLogFileName;
                            logInfo.m_strSource  = m_strLogFileName;

                            logInfo.m_strLogLV  = "F";
                            logInfo.m_TextColor  = new Color(LogColor.COLOR_FATAL);

                            addLogInfo(logInfo);
                            m_nAddCount++;
                        } else {
                            m_nAddCount = 1;
                            clearData();
                            m_tbLogTable.clearSelection();
                            m_tbLogTable.m_lastSelectRow = -1;
                        }

                        setStatus("parseFile");
                        while ((strLine = br.readLine()) != null)
                        {
    //                        if(strLine != null && !"".equals(strLine.trim()))
                            if(strLine != null)
                            {
                                logInfo = m_iLogParser.parseLog(strLine);
                                logInfo.m_strLine = "" + m_nAddCount;
                                logInfo.m_strSource = strLine;
                                addLogInfo(logInfo);
                                m_nAddCount++;
                            }

                            if((m_nAddCount % 100) == 0) {
                                if(m_tbLogTable.m_lastSelectRow == -1) {
                                    m_tbLogTable.showRow(m_nAddCount);
                                }
                            }
                        }
                        if(checkUseFilter()) {
                            runFilter();
                        }

                        setStatus("parseFile complete");

                        //jinube
                        if(line > 0 && (m_tbLogTable.m_lastSelectRow == -1)) {
                          m_jcGoto.addItem(String.valueOf(line));
                          m_jcGoto.requestFocus();
                          m_tbLogTable.showRow(Integer.parseInt((String)m_jcGoto.getSelectedItem()) - 1, false);
                        }

                        updateTable(-1, true);
                        mainFrame.revalidate();
                        mainFrame.repaint();
                    }
                } catch(Exception ioe) {
                    System.out.println("strLine( " + m_nAddCount + " ): " + strLine);
                    ioe.printStackTrace();
//                    T.e(ioe);
                }
                try
                {
                    if(br != null)br.close();
                    if(in != null) in.close();
                    if(fstream != null) fstream.close();
                }
                catch(Exception e)
                {
                    T.e(e);
                }
              }
        }).start();
    }

    void pauseProcess()
    {
        if(m_tbtnPause.isSelected())
        {
            m_bPauseADB = true;
            m_tbtnPause.setText("Resume");
        }
        else
        {
            m_bPauseADB = false;
            m_tbtnPause.setText("Pause");
        }
    }

    void setBookmark(int nLine, String strBookmark)
    {
        LogInfo logInfo = m_arLogInfoAll.get(nLine);
        logInfo.m_strBookmark = strBookmark;
        m_arLogInfoAll.set(nLine, logInfo);
    }

    void setDeviceList()
    {
        m_strSelectedDevice = "";

        DefaultListModel listModel = (DefaultListModel)m_lDeviceList.getModel();
        try
        {
            listModel.clear();
            String strType = (String) m_jcDeviceType.getSelectedItem();

            if(strType.equals(COMBO_UART))
            {
                Enumeration portList = CommPortIdentifier.getPortIdentifiers();

                while(portList.hasMoreElements())
                {
                    CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
                    if (portId.getPortType() == CommPortIdentifier.PORT_PARALLEL)
                    {
//                        System.out.println(portId.getName());
                    }
                    else
                    {
//                        System.out.println(portId.getName());
                        listModel.addElement(portId.getName());
                    }
                }
            }
            else
            {
                String s;
                int select_dev = m_jcDeviceType.getSelectedIndex();

                String strCommand = DEVICES_CMD[select_dev];

                if(select_dev == DEVICES_CUSTOM)
                    strCommand = (String)m_jcDeviceType.getSelectedItem();

                if(strCommand == null || strCommand.isEmpty() || ("".equals(strCommand.trim())))
                    return;

                Process oProcess = Runtime.getRuntime().exec(strCommand);

                // 외부 프로그램 출력 읽기
                BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));

                // "표준 출력"과 "표준 에러 출력"을 출력
                while ((s =   stdOut.readLine()) != null)
                {
                    if(!s.equals("List of devices attached ") && !s.equals("List of devices attached"))
                    {
                        s = s.replace("\t", " ");
                        s = s.replace("device", "");
                        listModel.addElement(s);
                    }
                }
                while ((s = stdError.readLine()) != null)
                {
                    listModel.addElement(s);
                }
            }
        }
        catch(Exception e)
        {
            T.e("e = " + e);
            listModel.addElement(e);
        }
    }

    void addAmProcStartList(int line, String strMessage, long time)
    {
        StringTokenizer stk = new StringTokenizer(strMessage, ",", false);

        if(stk.hasMoreElements()) {
            stk.nextToken();
            if(stk.hasMoreElements()) {
                int tid = Integer.parseInt(stk.nextToken().trim());
//                for(AMProcInfo thInfo : m_arAMProcInfo)
//                {
//                    if(thInfo.m_tid == tid)
//                    {
//                        thInfo.m_line = line;
//                        return;
//                    }
//                }

                AMProcInfo addThInfo = new AMProcInfo();
                addThInfo.m_tid  = tid;
                addThInfo.m_line = line;
                addThInfo.m_TimeStamp = time;
                m_arAMProcInfo.add(addThInfo);
                return;
            }
        }
    }

    int geLineInAMProcInfo(String strPid, long time)
    {
        try {
            if(strPid == null || strPid.isEmpty() || ("".equals(strPid.trim()))) return -1;

            int tid = Integer.parseInt(strPid.trim());
            AMProcInfo thInfo;
            for(int cnt=m_arAMProcInfo.size()-1; cnt>=0;cnt--)
            {
                thInfo = m_arAMProcInfo.get(cnt);
                if(thInfo.m_tid == tid)
                {
                    if(thInfo.m_TimeStamp <= time)
                        return thInfo.m_line;
                }
            }
        } catch(Exception err) {
          err.printStackTrace();
//          T.e(err);
        }
        return -1;
    }

    int getLineByTimestamp(long time)
    {
        try {
            for(int cnt=m_tbLogTable.getRowCount()-1; cnt>=0;cnt--)
            {
                LogInfo logInfo = m_tbLogTable.GetAtLog(cnt);
                long    timeStamp = logInfo.m_TimeStamp;
                if((timeStamp > 0) && (time >= timeStamp)) {
//                    System.out.printf("%d, %d >= %d\r\n", cnt, time, timeStamp);
                    return cnt;
                }
            }
        } catch(Exception err) {
          err.printStackTrace();
//          T.e(err);
        }
        System.out.println("Can't find timestamp log");
        return -1;
    }

    void addTagList(String strTag)
    {
//        for(TagInfo tagInfo : m_arTagInfo)
//            if(tagInfo.m_strTag.equals(strTag))
//                return;
//        String strRemoveFilter = m_tbLogTable.GetFilterRemoveTag();
//        String strShowFilter = m_tbLogTable.GetFilterShowTag();
//        TagInfo tagInfo = new TagInfo();
//        tagInfo.m_strTag = strTag;
//        if(strRemoveFilter.contains(strTag))
//            tagInfo.m_bRemove = true;
//        if(strShowFilter.contains(strTag))
//            tagInfo.m_bShow = true;
//        m_arTagInfo.add(tagInfo);
//        m_tmTagTableModel.setData(m_arTagInfo);
//
//        m_tmTagTableModel.fireTableRowsUpdated(0, m_tmTagTableModel.getRowCount() - 1);
//        m_scrollVTagBar.validate();
//        m_tbTagTable.invalidate();
//        m_tbTagTable.repaint();
//            m_tbTagTable.changeSelection(0);
    }

    void addLogInfo(LogInfo logInfo)
    {
        synchronized(FILTER_LOCK)
        {
            int log_line = Integer.parseInt(logInfo.m_strLine) - 1;

            m_tbLogTable.setTagLength( logInfo.m_strTag.length() );
            m_arLogInfoAll.add(logInfo);
//            addTagList(logInfo.m_strTag);

            if(logInfo.m_strTag.toLowerCase().contains("am_proc_start".toLowerCase()))
                addAmProcStartList(log_line, logInfo.m_strMessage, logInfo.m_TimeStamp);

            if(logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("F"))
                m_hmErrorAll.put(log_line, log_line);

            if(m_bUserFilter)
            {
                if(m_ipIndicator.m_chBookmark.isSelected() || m_ipIndicator.m_chError.isSelected())
                {
                    boolean bAddFilteredArray = false;
                    if(logInfo.m_bMarked && m_ipIndicator.m_chBookmark.isSelected())
                    {
                        bAddFilteredArray = true;
                        m_hmBookmarkFiltered.put(log_line, m_arLogInfoFiltered.size());
                        if(logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("F"))
                            m_hmErrorFiltered.put(log_line, m_arLogInfoFiltered.size());
                    }
                    if((logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("F")) && m_ipIndicator.m_chError.isSelected())
                    {
                        bAddFilteredArray = true;
                        m_hmErrorFiltered.put(log_line, m_arLogInfoFiltered.size());
                        if(logInfo.m_bMarked)
                            m_hmBookmarkFiltered.put(log_line, m_arLogInfoFiltered.size());
                    }

                    if(bAddFilteredArray) m_arLogInfoFiltered.add(logInfo);
                }
                else if(m_tbLogTable.CheckFilter(logInfo))
                {
                    m_arLogInfoFiltered.add(logInfo);
                    if(logInfo.m_bMarked) {
                        m_hmBookmarkFiltered.put(log_line, m_arLogInfoFiltered.size());
                    }
                    if((logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("F"))) {
//                        System.out.println("E : " + log_line);
                        m_hmErrorFiltered.put(log_line, m_arLogInfoFiltered.size());
                    } else {
//                        System.out.println("LV : " + logInfo.m_strLogLV);
                    }
                }
            }
        }
    }

    void addChangeListener()
    {
        m_scrollVBar.getViewport().addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if(getExtendedState() != JFrame.MAXIMIZED_BOTH)
                {
                    m_nLastWidth  = getWidth();
                    m_nLastHeight = getHeight();
                }
                m_ipIndicator.repaint();
            }
        });
    }

    public void setFindFocus()
    {
        openOptionDlgFilter();
        keywordCombo[FILTER_0].requestFocus();
    }

    public String getFindWord()
    {
//        keywordText[FILTER_0].requestFocus();
        return (String)keywordCombo[FILTER_0].getSelectedItem();
    }

    public String getShowTag()
    {
//        keywordText[FILTER_0].requestFocus();
        return (String)keywordCombo[FILTER_4].getSelectedItem();
    }

    void setDnDListener()
    {
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetListener()
        {
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            public void dragOver(DropTargetDragEvent dtde)          {}
            public void dragExit(DropTargetEvent dte)               {}
            public void dragEnter(DropTargetDragEvent event)        {}

            public void drop(DropTargetDropEvent event)
            {
                try
                {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = event.getTransferable();
                    List<?> list = (List<?>)(t.getTransferData(DataFlavor.javaFileListFlavor));
                    Iterator<?> i = list.iterator();
                    if(i.hasNext())
                    {
                        File file = (File)i.next();
//                        setTitle(file.getPath());

                        stopProcess();
                        parseFile(file, 0);
                        m_recentMenu.addEntry( file.getPath() ); // jinube
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    void setLogLV(int nLogLV, boolean bChecked)
    {
        if(bChecked)
            m_nFilterLogLV |= nLogLV;
        else
            m_nFilterLogLV &= ~nLogLV;
        m_tbLogTable.SetLogLevel(m_nFilterLogLV);
        m_nChangedFilter = STATUS_CHANGE;
        runFilter();
    }

    boolean setFilter(int filter, String str)
    {
//        System.out.println("setFilter : " + str);
        switch(filter)
        {
            case FILTER_0:
            case FILTER_1:
            case FILTER_2:
            case FILTER_3:
            case FILTER_4:
            case FILTER_5:
            case FILTER_6:
                m_tbLogTable.SetFilterKeyword(filter, str, keywordCSCheck[filter].isSelected());
                break;
            case FILTER_7: keywordCheck[filter].setSelected(false);
                break;
            default:
                break;
        }

        return true;
    }

    void useFilter(JCheckBox checkBox)
    {
        boolean update = false;

        for(int filter=0; filter < MAX_FILTER; filter++)
        {
            if(checkBox.equals(keywordCheck[filter]))
            {
//                String str = checkBox.isSelected() ? keywordText[filter].getText() : "";
                String current_str = (String)keywordCombo[filter].getSelectedItem();
                String str = checkBox.isSelected() ? current_str : "";

                setFilter(filter, str);
//                if(keywordText[filter].getText().length() > 0)
                if(current_str != null && current_str.length() > 0)
                {
                    if((filter < MAX_DEF_FILTER) || (filter == FILTER_6))
                    {
                        update = true;
                    }
                }
                break;
            }

            if((checkBox.equals(keywordCSCheck[filter])) && keywordCheck[filter].isSelected())
            {
                String current_str = (String)keywordCombo[filter].getSelectedItem();

                setFilter(filter, current_str);
                if(current_str != null && current_str.length() > 0)
                {
                    if((filter < MAX_DEF_FILTER) || (filter == FILTER_6))
                    {
                        update = true;
                    }
                }
                break;
            }

        }

        if(update == true) {
          m_nChangedFilter = STATUS_CHANGE;
          runFilter();
        }
    }

    void setProcessBtn(boolean bStart)
    {
        if(bStart)
        {
            m_btnRun.setEnabled(false);
            m_btnStop.setEnabled(true);
//            m_btnClear.setEnabled(true);
            m_tbtnPause.setEnabled(true);
//            if(m_jcDeviceType.getSelectedItem().equals(COMBO_UART))
                m_btnGetPS.setEnabled(true);

            m_jdDeviceSelectDialog.dispose();
            m_btnDeviceSelect.setEnabled(false);
        }
        else
        {
            m_btnRun.setEnabled(true);
            m_btnStop.setEnabled(false);
//            m_btnClear.setEnabled(false);
            m_tbtnPause.setEnabled(false);
            m_tbtnPause.setSelected(false);
            m_tbtnPause.setText("Pause");
            m_btnGetPS.setEnabled(false);

            m_btnDeviceSelect.setEnabled(true);
        }
    }

    String getProcessCmd()
    {
        if(m_lDeviceList.getSelectedIndex() < 0)
            return ANDROID_DEFAULT_CMD_FIRST + m_jcCommand.getSelectedItem();
//            return ANDROID_DEFAULT_CMD_FIRST + m_jcCommand.getSelectedItem() + makeFilename();
        else
            return ANDROID_SELECTED_CMD_FIRST + m_strSelectedDevice + m_jcCommand.getSelectedItem();
    }

    String getDefaultProcessCmd()
    {
        if(m_lDeviceList.getSelectedIndex() < 0)
            return ANDROID_DEFAULT_CMD_FIRST;
//            return ANDROID_DEFAULT_CMD_FIRST + m_jcCommand.getSelectedItem() + makeFilename();
        else
            return ANDROID_SELECTED_CMD_FIRST + m_strSelectedDevice;
    }

    void setProcessCmd(String strType, String strSelectedDevice)
    {
//        m_jcCommand.removeAllItems();

        if(strSelectedDevice != null && strSelectedDevice.length() > 0)
            m_strLogFileName = makeFilename(strSelectedDevice);
        else
            m_strLogFileName = makeFilename();
//        if(strSelectedDevice != null)
//        {
//            strSelectedDevice = strSelectedDevice.replace("\t", " ").replace("device", "").replace("offline", "");
//            T.d("strSelectedDevice = " + strSelectedDevice);
//        }

        if(strType.equals(COMBO_ANDROID))
        {
            if(strSelectedDevice != null && strSelectedDevice.length() > 0)
            {
//                m_jcCommand.addItem(ANDROID_SELECTED_CMD_FIRST + strSelectedDevice + ANDROID_SELECTED_CMD_LAST);
//                m_strProcessCmd = ANDROID_SELECTED_CMD_FIRST + strSelectedDevice + ANDROID_SELECTED_CMD_LAST;
            }
            else
            {
//                m_jcCommand.addItem(ANDROID_DEFAULT_CMD);
//                m_strProcessCmd = ANDROID_DEFAULT_CMD;
            }
        }
        else if(strType.equals(COMBO_IOS))
        {
            if(strSelectedDevice != null && strSelectedDevice.length() > 0)
            {
//                m_jcCommand.addItem(ANDROID_SELECTED_CMD_FIRST + strSelectedDevice + ANDROID_SELECTED_CMD_LAST);
//                m_strProcessCmd = IOS_SELECTED_CMD_FIRST + strSelectedDevice + IOS_SELECTED_CMD_LAST;
            }
          else
          {
//              m_jcCommand.addItem(IOS_DEFAULT_CMD);
//              m_strProcessCmd = IOS_DEFAULT_CMD;
          }
        }
        else
        {
//            m_jcCommand.addItem(ANDROID_DEFAULT_CMD);
        }
    }

    void setStatus(String strText)
    {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = dayTime.format(new Date(time));

        m_tfStatus.setText(((new Throwable()).getStackTrace()[1].getFileName()) + "[" + ((new Throwable()).getStackTrace()[1].getLineNumber()) + "]" + strText + " ( " + (time-m_nSetStatusTime) + " ms )");

        m_nSetStatusTime = System.currentTimeMillis();
    }

    void setSourceInfo(LogInfo logInfo)
    {
        if(logInfo == null) return;

        m_taSourceInfo.setFont(new Font((String)m_jcFontType.getSelectedItem(), Font.PLAIN, (Integer.parseInt(m_tfFontSize.getText()))));
        m_taSourceInfo.setText(logInfo.m_strSource);

        int line = geLineInAMProcInfo(logInfo.m_strPid, logInfo.m_TimeStamp);

        if(line == -1) {
            line = geLineInAMProcInfo(logInfo.m_strThread, logInfo.m_TimeStamp);
        }

        if(line != -1) {
            m_taSourceInfo.append(ENDL);
            m_taSourceInfo.append((line+1) + " : ");
            m_taSourceInfo.append(m_arLogInfoAll.get(line).m_strSource);
        }
        m_taSourceInfo.setCaretPosition(0);
        m_taSourceInfo.show();
    }

    void setFilterModify(int filter, LogInfo logInfo)
    {
        String current_str = (String)keywordCombo[filter].getSelectedItem();
        String compare_str = "";

        switch(filter)
        {
            case FILTER_2: compare_str = logInfo.m_strPid;      break;
            case FILTER_3: compare_str = logInfo.m_strThread;   break;
            case FILTER_4: compare_str = logInfo.m_strTag;      break;
            case FILTER_5: compare_str = logInfo.m_strTag;      break;
            default      : return;
        }

        if(current_str == null || current_str.isEmpty() || ("".equals(current_str.trim())))
            current_str = compare_str;
        else if(current_str.contains("|" + compare_str))
            current_str = current_str.replace("|" + compare_str, "");
        else if(current_str.contains(compare_str))
            current_str = current_str.replace(compare_str, "");
        else
            current_str += "|" + compare_str;

        if(current_str.substring(0, 1).equals("|"))
            current_str = current_str.substring(1, current_str.length());

        if(current_str.substring(current_str.length()-1, current_str.length()).equals("|"))
            current_str = current_str.substring(0, current_str.length()-1);

        keywordCombo[filter].setSelectedItem(current_str);
    }

    public void setTitle(String strTitle)
    {
        super.setTitle(strTitle);
    }

    void stopProcess()
    {
        setProcessBtn(false);
        if(m_Process != null) m_Process.destroy();
        if(m_thProcess != null) m_thProcess.interrupt();
        if(m_thWatchFile != null) m_thWatchFile.interrupt();
        m_Process = null;
        m_thProcess = null;
        m_thWatchFile = null;
        m_bPauseADB = false;
    }

    void startFileParse()
    {
        System.gc();
        m_chkAppendMode.setEnabled(true);
        m_thWatchFile = new Thread(new Runnable()
        {
            public void run()
            {
                FileInputStream fstream = null;
                DataInputStream in = null;
                BufferedReader br = null;

                try {
                    m_strLogDirName = System.getProperty("user.dir");
                    fstream = new FileInputStream(m_strLogFileName);
                    in = new DataInputStream(fstream);
                    if(m_comboEncode.getSelectedItem().equals("UTF-8"))
                        br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    else
                        br = new BufferedReader(new InputStreamReader(in));

                    String strLine;

                    setTitle(m_strLogFileName);

                    if(m_chkAppendMode.isSelected() && m_arLogInfoAll.size() > 0) {
                        m_nAddCount = m_arLogInfoAll.size() + 1;

                        LogInfo logInfo = new LogInfo();

                        logInfo.m_strLine   = "" + m_nAddCount;

                        logInfo.m_strTag    = "Append File";
                        logInfo.m_strMessage = m_strLogFileName;
                        logInfo.m_strSource  = m_strLogFileName;

                        logInfo.m_strLogLV  = "F";
                        logInfo.m_TextColor  = new Color(LogColor.COLOR_FATAL);

                        addLogInfo(logInfo);
                        m_nAddCount++;
                    } else {
                        m_arLogInfoAll.clear();
                        m_arAMProcInfo.clear();
    //                    m_arTagInfo.clear();
                    }

                    boolean bEndLine;
                    int nSelectedIndex;
                    int nAddCount;
                    int nPreRowCount = 0;
                    int nEndLine;

                    while(true)
                    {
                        try {
                            Thread.sleep(50); // 50 ms
                        } catch (InterruptedException ie) {
                            T.e(ie);
                        }

                        if(m_nChangedFilter == STATUS_CHANGE || m_nChangedFilter == STATUS_PARSING)
                            continue;
                        if(m_bPauseADB) continue;

                        bEndLine = false;
                        nSelectedIndex = m_tbLogTable.getSelectedRow();
                        nPreRowCount = m_tbLogTable.getRowCount();
                        nAddCount = 0;

                        if((nSelectedIndex < 0) || (nSelectedIndex >= (m_tbLogTable.getRowCount() - 1))) {
                            bEndLine = true;
                        }

                        synchronized(FILE_LOCK)
                        {
                            m_nAddCount = m_arLogInfoAll.size() + 1;
                            while (!m_bPauseADB && (strLine = br.readLine()) != null)
                            {
//                                if(strLine != null && !"".equals(strLine.trim()))
                                if(strLine != null)
                                {
                                    LogInfo logInfo = m_iLogParser.parseLog(strLine);
                                    logInfo.m_strLine = "" + m_nAddCount;
                                    logInfo.m_strSource = strLine;
                                    addLogInfo(logInfo);
                                    m_nAddCount++;
                                    nAddCount++;

                                    // jinube
                                    if(m_nAddCount > m_nAutoClearNum)
                                    {
                                      if(m_chkEnableAutoClear.isSelected())
                                      {
                                        boolean bBackup = m_bPauseADB;
                                        m_bPauseADB = true;
                                        synchronized(FILTER_LOCK)
                                        {
                                            clearData();
                                            updateTable(-1, false);
                                        }
                                        m_bPauseADB = bBackup;
                                        System.out.println("m_nAutoClearNum = " + m_nAutoClearNum);
                                        break;
                                      }
                                    }

                                    if(keywordCheck[FILTER_7].isSelected()) {
                                      if(m_tbLogTable.CheckFilterKeyword(FILTER_7, logInfo)) {
                                        if(m_tbtnPause.isSelected() == false) {
                                            if(m_thPSDialog == null)
                                                createProcessInfoDialog();
                                            else
                                                m_thPSDialog.runProcessDialogThread();
                                            m_tbtnPause.setSelected(true);
                                            m_bPauseADB = true;
                                            m_tbtnPause.setText("Resume");
                                        }
                                      }
                                    }
                                }
                            }

                        if(nAddCount == 0) continue;

                        synchronized(FILTER_LOCK)
                        {
                            if(m_bUserFilter == false)
                            {
                                m_tmLogTableModel.setData(m_arLogInfoAll);
                                m_ipIndicator.setData(m_arLogInfoAll, m_hmBookmarkAll, m_hmErrorAll);
                            }
                            else
                            {
                                m_tmLogTableModel.setData(m_arLogInfoFiltered);
                                m_ipIndicator.setData(m_arLogInfoFiltered, m_hmBookmarkFiltered, m_hmErrorFiltered);
                            }

                            nEndLine = m_tmLogTableModel.getRowCount();
                            if(nPreRowCount != nEndLine)
                            {
                                if(bEndLine) {
                                    updateTable(nEndLine - 1, true);
                                } else {
                                    m_scrollVBar.validate();
                                    m_tbLogTable.invalidate();
                                    m_tbLogTable.repaint();
//                                    updateTable(nSelectedIndex, false);
                                }
                            }
                        }
                      }
                    }
                } catch(Exception e) {
//                    T.e(e);
                    e.printStackTrace();
                }
                try
                {
                    if(br != null)br.close();
                    if(in != null) in.close();
                    if(fstream != null) fstream.close();
                }
                catch(Exception e)
                {
                    T.e(e);
                }
                System.out.println("End m_thWatchFile thread");
//                setTitle(LOGFILTER + " " + VERSION);
            }
        });
        m_thWatchFile.start();
    }

    void runFilter()
    {
        checkUseFilter();
        while(m_nChangedFilter == STATUS_PARSING)
        {
          try
          {
              System.out.println("STATUS_PARSING Waitting!!!");
              Thread.sleep(100); // 100 ms
          }
          catch(Exception e)
          {
              e.printStackTrace();
          }
        }

        synchronized(FILTER_LOCK)
        {
            FILTER_LOCK.notify();
        }
    }

    void startFilterParse()
    {
        m_thFilterParse = new Thread(new Runnable()
        {
            int FilterParseCnt = 0;
            public void run()
            {
                try {
                    while(true)
                    {
                        synchronized(FILTER_LOCK)
                        {
                            m_nChangedFilter = STATUS_READY;
                            FILTER_LOCK.wait();

                            m_nChangedFilter = STATUS_PARSING;

                            int last_line = -1;
                            try {
                                last_line = Integer.parseInt(m_tbLogTable.GetCurrentLog().m_strLine) - 1;
                            } catch(Exception ee) {
                                T.e(ee);
//                                ee.printStackTrace();
                            }

                            m_arLogInfoFiltered.clear();
                            m_hmBookmarkFiltered.clear();
                            m_hmErrorFiltered.clear();
                            m_tbLogTable.clearSelection();
                            m_tbLogTable.m_lastSelectRow = -1;

                            if(m_bUserFilter == false)
                            {
                                m_tmLogTableModel.setData(m_arLogInfoAll);
                                m_ipIndicator.setData(m_arLogInfoAll, m_hmBookmarkAll, m_hmErrorAll);
                                updateTable(m_arLogInfoAll.size() - 1, true);
                                m_tbLogTable.showRow(last_line, false);
                                m_nChangedFilter = STATUS_READY;
                                continue;
                            }
                            FilterParseCnt++;

                            m_tmLogTableModel.setData(m_arLogInfoFiltered);
                            m_ipIndicator.setData(m_arLogInfoFiltered, m_hmBookmarkFiltered, m_hmErrorFiltered);
    //                        updateTable(-1);
                            setStatus("FilterParse : " + FilterParseCnt);

                            int nRowCount = m_arLogInfoAll.size();
                            LogInfo logInfo;
                            boolean bAddFilteredArray;

                            for(int nIndex = 0; nIndex < nRowCount; nIndex++)
                            {
                                if(nIndex % 1000 == 0)
                                    Thread.sleep(1); // 1ms
                                if(m_nChangedFilter == STATUS_CHANGE)
                                {
//                                    T.d("m_nChangedFilter == STATUS_CHANGE");
                                    break;
                                }
                                logInfo = m_arLogInfoAll.get(nIndex);

                                int log_line = Integer.parseInt(logInfo.m_strLine) - 1;

                                if(m_ipIndicator.m_chBookmark.isSelected() || m_ipIndicator.m_chError.isSelected())
                                {
                                    bAddFilteredArray = false;
                                    if(logInfo.m_bMarked && m_ipIndicator.m_chBookmark.isSelected())
                                    {
                                        bAddFilteredArray = true;
                                        m_hmBookmarkFiltered.put(log_line, m_arLogInfoFiltered.size());
                                        if((logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("F")))
                                            m_hmErrorFiltered.put(log_line, m_arLogInfoFiltered.size());
                                    }
                                    if((logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("F")) && m_ipIndicator.m_chError.isSelected())
                                    {
                                        bAddFilteredArray = true;
                                        m_hmErrorFiltered.put(log_line, m_arLogInfoFiltered.size());
                                        if(logInfo.m_bMarked)
                                            m_hmBookmarkFiltered.put(log_line, m_arLogInfoFiltered.size());
                                    }

                                    if(bAddFilteredArray) m_arLogInfoFiltered.add(logInfo);
                                }
                                else if(m_tbLogTable.CheckFilter(logInfo))
                                {
                                    m_arLogInfoFiltered.add(logInfo);
                                    if(logInfo.m_bMarked)
                                        m_hmBookmarkFiltered.put(log_line, m_arLogInfoFiltered.size());
                                    if((logInfo.m_strLogLV.equals("E") || logInfo.m_strLogLV.equals("F"))) {
                                        m_hmErrorFiltered.put(log_line, m_arLogInfoFiltered.size());
                                    }
                                }
                            }
                            if(m_nChangedFilter == STATUS_PARSING)
                            {
                                m_nChangedFilter = STATUS_READY;
                                m_tmLogTableModel.setData(m_arLogInfoFiltered);
                                m_ipIndicator.setData(m_arLogInfoFiltered, m_hmBookmarkFiltered, m_hmErrorFiltered);
                                updateTable(m_arLogInfoFiltered.size() - 1, true);
                                m_tbLogTable.showRow(last_line, false);
                                setStatus("FilterParse Complete : " + FilterParseCnt);

                                mainFrame.revalidate();
                                mainFrame.repaint();
                            }
                        }
                    }
                } catch(Exception e) {
//                    T.e(e);
                    e.printStackTrace();
                }
                System.out.println("End m_thFilterParse thread");
            }
        });
        m_thFilterParse.start();
    }

    void startProcess(BufferedReader buff, Writer file)
    {
        if(m_chkAppendMode.isSelected() && m_arLogInfoAll.size() > 0) {

        } else {
            clearData();
            updateTable(-1, false);

            m_tbLogTable.clearSelection();
            m_tbLogTable.m_lastSelectRow = -1;
        }

        final BufferedReader stdOut  = buff;
        final Writer         fileOut = file;

        m_thProcess = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    startFileParse();
                    String s;
                    while (((s = stdOut.readLine()) != null) && (m_thProcess != null))
                    {
                        if(s != null && !"".equals(s.trim()))
                        {
                            synchronized(FILE_LOCK)
                            {
                                fileOut.write(s);
                                fileOut.write(ENDL);
                                fileOut.flush();
                            }
//                            Thread.sleep(1); // 1ms
                        }
                    }

                    if(m_serialPort != null) {
                        m_serialPort.removeEventListener();
                        m_serialPort.close();

                        m_serialInput.close();
                        m_serialOutput.close();

                        m_serialPort = null;
                        System.out.println("Serial Close");
                        Thread.sleep(1000); // 1000ms
                    }
                    fileOut.close();
                }
                catch(Exception e)
                {
                    T.e("e = " + e);
                    try {
                        if(m_serialPort != null) {
                            m_serialPort.removeEventListener();
                            m_serialPort.close();

                            m_serialInput.close();
                            m_serialOutput.close();

                            m_serialPort = null;
                            System.out.println("Serial Close");
                            Thread.sleep(1000); // 1000ms
                        }
                        fileOut.close();
                    }
                    catch(Exception ee)
                    {
                        T.e("ee = " + ee);
                    }
                }
                stopProcess();
            }
        });
        m_thProcess.start();
        setProcessBtn(true);
    }

    void runProcess()
    {
        try {
            m_Process = null;
            setProcessCmd((String)m_jcDeviceType.getSelectedItem(), m_strSelectedDevice);

            T.d("cmd = " + getProcessCmd());
            m_Process = Runtime.getRuntime().exec(getProcessCmd());
            BufferedReader stdOut   = new BufferedReader(new InputStreamReader(m_Process.getInputStream(), "UTF-8"));
            Writer fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(m_strLogFileName), "UTF-8"));
            startProcess(stdOut, fileOut);
        }
        catch(Exception ee)
        {
            T.e("e = " + ee);
        }
    }

    void runProcess(String comport) {
        try {
            final String portName = comport;

            setProcessCmd((String)m_jcDeviceType.getSelectedItem(), m_strSelectedDevice);

            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            if (portIdentifier.isCurrentlyOwned())
            {
                System.out.println("Error: Port is currently in use");
            }
            else
            {
                CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000); //open(java.lang.String appname, int timeout)

                if (commPort instanceof SerialPort)
                {
                    newModalDialog("Setting", m_comComportSetPanel, m_btnRun);
                    System.out.println("m_nComportSetSpeed : " + m_nComportSetSpeed);

                    m_serialPort = (SerialPort) commPort;
                    m_serialPort.setSerialPortParams(m_nComportSetSpeed,	// 통신속도
                                                    SerialPort.DATABITS_8, 			// 데이터 비트
                                                    SerialPort.STOPBITS_1,			// stop 비트
                                                    SerialPort.PARITY_NONE);		// 패리티

                    m_serialInput  = m_serialPort.getInputStream();
                    m_serialOutput = m_serialPort.getOutputStream();
                    BufferedReader stdOut = new BufferedReader(new InputStreamReader(m_serialPort.getInputStream(), "UTF-8"));
                    Writer fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(m_strLogFileName), "UTF-8"));
                    startProcess(stdOut, fileOut);
                    uartSendCommand("");
                }
            }
        }
        catch(Exception ee)
        {
            T.e("e = " + ee);
        }
    }

    void uartSendCommand(String cmd)
    {
        try {
            m_serialOutput.write(cmd.getBytes());
            m_serialOutput.write(0x0d);
            m_serialOutput.write(0x0a);
            m_serialOutput.flush();
//            System.out.println("SEND : " + cmd);
        }
        catch(Exception ee)
        {
            T.e("e = " + ee);
        }
    }

    void uartSendCommand(byte code)
    {
        try {
            m_serialOutput.write(code);
            m_serialOutput.flush();
//            System.out.println("SEND : " + cmd);
        }
        catch(Exception ee)
        {
            T.e("e = " + ee);
        }
    }

    void SendSyncCommand()
    {
        try {
            if(m_chkSyncLine.isSelected()) {
                long line = Long.parseLong(m_tbLogTable.GetCurrentLog().m_strLine);
                setStatus("Sync[s] port[" + m_broadcastServer.port + "], Line[" + m_tbLogTable.GetCurrentLog().m_strLine + "]");
                new SendBroadcast(1, line, m_broadcastServer.port);
            } else {
                long time = m_tbLogTable.GetCurrentLog().m_TimeStamp;
                if(time > 0) {
                    setStatus("Sync[s] port[" + m_broadcastServer.port + "], Line[" + m_tbLogTable.GetCurrentLog().m_strLine + "], Time[" + time +"]");
                    new SendBroadcast(0, m_tbLogTable.GetCurrentLog().m_TimeStamp, m_broadcastServer.port);
                }
            }
        } catch(Exception ee) {
            T.e("e = " + ee);
        }
    }

    boolean checkUseFilter()
    {
        if(!m_ipIndicator.m_chBookmark.isSelected()
            && !m_ipIndicator.m_chError.isSelected()
            && m_tbLogTable.checkLogLVFilter(new LogInfo())
            && (m_tbLogTable.GetFilterKeywordString(FILTER_2).length() == 0 || !keywordCheck[FILTER_2].isSelected())
            && (m_tbLogTable.GetFilterKeywordString(FILTER_3).length() == 0 || !keywordCheck[FILTER_3].isSelected())
            && (m_tbLogTable.GetFilterKeywordString(FILTER_4).length() == 0 || !keywordCheck[FILTER_4].isSelected())
            && (m_tbLogTable.GetFilterKeywordString(FILTER_5).length() == 0 || !keywordCheck[FILTER_5].isSelected())
            && (m_tbLogTable.GetFilterKeywordString(FILTER_0).length() == 0 || !keywordCheck[FILTER_0].isSelected())
            && (m_tbLogTable.GetFilterKeywordString(FILTER_1).length() == 0 || !keywordCheck[FILTER_1].isSelected()))
        {
            m_bUserFilter = false;
        }
        else m_bUserFilter = true;
        return m_bUserFilter;
    }

    ActionListener m_alComboListener = new ActionListener()
    {
        public boolean InseartComboBox(ActionEvent e)
        {
            String cmd = e.getActionCommand();
            if(cmd.equals("comboBoxEdited"))
            {
                JComboBox combo  = (JComboBox)e.getSource();
                String    selStr = (String)combo.getSelectedItem();

                if(cmd == null || cmd.isEmpty() ||  ("".equals(cmd.trim()))) return false;
                if(selStr == null || selStr.isEmpty() ||  ("".equals(selStr.trim()))) return false;

                if(combo.getItemCount() > 0 && selStr.equals((String)combo.getItemAt(0)))
                    return true;

                combo.insertItemAt(selStr, 0);
                return true;
            }
            return false;
        }

        public boolean InseartComboBox(ActionEvent e, RecentFileMenu recentMenu)
        {
            String cmd = e.getActionCommand();
            if(cmd.equals("comboBoxEdited"))
            {
                JComboBox combo  = (JComboBox)e.getSource();
                String    selStr = (String)combo.getSelectedItem();

                if(cmd == null || cmd.isEmpty() ||  ("".equals(cmd.trim()))) return false;
                if(selStr == null || selStr.isEmpty() ||  ("".equals(selStr.trim()))) return false;

                for(int i=0;i<combo.getItemCount();i++)
                {
                    if(selStr.equals((String)combo.getItemAt(i))) {
                        return true;
                    }
                }
                combo.insertItemAt(selStr, 0);
                recentMenu.addEntry(selStr);
                return true;
            }
            return false;
        }

        public boolean InseartComboBox(ActionEvent e, int filter)
        {
            String cmd = e.getActionCommand();
            if(cmd.equals("comboBoxEdited"))
            {
                JComboBox combo  = (JComboBox)e.getSource();
                String    selStr = (String)combo.getSelectedItem();

                if(cmd == null || cmd.isEmpty() ||  ("".equals(cmd.trim()))) return false;
                if(selStr == null || selStr.isEmpty() ||  ("".equals(selStr.trim()))) return false;

                for(int i=0;i<combo.getItemCount();i++)
                {
                    if(selStr.equals((String)combo.getItemAt(i))) {
                        return true;
                    }
                }
                combo.insertItemAt(selStr, 0);
                keywordRecent[filter].addEntry(selStr);
                return true;
            }
            return false;
        }

        public void actionPerformed(ActionEvent e)
        {
            String cmd = e.getActionCommand();

            for(int filter=0; filter < MAX_FILTER; filter++)
            {
//                if(e.getSource().equals(keywordText[filter]))
//                {
//                    keywordCombo[filter].insertItemAt(keywordText[filter].getText(), 0);
//                    keywordRecent[filter].addEntry(keywordText[filter].getText());
//                    return;
//                }

                if(e.getSource().equals(keywordCombo[filter]))
                {
                    JComboBox combo = (JComboBox)e.getSource();
                    String    str   = (String)combo.getSelectedItem();

                    InseartComboBox(e, filter);

//                    keywordText[filter].setText(str);
                    if(keywordCheck[filter].isSelected())
                        setFilter(filter, str);
//                    combo.removeItemAt(combo.getSelectedIndex());
//                    combo.insertItemAt(str, 0);
                    return;
                }
            }

            if(e.getSource().equals(m_comboSendCommand))
            {
                InseartComboBox(e, m_rfmSendCommand);
                if(cmd.equals("comboBoxEdited")) {
                    m_tbLogTable.changeSelection(m_tbLogTable.getRowCount(), 0, false, false);
                    uartSendCommand((String)m_comboSendCommand.getSelectedItem());
                    if(m_chkSendCommandClear.isSelected()) {
                        m_comboSendCommand.setSelectedItem("");
                    }
                } else {

                }
            }
            else if(e.getSource().equals(m_jcGoto))
            {
                try {
                    InseartComboBox(e);
                    if(cmd.equals("comboBoxChanged"))
                    {
                        int nIndex = Integer.parseInt((String)m_jcGoto.getSelectedItem()) - 1;
                        m_tbLogTable.showRow(nIndex, false);
                    }
                }
                catch(Exception ee)
                {
                    T.e("e = " + ee);
                }
            }
            else if(e.getSource().equals(m_jcFontType))
            {
//                T.d("font = " + m_tbLogTable.getFont());

                m_tbLogTable.setFont(new Font((String)m_jcFontType.getSelectedItem(), Font.PLAIN, 12));
                m_tbLogTable.setFontSize(Integer.parseInt(m_tfFontSize.getText()));
                if(m_thPSDialog != null) {
                    m_thPSDialog.update_font_style();
                }
            }
            else if(e.getSource().equals(m_jcBookmarkColor))
            {
              int color_table[] = {
                LogColor.BOOKMARK_COLOR1, LogColor.BOOKMARK_COLOR2, LogColor.BOOKMARK_COLOR3,
                LogColor.BOOKMARK_COLOR4, LogColor.BOOKMARK_COLOR5
//                , LogColor.BOOKMARK_COLOR6,
//                LogColor.BOOKMARK_COLOR7, LogColor.BOOKMARK_COLOR8, LogColor.BOOKMARK_COLOR9
              };

              LogColor.COLOR_BOOKMARK = color_table[m_jcBookmarkColor.getSelectedIndex()];
            }
            else if(e.getSource().equals(m_jcComportSetSpeed))
            {
                InseartComboBox(e);

                JComboBox combo = (JComboBox)e.getSource();
                m_nComportSetSpeed = Integer.parseInt((String)combo.getSelectedItem());
            }
        }
    };

    void createProcessInfoDialog()
    {
        m_btnGetPS.setEnabled(false);
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    if(m_jcDeviceType.getSelectedItem().equals(COMBO_UART))
                        m_thPSDialog = new ProcessInfoDialog(mainFrame);
                    else
                        m_thPSDialog = new ProcessInfoDialog(getDefaultProcessCmd() + " shell ", mainFrame);
                }
                catch(Exception e)
                {
                    T.e("e = " + e);
                }
            }
        }).start();
    }

    ActionListener m_alButtonListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            String cmd = e.getActionCommand();

            if(e.getSource().equals(m_btnDevice))
            {
                setDeviceList();
            }
//            else if(e.getSource().equals(m_btnSetFont))
            else if(e.getSource().equals(m_tfFontSize))
            {
                m_tbLogTable.setFontSize(Integer.parseInt(m_tfFontSize.getText()));
                updateTable(-1, false);
                if(m_thPSDialog != null) {
                    m_thPSDialog.update_font_style();
                }
            }
            else if(e.getSource().equals(m_btnRun))
            {
                if(m_jcDeviceType.getSelectedItem().equals(COMBO_UART)) {
                    runProcess(m_lDeviceList.getSelectedValue().toString());
                    m_jcCommand.setEnabled(false);
                    m_btnCommand.setEnabled(true);
                } else {
                    runProcess();
                    m_jcCommand.setEnabled(true);
                    m_btnCommand.setEnabled(false);
                }
            }
            else if(e.getSource().equals(m_btnStop))
            {
                stopProcess();
            }
            else if(e.getSource().equals(m_btnGetPS))
            {
                createProcessInfoDialog();
            }
            else if(e.getSource().equals(m_btnClear))
            {
                boolean bBackup = m_bPauseADB;
                m_bPauseADB = true;
                clearData();
                updateTable(-1, false);
                m_tbLogTable.m_lastSelectRow = -1;
                m_bPauseADB = bBackup;
            }
            else if(e.getSource().equals(m_tbtnPause))
            {
                pauseProcess();
            }
            else if(e.getSource().equals(m_tbtnExplorer))
            {
                openExplorer();
            }
            else if(e.getSource().equals(m_btnFilterOption))
            {
                openOptionDlgFilter();
            }
            else if(e.getSource().equals(m_btnSettingOption))
            {
                openOptionDlgSetting();
            }
            else if(e.getSource().equals(m_btnDeviceSelect))
            {
                openOptionDlgDevice();
            }
            else if(e.getSource().equals(m_btnMark))
            {
                m_tbLogTable.mark(true);
            }
            else if(e.getSource().equals(m_btnUnmark))
            {
                m_tbLogTable.mark(false);
            }
            else if(e.getSource().equals(m_btnUnmarkAll))
            {
                m_tbLogTable.unmark_all();
            }
            else if(e.getSource().equals(m_btnScroll))
            {
                m_tbLogTable.changeSelection(m_tbLogTable.getRowCount(), 0, false, false);
//                m_tbLogTable.changeSelection(m_tbLogTable.getRowCount() - 1);
            }
            else if(e.getSource().equals(m_btnSave))
            {
                saveFileBrowser();
            }
            else if(e.getSource().equals(m_btnSync))
            {
                SendSyncCommand();
            }
            else if(e.getSource().equals(m_btnTimeGapLess))
            {
              m_tbLogTable.TimeGapMark(false,  Integer.parseInt(m_tfTimeGap.getText()));
            }
            else if(e.getSource().equals(m_btnTimeGapMore))
            {
              m_tbLogTable.TimeGapMark(true,  Integer.parseInt(m_tfTimeGap.getText()));
            }
            else if(e.getSource().equals(m_btnCommand))
            {
                openOptionDlgSendCommand();
            }
        }
    };

    public void openExplorer()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        System.out.println("OS : " + osName);

        try {
        if(osName.indexOf("linux") > -1) {
        			Runtime.getRuntime().exec("/usr/bin/xterm " + m_strLogFileName);
        } else {
        			Runtime.getRuntime().exec("explorer.exe /select," + m_strLogFileName);
        }
        	} catch (Exception e1) {
        			// TODO Auto-generated catch block
        			e1.printStackTrace();
        		}
        //              File file = new File (m_strLogFileName);
        //              Desktop desktop = Desktop.getDesktop();
        //              try {
        //                  desktop.open(file);
        //              } catch (Exception e1) {
        //                  // TODO Auto-generated catch block
        //                  e1.printStackTrace();
        //              }
    }

    public void notiEvent(EventParam param)
    {
        switch(param.nEventId)
        {
            case EVENT_CLICK_BOOKMARK:
            case EVENT_CLICK_ERROR:
                m_nChangedFilter = STATUS_CHANGE;
                runFilter();
                break;

            case EVENT_ADD_GOTO_LINE:
                String strLine = m_tbLogTable.GetCurrentLog().m_strLine;

                if(strLine.equals((String)m_jcGoto.getItemAt(0)) == false) {
                    m_jcGoto.insertItemAt(strLine, 0);
//                    m_jcGoto.setSelectedItem(strLine);
                }
                break;

            case EVENT_ADD_PID:
                setFilterModify(FILTER_2, m_tbLogTable.GetCurrentLog());
                break;

            case EVENT_ADD_TID:
                setFilterModify(FILTER_3, m_tbLogTable.GetCurrentLog());
                break;

            case EVENT_CHANGE_FILTER_SHOW_TAG:
                setFilterModify(FILTER_4, m_tbLogTable.GetCurrentLog());
                break;

            case EVENT_CHANGE_FILTER_REMOVE_TAG:
                setFilterModify(FILTER_5, m_tbLogTable.GetCurrentLog());
                break;

            case EVENT_CHANGESELECTION:
                setSourceInfo(m_tbLogTable.GetCurrentLog());
                if(m_chkSyncReq.isSelected())
                {
                    SendSyncCommand();
                }
                break;

            case EVENT_GET_PID_INFO:
                {
                    LogInfo loginfo = m_tbLogTable.GetCurrentLog();
                    if(loginfo != null)
                    {
                        try {
                            if(m_thPSDialog != null)
                                m_thPSDialog.findProcessID(loginfo.m_strPid);
                        } catch(Exception e) {
                            T.e("e = " + e);
                        }
                    }
                }
                break;

            case EVENT_GET_TID_INFO:
                {
                    LogInfo loginfo = m_tbLogTable.GetCurrentLog();
                    if(loginfo != null)
                    {
                        try {
                            if(m_thPSDialog != null)
                                m_thPSDialog.findProcessID(loginfo.m_strThread);
                        } catch(Exception e) {
                            T.e("e = " + e);
                        }
                    }
                }
                break;

            case EVENT_EXIT_PROC_INFO:
                m_thPSDialog = null;
                if(m_btnStop.isEnabled())
                    m_btnGetPS.setEnabled(true);
                break;

            case EVENT_GOTO_TIMESTAMP:
                if(m_chkSyncRcv.isSelected())
                {
                    long time = m_broadcastServer.timeStamp;
                    int line = getLineByTimestamp(time);
//                    System.out.printf("goto line[%d] time[%d]\r\n", line+1, time);
                    setStatus("Sync[r] port[" + m_broadcastServer.port + "], Line[" + (line + 1) + "], Time[" + time +"]");
                    if(line != -1) {
                        m_tbLogTable.changeSelection(line, 0, false, false);
                        setSourceInfo(m_tbLogTable.GetCurrentLog());
                    }
                }
                break;

            case EVENT_GOTO_LINENUMBER:
                if(m_chkSyncRcv.isSelected())
                {
                    int line = (int)m_broadcastServer.lineNumber - 1;
//                    System.out.printf("goto line[%d] time[%d]\r\n", line+1, time);
                    setStatus("Sync[r] port[" + m_broadcastServer.port + "], Line[" + (line + 1) + "]");
                    if(line != -1) {
                        m_tbLogTable.changeSelection(line, 0, false, false);
                        setSourceInfo(m_tbLogTable.GetCurrentLog());
                    }
                }
                break;

        }
    }

    void updateTable(int nRow, boolean bMove)
    {
        try {
            int row_cnt = m_tmLogTableModel.getRowCount();

            m_tmLogTableModel.fireTableRowsUpdated(0, row_cnt - 1);

            m_scrollVBar.validate();
            m_tbLogTable.changeSelection(nRow);
            m_tbLogTable.invalidate();

            m_tbLogTable.repaint();
            m_tbLogTable.changeSelection(nRow, 0, false, false, bMove);
        }
        catch(Exception e)
        {
            T.e(e);
        }
//        System.out.println("updateTable nR=" + nRow + " bMove=" + bMove + " idf=" + m_ipIndicator.m_bDrawFull);
    }


    KeyListener m_klCommandCodeListener = new KeyListener() {
        public void keyPressed(KeyEvent keyEvent) {
//            printIt("Pressed", keyEvent);
//http://academic.evergreen.edu/projects/biophysics/technotes/program/ascii_ctrl.htm
            if(keyEvent.isControlDown()) {
                byte code = 0;
                int keyCode = keyEvent.getKeyCode();

                switch(keyCode)
                {
                    case KeyEvent.VK_A: case KeyEvent.VK_B: case KeyEvent.VK_C: case KeyEvent.VK_D:
                    case KeyEvent.VK_E: case KeyEvent.VK_F: case KeyEvent.VK_G: case KeyEvent.VK_H:
                    case KeyEvent.VK_I: case KeyEvent.VK_J: case KeyEvent.VK_K: case KeyEvent.VK_L:
                    case KeyEvent.VK_M: case KeyEvent.VK_N: case KeyEvent.VK_O: case KeyEvent.VK_P:
                    case KeyEvent.VK_Q: case KeyEvent.VK_R: case KeyEvent.VK_S: case KeyEvent.VK_T:
                    case KeyEvent.VK_U: case KeyEvent.VK_V: case KeyEvent.VK_W: case KeyEvent.VK_X:
                    case KeyEvent.VK_Y: case KeyEvent.VK_Z:
                        code = (byte)(0x01 + (keyCode - KeyEvent.VK_A));
                        break;
                    default: return;
                }
                uartSendCommand(code);
            }
        }

        public void keyReleased(KeyEvent keyEvent) {
//            printIt("Released", keyEvent);
        }

        public void keyTyped(KeyEvent keyEvent) {
//            printIt("Typed", keyEvent);
        }

//        private void printIt(String title, KeyEvent keyEvent) {
//            int keyCode = keyEvent.getKeyCode();
//            String keyText = KeyEvent.getKeyText(keyCode);
//            System.out.println(title + " : " + keyText + " / " + keyEvent.getKeyChar());
//        }
    };

    DocumentListener m_doclFilterListener = new DocumentListener()
    {
        public void TableFilterUpdate(DocumentEvent arg0)
        {
            try
            {
                for(int filter=0; filter < MAX_FILTER; filter++) {
                    if(arg0.getDocument().equals(((JTextComponent)(keywordCombo[filter].getEditor().getEditorComponent())).getDocument()))
                    {
                        String str = arg0.getDocument().getText(0, arg0.getDocument().getLength());
//                        keywordText[filter].setText(str);
                        if(keywordCheck[filter].isSelected())
                        {
                            setFilter(filter, str);
                            break;
                        }
                        return;
                    }
                }

                m_nChangedFilter = STATUS_CHANGE;
                runFilter();
            }
            catch(Exception e)
            {
                T.e(e);
            }
        }

        public void changedUpdate(DocumentEvent arg0)
        {
            TableFilterUpdate(arg0);
        }

        public void insertUpdate(DocumentEvent arg0)
        {
            TableFilterUpdate(arg0);
        }

        public void removeUpdate(DocumentEvent arg0)
        {
            TableFilterUpdate(arg0);
        }
    };

    KeyListener m_keylFilterListener = new KeyListener()
    {
        public void keyPressed(KeyEvent keyEvent) {
//            printIt("Pressed", keyEvent);
//http://academic.evergreen.edu/projects/biophysics/technotes/program/ascii_ctrl.htm
            if(keyEvent.isControlDown() && (keyEvent.getID() == KeyEvent.KEY_PRESSED)) {
                byte code = 0;
                int keyCode = keyEvent.getKeyCode();

                if(keyCode == KeyEvent.VK_F)
                {
                    m_jdFilterDialog.dispose();
                }
                else
                {
                    for(int filter=0; filter < MAX_FILTER; filter++) {
                        if(keyEvent.getSource().equals(((JTextComponent)(keywordCombo[filter].getEditor().getEditorComponent()))))
                        {
                            if(keyCode == KeyEvent.VK_SPACE) {
                                try {
                                    Document doc = ((JTextComponent)(keywordCombo[filter].getEditor().getEditorComponent())).getDocument();
                                    String str = doc.getText(0, doc.getLength());
                                    keywordCombo[filter].setSelectedItem(str);
                                } catch (Exception ee) {
                                    T.e("ee = " + ee);
                                }
                                keywordCheck[filter].setSelected(!keywordCheck[filter].isSelected());
                            }
                            return;
                        }
                    }
                }
            }
        }

        public void keyReleased(KeyEvent keyEvent) {
        }

        public void keyTyped(KeyEvent keyEvent) {
        }
    };

    ItemListener m_itemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent itemEvent) {
            JCheckBox check = (JCheckBox)itemEvent.getSource();

            if(check.equals(m_chkVerbose))
                setLogLV(LogInfo.LOG_LV_VERBOSE, check.isSelected());
            else if(check.equals(m_chkDebug))
                setLogLV(LogInfo.LOG_LV_DEBUG, check.isSelected());
            else if(check.equals(m_chkInfo))
                setLogLV(LogInfo.LOG_LV_INFO, check.isSelected());
            else if(check.equals(m_chkWarn))
                setLogLV(LogInfo.LOG_LV_WARN, check.isSelected());
            else if(check.equals(m_chkError))
                setLogLV(LogInfo.LOG_LV_ERROR, check.isSelected());
            else if(check.equals(m_chkFatal))
                setLogLV(LogInfo.LOG_LV_FATAL, check.isSelected());
            else if(check.equals(m_chkClmBookmark))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_BOOKMARK, check.isSelected());
            else if(check.equals(m_chkClmGap))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_GAP, check.isSelected());
            else if(check.equals(m_chkClmLine))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_LINE, check.isSelected());
            else if(check.equals(m_chkClmDate))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_DATE, check.isSelected());
            else if(check.equals(m_chkClmTime))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_TIME, check.isSelected());
            else if(check.equals(m_chkClmLogLV))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_LOGLV, check.isSelected());
            else if(check.equals(m_chkClmPid))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_PID, check.isSelected());
            else if(check.equals(m_chkClmThread))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_THREAD, check.isSelected());
            else if(check.equals(m_chkClmTag))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_TAG, check.isSelected());
            else if(check.equals(m_chkClmMessage))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_MESSAGE, check.isSelected());
            else if(check.equals(m_chkClmTimeUs))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_TIMEUS, check.isSelected());
            else if(check.equals(m_chkClmTimeStamp))
                m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_TIMESTAMP, check.isSelected());
            else if(check.equals(keywordCheck[FILTER_7]))
                m_tbLogTable.SetFilterKeyword(FILTER_7, (String)(keywordCombo[FILTER_7].getSelectedItem()), keywordCSCheck[FILTER_7].isSelected());
            else if(check.equals(keywordCheck[FILTER_0])
                    || check.equals(keywordCheck[FILTER_1])
                    || check.equals(keywordCheck[FILTER_2])
                    || check.equals(keywordCheck[FILTER_3])
                    || check.equals(keywordCheck[FILTER_4])
                    || check.equals(keywordCheck[FILTER_5])
                    || check.equals(keywordCheck[FILTER_6])
                    || check.equals(keywordCSCheck[FILTER_0])
                    || check.equals(keywordCSCheck[FILTER_1])
                    || check.equals(keywordCSCheck[FILTER_2])
                    || check.equals(keywordCSCheck[FILTER_3])
                    || check.equals(keywordCSCheck[FILTER_4])
                    || check.equals(keywordCSCheck[FILTER_5])
                    || check.equals(keywordCSCheck[FILTER_6])
                    )
                useFilter(check);
        }
    };

    public void openFileBrowser()
    {
        FileDialog fd = new FileDialog(this, "File open", FileDialog.LOAD);
//        fd.setDirectory( m_strLastDir );
        fd.setVisible( true );
        if (fd.getFile() != null)
        {
            parseFile(new File(fd.getDirectory() + fd.getFile()), 0);
            m_recentMenu.addEntry( fd.getDirectory() + fd.getFile() );
        }

        //In response to a button click:
//        final JFileChooser fc = new JFileChooser(m_strLastDir);
//        int returnVal = fc.showOpenDialog(this);
//        if (returnVal == JFileChooser.APPROVE_OPTION)
//        {
//            File file = fc.getSelectedFile();
//            m_strLastDir = fc.getCurrentDirectory().getAbsolutePath();
//            T.d("file = " + file.getAbsolutePath());
//            parseFile(file);
//            m_recentMenu.addEntry( file.getAbsolutePath() );
//        }
    }

    public void openStreamFileBrowser(String filePath)
    {
        m_strLogFileName = filePath;
        stopProcess();

        if(m_chkAppendMode.isSelected() && m_arLogInfoAll.size() > 0) {

        } else {
            clearData();
            m_tbLogTable.clearSelection();
            m_tbLogTable.m_lastSelectRow = -1;
        }
        startFileParse();
        setProcessBtn(true);
    }

    public void openStreamFileBrowser()
    {
        FileDialog fd = new FileDialog(this, "File open", FileDialog.LOAD);
//        fd.setDirectory( m_strLastDir );
        fd.setVisible( true );
        if (fd.getFile() != null)
        {
            m_strLogFileName = fd.getDirectory() + fd.getFile();
            openStreamFileBrowser(m_strLogFileName);
            m_st_recentMenu.addEntry(m_strLogFileName);
        }
    }

    public void saveFileBrowser()
    {
        FileDialog fd = new FileDialog(this, "File save", FileDialog.SAVE);
        fd.setDirectory( m_strLogDirName );
        fd.setFile( makeFilename(m_strLogFileName + "_analysis") );
        fd.setVisible( true );

        if (fd.getFile() != null)
        {
            try {
                Writer fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fd.getDirectory() + fd.getFile()), "UTF-8"));

                int nRowCount = m_arLogInfoAll.size();
                LogInfo logInfo;

                synchronized(FILE_LOCK)
                {
                    for(int nIndex = 0; nIndex < nRowCount; nIndex++)
                    {
                        logInfo = m_arLogInfoAll.get(nIndex);
                        if(logInfo.m_bMarked)
                        {
                            fileOut.write(logInfo.m_strSource);
                            fileOut.write(ENDL);
                        }
                    }
                    fileOut.flush();
                    fileOut.close();
                }
            }
            catch (Exception e) {
                T.e("e = " + e);
            }
        }
    }


//	private void connect(String portName) throws Exception {
//
//		System.out.printf("Port : %s\n", portName);
//
//		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
//
//		if (portIdentifier.isCurrentlyOwned()) {
//			System.out.println("Error: Port is currently in use");
//		} else {
//			CommPort commPort = portIdentifier.open(this.getClass().getName(),
//					2000);
//
//			if (commPort instanceof SerialPort) {
//				SerialPort serialPort = (SerialPort) commPort;
//				serialPort.setSerialPortParams(115200,	// 통신속도
//						SerialPort.DATABITS_8, 			// 데이터 비트
//						SerialPort.STOPBITS_1,			// stop 비트
//						SerialPort.PARITY_NONE);		// 패리티
//
//				// 입력 스트림
//				InputStream in = serialPort.getInputStream();
//
//				// 출력 스트림
//				OutputStream out = serialPort.getOutputStream();
//
//				(new Thread(new SerialReader(in))).start();
//				(new Thread(new SerialWriter(out))).start();
//
//			} else {
//				System.out
//						.println("Error: Only serial ports are handled by this example.");
//			}
//		}
//	}
//
//	/**
//	 * 시리얼 읽기
//	 */
//	public static class SerialReader implements Runnable {
//		InputStream in;
//
//		public SerialReader(InputStream in) {
//			this.in = in;
//		}
//
//		public void run() {
//			byte[] buffer = new byte[1024];
//			int len = -1;
//
//			try {
//				while ((len = this.in.read(buffer)) > -1) {
//					System.out.print(new String(buffer, 0, len));
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	/**
//	 * 시리얼에 쓰기
//	 */
//	public static class SerialWriter implements Runnable {
//		OutputStream out;
//
//		public SerialWriter(OutputStream out) {
//			this.out = out;
//		}
//
//		public void run() {
//			try {
//				int c = 0;
//
//				System.out.println("\nKeyborad Input Read!!!!");
//				while ((c = System.in.read()) > -1) {
//					this.out.write(c);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}


    public JDialog newOptionDialog(String title, Component component){
        final JDialog dialog = new JDialog();
//        dialog.setModal(true);
        dialog.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                //Runtime.getRuntime().gc();
                //System.gc();
            }
        });
//        dialog.setUndecorated(true);
//        dialog.getRootPane().setOpaque(false);

        dialog.setTitle(title);

        dialog.setContentPane((Container)component);

//        dialog.setMinimumSize(new Dimension(300, 180));

//        Point pos       = this.getLocation();
//        Dimension size  = this.getSize();
//
//        dialog.setLocation(pos.x, pos.y + 75);
//        dialog.setLocation(pos.x + size.width/2, pos.y);

        //Display the window.
        dialog.pack();
        dialog.setAlwaysOnTop( true );
//        dialog.setVisible(true);

        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        Action actionListener = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
//                setVisible(false);
            }
        };

        JRootPane root = dialog.getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        root.getActionMap().put("ESCAPE", actionListener);

        return dialog;
    }

    public void newModalDialog(String title, Component component, Component base){
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setTitle(title);

        dialog.setContentPane((Container)component);

        Point pos       = base.getLocationOnScreen();
        Dimension size  = base.getSize();
        dialog.setLocation(pos.x, pos.y + size.height);

        JButton btExit = new JButton("Exit");
        btExit.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
          }
        });
        dialog.add(btExit);

        //Display the window.
        dialog.pack();
        dialog.setAlwaysOnTop( true );
        dialog.setVisible(true);
    }

    public void runMultiCastThread()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                m_broadcastServer = new BroadcastServer(mainFrame);
            }
        }).start();
    }
}

class DOSOpen {
  public static String FSEPARATOR = System.getProperty("file.separator");
  public DOSOpen(String dir){
    try{
      String osName = System.getProperty("os.name").toLowerCase();
      ProcessBuilder processBulder = new ProcessBuilder();
      List<String> cmdList = new ArrayList<String>();
      if(osName.indexOf("windwos 9") > -1){
        cmdList.add("command.com");
        cmdList.add("/c");
        cmdList.add("start");
      } else if(
        (osName.indexOf("nt") > -1) ||
        (osName.indexOf("windows 2000") > -1) ||
        (osName.indexOf("windows 7") > -1) ||
        (osName.indexOf("windows xp") > -1)) {
        cmdList.add("cmd.exe");
        cmdList.add("/c");
        cmdList.add("start");
      } else if((osName.indexOf("linux") > -1)) {
        cmdList.add("/usr/bin/xterm");
      }

      System.out.println("OS : " + osName + " / CMD : " + cmdList);

      processBulder = processBulder.directory(new File(dir));
      processBulder.command(cmdList);
      processBulder.start();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  public DOSOpen(String dir, String cmd, String param){
    try{
      String osName = System.getProperty("os.name").toLowerCase();
      ProcessBuilder processBulder = new ProcessBuilder();
      List<String> cmdList = new ArrayList<String>();

      cmdList.add(dir + FSEPARATOR + cmd);
      cmdList.add(param);

      System.out.println("OS : " + osName + " / CMD : " + cmdList);

      processBulder = processBulder.directory(new File(dir));
      processBulder.command(cmdList);
      processBulder.start();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }
}



class ProcessInfoDialog extends JDialog {
    public static String ENDL = System.getProperty("line.separator");
    LogFilterMain m_LogFilterMain;
    final String[] columnName = {
                    "LABEL",
                    "USER",
                    "PID",
                    "PPID",
                    "VSIZE",
                    "RSS",
                    "CPU",
                    "PRIO",
                    "NICE",
                    "RTPRI",
                    "SCHED",
                    "PCY",
                    "WCHAN",
                    "PC",
                    "STATUS",
                    "NAME"
                 };
        int columnWidth[] = {
                    100, //"LABEL",
                    50, //"USER",
                    30, //"PID",
                    30, //"PPID",
                    30, //"VSIZE",
                    30, //"RSS",
                    30, //"CPU",
                    30, //"PRIO",
                    30, //"NICE",
                    30, //"RTPRI",
                    30, //"SCHED",
                    30, //"PCY",
                    50, //"WCHAN",
                    30, //"PC",
                    30, //"STATUS",
                    100, //"NAME"
                    0
                };

    final int PC_COLUMN   = 13;
    final int NAME_COLUMN = 15;
    final int MAX_COLUMN  = 16;

    ColorRenderer m_crList=new ColorRenderer("A");

    String data[][] = {};
    DefaultTableModel model = new DefaultTableModel(data, columnName);
    ListSelectionModel listSelectionModel;

    JTextArea    m_taProcessInfo = new JTextArea("");;
    JCheckBox    m_chkRefresh;

    String strPSCommand = "";
    String strPrefixCommand = "";
    JComboBox m_comboCommand;

    Object GET_DATA_LOCK = new Object();

    JTable SearchResultsTable = new JTable(model){
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };

    public ProcessInfoDialog(LogFilterMain filterMain) {
        m_LogFilterMain = filterMain;

        setTitle("Get Process Info");

        addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
            dispose();
            Runtime.getRuntime().gc();
            //System.gc();
            }
        });

/////////////////////////////////////////////////////////////////////////////////////////////
//        JButton btExit = new JButton("Exit");
//        btExit.addActionListener(new ActionListener() {
//          public void actionPerformed(ActionEvent e) {
//            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_EXIT_PROC_INFO));
//            dispose();
//          }
//        });

        JLabel jlPID = new JLabel("PID NUM : ");
        final JTextField tfPID = new JTextField(10);
        tfPID.setEditable(true);
        tfPID.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendProcessInfoCmd(tfPID.getText());
            }
        });

        JButton btnSend = new JButton("SEND");
        btnSend.setMargin(new Insets(0, 0, 0, 0));
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendProcessInfoCmd(tfPID.getText());
            }
        });

        getContentPane().add(jlPID,   BorderLayout.WEST);
        getContentPane().add(tfPID,   BorderLayout.CENTER);
        getContentPane().add(btnSend, BorderLayout.EAST);
//        getContentPane().add(btExit, BorderLayout.EAST);

        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                  m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_EXIT_PROC_INFO));
                  dispose();
            }
        });

        Point pos       = m_LogFilterMain.getLocation();
        Dimension size  = m_LogFilterMain.getSize();
//        this.setSize(1200,500);

        // Determine the new location of the window
//        int w = this.getSize().width;
//        int h = this.getSize().height;

        this.setLocation(pos.x + size.width/2, pos.y + 75);
        update_font_style();

    //    this.setModal(true);
        setAlwaysOnTop( true );
        this.pack();
        this.setVisible(true);
    }

    public ProcessInfoDialog(String device_cmd, LogFilterMain filterMain) {

        m_LogFilterMain = filterMain;
//        System.out.println("ProcessInfoDialog : " + device_cmd);

        addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
            dispose();
            Runtime.getRuntime().gc();
            //System.gc();
            }
        });

//        ScheduledJob job = new ScheduledJob();
//        Timer jobScheduler = new Timer();
//        jobScheduler.scheduleAtFixedRate(job, 1, 1000);
//        jobScheduler.cancel();

        setDesign();

        strPrefixCommand = device_cmd;
        runProcessDialogThread();
    }

    public void findProcessID(String find) {
        synchronized(GET_DATA_LOCK)
        {
            String pid = "";
            int    col = SearchResultsTable.getColumnModel().getColumnIndex("PID");
            for(int i = 0; i < SearchResultsTable.getRowCount(); i++)
            {
                pid = (String) SearchResultsTable.getValueAt(i, col);
//                if (find.equalsIgnoreCase(pid))
                if (find.equals(pid))
                {
                    SearchResultsTable.changeSelection(i, col, false, false);

                    try {
                        if(m_chkRefresh.isSelected())
                            updateProcessInfo(Integer.parseInt(pid));
                    } catch (Exception e) { // 에러 처리
                        T.e(e);
//                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public void getStreamInfo(String cmd, JTextArea textArea) {
//        System.out.println("cmd : " + cmd);
        synchronized(GET_DATA_LOCK)
        {
            try {
                Process oProcess = Runtime.getRuntime().exec(cmd);
    //            System.out.println("cmd : " + cmd);

                // 외부 프로그램 출력 읽기
                BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));

                // "표준 출력"과 "표준 에러 출력"을 출력
                int cnt = 0;
                String s;
                boolean bTitle = true;
                while ((s = stdOut.readLine()) != null)
                {
    //                System.out.println("read : " + s);
                    if(s != null && !"".equals(s.trim()))
                    {
                        textArea.append(ENDL);
                        textArea.append(s);
                    }
                }
            } catch (Exception e) { // 에러 처리
                System.out.println("SearchResults : Exception" + e);
                e.printStackTrace();
            }
        }
    }
//    class ScheduledJob extends TimerTask {
//
//       public void run() {
//            synchronized(GET_DATA_LOCK)
//            {
//                try {
//                    model.setRowCount(0);
//
//                    Process oProcess = Runtime.getRuntime().exec(strPSCommand);
//        //            System.out.println("cmd : " + strPSCommand);
//
//                    // 외부 프로그램 출력 읽기
//                    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
//                    BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));
//
//                    // "표준 출력"과 "표준 에러 출력"을 출력
//                    int cnt = 0;
//                    String s;
//                    boolean bTitle = true;
//                    while ((s = stdOut.readLine()) != null)
//                    {
//        //                System.out.println("read : " + s);
//                        if(s != null && !"".equals(s.trim()))
//                        {
//                            String[] d={"","","","","","","","","","","","","","","","","","",""};
//                            if(bTitle) {
//                                bTitle = false;
//                                continue;
//                            }
//                            StringTokenizer stk = new StringTokenizer(s, " ", false);
//                            for(int idx=0; idx<13; idx++)
//                            {
//                                if(stk.hasMoreElements())
//                                    d[idx] = stk.nextToken();
//                            }
//                            while(stk.hasMoreElements())
//                            {
//                                d[13] += stk.nextToken("");
//                            }
//
//                            model.insertRow(cnt++,new Object[]{d[0],d[1],d[2],d[3],d[4],d[5],d[6],d[7],d[8],d[9],d[10],d[11],d[12],d[13]});
//                        }
//                    }
//                } catch (Exception e) { // 에러 처리
//                    System.out.println("SearchResults : Exception" + e);
//                    e.printStackTrace();
//                }
////                setWidth();
//            }
//       }
//    }


    public void runProcessDialogThread()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                synchronized(GET_DATA_LOCK)
                {
                    try {
                        Date now = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        strPSCommand = strPrefixCommand + (String)m_comboCommand.getSelectedItem();

                        setTitle(format.format(now) + " : "+ strPSCommand);

                        model.setRowCount(0);

                        Process oProcess = Runtime.getRuntime().exec(strPSCommand);
            //            System.out.println("cmd : " + strPSCommand);

                        // 외부 프로그램 출력 읽기
                        BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
                        BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));

                        // "표준 출력"과 "표준 에러 출력"을 출력
                        int cnt = 0;
                        String s;
                        boolean bTitle = true;
                        int colIdx[]={15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15};
                        while ((s = stdOut.readLine()) != null)
                        {
            //                System.out.println("read : " + s);
                            if(s != null && !"".equals(s.trim()))
                            {
                                int idx = 0;
                                String[] d={"","","","","","","","","","",
                                            "","","","","","","","","",""};
                                StringTokenizer stk = new StringTokenizer(s, " ", false);

                                if(bTitle) {
                                    bTitle = false;
                                    for(idx=0; idx<MAX_COLUMN; idx++)
                                    {
                                        if(stk.hasMoreElements())
                                        {
                                            d[idx] = stk.nextToken().trim();
                                            for(int col=0; col<MAX_COLUMN; col++)
                                            {
                                                if(columnName[col].equals(d[idx])) {
                                                    colIdx[col] = idx;

                                                    if(col == PC_COLUMN) {
                                                        colIdx[col+1] = ++idx;
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    continue;
                                }

                                for(idx=0; idx<NAME_COLUMN; idx++)
                                {
                                    if(stk.hasMoreElements())
                                        d[idx] = stk.nextToken().trim();
                                }
                                while(stk.hasMoreElements())
                                {
                                    d[idx] += stk.nextToken("");
                                }

                                model.insertRow(cnt++,new Object[]{d[colIdx[0]],d[colIdx[1]],d[colIdx[2]],d[colIdx[3]],d[colIdx[4]],
                                                                   d[colIdx[5]],d[colIdx[6]],d[colIdx[7]],d[colIdx[8]],d[colIdx[9]],
                                                                   d[colIdx[10]],d[colIdx[11]],d[colIdx[12]],d[colIdx[13]],d[colIdx[14]],d[colIdx[15]]});
                            }
                        }
                    } catch (Exception e) { // 에러 처리
                        System.out.println("SearchResults : Exception" + e);
                        e.printStackTrace();
                    }

                    m_taProcessInfo.setText("");
//                    setWidth();

//                    pack();
//                    setVisible(true);
                }
            }
        }).start();
    }

    public void update_font_style()
    {
        m_crList.set_font(new Font((String)m_LogFilterMain.m_jcFontType.getSelectedItem(), Font.PLAIN, (Integer.parseInt(m_LogFilterMain.m_tfFontSize.getText()))));
        SearchResultsTable.setRowHeight((Integer.parseInt(m_LogFilterMain.m_tfFontSize.getText())+4));
    }

    public void setWidth()
    {

        for (int column = 0; column < SearchResultsTable.getColumnCount(); column++)
        {
            TableColumn tableColumn = SearchResultsTable.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < SearchResultsTable.getRowCount(); row++)
            {
                TableCellRenderer cellRenderer = SearchResultsTable.getCellRenderer(row, column);
                Component c = SearchResultsTable.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + SearchResultsTable.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows

                if (preferredWidth >= maxWidth)
                {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth( preferredWidth + 2);
        }
    }

    public void addProcessInfo(String cmd, JTextArea textArea)
    {
        textArea.append(ENDL);
        textArea.append(ENDL);
        textArea.append(cmd);
        getStreamInfo(cmd, textArea);
    }

    public void updateProcessInfo(int pid)
    {
        synchronized(GET_DATA_LOCK)
        {
            try {
                String cmd = strPrefixCommand + "cat /proc/" + pid + "/";

                m_taProcessInfo.setFont(new Font((String)m_LogFilterMain.m_jcFontType.getSelectedItem(), Font.PLAIN, (Integer.parseInt(m_LogFilterMain.m_tfFontSize.getText()))));
                m_taProcessInfo.setText(cmd + "comm");
                getStreamInfo(cmd + "comm", m_taProcessInfo);

                addProcessInfo(cmd + "status",  m_taProcessInfo);
                addProcessInfo(cmd + "stack",   m_taProcessInfo);
                addProcessInfo(cmd + "sched",   m_taProcessInfo);
                addProcessInfo(cmd + "limits",  m_taProcessInfo);

                addProcessInfo(strPrefixCommand + "ls -al /proc/" + pid + "/task", m_taProcessInfo);
                addProcessInfo(strPrefixCommand + "dumpsys meminfo " + pid, m_taProcessInfo);
                m_taProcessInfo.setCaretPosition(0);
            }
            catch (Exception Ee) // 에러 처리
            {
                T.e("e = " + Ee);
            }
        }
    }

    public void sendProcessInfoCmd(String pid)
    {
        synchronized(GET_DATA_LOCK)
        {
            m_LogFilterMain.m_tbLogTable.changeSelection(m_LogFilterMain.m_tbLogTable.getRowCount(), 0, false, false);
            if(pid == null || pid.isEmpty() ||  ("".equals(pid.trim())))
            {
                m_LogFilterMain.uartSendCommand("ps");
            }
            else
            {
                String cmd = "cat /proc/" + pid + "/";

                m_LogFilterMain.uartSendCommand("echo ==========================");
                m_LogFilterMain.uartSendCommand(cmd + "comm");
                m_LogFilterMain.uartSendCommand("echo ==========================");
                m_LogFilterMain.uartSendCommand(cmd + "status");
                m_LogFilterMain.uartSendCommand("echo ==========================");
                m_LogFilterMain.uartSendCommand(cmd + "stack");
                m_LogFilterMain.uartSendCommand("echo ==========================");
                m_LogFilterMain.uartSendCommand(cmd + "sched");
                m_LogFilterMain.uartSendCommand("echo ==========================");
                m_LogFilterMain.uartSendCommand(cmd + "limits");

                m_LogFilterMain.uartSendCommand("echo ==========================");
                m_LogFilterMain.uartSendCommand("ls -al /proc/" + pid + "/task");
                m_LogFilterMain.uartSendCommand("echo ==========================");
                m_LogFilterMain.uartSendCommand("dumpsys meminfo " + pid);
                m_LogFilterMain.uartSendCommand("echo ==========================");
            }
        }
    }

    private void setDesign()
    {
/////////////////////////////////////////////////////////////////////////////////////////////
        JPanel buttonPanel  = new JPanel(new BorderLayout());
//        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));

        JLabel jlRefresh = new JLabel("One Click Update");
        m_chkRefresh   = new JCheckBox();
        m_chkRefresh.setSelected(false);
        m_chkRefresh.setEnabled(true);
        m_chkRefresh.setToolTipText("Off : Only Double Click");

        JButton btRefresh = new JButton("PS Refresh");
        btRefresh.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            runProcessDialogThread();
          }
        });

//        JButton btExit = new JButton("Exit");
//        btExit.addActionListener(new ActionListener() {
//          public void actionPerformed(ActionEvent e) {
//            m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_EXIT_PROC_INFO));
//            dispose();
//          }
//        });

        JButton btInfo = new JButton("Info");
        btInfo.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
                    try {
                        int row = SearchResultsTable.getSelectedRow();
                        int col = SearchResultsTable.getColumnModel().getColumnIndex("PID");
                        int pid = Integer.parseInt((String)SearchResultsTable.getValueAt(row, col));

                        updateProcessInfo(pid);
                    }
                    catch (Exception Ee) // 에러 처리
                    {
                        T.e("e = " + Ee);
                    }
          }
        });

        m_comboCommand = new JComboBox();
        m_comboCommand.setEditable(true);
//        m_comboCommand.addActionListener(m_alComboListener);
        m_comboCommand.setPreferredSize( new Dimension(300, 25) );
        m_comboCommand.insertItemAt("ps -Z -t -p -P -c", 0);
        m_comboCommand.insertItemAt("ps -t -p -P -c", 0);
        m_comboCommand.insertItemAt("ps", 0);
        m_comboCommand.setSelectedIndex(0);

        m_comboCommand.setMaximumRowCount(30);
        m_comboCommand.setAutoscrolls(true);
        m_comboCommand.setRenderer(new ComboBoxRenderer());

        JPanel jpInfo  = new JPanel(new BorderLayout());
        jpInfo.setLayout(new BoxLayout(jpInfo,BoxLayout.X_AXIS));
        jpInfo.add(btInfo);
        jpInfo.add(jlRefresh);
        jpInfo.add(m_chkRefresh);
        jpInfo.add(m_comboCommand);

        buttonPanel.add(btRefresh, BorderLayout.WEST);
        buttonPanel.add(jpInfo, BorderLayout.CENTER);
//        buttonPanel.add(btExit, BorderLayout.EAST);

/////////////////////////////////////////////////////////////////////////////////////////////
        JScrollPane psScrollPane = new JScrollPane(SearchResultsTable);
        psScrollPane.setPreferredSize( new Dimension(500, 250) );

        for (int column = 0; column < SearchResultsTable.getColumnCount(); column++) {
            SearchResultsTable.getColumn(SearchResultsTable.getColumnName(column)).setCellRenderer(m_crList);
            SearchResultsTable.getColumnModel().getColumn(column).setPreferredWidth(columnWidth[column]);
        }


        // Disable auto resizing
        SearchResultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        SearchResultsTable.setFillsViewportHeight(true);
        SearchResultsTable.setAutoCreateRowSorter(true);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("PID"),    0);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("PPID"),   1);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("NAME"),   2);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("LABEL"),  3);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("USER"),   4);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("WCHAN"),  5);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("PCY"),    6);
        SearchResultsTable.moveColumn(SearchResultsTable.getColumnModel().getColumnIndex("STATUS"), 7);

        listSelectionModel = SearchResultsTable.getSelectionModel();
        SearchResultsTable.setSelectionModel(listSelectionModel);

        SearchResultsTable.addMouseListener(new MouseAdapter(){
            public void mouseClicked( MouseEvent e )
            {
//                System.out.println("mouseClicked");
                if ( SwingUtilities.isLeftMouseButton( e ) )
                {
                    int row = SearchResultsTable.getSelectedRow();
                    int col = SearchResultsTable.getColumnModel().getColumnIndex("PID");
                    int pid = Integer.parseInt((String)SearchResultsTable.getValueAt(row, col));

                    if(m_chkRefresh.isSelected() || (e.getClickCount() == 2))
                        updateProcessInfo(pid);
                }
            }
        });

/////////////////////////////////////////////////////////////////////////////////////////////
        m_taProcessInfo.setEditable(false);
//        m_taProcessInfo.setFont(new Font("monospaced", Font.PLAIN, 12));

        JScrollPane infoScrollPane = new JScrollPane(m_taProcessInfo);
        infoScrollPane.setPreferredSize( new Dimension(500, 500) );

        getContentPane().add(buttonPanel,       BorderLayout.NORTH);
        getContentPane().add(psScrollPane,      BorderLayout.CENTER);
        getContentPane().add(infoScrollPane,    BorderLayout.SOUTH);

//        this.setSize(800,500);
//
//        // Determine the new location of the window
//        int w = this.getSize().width;
//        int h = this.getSize().height;
//        this.setLocation(pos.x + size.width - w, pos.y + size.height - h);

        this.addWindowListener(new WindowAdapter()
          {
            @Override
            public void windowClosing(WindowEvent e)
            {
              m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_EXIT_PROC_INFO));
              dispose();
            }
          });


        Point pos       = m_LogFilterMain.getLocation();
        Dimension size  = m_LogFilterMain.getSize();
//        this.setSize(1200,500);

        // Determine the new location of the window
//        int w = this.getSize().width;
//        int h = this.getSize().height;

        this.setLocation(pos.x + size.width/2, pos.y + 75);
        update_font_style();

    //    this.setModal(true);
        setAlwaysOnTop( true );
        this.pack();
        this.setVisible(true);
    }
}

class ComboBoxRenderer extends BasicComboBoxRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
        if (-1 < index) {
          list.setToolTipText(value.toString());
        }
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setFont(list.getFont());
      setText((value == null) ? "" : value.toString());
      return this;
    }
}

class ColorRenderer extends JLabel implements TableCellRenderer
{
    Font   m_font;

//  private String columnName;
  public ColorRenderer(String column)
  {
//    this.columnName = column;
    setOpaque(true);
  }

  public void set_font(Font font)
  {
//    this.columnName = column;
    m_font = font;
  }

  public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)
  {
//    Object columnValue=table.getValueAt(row,table.getColumnModel().getColumnIndex(columnName));

    setFont(m_font);

    if (value != null) setText(value.toString());

    if(isSelected)
    {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    }
    else
    {
      if(row%2==0){
        setBackground(new Color(255,255,160));
        setForeground(table.getForeground());
      }
      else
      {
        setBackground(table.getBackground());
        setForeground(table.getForeground());
      }
/*
      if (columnValue.equals("1")) setBackground(java.awt.Color.pink);
      if (columnValue.equals("2")) setBackground(java.awt.Color.green);
      if (columnValue.equals("3")) setBackground(java.awt.Color.red);
      if (columnValue.equals("4")) setBackground(java.awt.Color.blue);
*/
    }

    return this;
  }
}

class Help extends JDialog implements ActionListener {
  public static String ENDL = System.getProperty("line.separator");
  JTextArea helpTxt = new JTextArea();
  JButton btExit = new JButton("          Exit          ");

  public Help() {
    setTitle("Help");

    addWindowListener( new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e)
      {
        dispose();
        Runtime.getRuntime().gc();
        //System.gc();
      }
    });

    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new BoxLayout(helpPanel,BoxLayout.Y_AXIS));
//    helpPanel.setBackground(Color.orange);

    helpTxt.setEditable(false);
//    helpTxt.setBackground(Color.orange);
//    helpTxt.setForeground(Color.blue);

    JScrollPane helpScrollPane = new JScrollPane(helpTxt);
    helpScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//    helpScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    helpScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    helpScrollPane.setPreferredSize(new Dimension(500, 500));
//    helpScrollPane.setMinimumSize(new Dimension(10, 10));

    helpPanel.add(helpScrollPane, BorderLayout.SOUTH);

    btExit.addActionListener(this);
    helpPanel.add(btExit, BorderLayout.NORTH);

    getContentPane().add(helpPanel, BorderLayout.SOUTH);

    this.setSize(500,500);

    // Determine the new location of the window
    int w = this.getSize().width;
//    int h = this.getSize().height;
//    this.setLocation(pos.x + size.width - w, pos.y + size.height - h);
//    this.setLocation(pos.x-w, pos.y);

//    this.setModal(true);
    this.pack();
    this.setVisible(true);

//===========================================================================

    helpTxt.setText("");
    helpTxt.append("[File Open]" + ENDL);
    helpTxt.append("Alt+O" + ENDL);
    helpTxt.append("" + ENDL);

    helpTxt.append("[Tag Filter]" + ENDL);
    helpTxt.append("Alt+Left/Right Click : Show/Remove Tag" + ENDL);
    helpTxt.append("" + ENDL);

    helpTxt.append("[Bookmark]" + ENDL);
    helpTxt.append("Ctrl+F2/Double Click: Bookmark toggle" + ENDL);
    helpTxt.append("F2 : Pre bookmark" + ENDL);
    helpTxt.append("F3 : Next bookmark" + ENDL);
    helpTxt.append("" + ENDL);

    helpTxt.append("[Copy]" + ENDL);
    helpTxt.append("Ctrl+C : Row copy" + ENDL);
    helpTxt.append("Right Click : Cloumn copy" + ENDL);
    helpTxt.append("" + ENDL);

    helpTxt.append("[Help]" + ENDL);
    helpTxt.append("Alt+H" + ENDL);
    helpTxt.append("" + ENDL);

    helpTxt.append("[Command]" + ENDL);
    helpTxt.append("logcat -b main -v threadtime & logcat -b events -v threadtime & logcat -b radio -v threadtime" + ENDL);
    helpTxt.append("" + ENDL);
//===========================================================================
  }

  public void actionPerformed(ActionEvent e){
    if(e.getSource()==btExit)
    {
      dispose();
    }
  }
}

class SendBroadcast {
    static final int   DEFAULT_PORT     = 9888;
    static final int   MAX_PORT         = 9900;

    public SendBroadcast(int type , long param, int local_port) {
        for(int port = DEFAULT_PORT; port < MAX_PORT; port++) {
            if(port == local_port) continue;
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                clientSocket.setSoTimeout(10);  // 10ms

                InetAddress IPAddress = InetAddress.getByName("localhost");
                byte[] data = new byte[128];
                String sentence = null;
                switch(type)
                {
                    case 0: // Time Stamp
                        sentence = "TS ";
                        break;
                    case 1: // Line Number
                        sentence = "LN ";
                        break;
                }
                sentence += Long.toString(param) + " ";
                data = sentence.getBytes();
//                System.out.println("sendData:" + sentence);
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
                clientSocket.send(sendPacket);

                DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                clientSocket.receive(receivePacket);
                String modifiedSentence = new String(receivePacket.getData());
//                System.out.printf("ACK[%d]:%s\r\n", local_port, modifiedSentence);

                clientSocket.close();
            }
            catch (SocketTimeoutException e)
            {

            }
            catch (Exception e) // 에러 처리
            {
                T.e("e = " + e);
            }
        }
    }
}

class BroadcastServer {
    static final int   DEFAULT_PORT     = 9888;
    static final int   MAX_PORT         = 9900;

    static LogFilterMain    m_LogFilterMain;

    public static long      timeStamp = 0;
    public static long      lineNumber = 0;
    public static int       port = DEFAULT_PORT;

    public BroadcastServer(LogFilterMain filterMain) {
        m_LogFilterMain = filterMain;
        BroadcastServer();
    }

    private void BroadcastServer()
    {
        try {
            DatagramSocket serverSocket = new DatagramSocket(port);
//            serverSocket.setSoTimeout(100);  // 100ms

            byte[] data = new byte[128];
            while(true)
            {
                timeStamp = lineNumber = 0;
                DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                serverSocket.receive(receivePacket);

                String sentence = new String( receivePacket.getData());
                System.out.println("receiveData:" + sentence);
                StringTokenizer stk = new StringTokenizer(sentence, " ", false);
                String s = stk.nextToken().trim();
                if(s.equals("TS")) {
                    timeStamp = Long.parseLong(stk.nextToken().trim());
                    m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_GOTO_TIMESTAMP));
                } else if(s.equals("LN")) {
                    lineNumber = Long.parseLong(stk.nextToken().trim());
                    m_LogFilterMain.notiEvent(new INotiEvent.EventParam(INotiEvent.EVENT_GOTO_LINENUMBER));
                } else {
                    System.out.println("unknown: " + sentence);
                }

                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = sentence.toUpperCase();
                data = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
        }
        catch (BindException e)
        {
            System.out.printf("port=%d\r\n", port);
            T.e("e = " + e);
            if(port < MAX_PORT) {
                port++;
                BroadcastServer();
            }
        }
        catch (Exception e) // 에러 처리
        {
            T.e("e = " + e);
            e.printStackTrace();
            BroadcastServer();
        }
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public long getLineNumber()
    {
        return lineNumber;
    }
}


