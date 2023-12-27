package com.techatpark.util;

import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;

import com.google.zxing.*;

import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
public class ImageExtractor extends PDFStreamEngine {

    private int imageNumber = 1;

    private List<BufferedImage> images;

    private final File folder ;

    private final int minWidth;

    private final int minHeight;

    public ImageExtractor(File folder,int minWidth,
                          int minHeight) {
        this.folder = folder;
        this.minWidth = minWidth;
        this.minHeight = minHeight;

        images = new ArrayList<>();

        for (File imageFile:
                folder.listFiles((dir, name) -> name.endsWith(".png"))) {
            imageFile.delete();
        }


    }

    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     *
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator( Operator operator, List<COSBase> operands) throws IOException
    {
        String operation = operator.getName();
        if( "Do".equals(operation) )
        {
            COSName objectName = (COSName) operands.get( 0 );
            PDXObject xobject = getResources().getXObject( objectName );
            if( xobject instanceof PDImageXObject)
            {

                PDImageXObject image = (PDImageXObject)xobject;

                // same image to local
                BufferedImage bImage = image.getImage();



                if( bImage.getHeight() > minHeight
                        && bImage.getWidth() > minWidth
                            && !isQRCode(bImage)
                        && !doesImageExists(bImage)) {

                        File imageFile = new File(folder, "image_"+imageNumber+".png");
                        images.add(bImage);

                        ImageIO.write(bImage,"PNG",imageFile);

                        imageNumber++;

                }



            }
            else if(xobject instanceof PDFormXObject)
            {
                PDFormXObject form = (PDFormXObject)xobject;
                showForm(form);
            }
        }
        else
        {
            super.processOperator( operator, operands);
        }
    }

    private boolean doesImageExists(BufferedImage image) {
        if(images.stream().filter(image1 -> bufferedImagesEqual(image1, image))
                .findFirst().isPresent()) {
            return true;
        }
        return false;
    }


    boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y))
                        return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private static boolean isQRCode(BufferedImage bImage) {
        return bImage.getHeight() == bImage.getWidth()
                && decodeQRCode(bImage) != null;
    }


    private static String decodeQRCode(BufferedImage bufferedImage)  {
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        }
    }


}
