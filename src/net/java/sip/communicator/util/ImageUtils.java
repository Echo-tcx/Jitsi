/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;

import net.java.sip.communicator.util.swing.*;

/**
 * Utility methods for image manipulation.
 *
 * @author Sebastien Mazy
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 */
public class ImageUtils
{
    private static final Logger logger = Logger.getLogger(ImageUtils.class);

    /**
     * Returns a scaled image fitting within the given bounds while keeping the
     * aspect ratio.
     *
     * @param image the image to scale
     * @param width maximum width of the scaled image
     * @param height maximum height of the scaled image
     * @return the scaled image
     */
    public static Image scaleImageWithinBounds(Image image, int width,
        int height)
    {
        if (image.getWidth(null) <= width && image.getHeight(null) <= height)
            return image;

        Image scaledImage;
        int scaleHint = Image.SCALE_SMOOTH;
        double originalRatio =
            (double) image.getWidth(null) / image.getHeight(null);
        double areaRatio = (double) width / height;

        if(originalRatio > areaRatio)
        {
            scaledImage = image.getScaledInstance(width, -1,scaleHint);
        }
        else
        {
            scaledImage = image.getScaledInstance(-1, height, scaleHint);
        }
        return scaledImage;
    }

    public static ImageIcon scaleIconWithinBounds(Image image, int width,
        int height)
    {
        return new ImageIcon(scaleImageWithinBounds(image, width, height));
    }

    /**
     * Creates a rounded avatar image.
     *
     * @param avatarBytes The bytes of the initial avatar image.
     *
     * @return The rounded corner image.
     */
    public static Image getScaledRoundedImage(Image image, int width, int height)
    {
        ImageIcon scaledImage =
            ImageUtils.scaleIconWithinBounds(image, width, height);
        int scaledImageWidth = scaledImage.getIconWidth();
        int scaledImageHeight = scaledImage.getIconHeight();

        if(scaledImageHeight <= 0 ||
           scaledImageWidth <= 0)
            return null;

        BufferedImage destImage =
            new BufferedImage(scaledImageWidth, scaledImageHeight,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = destImage.createGraphics();

        try
        {
            AntialiasingManager.activateAntialiasing(g);

            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, scaledImageWidth, scaledImageHeight, 10, 10);
            g.setComposite(AlphaComposite.SrcIn);

            g.drawImage(scaledImage.getImage(), 0, 0, null);
        }
        finally
        {
            g.dispose();
        }
        return destImage;
    }

    public static ImageIcon getScaledRoundedIcon(Image image, int width,
        int height)
    {
        Image scaledImage = getScaledRoundedImage(image, width, height);

        if (scaledImage != null)
            return new ImageIcon(scaledImage);

        return null;
    }

    /**
     * Creates a rounded corner scaled image.
     *
     * @param imageBytes The bytes of the image to be scaled.
     * @param width The maximum width of the scaled image.
     * @param height The maximum height of the scaled image.
     *
     * @return The rounded corner scaled image.
     */
    public static ImageIcon getScaledRoundedIcon(  byte[] imageBytes,
                                                    int width,
                                                    int height)
    {
        if (imageBytes == null || !(imageBytes.length > 0))
            return null;

        ImageIcon imageIcon = null;

        try
        {
            Image image = null;

            // sometimes ImageIO fails, will fall back to awt Toolkit
            try
            {
                image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            } catch (Exception e)
            {
                try
                {
                    image = Toolkit.getDefaultToolkit().createImage(imageBytes);
                } catch (Exception e1)
                {
                    // if it fails throw the original exception
                    throw e;
                }
            }
            if(image != null)
                imageIcon = getScaledRoundedIcon(image, width, height);
            else
                logger.trace("Unknown image format or error reading image");
        }
        catch (Exception e)
        {
            logger.debug("Could not create image.", e);
        }

        return imageIcon;
    }

    /**
     * Returns the buffered image corresponding to the given url image path.
     *
     * @param imagePath the path indicating, where we can find the image.
     *
     * @return the buffered image corresponding to the given url image path.
     */
    public static BufferedImage getBufferedImage(URL imagePath)
    {
        BufferedImage image = null;

        if (imagePath == null)
        {
            return null;
        }

        try
        {
            image = ImageIO.read(imagePath);
        }
        catch (Exception exc)
        {
            logger.debug("Failed to load image:" + imagePath, exc);
        }

        return image;
    }
}
