package net.demipixel.purple;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

public class Main extends Canvas implements Runnable {
	
	private static final long serialVersionUID = 1L;
	public int width = 800;
	public int height = width / 16 * 9;
	public static String title = "Purple America Problem";
	
	private JFrame frame;
	public boolean running = true;
	
	public HashMap<String, String> hash= new HashMap<String, String>();
	public HashMap<String, String> reverseHash= new HashMap<String, String>();
	
	public void initRegions() {
		String[] stateAbrev = {"AL", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"};
		String[] stateFull = {"Alabama","Arizona","Arkansas","California","Colorado","Connecticut","Delaware","District of Columbia", "Florida","Georgia","Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina","North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Vermont","Virginia","Washington","West Virginia","Wisconsin","Wyoming"};
		
		for(int i= 0; i < stateAbrev.length; i++){
			hash.put(stateAbrev[i], stateFull[i]);
			reverseHash.put(stateFull[i], stateAbrev[i]);
		}
		
	}
	
	private Region USA = null;
	public Main() throws IOException {
		frame = new JFrame();
		initRegions();
		USA = generateRegions(new Region(), "./purple/USA.txt");
		
		
		File[] files = new File("./purple/Counties").listFiles();
		for (File file : files) {
			String mini = file.getName().replaceFirst(".txt", "");
			String fullname = hash.get(mini);
			Region state = USA.getSubByName(fullname);
			if (state == null) {
				if (!mini.equals("AK") && !mini.equals("HI")) {
					System.out.println("COULDN'T FIND STATE FOR " + mini + ":" + fullname);
				} else {
					System.out.println("Ignoring state " + mini);
				}
				continue;
			}
			generateRegions(state, file.toString());
			System.out.println(" == Created " + fullname + " counties (" + state.subregions.length + ")");
		}
		//Storing Election Data
		File[] electionFiles = new File("./purple/ElectionData").listFiles();
		List<Stack> electionDataList = new ArrayList<Stack>();
		for (File elect : electionFiles) {
			Stack<String> fileLines = new Stack<String>();
			String fileName = elect.getName().replaceFirst(".txt", "");
			BufferedReader br = new BufferedReader(new FileReader(elect));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
			   fileLines.push(line);
			}
			fileLines.push(fileName);
			Stack<String> copied = new Stack<String>();
			copied.addAll(fileLines);
			electionDataList.add(copied);
		}
		//Using the election data
		for (int i = 0; i < electionDataList.size(); i++) {
			Stack<String> dataStack = electionDataList.get(i);
			System.out.println(dataStack.pop());
			
		}
		
		for (Region state : this.USA.subregions) {
			if (state.subregions == null) state.subregions = new Region[0];
		}
	}
	

	
	private Region generateRegions(Region region, String file) throws IOException {
		ArrayList<String> lines = readFile(file);
		
		Region current = null;
		
		int positionDown = 0;
		int stateNum = 0;
		for (String l : lines) {
			if (l.length() == 0) {
				positionDown = 0;
				/*if (current != null && current.xPoints != null) {
					System.out.println("Region " + current.name + "," + current.xPoints.length);
				}*/
				current = new Region();
				continue;
			} else {
				if (current != null) { // Have current state
					if (positionDown == 0) { // Create region with name
						current.name = l;
						if (reverseHash.get(current.name) == null && file.equals("./purple/USA.txt")) {
							System.out.println("NO HASH FOR " + current.name);
							System.out.println(reverseHash.get("California"));
						}
						region.subregions[stateNum] = current;
						stateNum++;
					}
					else if (positionDown == 1) current.parentregion = l; // Name of parent
					else if (positionDown == 2) { // # of points
						int pointLength = Integer.parseInt(l);
						current.xPoints = new int[pointLength];
						current.yPoints = new int[pointLength];
					} else if (positionDown >= 3) {
						Point point = loadNumber(l);
						current.xPoints[positionDown-3] = point.x;
						current.yPoints[positionDown-3] = point.y;
					}
				} else { // Still at start of file
					if (positionDown == 0) { // Get mins
						Point min = loadNumber(l);
						region.minX = min.x;
						region.minY = min.y;
					} else if (positionDown == 1) { // Get maxs
						Point max = loadNumber(l);
						region.maxX = max.x;
						region.maxY = max.y;
					} else if (positionDown == 2) { // Get # of regions
						region.subregions = new Region[Integer.parseInt(l)];
					}
				}
				positionDown++;
			}
		}
		return region;
	}
	
	private Point loadNumber(String line) {
		Pattern pointPattern = Pattern.compile(" ?-?(.+)   (.+)");
		Matcher m = pointPattern.matcher(line);
		m.matches();
		return new Point((int) (Float.parseFloat(m.group(1)) * 256 * -1), (int) (Float.parseFloat(m.group(2)) * 64));
	}
	
	private ArrayList<String> readFile(String path) throws IOException {
		String line;
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(path));
		int i = 0;
		while((line = in.readLine()) != null) {
			lines.add(line);
			i++;
		}
		in.close();
		return lines;
	}
	
	public static void main(String[] args) throws IOException {
		Main display = new Main();
		display.frame.setTitle(title);
		display.frame.add(display);
		display.frame.pack();
		display.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		display.frame.setLocationRelativeTo(null);
		display.frame.setVisible(true);
		display.frame.setSize(display.width, display.height);
		
		display.run();
	}
	
	@Override
	public void run() {
		requestFocus();
		
		running = true;
		
		while (running) {
			render();
		}
	}
	
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		Rectangle r = frame.getBounds();
		
		int top = 15;
		int padding = -5;
		
		Graphics g = bs.getDrawGraphics();
		if (this.USA != null) {
			double scaleX = 1.0/20.0;
			double scaleY = 1.0/20.0 * 5;
			int width = this.USA.maxX - this.USA.minX;
			int height = this.USA.maxY - this.USA.minY;
			int offsetX = (int) ((this.USA.minX) * -scaleX) + 40;
			int offsetY = (int) ((this.USA.minY + height) * scaleY) + 20;
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, this.width, this.height);
			for (Region state : this.USA.subregions) {
				for (Region county : state.subregions) {
					county.draw(g, scaleX, -scaleY, offsetX, offsetY);
				}
				
				state.draw(g, scaleX, -scaleY, offsetX, offsetY);
			}
		}
		g.dispose();
		bs.show();
	}
}
