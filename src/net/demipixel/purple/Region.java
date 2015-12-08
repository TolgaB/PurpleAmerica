package net.demipixel.purple;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;

public class Region {
	
	Region[] subregions;
	int[] xPoints;
	int[] yPoints;
	String name;
	String parentregion;
	
	int maxX;
	int maxY;
	int minX;
	int minY;
	
	double dem = 0;
	double rep = 0;
	double oth = 0;
	
	public Region() {
		
	}
	
	public void pushData(int x, int y) {
		xPoints[xPoints.length-1] = x;
		yPoints[yPoints.length-1] = y;
		
		this.minX = Math.min(this.minX, x);
		this.minY = Math.min(this.minY, y);
		this.maxX = Math.max(this.maxY, x);
		this.maxY = Math.max(this.maxY, y);
	}
	
	public Region getSubByName(String name) {
		for (Region region : this.subregions) {
			if (region.name.equals(name)) return region;
		}
		return null;
	}
	
	public void draw(Graphics g, double scaleX, double scaleY, double offsetX, double offsetY) {
		int[] x = scalePoints(this.xPoints, scaleX, offsetX);
		int[] y = scalePoints(this.yPoints, scaleY, offsetY);
		
		Polygon shape = new Polygon(x, y, x.length);
		
		
		/*if (this.parentregion.equals("USA")) {
			g.setColor(new Color(255, 255, 255));
			g.drawPolygon(shape);
		} else {
			g.setColor(new Color(0, 0, 0));
			g.drawPolygon(shape);
		}*/
		int red = (int) (rep / (dem + rep + oth) * 255);
		int green = (int) (oth / (dem + rep + oth) * 255);
		int blue = (int) (dem / (dem + rep + oth) * 255);
		g.setColor(new Color(red,green,blue));
		if (red + green + blue > 0) {
			g.fillPolygon(shape);
		}
	}
	
	private int[] scalePoints(int[] points, double scale, double offset) {
		int[] newPoints = new int[points.length];
		for (int n = 0; n < newPoints.length; n++) {
			//System.out.println(points[n] + "*" + scale + " + " + offset + " = " + (points[n] * scale + offset));
			newPoints[n] = (int)Math.round(points[n] * scale + offset);
		}
		return newPoints;
	}
}
