import java.io.*;
import java.util.*;

public class Picture {
    public static void main(String[] args) 
	throws FileNotFoundException, IOException {
	if (args.length > 0) { // Parser
	    // Canvas Setup
	    Canvas c = new Canvas(500, 500, 255, 255, 255);
	    
	    // Script Reading
	    String f = args[0]; // Filename
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(f))));

	    // Initialization
	    int token;
	    ArrayDeque<Object> buffer = new ArrayDeque<Object>();
	    ArrayDeque<Integer> typebuffer = new ArrayDeque<Integer>();

	    // Pass 1 - Checking Frames and Vary
	    boolean frames = false;
	    boolean vary = false;
	    StreamTokenizer st1 = new StreamTokenizer(br);
	    st1.slashSlashComments(true);
	    st1.eolIsSignificant(true);

	    while ((token = st1.nextToken()) != -1) {
		if (token == StreamTokenizer.TT_WORD) {
		    if (st1.sval.equals("frames")) { // Keyword Frames
			if ((token = st1.nextToken()) == StreamTokenizer.TT_NUMBER)
			    if (st1.nval > 0) // Frame Value > 0
				frames = true;
		    }
		    else if (st1.sval.equals("vary")) { // Keyword Vary
			vary = true;
		    }
		}
	    } // Complete Token Parsing For Pass One
	    
	    System.out.println("Frames: " + frames); // Debugging
	    System.out.println("Vary  : " + vary); // Debugging

	    if (vary && !frames) { // Vary is true, Frames is false
		System.out.println("ERROR: vary keyword used without declaring frames");
		throw new IOException();
	    }

	    // Pass 3 - Drawing
	    StreamTokenizer st3 = new StreamTokenizer(br);
	    st3.slashSlashComments(true);
	    st3.eolIsSignificant(true);

	    // Parsing and Execution
	    while ((token = st3.nextToken()) != -1) {
		if (token == StreamTokenizer.TT_NUMBER) {
		    // System.out.println(st3.nval); // Debugging
		    buffer.offer(st3.nval);
		    typebuffer.offer(token);
		} else if (token == StreamTokenizer.TT_WORD) {
		    // System.out.println(st3.sval); // Debugging 
		    buffer.offer(st3.sval);
		    typebuffer.offer(token);
		} else if (token == StreamTokenizer.TT_EOL) {
		    // System.out.println("END OF LINE: EXECUTE COMMAND\n"); // Debugging
		    // System.out.println("COMMAND: " + buffer); // Debugging
		    // System.out.println("TYPES  : " + typebuffer); // Debugging
		    execute(c, buffer, typebuffer);
		} else break; // Should Not Happen, Failsafe
	    }
	    execute(c, buffer, typebuffer);
	    // System.out.println("END OF FILE: EXECUTE COMMAND AND END\n"); // Debugging
	    // System.out.println("COMMAND: " + buffer); // Debugging
	    // System.out.println("TYPES  : " + typebuffer); // Debugging

	    return;
	} 
    }
    // Execution Command for Parser
    public static void execute(Canvas c,
			       ArrayDeque<Object> buffer,
			       ArrayDeque<Integer> typebuffer) 
	throws FileNotFoundException{
	String cmd = "";
	Pixel color = new Pixel(0,0,0);
	if (!typebuffer.isEmpty()) {
	    // Commands 
	    if (typebuffer.poll() != -3) { 
		return;
	    } else {
		cmd = nextString(buffer);
	    }
	    int pad = 12;
	    String cmdpad = cmd;
	    while (cmdpad.length() < pad)
		cmdpad += " ";
	    System.out.println("Executing Command: " + cmdpad + "| Inputs: " + buffer);
	    
	    int len = typebuffer.size(); // Input - Excludes Command
	    if (cmd.equals("line")) {
		if (len == 6)
		    c.edge(nextDouble(buffer), nextDouble(buffer), 
			   nextDouble(buffer), nextDouble(buffer),
			   nextDouble(buffer), nextDouble(buffer), 
			   color);
	    } else if (cmd.equals("bezier")) {
		if (len == 8)
		    c.bezier(nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer),
			     color);
	    } else if (cmd.equals("hermite")) {
		if (len == 8)
		    c.hermite(nextDouble(buffer), nextDouble(buffer),
			      nextDouble(buffer), nextDouble(buffer),
			      nextDouble(buffer), nextDouble(buffer),
			      nextDouble(buffer), nextDouble(buffer),
			      color);
	    } else if (cmd.equals("circle")) {
		if (len == 4)
		    c.circle(nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer),
			     color);
	    } else if (cmd.equals("box")) {
		if (len == 6)
		    c.box(nextDouble(buffer), nextDouble(buffer),
			  nextDouble(buffer), nextDouble(buffer),
			  nextDouble(buffer), nextDouble(buffer),
			  color);
	    } else if (cmd.equals("sphere")) {
		if (len == 4)
		    c.sphere(nextDouble(buffer), nextDouble(buffer), 
			     nextDouble(buffer), nextDouble(buffer),
			     color);
	    } else if (cmd.equals("torus")) {
		if (len == 5)
		    c.torus(nextDouble(buffer), nextDouble(buffer),
			    nextDouble(buffer), nextDouble(buffer),
			    nextDouble(buffer), color);
	    } else if (cmd.equals("color")) {
		if (len == 3)
		    color = new Pixel(nextInt(buffer), 
				      nextInt(buffer),
				      nextInt(buffer));
	    
	    } else if (cmd.equals("push")) {
		if (len == 0)
		    c.push();
	    } else if (cmd.equals("pop")) {
		if (len == 0)
		    c.pop();
	    } else if (cmd.equals("scale")) {
		if (len == 3) 
		    c.scale(nextDouble(buffer),
			    nextDouble(buffer),
			    nextDouble(buffer));
	    } else if (cmd.equals("move")) {
		if (len == 3) 
		    c.translate(nextDouble(buffer),
				nextDouble(buffer),
				nextDouble(buffer));
	    } else if (cmd.equals("rotate")) {
		if (len == 2) 
		    c.rotate(nextChar(buffer), nextDouble(buffer));
	    } else if (cmd.equals("apply")) {
		if (len == 0)
		    c.apply();
	    } else if (cmd.equals("clear")) {
		if (len == 0)
		    c.clearEdges();
	    } else if (cmd.equals("ident")) {
		if (len == 0)
		    c.clearTransform();
	    } else if (cmd.equals("draw")) {
		if (len == 0)
		    c.draw();
	    } else if (cmd.equals("save")) {
		if (len == 1) {
		    c.draw();
		    c.save(nextString(buffer));
		}
	    } else if (cmd.equals("savestate")) {
		if (len == 0)
		    c.savestate();
	    } else if (cmd.equals("loadstate")) {
		if (len == 0)
		    c.loadstate();
	    } else if (cmd.equals("mode")) {
		if (len == 1)
		    c.setMode(nextInt(buffer));
	    } else if (cmd.equals("reset")) {
		if (len == 5) 
		    c = new Canvas(nextInt(buffer), nextInt(buffer),
				   nextInt(buffer), nextInt(buffer),
				   nextInt(buffer));
	    } 
	    buffer.clear();
	    typebuffer.clear();
	}
    }

    // Token Parsing - Execute
    public static double nextDouble(ArrayDeque<Object> buffer) {
	return (double)(buffer.poll());
    }
    public static String nextString(ArrayDeque<Object> buffer) {
	return (String)(buffer.poll());
    }
    public static char nextChar(ArrayDeque<Object> buffer) {
	return nextString(buffer).charAt(0);
    }
    public static int nextInt(ArrayDeque<Object> buffer) {
	return (int)(buffer.poll());
    }
}
