//----------------------------------------------------------------------------//
//                                                                            //
//                          S t a f f B u i l d e r                           //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright (C) Herve Bitteur 2000-2010. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.sheet;

import omr.glyph.GlyphLag;

import omr.log.Logger;

import omr.sheet.grid.LineInfo;
import omr.sheet.grid.StaffInfo;

import omr.step.StepException;

import omr.util.HorizontalSide;

import java.util.*;

/**
 * Class <code>StaffBuilder</code> processes the (five) line areas, according to
 * the peaks found previously.
 *
 * @author Hervé Bitteur
 */
public class StaffBuilder
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(StaffBuilder.class);

    //~ Instance fields --------------------------------------------------------

    /** Related sheet */
    private Sheet sheet;

    /** Related lag */
    private GlyphLag hLag;

    //~ Constructors -----------------------------------------------------------

    //--------------//
    // StaffBuilder //
    //--------------//
    /**
     * Create a staff retriever, based on an underlying horizontal lag.
     *
     * @param sheet the sheet we are analyzing
     * @param hLag  the horizontal lag
     */
    public StaffBuilder (Sheet    sheet,
                         GlyphLag hLag)
    {
        this.sheet = sheet;
        this.hLag = hLag;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Create a staff info, using the staffCandidate that corresponds to the
     * staff lines.
     *
     * @param candidate the staff candidate (sequence of peaks)
     * @return the created staff information
     * @throws StepException if anything goes wrong
     */
    public StaffInfo buildInfo (StaffCandidate candidate)
        throws StepException
    {
        if (logger.isFineEnabled()) {
            logger.fine("candidate: " + candidate);
        }

        // Specific staff scale
        Scale          scale = new Scale(
            (int) Math.rint(candidate.interval),
            sheet.getScale().mainFore());

        // Process each peak into a line of the set
        List<LineInfo> lines = new ArrayList<LineInfo>();

        for (Peak peak : candidate.getPeaks()) {
            LineBuilder builder = new LineBuilder(
                hLag,
                peak.getTop(),
                peak.getBottom(),
                sheet,
                scale);
            lines.add(builder.buildInfo());
        }

        // Retrieve left and right abscissa for the staff lines of the set We
        // use a kind of vote here, since one or two lines can be read as longer
        // than real, so we use the abscissa of the second widest.
        List<Integer> lefts = new ArrayList<Integer>();
        List<Integer> rights = new ArrayList<Integer>();

        for (LineInfo line : lines) {
            lefts.add(line.getEndPoint(HorizontalSide.LEFT).x);
            rights.add(line.getEndPoint(HorizontalSide.RIGHT).x);
        }

        Collections.sort(lefts);
        Collections.sort(rights);

        int left = lefts.get(1);
        int right = rights.get(3);

        if (logger.isFineEnabled()) {
            logger.fine("End of Staff #" + candidate.id);
        }

        // Allocate the staff info
        return new StaffInfo(candidate.id, left, right, scale, lines);
    }
}
