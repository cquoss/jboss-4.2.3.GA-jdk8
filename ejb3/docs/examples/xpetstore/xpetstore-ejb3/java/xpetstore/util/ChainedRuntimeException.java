package xpetstore.util;


/**
 * Use this class to support chained exceptions in jdk1.3
 *
 * @author Isabelle Therrien
 */
public class ChainedRuntimeException
    extends RuntimeException
{
    //~ Instance fields --------------------------------------------------------

    private Throwable cause = null;

    //~ Constructors -----------------------------------------------------------

    public ChainedRuntimeException(  )
    {
        super(  );
    }

    public ChainedRuntimeException( String message )
    {
        super( message );
    }

    public ChainedRuntimeException( String    message,
                                    Throwable cause )
    {
        super( message );
        this.cause = cause;
    }

    public ChainedRuntimeException( Throwable cause )
    {
        super( "no message" );
        this.cause = cause;
    }

    //~ Methods ----------------------------------------------------------------

    public Throwable getCause(  )
    {
        return cause;
    }

    public String toString(  )
    {
        if ( cause == null )
        {
            return super.toString(  );
        }
        else
        {
            return super.toString(  ) + "  <---- Caused by: " + cause.toString(  ) + " ---->";
        }
    }

    public void printStackTrace(  )
    {
        super.printStackTrace(  );

        if ( cause != null )
        {
            System.err.println( "<---- Caused by:" );
            cause.printStackTrace(  );
            System.err.println( "---->" );
        }
    }

    public void printStackTrace( java.io.PrintStream ps )
    {
        super.printStackTrace( ps );

        if ( cause != null )
        {
            ps.println( "<---- Caused by:" );
            cause.printStackTrace( ps );
            ps.println( "---->" );
        }
    }

    public void printStackTrace( java.io.PrintWriter pw )
    {
        super.printStackTrace( pw );

        if ( cause != null )
        {
            pw.println( "<---- Caused by:" );
            cause.printStackTrace( pw );
            pw.println( "---->" );
        }
    }
}
