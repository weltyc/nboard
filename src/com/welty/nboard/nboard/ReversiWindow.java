package com.welty.nboard.nboard;

import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.Grid;
import com.welty.nboard.gui.RadioGroup;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.thor.DatabaseData;
import com.welty.nboard.thor.ThorWindow;
import com.welty.othello.c.CReader;
import com.welty.othello.c.CWriter;
import com.welty.othello.core.OperatingSystem;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gdk.COsPosition;
import com.welty.othello.gui.selector.GuiOpponentSelector;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.welty.nboard.gui.MenuItemBuilder.menuItem;

/**
 * Main window class for the Reversi app. Controls interaction with the user, the engine, and the menu.
 * The ReversiData class processes this data.
 * <p/>
 * See the ReversiEngine class for a description of synchronization issues.
 */
public class ReversiWindow extends JFrame implements OptionSource, EngineTalker, ReversiEngine.Listener {
    private ReversiEngine m_engine;
    // Pointer to application data. Needs to be listed early because constructors for some members make use of it.
    public final ReversiData m_pd;

    private final ThorWindow m_pwThor;    //< Window where thor games are displayed
    private final StatusBar m_statusBar;

    private final MoveGrid m_pmg;
    private final ReversiBoard m_prb;

    private final GameSelectionWindow m_pgsw;    //< Used in File/Open... dialog when there are multiple games in a file
    private final Hints m_hints;
    private final DatabaseData dd;

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
        final ImageIcon icon = NBoard.getImage(path);
        setIconImage(icon.getImage());

        m_pd = new ReversiData(this, this);
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

        m_pd.AddListener(m_hints);

        pack();
        setVisible(true);


