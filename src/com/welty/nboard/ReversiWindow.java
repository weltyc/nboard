package com.welty.nboard;

import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.Grid;
import com.welty.nboard.gui.RadioGroup;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.thor.DatabaseData;
import com.welty.nboard.thor.ThorWindow;
import com.welty.othello.c.CReader;
import com.welty.othello.c.CWriter;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gdk.COsPosition;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main window class for the Reversi app. Controls interaction with the user, the engine, and the menu.
 * The ReversiData class processes this data.
 * <p/>
 * See the ReversiEngine class for a description of synchronization issues.
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 17, 2009
 * Time: 1:21:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReversiWindow extends JFrame implements OptionSource, EngineTalker {
    private ReversiEngine m_engine;
    // Pointer to application data. Needs to be listed early because constructors for some members make use of it.
    public ReversiData m_pd;

    private final ThorWindow m_pwThor;    //< Window where thor games are displayed
    private StatusBar m_statusBar;

    private MoveGrid m_pmg;
    private ReversiBoard m_prb;

    private JMenu m_depthMenu;
    private GameSelectionWindow m_pgsw;    //< Used in File/Open... dialog when there are multiple games in a file
    private Hints m_hints;
    private DatabaseData dd;

    DatabaseData PD() {
        return m_pwThor.PD();
    }

    // size of the board
    private final int n = 8;


    private static final int boardSize = 400;
    private static final int boardFrameWidth = 15;
    static final int boardFrameSize = boardSize + 2 * boardFrameWidth;
    private JMenuItem viewAlwaysShowEvals;
    private JMenuItem viewD2;
    private JMenuItem viewPhotoStyle;
    private JRadioButtonMenuItem viewHighlightLegal;
    private JRadioButtonMenuItem viewHighlightBest;
    private JMenuItem viewCoordinates;
    private JMenuItem engineLearnAll;
    private JMenuItem thorLookUpAll;

    private final java.util.List<Runnable> shutdownHooks = new ArrayList<>();
    private RadioGroup mode;
    private RadioGroup drawsTo;
    private RadioGroup engineTop;
    private final GgfFileChooser chooser;
/*
// Screen output locations and colors
final int scoreWidth=120;

final int top1=53;
final int bottom1=top1+boardSize;

final int top2=bottom1+5;
final int bottom2=top2+99;
Rectangle moveGridArea(5, top2, right0, bottom2);
*/


    ReversiWindow() {
        super("NBoard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                setVisible(false);
                for (Runnable hook : shutdownHooks) {
                    hook.run();
                }
            }
        });
        final String path = "small.PNG";
        final ImageIcon icon = getImage(path);
        setIconImage(icon.getImage());

        m_pd = new ReversiData(this, this);
        try {
            m_engine = new ReversiEngine(this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to start engine: " + e, "External Engine Error", JOptionPane.ERROR_MESSAGE);
        }
        chooser = new GgfFileChooser(this);
        setResizable(false);
        m_pgsw = new GameSelectionWindow(this);
        dd = new DatabaseData(this, m_pd);
        m_pwThor = new ThorWindow(this, m_pd, dd);
        m_pd.AddListener(new SignalListener<COsMoveListItem>() {

            public void handleSignal(COsMoveListItem data) {
                if (data != null) {
                    SendCommand("move " + data, true);
                } else {
                    SendSetGame();
                }
            }
        }

        );


        // engine initialization
        SendCommand("nboard 1", false);

        m_hints = new Hints();

        // and show the move grid
        m_pmg = new MoveGrid(m_pd, PD(), m_hints);

        Grid m_pml = new MoveList(m_pd);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        leftPanel.add(m_statusBar = new StatusBar(m_pd));
        leftPanel.add(new ScoreWindow(m_pd));
        leftPanel.add(m_prb = new ReversiBoard(m_pd, this, m_hints));
        leftPanel.add(m_pmg);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
        mainPanel.add(leftPanel);
        mainPanel.add(m_pml);
        add(mainPanel);

        ConstructMenus();
        m_statusBar.SetStatus("Loading Engine");

        m_pd.AddListener(m_hints);

        pack();
        setVisible(true);
    }

    public static ImageIcon getImage(String path) {
        java.net.URL imgURL = NBoard.class.getResource("images/" + path);
        Require.notNull(imgURL, "image url for " + path);
        final ImageIcon icon = new ImageIcon(imgURL);
        Require.notNull(icon, "icon");
        return icon;
    }

    /**
     * Construct the menus and display them in the window
     */
    void ConstructMenus() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createMenuItem("&File", createFileMenu()));
        menuBar.add(createMenuItem("&Edit", createEditMenu()));
        menuBar.add(createMenuItem("&View", createViewMenu()));

        menuBar.add(createMenuItem("E&ngine", createEngineMenu()));

        // set up the Thor menu
        JMenu m_thorMenu = new JMenu();
        m_thorMenu.add(createMenuItem("Load &games", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadGames();
            }
        }));
        m_thorMenu.add(createMenuItem("&Unload games", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().UnloadGames();
            }
        }));
        m_thorMenu.add(createMenuItem("Load &players", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadPlayers();
            }
        }));
        m_thorMenu.add(createMenuItem("Load &tournaments", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadTournaments();
            }
        }));
        m_thorMenu.addSeparator();
        m_thorMenu.add(createMenuItem("Load &config", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadConfig();
            }
        }));
        m_thorMenu.add(createMenuItem("&Save config", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().SaveConfig();
            }
        }));
        m_thorMenu.addSeparator();
        m_thorMenu.add(createMenuItem("&Look up position", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LookUpPosition();
            }
        }));
        thorLookUpAll = createCheckBoxMenuItem("Look up &all", "Thor/LookUpAll", true);
        m_thorMenu.add(thorLookUpAll);
        m_thorMenu.addSeparator();
        m_thorMenu.add(createMenuItem("Save Opening &Frequencies", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().SaveOpeningFrequencies();
            }
        }));

        menuBar.add(createMenuItem("&Database", m_thorMenu));

        setJMenuBar(menuBar);
    }

    private JMenu createViewMenu() {
        final ActionListener repaintBoard = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_prb.repaint();
            }
        };
        // set up the view menu
        JMenu m_viewMenu = new JMenu();
        m_viewMenu.add(viewPhotoStyle = createCheckBoxMenuItem("&Photo Style", "View/PhotoStyle", true, repaintBoard));
        m_viewMenu.add(viewAlwaysShowEvals = createCheckBoxMenuItem("Always show &evals", "View/AlwaysShowEvals", true, engineUpdater));

        m_viewMenu.add(viewD2 = createCheckBoxMenuItem("&D2B/D2W on board", "View/D2", false, repaintBoard));
        m_viewMenu.add(viewCoordinates = createCheckBoxMenuItem("&Coordinates", "View/Coordinates", true, repaintBoard));
        m_viewMenu.add(createCheckBoxMenuItem("&Tip of the Day", "View/Totd", true, repaintBoard));

        m_viewMenu.addSeparator();
        JRadioButtonMenuItem viewHighlightNone = createRadioButtonMenuItem("No highlighting", repaintBoard);
        viewHighlightLegal = createRadioButtonMenuItem("Highlight &Legal moves", repaintBoard);
        viewHighlightBest = createRadioButtonMenuItem("Highlight &Best move", repaintBoard);
        new RadioGroup(m_viewMenu, "View/Highlight", 1, shutdownHooks, viewHighlightNone, viewHighlightLegal, viewHighlightBest);
        return m_viewMenu;
    }

    private JMenu createEditMenu() {
        JMenu m_flipMenu = createFlipMenu();
        // set up the Edit menu
        JMenu m_editMenu = new JMenu();
        m_editMenu.add(createMenuItem("&Undo\tCtrl+Z", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Undo();
            }
        }));
        m_editMenu.addSeparator();
        m_editMenu.add(createMenuItem("&Copy Game\tCtrl+C", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SetClipboardText(m_pd.Game().toString());
            }
        }));

        m_editMenu.add(createMenuItem("Copy &Move List", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SetClipboardText(m_pd.Game().GetMoveList().toMoveListString());
            }
        }));

        m_editMenu.add(createMenuItem("Copy &Board", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuilder os = new StringBuilder();
                COsPosition displayedPosition = m_pd.DisplayedPosition();
                for (int row = 0; row < n; row++) {
                    for (int col = 0; col < n; col++) {
                        os.append(displayedPosition.board.Piece(row, col));
                    }
                    os.append("\n");
                }
                os.append(m_pd.Game().pos.board.CMover());
                SetClipboardText(os.toString());

            }
        }));
        m_editMenu.add(createMenuItem("&Paste Game\tCtrl+V", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String s = GetClipboardText();
                if (s != null) {
                    m_pd.Update(s, true);
                }
            }
        }));

        m_editMenu.add(createMenuItem("Paste Move &List", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String s = GetClipboardText();
                if (s != null) {
                    COsGame game = new COsGame();
                    game.SetDefaultStartPos();
                    game.SetMoveList(s);
                    m_pd.Update(game, true);
                }
            }
        }));

        m_editMenu.add(createMenuItem("Paste Board", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String s = GetClipboardText();
                if (s != null) {
                    CReader is = new CReader("8 " + s);
                    try {
                        COsGame game = new COsGame();
                        game.Clear();
                        game.SetDefaultStartPos();
                        game.posStart.board.In(is);
                        game.CalcCurrentPos();
                        m_pd.Update(game, true);
                    } catch (IllegalArgumentException ex) {
                        final String msg = (s.length() < 200 ? s + " is not a legal board" : "Not a legal board");
                        JOptionPane.showMessageDialog(ReversiWindow.this, msg, "Paste Board error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }));

        m_editMenu.addSeparator();
        m_editMenu.add(createMenuItem("Flip", m_flipMenu));
        m_editMenu.add(createMoveMenu());
        return m_editMenu;
    }

    private JMenuItem createMoveMenu() {
        final JMenu menu = new JMenu("Move");
        menu.add(createMenuItem("First\tup arrow", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.First();
            }
        }));
        menu.add(createMenuItem("Last\tdown arrow", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Last();
            }
        }));
        menu.add(createMenuItem("Back\tleft arrow", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Back();
            }
        }));
        menu.add(createMenuItem("Fore\tright arrow", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Fore();
            }
        }));
        return menu;
    }

    private JMenu createFileMenu() {
        // set up the File menu
        JMenu m_fileMenu = new JMenu();
        m_fileMenu.setMnemonic(KeyEvent.VK_F);
        m_fileMenu.add(createMenuItem("&New\tCtrl+N", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                m_pd.StartNewGame();
                // if the engine is self-playing it is really annoying to have it self-play again when you start
                // a new game. Reset mode to user plays in this case.
                if (mode.getIndex() == 3) {
                    mode.setIndex(0);
                }
            }
        }));
        m_fileMenu.add(createMenuItem("&Open\tCtrl+O", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // note: We continue to receive windows messages in the GetOpenFilename() function.
                final File file = chooser.open();
                if (file != null) {
                    OpenFile(file);
                }
            }
        }));
        m_fileMenu.add(createMenuItem("&Save\tCtrl+S", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Save(false);
            }
        }));
        m_fileMenu.add(createMenuItem("&Append\tCtrl+A", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Save(true);
            }
        }));
        m_fileMenu.addSeparator();
        m_fileMenu.add(createMenuItem("E&xit", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getToolkit().getSystemEventQueue().postEvent(new WindowEvent(ReversiWindow.this, WindowEvent.WINDOW_CLOSING));
            }
        }));
        return m_fileMenu;
    }

    private DatabaseData PDD() {
        return m_pwThor.PDD();
    }

    private static void SetClipboardText(String s) {
        StringSelection ss = new StringSelection(s);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }

    private static @Nullable String GetClipboardText() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) t.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            // just return null
        }
        return null;
    }


    private void Save(boolean append) {
        File file = chooser.save();
        if (file != null) {
            if (!file.getName().endsWith(".ggf")) {
                file = new File(file.getAbsolutePath() + ".ggf");
            }
            final CWriter out = new CWriter(file, append);
            out.println(m_pd.Game());
            out.close();
        }
    }

    private JMenu createEngineMenu() {
        // set up the Engine menu
        JMenu menu = new JMenu();

        ActionListener modeSetter = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SetMode(mode.getIndex());
            }
        };

        mode = new RadioGroup(menu, "Engine/Mode", 1, shutdownHooks,
                createRadioButtonMenuItem("&User plays both", modeSetter),
                createRadioButtonMenuItem("User plays &Black", modeSetter),
                createRadioButtonMenuItem("User plays &White", modeSetter),
                createRadioButtonMenuItem("&Engine plays both", modeSetter)
        ) {
            @Override public int readIndex() {
                // it's really annoying to have engine/engine matches on startup. Switch to user/user in this case
                final int mode = Math.max(0, Math.min(super.getIndex(), 2));
                SetMode(mode, false);
                return mode;
            }
        };

        menu.addSeparator();

        menu.add(engineLearnAll = createCheckBoxMenuItem("Learn &all completed games", "Engine/LearnAll", false));
        menu.add(createMenuItem("Learn &this game", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TellEngineToLearn();
            }
        }));
        menu.addSeparator();

        ActionListener contemptSetter = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SendSetContempt();
            }
        };
        drawsTo = new RadioGroup(menu, "Engine/DrawsTo", 1, shutdownHooks,
                createRadioButtonMenuItem("Draws to Black", contemptSetter),
                createRadioButtonMenuItem("Draws = 0", contemptSetter),
                createRadioButtonMenuItem("Draws to White", contemptSetter)
        );

        // top n list
        menu.addSeparator();

        engineTop = new RadioGroup(menu, "Engine/Top", 2, shutdownHooks,
                createRadioButtonMenuItem("Value >=1 move", engineUpdater),
                createRadioButtonMenuItem("Value >=2 moves", engineUpdater),
                createRadioButtonMenuItem("Value >=4 moves", engineUpdater),
                createRadioButtonMenuItem("Value all moves", engineUpdater)
        );

        createDepthMenu();

        menu.addSeparator();
        menu.add(createMenuItem("&Depth", m_depthMenu));

        return menu;
    }

    private void createDepthMenu() {
        // set up the depth menu
        m_depthMenu = new JMenu();
        DepthRadioGroup depth = new DepthRadioGroup(this, m_depthMenu, shutdownHooks);
        SendCommand("set depth " + depth.getDepth(), false);
    }

    private JMenu createFlipMenu() {
        class BoardFlipper implements ActionListener {
            private final int iReflection;

            BoardFlipper(int iReflection) {
                this.iReflection = iReflection;
            }

            public void actionPerformed(ActionEvent e) {
                m_pd.ReflectGame(iReflection);
            }
        }
        // set up the Flip menu
        JMenu m_flipMenu = new JMenu();
        m_flipMenu.add(createMenuItem("F5/C4 Turn 180�", new BoardFlipper(3)));
        m_flipMenu.add(createMenuItem("F5/E6 Flop", new BoardFlipper(4)));
        m_flipMenu.add(createMenuItem("F5/D3 Flop", new BoardFlipper(7)));
        m_flipMenu.addSeparator();
        m_flipMenu.add(createMenuItem("Flip Horizontally", new BoardFlipper(2)));
        m_flipMenu.add(createMenuItem("Flip Vertically", new BoardFlipper(1)));
        m_flipMenu.add(createMenuItem("Turn 90� Right", new BoardFlipper(6)));
        m_flipMenu.add(createMenuItem("Turn 90� Left", new BoardFlipper(5)));
        return m_flipMenu;
    }

    static JRadioButtonMenuItem createRadioButtonMenuItem(String textAndAccelerator, ActionListener... listeners) {
        final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
        updateMenuItem(textAndAccelerator, menuItem);
        addActionListeners(menuItem, listeners);
        return menuItem;
    }

    private JMenuItem createCheckBoxMenuItem(String textAndAccelerator, final String key, boolean defaultChecked, ActionListener... listeners) {
        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
        final boolean isChecked = NBoard.RegistryReadU4(key, defaultChecked ? 1 : 0) != 0;
        menuItem.setSelected(isChecked);
        addActionListeners(menuItem, listeners);
        shutdownHooks.add(new Runnable() {
            public void run() {
                NBoard.RegistryWriteU4(key, menuItem.isSelected() ? 1 : 0);
            }
        });

        return updateMenuItem(textAndAccelerator, menuItem);
    }

    private static void addActionListeners(JMenuItem menuItem, ActionListener... listeners) {
        for (ActionListener listener : listeners) {
            menuItem.addActionListener(listener);
        }
    }

    private JMenuItem createMenuItem(String textAndAccelerator, ActionListener... listeners) {
        final JMenuItem menuItem = new JMenuItem();
        for (ActionListener listener : listeners) {
            menuItem.addActionListener(listener);
        }
        return updateMenuItem(textAndAccelerator, menuItem);
    }

    private static JMenuItem updateMenuItem(String textAndAccelerator, JMenuItem menuItem) {
        String[] parts = textAndAccelerator.split("\t");
        int iMnemonic = parts[0].indexOf('&');
        final String name = parts[0].replace("&", "");
        menuItem.setText(name);
        if (iMnemonic >= 0) {
            final char mnemonic = Character.toUpperCase(name.charAt(iMnemonic));
            menuItem.setMnemonic(mnemonic);
        }
        if (parts.length > 1) {
            final String acc = parts[1];
            if (acc.startsWith("Ctrl+")) {
                final char accChar = Character.toUpperCase(acc.charAt(5));
                menuItem.setAccelerator(KeyStroke.getKeyStroke(accChar, InputEvent.CTRL_MASK));
            } else if (acc.equals("up arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
                menuItem.setIcon(getImage("first.GIF"));
            } else if (acc.equals("down arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
                menuItem.setIcon(getImage("last.GIF"));
            } else if (acc.equals("left arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
                menuItem.setIcon(getImage("undo.GIF"));
            } else if (acc.equals("right arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
                menuItem.setIcon(getImage("redo.GIF"));
            } else {
                throw new IllegalArgumentException("bad accelerator : " + acc);
            }
        }
        return menuItem;
    }

    private JMenu createMenuItem(String textAndAccelerator, JMenu menu) {
        String[] parts = textAndAccelerator.split("\t");
        Require.eq(parts.length, "# of parts", 1);
        int iMnemonic = parts[0].indexOf('&');
        final String name = parts[0].replace("&", "");
        if (iMnemonic >= 0) {
            final char mnemonic = Character.toUpperCase(name.charAt(iMnemonic));
            menu.setMnemonic(mnemonic);
        }
        menu.setText(name);
        return menu;
    }

    /**
     * Open the file in the given filename.
     * <p/>
     * If there is only 1 game in the file, immediately load to the board.
     * if there are multiple games, open a GameSelectionWindow with the games in the file.
     */
    void OpenFile(final File file) {
        try {
            CReader is = new CReader(file);
            COsGame game = new COsGame();
            game.In(is);
            // check if this file has multiple games
            if (is.ignoreTo('(')) {
                m_pgsw.LoadAndShow(file);
            } else {
                m_pd.Update(game, true);
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Unable to load game from file '" + file + "'", e.toString(), JOptionPane.WARNING_MESSAGE);
        }
    }


    /**
     * The displayed position changed, and the engine should learn the game if it the game is complete and
     * EngineLearnAll is true
     */
    public void MayLearn() {
        if (m_pd.Game().pos.board.GameOver()) {
            if (engineLearnAll.isSelected()) {
                TellEngineToLearn();
            }
        }
        SendSetGame();
    }


    /**
     * @return true if the user is in study mode (i.e. reviewing a game, or playing human/human or engine/engine)
     */
    public boolean IsStudying() {
        final int nMode = mode.getIndex();
        return m_pd.Reviewing() || (nMode == 0 || nMode == 3);
    }

    /**
     * @return true if the eval should be displayed even in game mode
     */
    public boolean AlwaysShowEvals() {
        return viewAlwaysShowEvals.isSelected();
    }

    /**
     * @return true if the engine's evaluations should be displayed
     *         <p/>
     *         Currently the are displayed only if IsStudying() || AlwaysShowEvals().
     */
    public boolean ShowEvals() {
        return IsStudying() || AlwaysShowEvals();
    }

    /**
     * On a move message posted to the window (e.g. from an engine).
     */
    void OnMessageFromEngine(String string) {
        final CReader is = new CReader(string);
        String sCommand = is.readString();
        is.ignoreWhitespace();

        if (sCommand.equals("pong")) {
            int n;
            try {
                n = is.readInt();
            } catch (EOFException e) {
                throw new IllegalStateException("Engine response is garbage : " + string);
            }
            m_engine.SetPong(n);
            if (m_engine.IsReady()) {
                SetStatus("");
                TellEngineWhatToDo();
            }
        } else if (sCommand.equals("status")) {
            // the engine is busy and is telling the user why
            SetStatus(is.readLine());
        } else if (sCommand.equals("set")) {
            String variable = is.readString();
            if (variable.equals("myname")) {
                String sName = is.readString();
                m_engine.SetName(sName);
            }
        }
        // These commands are only used if the computer is up-to-date
        else if (m_engine.IsReady()) {
            switch (sCommand) {
                case "===":
                    SetStatus("");
                    // Need to check whether it's the computer's move. This is because the user may have
                    // switched modes while the computer was thinking.
                    if (!UsersMove()) {
                        // now update the move list
                        COsMoveListItem mli = new COsMoveListItem();
                        mli.In(is);
                        try {
                            m_pd.Update(mli, false);
                        } catch (IllegalArgumentException e) {
                            JOptionPane.showMessageDialog(this, "Illegal move from engine: " + mli, "Engine Error", JOptionPane.WARNING_MESSAGE);
                        }

                    }
                    break;
                // computer giving hints
                case "book": {
                    // if the engine is going to move from book, then don't display the book moves
                    // as it just makes the screen flicker.
                    boolean fBlackMove = m_pd.Game().pos.board.fBlackMove;
                    m_hints.Add(is, fBlackMove, true);
                    break;
                }
                case "search": {
                    boolean fBlackMove = m_pd.Game().pos.board.fBlackMove;
                    m_hints.Add(is, fBlackMove, false);
                    break;
                }
                case "learn":
                    SetStatus("");
                    break;
            }
        }
    }


    /**
     * @return true if the user is playing the given color
     */
    public boolean UserPlays(boolean fBlack) {
        return ((~mode.getIndex() >> (fBlack ? 1 : 0)) & 1) != 0;
    }

    /**
     * @return true if it's the user's move
     */
    public boolean UsersMove() {
        return UserPlays(m_pd.DisplayedPosition().board.fBlackMove);
    }

//* Message Dispatcher. This is for all messages that don't have their own OnXxx() handler.
//boolean OnUnknownMessage(int msg, void* wParam, void* lParam) {
//	if (msg==ReversiEngine::s_msgFromEngine) {
//		OnMessageFromEngine(wParam);
//		return true;
//	}
//	else if (msg==MoveGrid::s_commandMove) {
//		if (UsersMove()) {
//			COsMove mv=*(COsMove*)wParam;
//			m_pd.Update(mv, true);
//		}
//		return true;
//	}
//	else
//		return false;
//}

    //

    /**
     * Ping the engine. Send the current displayed position to the engine.
     */
    void SendSetGame() {
        SendSetGame(m_pd.IMove());
    }

    /**
     * Ping the engine. Set the position as after move iMove to the engine.
     */
    void SendSetGame(int iMove) {
        COsGame displayedGame = new COsGame(m_pd.Game());
        final int nMoves = displayedGame.ml.size();
        if (iMove < nMoves) {
            displayedGame.Undo(nMoves - iMove);
        }
        SendCommand("set game " + displayedGame, true);
    }

    void SetMode(int mode) {
        SetMode(mode, true);
    }

    /**
     * Set the engine's mode, update the radio buttons on the menu, and save the mode in m_mode
     *
     * @param updateUsers true if various objects should be notified; false during startup
     */
    void SetMode(int mode, boolean updateUsers) {
        m_pd.SetNames(getPlayerName((mode & 1) != 0), getPlayerName(((mode & 2) != 0)), updateUsers);
        if (updateUsers) {
            TellEngineWhatToDo();
        }
    }

    private String getPlayerName(boolean enginePlays) {
        return enginePlays ? m_engine.GetName() : System.getProperty("user.name");
    }

    void SetEngineDepth(int newDepth) {
        m_engine.SendCommand("set depth " + newDepth, false);
        m_hints.Clear();

        // engine has changed so we might want a hint
        TellEngineWhatToDo();
    }
//* Set the engine's contempt factor and update menu items.
//void SetDrawTo(final int drawsTo) {
//	if (m_drawsTo!=drawsTo) {
//		// put a radio button by the new drawsTo, and remove the one by the old drawsTo
//		m_engineMenu.ItemFromCommand(commandEngineDrawToBlack+m_drawsTo).SetChecked(false);
//		m_engineMenu.ItemFromCommand(commandEngineDrawToBlack+drawsTo).SetChecked(true);
//
//		// set m_drawsTo
//		m_drawsTo=drawsTo;
//		SendSetContempt();
//	}
//}

    /**
     * Set the engine's contempt factor so it can avoid or seek draws
     * <p/>
     * Sets it based on the drawsTo member
     */
    void SendSetContempt() {
        SendCommand("set contempt " + 100 * (1 - drawsTo.getIndex()), false);
    }

    /**
     * Tell the engine to learn the game, and perform associated tasks
     * <p/>
     * The engine learns the complete game, not just the portion up to the review point.
     * Also update the hints afterwards.
     */
    public void TellEngineToLearn() {
        // If we're reviewing we want the engine to learn the complete game,
        // not just the game up to the point where we're reviewing.
        // We alter iMove and restore it afterwards.
        boolean fReviewing = m_pd.Reviewing();
        if (fReviewing) {
            SendSetGame(m_pd.Game().ml.size());
        }

        // Tell the engine to learn the game
        SendCommand("learn", false);

        // reset the stored review point. The engine will update hints as a result.
        SendSetGame();
    }

    private static final int[] engineTops = {1, 2, 4, 64};

    /**
     * Tell the engine "go" or "hint" if it is ready, or "learn" if the game is over
     * <p/>
     * If the engine is not ready, it will get this information once it is caught up because we call
     * this routine from the "pong" command handler.
     * If the engine is ready, we will call it as soon as the board is updated.
     */
    void TellEngineWhatToDo() {
        if (m_engine.IsReady()) {
            String sCommand;
            if (m_pd.DisplayedPosition().board.GameOver()) {
                // do nothing. learning is handled in the Update function now to ensure
                // that the engine is learning the right game, and learning it just once.
                return;
            }
            // If the user is reviewing the game, the computer gives hints
            else if (m_pd.Reviewing()) {
                sCommand = "hint";
            }
            // If it's the engine's move, he should move.
            else if (!UsersMove()) {
                sCommand = "go";
                SetStatus("Thinking");
            }
            // If it's the user's move, get a hint if we're showing evals.
            else if (ShowEvals()) {
                sCommand = "hint";
            }
            // If it's the user's move and we're not showing evals, don't waste CPU time.
            else {
                return;
            }

            // for hints, figure out how many we want
            if (sCommand.equals("hint")) {
                sCommand = "hint " + engineTops[engineTop.getIndex()];
            }

            // the engine will now give us new hints, so delete the old ones
            m_hints.Clear();
            m_engine.SendCommand(sCommand, false);
        }
    }

    /**
     * Send the command to the engine. If the board is being updated, also tell the engine what to do.
     */
    void SendCommand(final String sCommand, boolean fUpdateBoard) {
        m_engine.SendCommand(sCommand, fUpdateBoard);
        if (fUpdateBoard)
            TellEngineWhatToDo();
    }

//* Handler for File menu commands
//void OnFileCommand(char command) {
//	static String fn;	// filename for open and save commands
//
//	switch(command) {
//		case commandFileAppend:
//			// note: We continue to receive windows messages in the GetSaveFilename() function.
//			if (Z::GetSaveFilename(fn, "ggf", OFN_PATHMUSTEXIST)) {
//				std::ofstream os(fn.c_str(), std::ios::app | std::ios::out);
//				os << m_pd.Game() << "\n";
//			}
//			break;
//		case commandFileExit:
//			SendMessage(WM_CLOSE);
//			break;
//	}
//}
//
//* Handler for Edit menu commands, and moving through the game
//void OnEditCommand(char command) {
//	switch(command) {
//		case commandEditUndo:
//			Undo();
//			break;
//		case commandEditFirst:
//			m_pd.First();
//			break;
//		case commandEditBack:
//			m_pd.Back();
//			break;
//		case commandEditFore:
//			m_pd.Fore();
//			break;
//		case commandEditLast:
//			m_pd.Last();
//			break;
//		case commandEditReflect1:
//		case commandEditReflect2:
//		case commandEditReflect3:
//		case commandEditReflect4:
//		case commandEditReflect5:
//		case commandEditReflect6:
//		case commandEditReflect7:
//			m_pd.ReflectGame(command-commandEditReflect0);
//			break;
//	}
//}
//
//* Handler for Engine menu commands
//void OnEngineCommand(char command) {
//	switch(command) {
//		case commandEngineMode0:
//		case commandEngineMode1:
//		case commandEngineMode2:
//		case commandEngineMode3:
//			SetMode(command-commandEngineMode0);
//
//			// mode change can affect whether we display evals
//			m_prb.Invalidate();
//
//			break;
//		case commandEngineDrawToBlack:
//		case commandEngineDrawNeutral:
//		case commandEngineDrawToWhite:
//			SetDrawTo(command-commandEngineDrawToBlack);
//			break;
//		default:
//			OnEngineTopCommand(command);
//			break;
//	}
//}
//
//* Handler for Depth menu commands.
//* command==commandDepth is user defined level, otherwise set depth=command-commandDepth
//void OnDepthCommand(char command) {
//	int newDepth=command-commandDepth;
//	if (newDepth==0) {
//		// get depth from a message box
//		std::ostringstream os;
//		os << m_userDefinedDepth;
//		String s=os.str();
//		if (!Z::InputMessageBox(this, "Midgame search depth (2-60):", "Depth", s, MB_OKCANCEL))
//			// user clicked cancel
//			return;
//
//		std::istringstream is(s);
//		int userDefinedDepth;
//		is >> userDefinedDepth;
//		if (userDefinedDepth<2 || userDefinedDepth>60) {
//			// depth out of range
//			Z::MessageBox(IDS_BAD_DEPTH, IDS_WARNING, MB_OK | MB_ICONINFORMATION);
//			return;
//		}
//
//		m_userDefinedDepth=userDefinedDepth;
//		newDepth=m_userDefinedDepth;
//	}
//	if (newDepth!=m_depth) {
//		m_depthMenu.ItemFromCommand(DepthMenuCommand(m_depth)).SetChecked(false);
//		m_depthMenu.ItemFromCommand(DepthMenuCommand(newDepth)).SetChecked(true);
//		std::ostringstream os;
//	}
//}
//
//* Handler for Depth menu commands
//void OnEngineTopCommand(char command) {
//	int newEngineTop=command-commandEngineTop;
//	if (newEngineTop!=m_depth) {
//		m_engineMenu.ItemFromCommand(commandEngineTop+m_engineTop).SetChecked(false);
//		m_engineMenu.ItemFromCommand(commandEngineTop+newEngineTop).SetChecked(true);
//		m_engineTop=newEngineTop;
//		m_pd.m_hints.Clear();
//
//		// engine has changed so we might want a hint
//		TellEngineWhatToDo();
//	}
//}
//
//* Handler for view menu commands
//void OnViewCommand(char command) {
//	switch(command) {
//		case commandViewPhotoStyle:
//			m_viewMenu.ItemFromCommand(commandViewPhotoStyle).ToggleChecked();
//			m_prb.Invalidate();
//			break;
//		case commandViewAlwaysShowEvals:
//			m_viewMenu.ItemFromCommand(commandViewAlwaysShowEvals).ToggleChecked();
//			m_prb.Invalidate();
//			break;
//		case commandViewD2:
//			m_viewMenu.ItemFromCommand(commandViewD2).ToggleChecked();
//			m_prb.Invalidate();
//			break;
//		case commandViewCoordinates:
//			m_viewMenu.ItemFromCommand(commandViewCoordinates).ToggleChecked();
//			m_prb.Invalidate();
//			break;
//		case commandViewTotd:
//			m_viewMenu.ItemFromCommand(commandViewTotd).ToggleChecked();
//			break;
//		case commandViewHighlightNone:
//			m_viewMenu.ItemFromCommand(commandViewHighlightNone).SetChecked(true);
//			m_viewMenu.ItemFromCommand(commandViewHighlightLegal).SetChecked(false);
//			m_viewMenu.ItemFromCommand(commandViewHighlightBest).SetChecked(false);
//			m_prb.Invalidate();
//			break;
//		case commandViewHighlightLegal:
//			m_viewMenu.ItemFromCommand(commandViewHighlightNone).SetChecked(false);
//			m_viewMenu.ItemFromCommand(commandViewHighlightLegal).SetChecked(true);
//			m_viewMenu.ItemFromCommand(commandViewHighlightBest).SetChecked(false);
//			m_prb.Invalidate();
//			break;
//		case commandViewHighlightBest:
//			m_viewMenu.ItemFromCommand(commandViewHighlightNone).SetChecked(false);
//			m_viewMenu.ItemFromCommand(commandViewHighlightLegal).SetChecked(false);
//			m_viewMenu.ItemFromCommand(commandViewHighlightBest).SetChecked(true);
//			m_prb.Invalidate();
//			break;
//		default:
//			break;
//	}
//}
//
//* Handler for Thor menu commands
//void OnThorCommand(char command) {
//	String fn;
//
//	switch(command) {
//		case commandThorLoadGames:
//			if (PD().LoadGames()) {
//				m_thorMenu.ItemFromCommand(commandThorLoadGames).SetChecked(PD().NGames()!=0);
//				ThorLookUpPosition();
//				m_pwThor.ShowIfReady();
//			}
//			break;
//		case commandThorLoadPlayers:
//			if (PD().LoadPlayers()) {
//				m_thorMenu.ItemFromCommand(commandThorLoadPlayers).SetChecked(PD().NPlayers()!=0);
//				m_pwThor.ShowIfReady();
//			}
//			break;
//		case commandThorLoadTournaments:
//			if (PD().LoadTournaments()) {
//				m_thorMenu.ItemFromCommand(commandThorLoadTournaments).SetChecked(PD().NTournaments()!=0);
//				m_pwThor.ShowIfReady();
//			}
//			break;
//		case commandThorUnloadGames:
//			PD().UnloadGames();
//			m_thorMenu.ItemFromCommand(commandThorLoadGames).SetChecked(PD().NGames()!=0);
//			ThorLookUpPosition();
//			m_pwThor.Show(false);
//			break;
//		case commandThorSaveConfig:
//			if (PD().IsReady()) {
//				PD().SaveConfig();
//			}
//			else {
//				Z::MessageBox(IDS_BAD_STORE_CONFIG, IDS_STORE_CONFIG, MB_OK);
//			}
//			break;
//		case commandThorLoadConfig:
//			if (PD().LoadConfig()) {
//				ThorLookUpPosition();
//				m_pwThor.ShowIfReady();
//				m_thorMenu.ItemFromCommand(commandThorLoadGames).SetChecked(PD().NGames()!=0);
//				m_thorMenu.ItemFromCommand(commandThorLoadPlayers).SetChecked(PD().NPlayers()!=0);
//				m_thorMenu.ItemFromCommand(commandThorLoadTournaments).SetChecked(PD().NTournaments()!=0);
//			}
//			else {
//				Z::MessageBox(IDS_BAD_LOAD_CONFIG, IDS_LOAD_CONFIG, MB_OK);
//			}
//			break;
//		case commandThorLookUpPosition:
//			ThorLookUpPosition();
//			break;
//		case commandThorLookUpAll:
//			{
//				Z::MenuItem mi=m_thorMenu.ItemFromCommand(commandThorLookUpAll);
//				if (mi.ToggleChecked()) {
//					ThorLookUpPosition();
//				}
//			}
//			break;
//		case commandThorSaveOpeningFrequencies:
//			PD().SaveOpeningFrequencies();
//			break;
//	}
//}

    //

    /**
     * Look up the displayed position in the currently loaded thor database
     */
    void ThorLookUpPosition() {
        dd.LookUpPosition();
        m_pmg.UpdateHints();
    }

//* On any menu commands for a ReversiWindow.
//boolean OnCommand(char command) {
//	switch(command>>8) {
//		case 1: OnFileCommand(command); break;
//		case 2: OnEditCommand(command); break;
//		case 3: OnEngineCommand(command); break;
//		case 4: OnDepthCommand(command); break;
//		case 5: OnViewCommand(command); break;
//		case 6: OnThorCommand(command); break;
//	}
//
//	return false;
//}
//
//* Terminate the engine and then close the window normally.
//* Saves options to the registry
//* This needs to happen here.
//* the menus won't exist if ~ReversiWindow is called after OnNcDestroy.
//* In fact, they don't seem to exist even in OnNcDestroy on Win98 (although they do in WinXP)
//boolean OnClose() {
//	m_engine.Terminate();
//
//	boolean fEngineLearnAll=m_engineMenu.ItemFromCommand(commandEngineLearnAll).GetChecked();
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"Engine", "LearnAll", fEngineLearnAll);
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"View", "PhotoStyle", ViewPhotoStyle());
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"View", "AlwaysShowEvals", AlwaysShowEvals());
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"View", "D2", ViewD2());
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"View", "Highlight", IHighlight());
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"View", "Coordinates", ViewCoordinates());
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"View", "Totd", ViewTotd());
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"Engine", "Mode", m_mode);
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"Engine", "DrawsTo", m_drawsTo);
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"Engine", "Depth", m_depth);
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"Engine", "UserDefinedDepth", m_userDefinedDepth);
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"Engine", "Top", m_engineTop);
//	Z::RegistryWriteU4(HKEY_CURRENT_USER, sRegKey+"Thor", "LookUpAll", ThorLookUpAll());
//
//	return Tlw::OnClose();
//}
//
//* Handle arrow keys
//boolean OnKeyDown(int vk, int code) {
//	char command=0;
//
//	switch(vk) {
//		case VK_LEFT:	command=commandEditBack;	break;
//		case VK_RIGHT:	command=commandEditFore;	break;
//		case VK_UP:		command=commandEditFirst;	break;
//		case VK_DOWN:	command=commandEditLast;	break;
//	}
//
//	if (command) {
//		SendMessage(WM_COMMAND, command, NULL);
//	}
//	return command!=0;
//}
//

    /**
     * @return true if we should use photo-style pieces
     */
    public boolean ViewPhotoStyle() {
        return viewPhotoStyle.isSelected();
    }

    /**
     * @return true if evals should be displayed with D2B/D2W on the board, false if just D=0 value
     */
    public boolean ViewD2() {
        return viewD2.isSelected();
    }

    /**
     * @return 1 if legal moves should be highlighted on the board, 2 if the best move should be highlighted
     */
    public int IHighlight() {
        if (viewHighlightLegal.isSelected())
            return 1;
        else if (viewHighlightBest.isSelected())
            return 2;
        else
            return 0;
    }

    /**
     * @return true if legal moves should be displayed on the board
     */
    public boolean ViewCoordinates() {
        return viewCoordinates.isSelected();
    }

//* Return true if "Tip of the Day" should be displayed on startup
//boolean ViewTotd() final {
//	return m_viewMenu.ItemFromCommand(commandViewTotd).GetChecked();
//}

    /**
     * @return true if all positions should be looked up in the Thor database
     */
    public boolean ThorLookUpAll() {
        return thorLookUpAll.isSelected();
    }

    /**
     * @return true if the engine should learn all completed games
     */
    public boolean EngineLearnAll() {
        return engineLearnAll.isSelected();
    }

    void SetStatus(String status) {
        m_statusBar.SetStatus(status);
    }

    public void BringToTop() {
        if (getExtendedState() == Frame.ICONIFIED) {
            setExtendedState(Frame.NORMAL);
        }
        setVisible(true);
        toFront();
        repaint();
    }

    private final ActionListener engineUpdater = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            TellEngineWhatToDo();
            m_prb.repaint();
        }
    };

    public String getEngineName() {
        return m_engine.GetName();
    }
}
