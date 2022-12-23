package com.techatpark;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageExtractor extends PDFStreamEngine {
    private List<Rectangle2D> imageRectangles;

    public ImageExtractor() {
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }

    public List<Rectangle2D> getImageRectangles() {
        return imageRectangles;
    }

    @Override
    public void processPage(final PDPage page) throws IOException {
        imageRectangles = new ArrayList<>();
        super.processPage(page);
    }

    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if ("Do".equals(operation)) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject image) {
                System.out.println(objectName.getName());
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                imageRectangles.add(new Rectangle2D.Float(ctmNew.getScalingFactorX(), ctmNew.getScalingFactorX(), image.getWidth(), image.getHeight()));
            } else if (xobject instanceof PDFormXObject form) {
                showForm(form);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }
}
