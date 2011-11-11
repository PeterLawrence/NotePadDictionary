package francois.pessaux.droidic.android;

public class DicManagement {
	public static boolean loadDictionaryFromRawData (int[] rawData)
	  {
	    if (rawData == null) return (false) ;
	    
	    /* Check magic word. */
	    if (rawData[0] != AutomatonDefs.TABLEMAGICNUMBER) return (false) ;

	    /* Check automaton size. */
	    Global.automaton_size = rawData[1] ;
	    if (Global.automaton_size == 0) return (false) ;

	    /* Get the initial state. */
	    Global.initial_state = rawData[2] ;

	    /* Allocate memory to store the dictionary words and assign the global
	       pointer representing the automaton. */
	    Global.automaton = new int [Global.automaton_size] ;
	    if (Global.automaton == null) {
	      /* Reset size and initial state. */
	      Global.automaton_size = 0 ;
	      Global.initial_state = AutomatonDefs.END_TRANSITION ;
	      return (false) ;
	    }

	    /* Copy the raw data of the resource into our memory. */
	    CLikeStringUtils.memcpyIntFrom
	      (Global.automaton, rawData, 3, Global.automaton_size) ;

	    return (true) ;
	  }



	  public static void unloadDictionary ()
	  {
	    if (Global.automaton != null) {
	      Global.automaton = null ;
	    }
	    Global.automaton_size = 0 ;
	    Global.initial_state = AutomatonDefs.END_TRANSITION ;
	  }
}
