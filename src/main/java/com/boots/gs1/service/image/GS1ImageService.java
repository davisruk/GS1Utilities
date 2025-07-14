package com.boots.gs1.service.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import uk.org.okapibarcode.backend.DataMatrix;
import uk.org.okapibarcode.backend.OkapiException;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.output.Java2DRenderer;

@Service
public class GS1ImageService {
	// data must be in human readable form where AIs are delimited with []
	public BufferedImage generateGS1BarcodeImage (String data, int width, int height) {
		DataMatrix symbol = new DataMatrix();
        symbol.setDataType(Symbol.DataType.GS1);
        for (int i = 1; i<=30; i++) {
        	symbol.setPreferredSize(i);
        	try {
        		symbol.setContent(data);
        		break;
        	} catch (OkapiException ex) {
      
        	}
        }
        Logger.getGlobal().log(Level.INFO, "Optimal barcode size: " + symbol.getPreferredSize());
        int symbolWidth = symbol.getWidth();
        int symbolHeight = symbol.getHeight();

        // Scale and centre
        double scaleX = (double) width / symbolWidth;
        double scaleY = (double) height / symbolHeight;
        double scale = Math.min(scaleX, scaleY);

        int imageWidth = (int) (symbolWidth * scale);
        int imageHeight = (int) (symbolHeight * scale);
        
        // Create a monochrome BufferedImage
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
        g2d.transform(transform);
        // Render the barcode
        Java2DRenderer renderer = new Java2DRenderer(g2d);
        renderer.render(symbol);

        g2d.dispose();
        return image;
	}
}
