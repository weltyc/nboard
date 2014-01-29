package com.welty.nboard.thor;

import com.welty.othello.c.CReader;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.core.CBitBoard;
import com.welty.othello.core.CMove;
import com.welty.othello.core.CMoves;
import com.welty.othello.core.CQPosition;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Opening lookup table
 */
public class ThorOpeningMap {
    private final TObjectIntHashMap<CBitBoard> openingMap = new TObjectIntHashMap<CBitBoard>();
    private final ArrayList<String> names = new ArrayList<String>();

    private final String[] openingTexts = {
            "",
            "F5d6                                     Perpendicular",
            "F5d6C3d3C4                               Tiger",
            "F5d6C3d3C4b3                             Aubrey, Tanaka",
            "F5d6C3d3C4f4C5b3C2                       Rose-Bill",
            "F5d6C3d3C4f4C5b3C2e3                     Nicolet",
            "F5d6C3d3C4f4C5b3C2b4                     Central Rose-Bill",
            "F5d6C3d3C4f4C5b3C2e6                     Tamenori",
            "F5d6C3d3C4f4C5b4B5c6F3e6E3               Ishii",
            "F5d6C3d3C4f4C5b4B5c6F3e6E3g6F6g5D7g3     Mainline Tiger",
            "F5d6C3d3C4f4E3                           Brightwell",
            "F5d6C3d3C4f4E6                           Leader's Tiger",
            "F5d6C3d3C4f4F6                           Stephenson",
            "F5d6C3d3C4f4F6b4                         Kung",
            "F5d6C3d3C4f4F6f3                         Comp'Oth",
            "F5d6C3d3C4f4F6g5                         No-Kung",
            "F5d6C3d3C4f4F6g5E3f3G4h3G3f2             No-Kung (Continuation)",
            "F5d6C3g5                                 Ganglion",
            "F5d6C4d3C5                               Cat",
            "F5d6C4d3C5f4E3f3C2b4B3                   Berner",
            "F5d6C4d3C5f4E3f3C2c6                     Sakaguchi",
            "F5d6C4d3E6                               Italian",
            "F5d6C4g5                                 No-Cat",
            "F5d6C4g5C6                               Swallow",
            "F5d6C4g5C6c5D7d3B4c3E3f3                 No-Cat (Continuation)",
            "F5d6C5f4D3                               Horse",
            "F5d6C5f4E3c6D3f3                         Ralle",
            "F5d6C5f4E3c6D3f6E6d7                     Rose",
            "F5d6C5f4E3c6D3f6E6d7E7                   Greenberg, Dawg",
            "F5d6C5f4E3c6D3f6E6d7G3c4                 Rose-Birth",
            "F5d6C5f4E3c6D3f6E6d7G3c4B4               Rose-birdie, Rose-Tamenori",
            "F5d6C5f4E3c6D3f6E6d7G3c4B4c3             Rose-Tamenori-Kling",
            "F5d6C5f4E3c6D3f6E6d7G3c4G5c3B4e2         Brightstein",
            "F5d6C5f4E3c6D3f6E6d7G4                   Flat",
            "F5d6C5f4E3c6D3f6E6d7G4c4                 Rotating Flat",
            "F5d6C5f4E3c6D3f6E6d7G4c4G5c3F7d2E7f2     Rotating Flat (Kling Continuation)",
            "F5d6C5f4E3c6D3f6E6d7G4c4G6               Murakami Variation",
            "F5d6C5f4E3c6D7                           Bhagat",
            "F5d6C5f4E3c6E6                           Inoue",
            "F5d6C5f4E3c6E6f6                         Iago",
            "F5d6C5f4E3c6F3                           Shaman, Danish",
            "F5d6C5f4E3d3                             Mimura",
            "F5f4                                     Parallel",
            "F5f6                                     Diagonal",
            "F5f6C4f4                                 Semi-Wing Variation",
            "F5f6D3f4                                 Wing Variation",
            "F5f6E6d6C3                               Buffalo, Kenichi Variation",
            "F5f6E6d6C3d3                             Hokuriku Buffalo ",
            "F5f6E6d6C3f4C6d3E3d2                     Tanida Buffalo",
            "F5f6E6d6C3g4C6                           Maruoka Buffalo",
            "F5f6E6d6C5                               Cow",
            "F5f6E6d6C5e3D3                           Rose-v-Toth",
            "F5f6E6d6C5e3D3c4C3                       Landau",
            "F5f6E6d6C5e3D3c4C6b5                     Maruoka",
            "F5f6E6d6C5e3D3g5                         Tanida",
            "F5f6E6d6C5e3D3g5D7                       Aircraft, Feldborg",
            "F5f6E6d6C5e3D3g5E2b5                     Sailboat",
            "F5f6E6d6C5e3E7                           Bat, Cow Bat, Cambridge",
            "F5f6E6d6C5e3E7c6D7f7C7f4G6e8D8c8G5       Bat (Kling Continuation)",
            "F5f6E6d6C5e3E7c7D7c6D3                   Melnikov, Bat (Piau Continuation 1)",
            "F5f6E6d6C5e3E7c7D7c6F7                   Bat (Piau Continuation 2)",
            "F5f6E6d6C5e3E7f4F7                       Bat (Kling Alternative)",
            "F5f6E6d6C5f4                             Chimney",
            "F5f6E6d6C7c6D7                           Hamilton",
            "F5f6E6d6C7f4                             Lollipop",
            "F5f6E6d6D7                               Raccoon Dog",
            "F5f6E6d6E7                               Heath, Tobidashi \"Jumping Out\"",
            "F5f6E6d6E7f4                             Heath-Chimney, \"Mass-Turning\"",
            "F5f6E6d6E7g5C5                           Heath-Bat",
            "F5f6E6d6E7g5G4                           Iwasaki variation",
            "F5f6E6d6E7g5G6e3C5c6D3c4B3               Mimura variation II",
            "F5f6E6d6F7                               Snake, Peasant",
            "F5f6E6d6F7e3D7e7C6c5D3                   Checkerboarding Peasant, Pyramid",
            "F5f6E6d6G7                               X-square Opening",

            // some that I've added
            "F5d6C5                                   Fishhook",
            "F5d6C3d3C4f4C5b4                         Grand Central",
            "F5d6C3d3C4f4F6f3E6e7                     Scorpion",
            "F5d6C3d3C4f4F6f3E6e7F7                   Scorpion (Classic)",
            "F5d6C3d3C4f4F6f3E6e7D7                   Scorpion (New)",
            "F5d6C3d3C4f4F6f3E3                       Lightning Bolt",
            "F5d6C3d3C4f4C5b3C2b4E3e6C6f6A5a4B5a6D7c7 20-move opening",
            "F5f6E6f4G5e7F7                           Classic Heath",
            "F5d6C3d3C4f4F6f3E6e7D7g6F8f7G5           Low-FAT",
            "F5d6C3d3C4f4F6f3E6e7D7g6F8f7G5h6H4g4H3h5H7c5B4  F.A.T.",
            "F5d6C3d3C4f4F6f3E6e7D7g6F8f7G5h6H4g4H3g3 No-FAT",
            "F5d6C5f4E3c6D3g5                         8-Shot",
            "F5d6C3d3C4f4C6                           Spencer",
    };
    private final int nOpenings = openingTexts.length;

