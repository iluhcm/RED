/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xl.gradients;

import java.awt.*;

/**
 * Provides a range of colours which form a smooth gradient from Blue through Green to Red.
 */
public class HotColdColourGradient extends ColourGradient {

    /**
     * A method initially called to create a palette of 100 pre-cached colours from which the closest match will be selected to return for future queries.
     * Setting the colours this way saves on the overhead of generating a lot of new objects
     *
     * @return An array of colours crossing the full palette.
     */
    protected Color[] makeColors() {
        /*
         * We pre-generate a list of 100 colours we're going to use for this display.
		 *
		 * Because a linear gradient ends up leaving too much green in the spectrum we put this on a log scale to emphasise low and high values so the display
		 * is clearer.
		 */

        Color[] colors = new Color[100];

        // We base colors on the square root of their raw value

        double min = 0 - Math.pow(50, 0.5);
        double max = Math.pow(99 - 50, 0.5);
        for (int c = 0; c < 100; c++) {
            int actualC = c - 50;
            if (actualC < 0) actualC = 0 - actualC;
            double corrected = Math.pow(actualC, 0.5);
            if (c < 50 && corrected > 0) corrected = 0 - corrected;
            RGB r = getRGB(corrected, min, max);
            colors[c] = new Color(r.r, r.g, r.b);
        }

        return colors;
    }

    public String name() {
        return "Hot Cold Colour Gradient";
    }


}