        // engine initialization - do this after we've constructed the windows for
        // the responses to be displayed in
        m_statusBar.SetStatus("Loading Engine");
        try {
            m_engine = new ReversiEngine(GuiOpponentSelector.getInstance(), this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to start engine: " + e, "External Engine Error", JOptionPane.ERROR_MESSAGE);
        }
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
        m_thorMenu.add(menuItem("Load &games").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadGames();
            }
        }));
        m_thorMenu.add(menuItem("&Unload games").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().UnloadGames();
            }
        }));
        m_thorMenu.add(menuItem("Load &players").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadPlayers();
            }
        }));
        m_thorMenu.add(menuItem("Load &tournaments").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadTournaments();
            }
        }));
        m_thorMenu.addSeparator();
        m_thorMenu.add(menuItem("Load &config").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LoadConfig();
            }
        }));
        m_thorMenu.add(menuItem("&Save config").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().SaveConfig();
            }
        }));
        m_thorMenu.addSeparator();
        m_thorMenu.add(menuItem("&Look up position").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PDD().LookUpPosition();
            }
        }));
        thorLookUpAll = createCheckBoxMenuItem("Look up &all", "Thor/LookUpAll", true);
        m_thorMenu.add(thorLookUpAll);
        m_thorMenu.addSeparator();
        m_thorMenu.add(menuItem("Save Opening &Frequencies").build(new ActionListener() {
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
        JRadioButtonMenuItem viewHighlightNone = menuItem("No highlighting").buildRadioButton(repaintBoard);
        viewHighlightLegal = menuItem("Highlight &Legal moves").buildRadioButton(repaintBoard);
        viewHighlightBest = menuItem("Highlight &Best move").buildRadioButton(repaintBoard);
        new RadioGroup(m_viewMenu, "View/Highlight", 1, shutdownHooks, viewHighlightNone, viewHighlightLegal, viewHighlightBest);
        return m_viewMenu;
    }

    private JMenu createEditMenu() {
        JMenu m_flipMenu = createFlipMenu();
        // set up the Edit menu
        JMenu m_editMenu = new JMenu();
        m_editMenu.add(menuItem("&Undo\tCtrl+Z").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Undo();
            }
        }));
        m_editMenu.addSeparator();
        m_editMenu.add(menuItem("&Copy Game\tCtrl+C").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SetClipboardText(m_pd.Game().toString());
            }
        }));

        m_editMenu.add(menuItem("Copy &Move List").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SetClipboardText(m_pd.Game().GetMoveList().toMoveListString());
            }
        }));

        m_editMenu.add(menuItem("Copy &Board").build(new ActionListener() {
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
        m_editMenu.add(menuItem("&Paste Game\tCtrl+V").build(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String s = GetClipboardText();
                if (s != null) {
                    m_pd.Update(s, true);
                }
            }
        }));

        m_editMenu.add(menuItem("Paste Move &List").build(new ActionListener() {

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

        m_editMenu.add(menuItem("Paste Board").build(new ActionListener() {

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
        menu.add(menuItem("First\tup arrow").icon("first.gif").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.First();
            }
        }));
        menu.add(menuItem("Last\tdown arrow").icon("last.GIF").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Last();
            }
        }));
        menu.add(menuItem("Back\tleft arrow").icon("undo.GIF").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Back();
            }
        }));
        menu.add(menuItem("Fore\tright arrow").icon("redo.GIF").build(new ActionListener() {
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
        m_fileMenu.add(menuItem("&New\tCtrl+N").build(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                m_pd.StartNewGame();
                // if the engine is self-playing it is really annoying to have it self-play again when you start
                // a new game. Reset mode to user plays in this case.
                if (mode.getIndex() == 3) {
                    mode.setIndex(0);
                }
            }
        }));
        m_fileMenu.add(menuItem("&Open\tCtrl+O").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // note: We continue to receive windows messages in the GetOpenFilename() function.
                final File file = chooser.open();
                if (file != null) {
                    OpenFile(file);
                }
            }
        }));
        m_fileMenu.add(menuItem("&Save\tCtrl+S").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Save(false);
            }
        }));
        m_fileMenu.add(menuItem("&Append\tCtrl+A").build(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Save(true);
            }
        }));
        m_fileMenu.addSeparator();
        final String text = OperatingSystem.os.isMacintosh() ? "&Quit\tCtrl+Q" : "E&xit";
        m_fileMenu.add(menuItem(text).build(new ActionListener() {
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
                menuItem("&User plays both").buildRadioButton(modeSetter),
                menuItem("User plays &Black").buildRadioButton(modeSetter),
                menuItem("User plays &White").buildRadioButton(modeSetter),
                menuItem("&Engine plays both").buildRadioButton(modeSetter)
        ) {
            @Override public int readIndex() {
                // it's really annoying to have engine/engine matches on startup. Switch to user/user in this case
                final int mode = Math.max(0, Math.min(super.readIndex(), 2));
                SetMode(mode, false);
                return mode;
            }
        };

        menu.addSeparator();

        menu.add(engineLearnAll = createCheckBoxMenuItem("Learn &all completed games", "Engine/LearnAll", false));
        menu.add(menuItem("Learn &this game").build(new ActionListener() {
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
                menuItem("Draws to Black").buildRadioButton(contemptSetter),
                menuItem("Draws = 0").buildRadioButton(contemptSetter),
                menuItem("Draws to White").buildRadioButton(contemptSetter)
        );

        // top n list
        menu.addSeparator();

        engineTop = new RadioGroup(menu, "Engine/Top", 2, shutdownHooks,
                menuItem("Value >=1 move").buildRadioButton(engineUpdater),
                menuItem("Value >=2 moves").buildRadioButton(engineUpdater),
                menuItem("Value >=4 moves").buildRadioButton(engineUpdater),
                menuItem("Value all moves").buildRadioButton(engineUpdater)
        );

        menu.addSeparator();
        menu.add(menuItem("&Select Opponent...").build(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                GuiOpponentSelector.getInstance().show();
            }
        }));

        return menu;
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
        m_flipMenu.add(menuItem("F5/C4 Turn 180�").build(new BoardFlipper(3)));
        m_flipMenu.add(menuItem("F5/E6 Flop").build(new BoardFlipper(4)));
        m_flipMenu.add(menuItem("F5/D3 Flop").build(new BoardFlipper(7)));
        m_flipMenu.addSeparator();
        m_flipMenu.add(menuItem("Flip Horizontally").build(new BoardFlipper(2)));
        m_flipMenu.add(menuItem("Flip Vertically").build(new BoardFlipper(1)));
        m_flipMenu.add(menuItem("Turn 90� Right").build(new BoardFlipper(6)));
        m_flipMenu.add(menuItem("Turn 90� Left").build(new BoardFlipper(5)));
        return m_flipMenu;
    }

    /**
     * Create a checkbox menu item whose value is stored in Preferences.
     *
     * @param textAndAccelerator Text of the menu item, with the accelerator choice preceded by an '&'
     * @param key                Preferences key where the checked/unchecked state will be stored on shutdown
     * @param defaultChecked     if no Preferences currently exist, should this menu item be checked?
     * @param listeners          ActionListeners that will be added to this menu item
     * @return the menu item
     */
    private JMenuItem createCheckBoxMenuItem(String textAndAccelerator, final String key, boolean defaultChecked, ActionListener... listeners) {
        final JCheckBoxMenuItem menuItem = menuItem(textAndAccelerator).buildCheckBox(listeners);
        final boolean isChecked = NBoard.RegistryReadU4(key, defaultChecked ? 1 : 0) != 0;
        menuItem.setSelected(isChecked);
        shutdownHooks.add(new Runnable() {
            public void run() {
                NBoard.RegistryWriteU4(key, menuItem.isSelected() ? 1 : 0);
            }
        });
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


    //

    /**
     * Look up the displayed position in the currently loaded thor database
     */
    void ThorLookUpPosition() {
        dd.LookUpPosition();
        m_pmg.UpdateHints();
    }


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

    ///////////////////////////////////
    // Engine listener
    //////////////////////////////////

    @Override public void status(String status) {
        SetStatus(status);
    }

    @Override public void engineMove(COsMoveListItem mli) {
        // Need to check whether it's the computer's move. This is because the user may have
        // switched modes while the computer was thinking.
        if (!UsersMove()) {
            try {
                m_pd.Update(mli, false);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Illegal move from engine: " + mli, "Engine Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Override public void engineReady() {
        TellEngineWhatToDo();
    }

    @Override public void hint(boolean fromBook, String pv, CReader rest) {
        boolean fBlackMove = m_pd.Game().pos.board.fBlackMove;
        m_hints.Add(pv, rest, fBlackMove, fromBook);
    }
}