    /**
     * Create the opening map from the "openings" array
     */
    private ThorOpeningMap() {
        for (int i = 0; i < nOpenings; i++) {
            String[] parts = openingTexts[i].split("\\s+");
            String sMoves = parts[0];
            String name = parts.length > 1 ? parts[1].split(",")[0] : "";
            names.add(name);

            // calculate the position at the end of the move list
            CQPosition pos = new CQPosition();
            for (int moveLoc = 0; moveLoc < sMoves.length(); moveLoc += 2) {
                CMove move = new CMove(sMoves.substring(moveLoc, moveLoc + 2));
                pos.MakeMove(move);
            }
            for (int reflection = 0; reflection < 8; reflection++) {
                CBitBoard bb = pos.BitBoard().Symmetry(reflection);
                openingMap.put(bb, i);
            }
        }
    }


    private static final ThorOpeningMap tom = new ThorOpeningMap();

    /**
     * @return the opening name given the opening code
     */
    public static String OpeningName(int openingCode) {
        return tom.names.get(openingCode);
    }


    /**
     * Find the opening code for a thor game
     *
     * @param moves [in/out] In: ist of moves in Ntest square format. -2 denotes end of game. Out: Illegal moves are replaced by -2
     * @return 0 if the opening does not exist, otherwise the last named opening for the game
     */
    public static int OpeningCode(@NotNull byte... moves) {
        CQPosition pos = new CQPosition();
        return OpeningCode(pos, moves);
    }

