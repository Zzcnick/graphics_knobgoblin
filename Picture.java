import java.io.*;
import java.util.*;

public class Picture {
    public static final int NUM = StreamTokenizer.TT_NUMBER;
    public static final int STR = StreamTokenizer.TT_WORD;
    public static final int EOL = StreamTokenizer.TT_EOL;
    public static final int EOF = StreamTokenizer.TT_EOF;

    public static void main(String[] args) 
	throws FileNotFoundException, IOException, IllegalArgumentException {
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
	    int framecount = 1;
	    boolean vary = false;
	    StreamTokenizer st1 = new StreamTokenizer(br);
	    st1.slashSlashComments(true);
	    st1.eolIsSignificant(true);

	    while ((token = st1.nextToken()) != -1) {
		if (token == STR) {
		    if (st1.sval.equals("frames")) { // Keyword Frames
			if ((token = st1.nextToken()) == NUM)
			    if (st1.nval > 0) { // Frame Value > 0
				frames = true;
				framecount = (int)st1.nval; // Framecount
				c.initFrames(framecount); // Initialize Frames in Canvas
			    } else {
				System.out.println("ERROR: invalid number of frames");
				throw new IllegalArgumentException();
			    }
		    }
		    else if (st1.sval.equals("vary")) { // Keyword Vary
			vary = true;
		    }
		}
	    } // Complete Token Parsing For Pass One
	    
	    // System.out.println("Frames: " + frames); // Debugging
	    // System.out.println("Vary  : " + vary); // Debugging

	    if (vary && !frames) { // Vary is true, Frames is false
		System.out.println("ERROR: vary keyword used without declaring frames");
		throw new IllegalArgumentException();
	    } else if (frames) {
		System.out.println("SAVETYPE: ANIMATION");
	    } else {
		System.out.println("SAVETYPE: SINGLE IMAGE");
	    }

	    // Pass 2 - Generating Knobs and Varying
	    if (frames && vary) {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(f))));
		StreamTokenizer st2 = new StreamTokenizer(br);
		st2.slashSlashComments(true);
		st2.eolIsSignificant(true);

		while ((token = st2.nextToken()) != -1) {
		    if (token == STR) {
			if (st2.sval.equals("vary")) { // Needs Checking 
			    token = st2.nextToken(); String knobname = st2.sval;
			    token = st2.nextToken(); int startFrame = (int)st2.nval;
			    token = st2.nextToken(); int endFrame = (int)st2.nval;
			    token = st2.nextToken(); double startValue = st2.nval;
			    token = st2.nextToken(); double endValue = st2.nval;
			    if (startFrame < 0 || endFrame >= framecount) {
				System.out.println("ERROR: vary indices out of range");
				throw new IllegalArgumentException();
			    } else {
				c.addKnob(knobname, 
					  startFrame, endFrame,
					  startValue, endValue);
			    }
			}
		    }
		}
	    }

	    // Pass 3 - Drawing
	    for (int curframe = 0; curframe < framecount; curframe++) {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(f))));
		StreamTokenizer st3 = new StreamTokenizer(br);
		st3.slashSlashComments(true);
		st3.eolIsSignificant(true);

		// Parsing and Execution
		while ((token = st3.nextToken()) != -1) {
		    if (token == NUM) {
			// System.out.println(st3.nval); // Debugging
			buffer.offer(st3.nval);
			typebuffer.offer(token);
		    } else if (token == STR) {
			// System.out.println(st3.sval); // Debugging 
			buffer.offer(st3.sval);
			typebuffer.offer(token);
		    } else if (token == EOL) {
			// System.out.println("END OF LINE: EXECUTE COMMAND\n"); // Debugging
			// System.out.println("COMMAND: " + buffer); // Debugging
			// System.out.println("TYPES  : " + typebuffer); // Debugging
			execute(c, buffer, typebuffer, curframe);
		    } else break; // Should Not Happen, Failsafe
		}
		execute(c, buffer, typebuffer, curframe);
		if (frames) {
		    c.saveFrame(curframe);
		    System.out.println("Saving Frame " + curframe);
		}
		// System.out.println("END OF FILE: EXECUTE COMMAND AND END\n"); // Debugging
		// System.out.println("COMMAND: " + buffer); // Debugging
		// System.out.println("TYPES  : " + typebuffer); // Debugging
	    }
	    return;
	} 
    }

    // Execution Helper - Type Checking
    public static boolean typecheck(ArrayDeque<Integer> typebuffer,
				    int[] types) {
	if (typebuffer.size() != types.length)
	    return false;
	for (int i = 0; i < types.length; i++)
	    if (types[i] != typebuffer.poll())
		return false;
	return true;
    }

    // Execution Command for Parser
    public static void execute(Canvas c,
			       ArrayDeque<Object> buffer,
			       ArrayDeque<Integer> typebuffer,
			       int curframe) 
	throws FileNotFoundException{
	String cmd = "";
	if (!typebuffer.isEmpty()) {
	    // Commands 
	    if (typebuffer.poll() != STR) { 
		return;
	    } else {
		cmd = nextString(buffer);
	    }
	    int pad = 12;
	    String cmdpad = cmd;
	    while (cmdpad.length() < pad)
		cmdpad += " ";
	    if (c.getFramecount() == 1)
		System.out.println("Executing Command: " + cmdpad + "| Inputs: " + buffer); // Debugging
	    
	    boolean executed = false;
	    if (cmd.equals("line")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM, NUM, NUM}))
		    c.edge(nextDouble(buffer), nextDouble(buffer), 
			   nextDouble(buffer), nextDouble(buffer),
			   nextDouble(buffer), nextDouble(buffer));
	    } else if (cmd.equals("bezier")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM, NUM, NUM, NUM, NUM}))
		    c.bezier(nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer));
	    } else if (cmd.equals("hermite")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM, NUM, NUM, NUM, NUM}))
		    c.hermite(nextDouble(buffer), nextDouble(buffer),
			      nextDouble(buffer), nextDouble(buffer),
			      nextDouble(buffer), nextDouble(buffer),
			      nextDouble(buffer), nextDouble(buffer));
	    } else if (cmd.equals("circle")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM}))
		    c.circle(nextDouble(buffer), nextDouble(buffer),
			     nextDouble(buffer), nextDouble(buffer));
	    } else if (cmd.equals("box")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM, NUM, NUM}))
		    c.box(nextDouble(buffer), nextDouble(buffer),
			  nextDouble(buffer), nextDouble(buffer),
			  nextDouble(buffer), nextDouble(buffer));
	    } else if (cmd.equals("sphere")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM}))
		    c.sphere(nextDouble(buffer), nextDouble(buffer), 
			     nextDouble(buffer), nextDouble(buffer));
	    } else if (cmd.equals("torus")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM, NUM}))
		    c.torus(nextDouble(buffer), nextDouble(buffer),
			    nextDouble(buffer), nextDouble(buffer),
			    nextDouble(buffer));
	    } else if (cmd.equals("color")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM}))
		    c.setDefaultColor(new Pixel(nextInt(buffer), 
						nextInt(buffer),
						nextInt(buffer)));
	    } else if (cmd.equals("push")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.push();
	    } else if (cmd.equals("pop")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.pop();
	    } else if (cmd.equals("scale")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM})) {
		    c.scale(nextDouble(buffer),
			    nextDouble(buffer),
			    nextDouble(buffer));
		}
		else if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, STR})) {
		    double x = nextDouble(buffer);
		    double y = nextDouble(buffer);
		    double z = nextDouble(buffer);
		    double kv = c.getKnobValue(nextString(buffer), curframe);
		    c.scale(x * kv, y * kv, z * kv);
		    // System.out.println("COMMAND: scale " + x * kv + " " +  y * kv + " " + z * kv); // Debugging
		}		    
	    } else if (cmd.equals("move")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM})) {
		    c.translate(nextDouble(buffer),
				nextDouble(buffer),
				nextDouble(buffer));
		}
		else if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, STR})) {
		    double x = nextDouble(buffer);
		    double y = nextDouble(buffer);
		    double z = nextDouble(buffer);
		    double kv = c.getKnobValue(nextString(buffer), curframe);
		    c.translate(x * kv, y * kv, z * kv);
		    // System.out.println("COMMAND: move " + x * kv + " " + y * kv + " " + z * kv); // Debugging
		} 
	    } else if (cmd.equals("rotate")) {
		if (executed = typecheck(typebuffer, new int[]{STR, NUM})) {
		    c.rotate(nextChar(buffer), nextDouble(buffer));
		}
		else if (executed = typecheck(typebuffer, new int[]{STR, NUM, STR})) {
		    char axis = nextChar(buffer);
		    double theta = nextDouble(buffer);
		    double kv = c.getKnobValue(nextString(buffer), curframe);
		    c.rotate(axis, theta * kv);
		    // System.out.println("COMMAND: rotate " + axis + " " + theta * kv); // Debugging
		}
	    } else if (cmd.equals("apply")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.apply();
	    } else if (cmd.equals("clear")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.clearEdges();
	    } else if (cmd.equals("ident")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.clearTransform();
	    } else if (cmd.equals("draw")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.draw();
	    } else if (cmd.equals("save")) {
		if (executed = typecheck(typebuffer, new int[]{STR})) {
		    c.draw();
		    c.save(nextString(buffer));
		} 
		else if (executed = typecheck(typebuffer, new int[]{})) {
		    c.draw();
		    c.save();
		}
	    } else if (cmd.equals("savestate")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.savestate();
	    } else if (cmd.equals("loadstate")) {
		if (executed = typecheck(typebuffer, new int[]{}))
		    c.loadstate();
	    } else if (cmd.equals("mode")) {
		if (executed = typecheck(typebuffer, new int[]{NUM}))
		    c.setMode(nextInt(buffer));
	    } else if (cmd.equals("reset")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM, NUM, NUM}))
		    c = new Canvas(nextInt(buffer), nextInt(buffer),
				   nextInt(buffer), nextInt(buffer),
				   nextInt(buffer));
	    } else if (cmd.equals("frames")) {
		if (executed = typecheck(typebuffer, new int[]{NUM}))
		    ; // Nothing
	    } else if (cmd.equals("vary")) {
		if (executed = typecheck(typebuffer, new int[]{STR, NUM, NUM, NUM, NUM}))
		    ; // Nothing
	    } else if (cmd.equals("basename")) {
		if (executed = typecheck(typebuffer, new int[]{STR}))
		    c.setBasename(nextString(buffer));
	    } else if (cmd.equals("fill")) {
		if (executed = typecheck(typebuffer, new int[]{NUM, NUM, NUM}))
		    c.fill(nextInt(buffer), nextInt(buffer), nextInt(buffer));
	    }
	    
	    // Check if Command Executed
	    if (!executed)    
		System.out.println("ERROR: " + buffer + " does not match any implementation of " + cmd);

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
	return ((Double)(buffer.poll())).intValue();
    }
}
