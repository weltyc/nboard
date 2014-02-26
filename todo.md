ID Fixed Description
H1   X   Don't display hints when it's the user's move
H2   X   When the user clicks on the board and the game is complete, don't add passes
H3   X   Piece count display needs to display two digits.
H4   X   Clear hint window when the hints are out of date
H5   X   Passes are counted twice when white passes in self-play games (edax:4, (;GM[Othello]PC[NBoard]DT[2014-01-29 23:05:30 GMT]PB[null]PW[null]RE[?]TI[0//0]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5]W[F4]B[E3]W[F6]B[E6]W[C5]B[G5]W[D3]B[D6]W[F7]B[G6]W[E7]B[C6]W[D7]B[F3]W[C4]B[F8]W[H6]B[B5]W[E8]B[C7]W[G8]B[D8]W[C8]B[C2]W[A6]B[C3]W[H5]B[B4]W[A3]B[B6]W[G3]B[G4]W[H4]B[A5]W[A4]B[B7]W[C1]B[B3]W[D2]B[F2]W[E2]B[B8]W[A8]B[A7]W[G7]B[E1]W[D1]B[B1]W[G2]B[H1]W[B2]B[H3]W[G1]B[H2]W[PA];))
H6   X   User menu choices should be stored in Preferences.
H7   X   Display Edax eval in move list
H8   X   Can't load ggs games file
H9   X   Database "look up position" doesn't, unless "Look up all" is already checked.
H10  X   Click on database game should use real player names in review window
H11  X   Click on database game should use the correct game
H12  X   Display progress window when loading games database
H13  X   Notify user with dialog box when engine gives a bad hint
H14  X   Notify engine of start position on new game
H15  X   Edax looping infinitely on hint
H16  X   Exception when trying to add new opponent
H17  X   Board display messed up on Windows
H18  X   Transcript does not put passes into game sent to reversiData
H19  X   SetUpBoard sending null board into BoardPanel.paintBoard()
H20  X   Transcript does not accept keyboard input when you click on the initially visible window rather than using the menu item
H21  X   Clock is not decreasing when internal engine self-plays, even though time is being taken.
H22  X   Node stats do not display during solve.
H23  X   SetUpBoard does not accept keyboard input second time around
H24  X   Node stats do not display when engine moves (rather than hints)
H25  X   Internal engine dies in endgame
H26  X   Vegtbl returning non-integer evals from solved positions. Occurs even from 1 empty.
H27  X   Click in move grid is performing the wrong move
H28  X   Early wipeout in analysis mode leaves hints on board if you click fast enough.

L1   X   Display hints on the initial board position when in startup mode
L2   X   On Mac, accelerators should use command key instead of control key
L3   X   Accelerator for quit/exit menu item on Mac
L4   X   Move list window too narrow on Mac
L5   NB  Display engine status [As per spec, engine status is cleared after the engine moves]
L6   X   GGF Load progress window should finish with complete number of games
L7   X   GGF Load progress window should display only 3 sig figs in engineering notation (12.3k not 12,345)
L8   X   MoveList should highlight both the move and its eval. Clicking on move or eval should select.
L9   X   MoveList should display nothing if eval = 0.
L10  X   MoveList displays eval*100 for internal engines
L11  X   MoveList displays too many decimal places for eval, should display just 1
L12  X   Hints are displayed after a self-play game is over
L13  X   Eval displaying as "-1073" when Abigail passes.
L14  X   Abigail's Engine Status is never updated
L15  X   Hints are displayed on the first move of the game when switching users' color
L16  X   "Engine communication error" dialog not displaying reason.
L17  X   Document 'set contempt' request.
L18  X   NaN displayed in Move Grid for eval sometimes, should be blank
L19  X   "pa" displayed in Move Grid at end of game (and other times?)
L20  X   Display "Engine Loading" when external engine is starting up.
L21  X   Don't allow reordering of move table columns - it results in exceptions and is horrible
L22  X   Use NTestJ for eval, it's the only one that works on all machines
L23  X   second-to-last move displayed in move grid when clicking on end-of-game after clicking on last move.
L24  X   NullPointerException when trying to run a nonexistent executable.
L25  X   MoveGrid database columns should have a blue background.
L26  X   Eval of 0 should show as 0 in move list [Note, GGF games have an empty eval for both eval=0 and eval=missing].
L27  X   SetUp window should be hidden in startup rather than shown/hidden flash.
L28  X   Hint depth incorrect when not using SOLVER_START_HEIGHT
L29  X   Don't decrement clock in analysis mode
L28a X   When database window is resized the games grid should also resize
L29b X   Time graph should include 0 on the y-axis
L30  X   Support analysis command in external engines

CC       Replace CQPosition with novello.Position
CC1      Make CMove immutable?
CC2  X   Make HintResponse.eval into a parsed class rather than an unparsed string

F1   X   External engine window
F1a  X   Remove depth parameter from NBoardEngine
F1b  NF  Simplify construction of SyncPlayer, we almost always construct an EvalSyncEngine right there.  [not really]
F2   X   Alternate start positions
F3   X   Internal engines should support NBoard protocol
F4   X   Symmetry menu items should display first move and icon
F5       More convenient external engine add window
F6   X   Display node count
F7   X   Display clock
F8   X   Timed matches
F9   X   Paste command should paste any format (move list, game, board)
F10  X   Ctrl-C should copy move list, Ctrl-Shift-C should copy game
F11  X   Filter database by player, tournament, etc.
F12  X   Score graph
F13  X   Remove overly verbose external engine communication
F14  X   Display node stats
F15  X   XOT start position should include moves
F18  X   Set up board command
F19  X   Time graph
F20      Lost disks graph
F21  X   Separate programs for analysis and opponent
F22  X   More than 1 hint from internal engines
F23  X   Threading on internal engines
F24  X   Enter Transcript command
F25  X   Display PV in move grid
F26  X   Iterative deepening in internal engine hints
F27  X   Internal engines shouldn't time out during games
F28      After a hint session is complete, remove moves from the previous ply
F27a X   Titles for graph window
F28a X   Analyze game and add to graph
F29  X   Internal engines should report node stats more frequently
F30  X   Clocks with < 1 minute left should display as 0:ss

Before release:
BR1  X   Update NBoard Protocol documentation
BR2  X   Fix Engine display names
BR3      Log engine communication to debugLog.txt instead of System.out (in ProcessLogger).

FL1      GGF Load progress window should display progress while reading from disk in addition to parsing games
FL2      GGF games should be stored in a more compact format
FL3  X   Enter tournament games
