package flash.display;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import flash.geom.Point;
import flash.geom.Rectangle;

public class BitmapData {

	public int width;
	public int height;

	public int[] pixels;

	public BufferedImage image;

	public Rectangle rect;

	public BitmapData(String path) {
		try {
			BufferedImage image = ImageIO.read(BitmapData.class.getResource(path));
			this.width = image.getWidth();
			this.height = image.getHeight();
			pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width); //write rgb pixels to pixels array
		} catch (Exception e) {
			System.err.println("Error with file : " + path + ", width : " + width + ", height : " + height + ".");
			e.printStackTrace();
		}
	}

	public BitmapData(BufferedImage image) {

		this.image = image;

		this.width = image.getWidth();
		this.height = image.getHeight();

		this.pixels = new int[width * height];

		image.getRGB(0, 0, width, height, pixels, 0, width); //write rgb pixels to pixels array

		updateWidthAndHeight();
	}

	/**
	 * Used by BitmapDataSpy
	 */
	public BitmapData(int width, int height, boolean param3, int param4) {
		this(width, height);
	}

	public BitmapData(int width, int height) {
		this(new BufferedImage(width, height, 1));
		updateWidthAndHeight();
	}

	public BitmapData(int i, int i1, boolean b, double v) {

	}

	private void updateWidthAndHeight() {
		width = image.getWidth();
		height = image.getHeight();

		rect = new Rectangle(0,0,width, height);
	}


	public BitmapData clone() {
		return new BitmapData(image);
	}

	/**
	 * From AS3 API :
	 * <p>
	 * This method copies a rectangular area of a source image to a rectangular
	 * area of the same size at the destination point of the destination BitmapData object.
	 * <p>
	 * This method is not implemented as it should. TODO
	 */
	public void copyPixels(BitmapData sourceImage, Rectangle rectangle, Point point) {
		this.image = sourceImage.image;
	}

}
