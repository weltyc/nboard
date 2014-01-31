package com.welty.nboard.thor;

import com.welty.nboard.nboard.GgfGameText;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.*;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.welty.nboard.thor.Thor.ThorFindMatchingPositions;
import static com.welty.nboard.thor.Thor.ThorSummarize;
import static com.welty.nboard.thor.ThorOpeningMap.OpeningName;
import static com.welty.othello.core.Utils.Col;
import static com.welty.othello.core.Utils.Row;

/**
 * Data model for DatabaseData.
 */
public class DatabaseDataModel {
    /**
     * WTB games followed by converted GGF games
     */
    private ArrayList<ThorGameInternal> m_tgis = new ArrayList<>();
    private ArrayList<String> m_players = new ArrayList<>();
    private ArrayList<String> m_tournaments = new ArrayList<>();

    private int m_nThorGames;                    //*< Number of games loaded from WTB files

    /**
     * GGF text of games loaded from GGF files
     */
    private final ArrayList<GgfGameText> m_ggfGames = new ArrayList<>();

    /**
     * Remove all games from this Model
     */
    void clearGames() {
        m_ggfGames.clear();
        m_tgis.clear();
        // Reclaim memory
        m_ggfGames.trimToSize();
        m_tgis.trimToSize();
    }

    public String getGgfText(int iGame) {
      return  m_ggfGames.get(iGame - m_nThorGames).getText();
    }

    /**
     * @return the text (e.g. "Welty Chris") given the game (item) and field (e.g. 0=black player name)
     */
    public String GameItemText(int item, int field) {
        int n;

        final ThorGameInternal game = m_tgis.get(item);

        if (item < m_nThorGames) {
            // Thor game

            switch (field) {
                case 0:
                    return PlayerName(game.iBlackPlayer);
                case 1:
                    return PlayerName(game.iWhitePlayer);
                case 2:
                    n = game.year;
                    break;
                case 3:
                    return TournamentName(game.iTournament);
                case 4:
                    n = game.nBlackDiscs * 2 - 64;
                    break;
                case 5:
                    return OpeningName(game.openingCode);
                default:
                    return "";
            }
        } else {
            // GGF game
            final GgfGameText text = m_ggfGames.get(item - m_nThorGames);
            switch (field) {
                case 0:
                    return text.PB();
                case 1:
                    return text.PW();
                case 2:
                    n = game.year;
                    break;
                case 3:
                    return text.PC();
                case 4:
                    n = game.nBlackDiscs * 2 - 64;
                    break;
                case 5:
                    return OpeningName(game.openingCode);
                default:
                    return "";
            }
        }

        return Integer.toString(n);
    }

    /**
     * @return result of the game, #black discs - #white discs, for Thor games only
     */
    int GameResult(int iGame) {
        return m_tgis.get(iGame).nBlackDiscs * 2 - 64;
    }

    int NPlayers() {
        return m_players.size();
    }

    int NTournaments() {
        return m_tournaments.size();
    }

    /**
     * @return the total number of games loaded (both Thor and GGF)
     */
    public int NGames() {
        return m_tgis.size();
    }

    /**
     * @param iGame index of the game
     * @return a game in GGS/os format.
     */
    public COsGame GameFromIndex(int iGame) {
        COsGame game = new COsGame();

        if (iGame < m_nThorGames) {
            final ThorGameInternal tg = m_tgis.get(iGame);

            game.SetDefaultStartPos();
            game.pis[0].sName = PlayerName(tg.iWhitePlayer);
            game.pis[1].sName = PlayerName(tg.iBlackPlayer);
            game.pis[0].dRating = game.pis[1].dRating = 0;
            game.sPlace = TournamentName(tg.iTournament);
            for (int i = 0; i < 60 && tg.moves[i] >= 0; i++) {
                final int sq = tg.moves[i];
                COsMoveListItem mli = new COsMoveListItem(new COsMove(Row(sq), Col(sq)), 0, 0);

                // illegal moves end the game. Yes, the Thor database has some.
                if (!game.pos.board.IsMoveLegal(mli.mv))
                    break;
                game.Update(mli);

                if (!game.pos.board.HasLegalMove() && !game.pos.board.GameOver()) {
                    mli.mv.SetPass();
                    game.Update(mli);
                }
            }
            if (!game.pos.board.GameOver()) {
                COsResult osResult = new COsResult();
                osResult.status = COsResult.TStatus.kTimeout;
                osResult.dResult = tg.nBlackDiscs * 2 - 64;
                game.SetResult(osResult);
            }
        } else {
            game.In(new CReader(getGgfText(iGame)));
        }

        return game;
    }

    public Thor.MatchingPositions findMatchingPositions(OsBoard pos) {
        return ThorFindMatchingPositions(m_tgis, pos);
    }

    public TThorSummary summarize(OsBoard pos, TIntArrayList fi, TIntArrayList fir) {
        return ThorSummarize(m_tgis, pos, fi, fir);
    }

    String PlayerName(char iPlayer) {
        if (iPlayer >= NPlayers())
            return "???";
        else
            return m_players.get(iPlayer);
    }

    String TournamentName(char iTournament) {
        if (iTournament >= NTournaments())
            return "???";
        else
            return m_tournaments.get(iTournament);
    }

    public void setGames(ArrayList<ThorGameInternal> games) {
        // copy database data for games into m_ggfTgis for faster searching
        m_tgis = games;
        m_nThorGames = games.size();
        for (final GgfGameText game : m_ggfGames) {
            final int nBlackSquares = (new CReader(game.RE()).readInt(0) / 2) + 32;
            final String dt = game.DT();
            int year = new CReader(dt).readInt(0);
            if (year > 100000) {
                // early GGF games have a bug where the DT field is given in seconds since 1970-01-01 instead of
                // the standard format. In this case, translate
                final GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis((long) year * 1000);
                year = cal.get(Calendar.YEAR);
            }
            ThorGameInternal tgi = new ThorGameInternal(nBlackSquares, game.Moves(), game.m_openingCode, year);
            m_tgis.add(tgi);
        }

    }

    public void addGgfGames(ArrayList<GgfGameText> ggfGameTexts) {
        m_ggfGames.addAll(ggfGameTexts);
    }

    public void setPlayers(ArrayList<String> strings) {
        m_players = strings;
    }

    public void setTournaments(ArrayList<String> strings) {
        m_tournaments = strings;
    }

    /**
     * @param nOpenings number of possible openings ; must be > the highest opening number in this database.
     * @return the number of times each opening occurs in this database.
     */
    public int[] getOpeningCounts(int nOpenings) {
        final int[] counts = new int[nOpenings];
        for (ThorGameInternal it : m_tgis) {
            counts[it.openingCode]++;
        }
        return counts;
    }

    public int getGameYear(int iGame) {
        return m_tgis.get(iGame).year;
    }
}
