package com.welty.nboard;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 13, 2009
 * Time: 9:19:06 PM
 * </PRE>
 */
public interface OptionSource {
    boolean ShowEvals();

    boolean UsersMove();

    boolean ViewPhotoStyle();

    boolean ViewD2();

    int IHighlight();

    boolean ViewCoordinates();

    boolean IsStudying();

    boolean AlwaysShowEvals();

    boolean ThorLookUpAll();

    boolean EngineLearnAll();

    boolean UserPlays(boolean fBlack);
}
