// Import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// Import some Java "stuff".
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;


public class tnsnames_checker 
{
    public static void main(String[] args) throws Exception 
    {

        // A bit of "sign on and blow my own trumpet stuff" ;-)
        String thisVersion = "0.3";   // Version of this utility.
        String tnsVersion = "11gR2";   // Version of tnsnames specification used.
 
        
        System.out.println(String.format("%-80s", "=").replace(" ", "="));
        System.out.println("Tnsnames Checker - Version " + thisVersion + ".");
        System.out.println(String.format("%-80s", "=").replace(" ", "="));
        System.out.println("\nUsing TNSNAMES specification version for Oracle " + tnsVersion + ", as defined at:");
        System.out.println("\thttp://docs.oracle.com/cd/E11882_01/network.112/e10835/tnsnames.htm.\n");

        
        // Assume tnsnames.ora will be piped via stdin, unless we get a parameter passed.
        String tnsnamesFilename = null;
        InputStream iStream = System.in;
        File inputFile = null;

        if ( args.length > 0 ) 
        {
            // We have a filename passed to us, use it, assuming it is a valid one.
            tnsnamesFilename = args[0];
            inputFile = new File(tnsnamesFilename);
            if (inputFile.isFile()) 
            {
                iStream = new FileInputStream(tnsnamesFilename);
                tnsnamesFilename = inputFile.getCanonicalPath();
            } 
            else 
            {
                System.out.println("\nERROR 1: '" + tnsnamesFilename + 
                                   "' is not a valid filename.\n");
                System.exit(1);  // Error exit.
            }
                
        } 
        else 
        {
            // Nothing passed, use standard input instead.
            iStream = System.in;
            tnsnamesFilename = "Standard Input";
        }
             

        ANTLRInputStream input = new ANTLRInputStream(iStream);

        // The lexer reads from the input CharStream
        tnsnamesLexer lexer = new tnsnamesLexer(input);

        // Fetch a list of lexer tokens. For the parser.
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Stuff those tokens into the parser.
        tnsnamesParser parser = new tnsnamesParser(tokens);

        System.out.println("Using grammar defined in " + 
                           parser.getGrammarFileName() + ", downloadable from:" );
        System.out.println("\thttps://github.com/NormanDunbar/grammars-v4/tree/master/tnsnames." );

        System.out.println("\nThe official ANTLR version is downloadable from:");
        System.out.println("\thttps://github.com/antlr/grammars-v4/tree/master/tnsnames." );
        System.out.println("(This may not be as up to date if there are any pull requests outstanding.)\n" );
        System.out.println(String.format("%-80s", "=").replace(" ", "="));

        // Start parsing the tnsnames.ora file on stdin.
        // Parsing is deemed to be from the 'tnsnames' rule.
        System.out.println("Parsing tnsnames file '" + 
                           tnsnamesFilename + "' ...");
        System.out.println(String.format("%-80s", "=").replace(" ", "="));

        System.out.println("\n" + String.format("%-80s", "-").replace(" ", "-"));
        System.out.println("Syntax checking ...");
        System.out.println(String.format("%-80s", "-").replace(" ", "-"));
        ParseTree tree = parser.tnsnames();
        System.out.println("Syntax checking complete.");
        
        // Freak someone out! Print the entire parse tree.
        //System.out.println(tree.toStringTree(parser));

        ParseTreeWalker tnsWalker = new ParseTreeWalker();
        tnsnamesInterfaceListener tnsListener = new tnsnamesInterfaceListener(parser);
        System.out.println("\n" + String.format("%-80s", "-").replace(" ", "-"));
        System.out.println("Semantic checking ...");
        System.out.println(String.format("%-80s", "-").replace(" ", "-"));
        tnsWalker.walk(tnsListener, tree);
        System.out.println("\nSemantic checking complete.\n");

        System.out.println(String.format("%-80s", "=").replace(" ", "="));
        System.out.println("Parsing tnsnames file '" + 
                           tnsnamesFilename + "' complete.");
        System.out.println(String.format("%-80s", "=").replace(" ", "="));
    }
}
