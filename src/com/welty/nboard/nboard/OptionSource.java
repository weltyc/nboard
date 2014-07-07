/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.nboard.nboard;

import com.welty.nboard.nboard.startpos.StartPosition;
import org.jetbrains.annotations.NotNull;

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

    @NotNull StartPosition getStartPosition();

    /**
     * @return true if the user is analyzing a game (no game is currently being played)
     */
    boolean isAnalyzing();
}
