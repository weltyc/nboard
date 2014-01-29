package com.welty.nboard.thor;

import com.welty.othello.c.CBinaryReader;

/**
 * 16-byte header that occurs at the beginning of all thor databases
 */
@SuppressWarnings({"FieldCanBeLocal"}) class ThorHeader {
    private final byte crCentury;        //*< Century of creation of databse
    private final byte crYear;        //*< Year of creation of database
    final byte crMonth;        //*< Month of creation of database
    final byte crDay;            //*< Day of creation of database
    final int n1;                //*< Number of records in games and solitaires databases. 0 for DB of players and tournaments.
    final char n2;            //*< Number of records in players and tournaments databases. Number of empties in solitaires databases. 0 for DB of games.
    final char year;            //*< Year, in games files. 0 otherwise.
    final byte boardSize;        //*< 0 unless in a games file. In games file, 0 or 8 = 8x8, 10=10x10
    final boolean fSolitaire;    //*< 1 in solitaire DB, 0 otherwise
    final byte nPerfectPlay;    //*< in games files, depth for which perfect play has been calculated. 0 is a default value meaning 22.
    private final byte reserved;        //*< Must be 0

    public ThorHeader(CBinaryReader is) {
        crCentury = is.readByte();
        crYear = is.readByte();
        crMonth = is.readByte();
        crDay = is.readByte();
        n1 = is.readInt();
        n2 = is.readChar();
        year = is.readChar();
        boardSize = is.readByte();
        fSolitaire = is.readByte() != 0;
        nPerfectPlay = is.readByte();
        reserved = is.readByte();
    }
}
