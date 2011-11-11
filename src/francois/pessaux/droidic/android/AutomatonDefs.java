package francois.pessaux.droidic.android;

public class AutomatonDefs {
	 public static int MAX_STRINGLEN = 256 ;

	  public static int TAGTOLONG (byte a, byte b,byte c,byte d) 
	  {
	    return ((int)(((a)<<24)|((b)<<16)|((c)<<8)|(d))) ;
	  }
	  
	  public static int TABLEMAGICNUMBER =
	    TAGTOLONG ((byte) 'M',(byte) 'D',(byte) 'I', (byte) 'C') ;

	  /* DAGElement = ULONG. */
	  /* Bits 0-7 de poids faible = Char */
	  /* Bit 8 = IsBase | NextFilled */
	  /* Bit 9 = IsFinal */
	  /* Bits 10-31 = NextBase */

	  public static int CLEARELEM (int foo) { return (0) ; }

	  public static int GETCHAR (int foo)
	  {
	    return (foo & 0xFF) ;
	  }

	  public static boolean GETISBASE (int foo)
	  {
	    return ((foo & 0x00000100) != 0) ;
	  }

	  public static boolean GETNEXTFILLED (int foo)
	  {
	    return ((foo & 0x00000100) != 0) ;
	  }
	  
	  public static boolean GETISFINAL (int foo)
	  {
	    return ((foo & 0x00000200) != 0) ;
	  }
	    
	  public static int GETNEXTBASE (int foo)
	  {
	    return ((int) (foo >> 10)) ;
	  }

	  public static int SETCHAR (int foo, byte c)
	  {
	    foo &= 0xFFFFFF00 ;
	    return (foo | c) ;
	  }
	  
	  public static int SETISBASE (int foo)
	  {
	    return (foo = (1 << 8)) ;
	  }

	  public static int UNSETISBASE (int foo)
	  {
	    return (foo & 0xFFFFFEFF) ;
	  }

	  public static int SETNEXTFILLED (int foo)
	  {
	    return (foo | (1 << 8)) ;
	  }

	  public static int UNSETNEXTFILLED (int foo)
	  {
	    return (foo & 0xFFFFFEFF) ;
	  }

	  public static int SETISFINAL (int foo)
	  {
	    return (foo | (1 << 9)) ;
	  }
	    
	  public static int UNSETISFINAL (int foo)
	  {
	    return (foo & 0xFFFFFDFF) ;
	  }

	  public static int SETNEXTBASE (int foo, int baseValue)
	  {
	    foo &= 0x000003FF ;
	    return (foo | (baseValue << 10)) ;
	  }

	  /* VALEUR DU <<POINTEUR>> SIGNIFIANT TRANSITION NON EXISTANTE */
	  public static int END_TRANSITION = 0x3FFFFF ;
}