    /**
     * Find the opening code for a thor game
     *
     * @param moves [in/out] In: ist of moves in Ntest square format. -2 denotes end of game. Out: Illegal moves are replaced by -2
     * @return 0 if the opening does not exist, otherwise the last named opening for the game
     */
    private static int OpeningCode(@NotNull CQPosition pos, @NotNull byte... moves) {
        int openingCode = 0;
        for (int i = 0; i < 60; i++) {
            int mv = moves[i];
            if (mv < 0)
                break;
            CMoves legalMoves = new CMoves();
            pos.CalcMovesAndPass(legalMoves);
            if (!Thor.GetBit(legalMoves.All(), mv)) {
                moves[i] = -2;
                break;
            }
            pos.MakeMove(new CMove((byte) mv));
            final CBitBoard bb = pos.BitBoard();
            if (tom.openingMap.contains(bb)) {
                openingCode = tom.openingMap.get(bb);
            }
        }
        return openingCode;
    }


    /**
     * Return the opening code; determine whether the game was played on an 8x8 board.
     *
     * @param sGgfGame text of game in GGF format
     *                 todo C code returns fStandard, which is true if it's an 8x8 game from the standard start position. Do we need this?
     * @return opening code
     */
    public static char CalcOpeningCode(final String sGgfGame) {
        return (char) OpeningCodeFromGgf(sGgfGame);
    }

    /**
     * @return the total number of named openings
     */
    static int NOpenings() {
        return tom.names.size();
    }

    /**
     * convert the game in internal movelist format; simultaneously determine whether it is an 8x8 game.
     *
     * @return array of moves, or null if this is not a standard 8x8 game
     */
    public static int OpeningCodeFromGgf(String sGgfGame) {
        // Check to see if the game is standard-start. In the process check that a BO tag exists and is properly formed
        int bo = sGgfGame.indexOf("]BO[");
        if (bo == -1)
            throw new IllegalArgumentException("Corrupt GGF game");
        bo += 4;
        int boEnd = sGgfGame.indexOf(']', bo);
        if (boEnd == -1)
            throw new IllegalArgumentException("Corrupt GGF game");
        String s = sGgfGame.substring(bo, boEnd);
        final CReader boardIn = new CReader(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 66; i++) {
            sb.append(boardIn.readChar());
        }
        String boText = sb.toString();
        if (!(boText.equals("8---------------------------O*------*O---------------------------*"))) {
            // not 8x8 standard start, so either a rand game or not 8x8.
            return 0;
        }

        final byte[] moves = moveBytesFromGgf(sGgfGame);

        final COsBoard board = new COsBoard(new CReader(boText));
        return OpeningCode(new CQPosition(board), moves);
    }

    public static byte[] moveBytesFromGgf(String sGgfGame) {
        final byte[] moves = new byte[60];

        int loc = 0;
        for (int i = 0; i < 60;) {
            loc = sGgfGame.indexOf(']', loc) + 1;
            if (loc + 4 >= sGgfGame.length()) {
                moves[i] = -2;
                break;
            }
            if ((sGgfGame.charAt(loc) == 'B' || sGgfGame.charAt(loc) == 'W') && sGgfGame.charAt(loc + 1) == '[') {
                CMove mv = new CMove(sGgfGame.substring(loc + 2, loc + 4));
                if (!mv.IsPass()) {
                    moves[i++] = (byte) mv.Square();
                }
            }
        }
        return moves;
    }
}
