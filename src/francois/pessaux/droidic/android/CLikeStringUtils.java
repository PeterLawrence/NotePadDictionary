package francois.pessaux.droidic.android;

import java.io.UnsupportedEncodingException ;

public class CLikeStringUtils {
	public static int toupper (int i)
	  {
	    if (i >= 97 && i <= 122) return (byte) (i - 32) ;
	    else return (i) ;
	  }

	  public static int tolower (int i)
	  {
	    if (i >= 65 && i <= 90) return (i + 32) ;
	    else return (i) ;
	  }

	  public static boolean isupper (int i)
	  {
	    return (i >= 97 && i <= 122) ;
	  }
	  
	  public static int strlen (byte s[])
	  {
	    int l = 0 ;
	    while (s[l] != '\0') l++ ;
	    return (l) ;
	  }
	  
	  public static int strcmp (byte[] s1, byte[] s2)
	  {
	    int i ;
	    
	    for(i = 0; s1[i] == s2[i]; i++) {
	      if(s1[i] == 0) return (0) ;
	    }
	    return ((s1[i] < s2[i]) ? -1 : 1) ;
	  }
	  
	  public static void strcpy (byte[] dest, byte[] src)
	  {
	    int i ;
	    for (i = 0; src[i] != '\0'; i++) dest[i] = src[i] ;
	    dest[i] = '\0';
	  }

	  public static void strcatLnAt (int at, byte[] dest, byte[] src)
	  {
	    int i ;

	    for (i = 0; src[i] != '\0'; i++, at++) dest[at] = src[i] ;
	    //dest[at] ='\n' ;
	    //dest[at + 1] ='\0' ;
	    dest[at] =' ' ;
	    dest[at + 1] =' ' ;
	  }
	  
	  public static void memcpyIntFrom (int[] dest, int[] src, int from, int n)
	  {
	    int i ;
	    
	    for (i = 0; i < n; i++, from++) dest[i] = src[from] ;
	  }
	  
	  public static byte[] bytesCStringFromString (String s)
	    throws UnsupportedEncodingException
	  {
	    int i ;
	    int l = s.length () ;
	    byte[] bytes = new byte [l + 1] ;
	    byte[] sBytes = s.getBytes ("ISO-8859-1") ;
	    for (i = 0; i < l ; i++) bytes[i] = sBytes[i] ;
	    bytes[i] = '\0' ;
	    return (bytes) ;
	  }
	  
	  public static int[] intArrayFromByteArray (byte[] bytes)
	  {
	    int i, j ;
	    int l = bytes.length ;
	    if (l % 4 != 0) {
	      System.err.printf ("Mangled file.\n") ;
	      System.exit (-1) ;
	    }
	    int[] ints = new int[l / 4] ;
	    
	    for (j = 0, i = 0; i < l; i += 4, j++) {
	      ints[j] =
	        (bytes[i + 3] << 24) +
	        ((bytes[i + 2] & 0xFF) << 16) +
	        ((bytes[i + 1] & 0xFF) << 8) +
	        (bytes[i] & 0xFF) ;
	    }
	    return (ints) ;
	  }

}
