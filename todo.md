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

L1   X   Display hints on the initial board position when in startup mode
L2   X   On Mac, accelerators should use command key instead of control key
L3   X   Accelerator for quit/exit menu item on Mac
L4   X   Move list window too narrow on Mac
L5   NB  Display engine status [As per spec, engine status is cleared after the engine moves]
L6   X   GGF Load progress window should finish with complete number of games
L7   X   GGF Load progress window should display only 3 sig figs in engineering notation (12.3k not 12,345)
L8   X   MoveList should highlight both the move and its eval. Clicking on move or eval should select.
L9   X   MoveList should display nothing if eval = 0.
L10      MoveList displays eval*100 for internal engines

F1       External engine window
F1a      Remove depth parameter from NBoardEngine
F1b      Simplify construction of SyncPlayer, almost always construct an EvalSyncEngine right there.
F2   X   Alternate start positions
F3       Internal engines should support NBoard protocol
F4       Symmetry menu items should display first move and icon
F5       More convenient external engine add window
F6       Display node count
F7       Display clock
F8       Timed matches
F9       Single Paste
F10      Copy should copy move list, not game
F11      Filter database by player, tournament, etc.
F12      Score graph

FL1      GGF Load progress window should display progress while reading from disk in addition to parsing games
FL2      GGF games should be stored in a more compact format
FL3      Stop sending the position twice to the engine. Don't send it at all if we can send 'move' instead.