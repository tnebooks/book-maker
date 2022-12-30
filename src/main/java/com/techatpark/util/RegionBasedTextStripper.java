package com.techatpark.util;


import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.tools.PDFText2HTML;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

/**
 * This will extract text from a specified region in the PDF.
 *
 * @author Ben Litchfield
 */
public class RegionBasedTextStripper extends PDFText2HTML
{

    private final Rectangle2D rect;


    public RegionBasedTextStripper(final Rectangle2D rect) throws IOException {
        super();
        this.rect = rect;
    }

    @Override
    protected void processTextPosition(final TextPosition text) {
        if (rect.contains(text.getX(), text.getY()))
        {


                super.processTextPosition(text);


        }
    }
}
