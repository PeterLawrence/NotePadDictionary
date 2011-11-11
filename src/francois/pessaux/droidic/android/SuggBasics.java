package francois.pessaux.droidic.android;

public class SuggBasics {
	public static class suggestion {
	    byte[] string ;
	    int weight ;
	    suggestion next ;
	  };

	  private static suggestion suggestions = null ;
	  private static int number_suggestions = 0 ;

	  /* Note: addSuggestion MUST copy the string passed in argument ! */
	  public static void addSuggestion (byte[] word, int weight)
	  {    
	    suggestion tmp ;

	    /* This function must copy the string passed in argument !
	       First, it searches if the word already exists as a suggestion.
	       If not, then it add it with the given weight.
	       If yes, then if the given weight is lower than the weight already
	       associated to the suggestion, then we update the weight, otherwise
	       one do nothing. */
	    tmp = suggestions ;
	    while (tmp != null) {
	      if (CLikeStringUtils.strcmp (tmp.string, word) == 0) {
	        // Just update the weight of the suggestion
	        if (weight < tmp.weight) tmp.weight = weight ;
	        return ;
	      }
	      tmp = tmp.next ;
	    }

	    /* If we get here, that's because the word do not exist yet as a
	       suggestion. */
	    tmp = new suggestion () ;
	    tmp.string = new byte [ (1 + CLikeStringUtils.strlen (word))] ;
	    CLikeStringUtils.strcpy (tmp.string, word) ;
	    tmp.weight = weight ;
	    /* Insert in head. */
	    tmp.next = suggestions ;
	    number_suggestions++ ;
	    suggestions = tmp ;
	  }



	  public static void releaseSuggestions ()
	  {
	    suggestions = null ;
	    number_suggestions = 0 ;
	  }


	  public static byte[] getStringFromSuggestions ()
	  {
	    suggestion tmp ;
	    suggestion[] sugg_table ;
	    int inserted_count = 0 ;
	    int i, j ;
	    int space_needed = 0 ;
	    byte[] all_suggestions_buffer ;

	    /* Directly skip everything if no suggestion, so that's it... */
	    if (suggestions == null) return (null) ;

	    sugg_table = new suggestion[number_suggestions] ;

	    tmp = suggestions ;
	    while (tmp != null) {
	      /* Insert the string of this suggestion in the table, sorted first by
	         error weight, then alphabetically.
	         First, sort by increasing weight and stop as soon as we found
	         a suggestion with a weight equal or greater. */
	      for (i = 0; i<inserted_count; i++) {
	        if (sugg_table[i].weight >= tmp.weight) break ;
	      }
	      /* Then, sort alphabetically amongst suggestions with this weight. */
	      for (;i<inserted_count; i++) {
	        if ((sugg_table[i].weight > tmp.weight)
	            || (CLikeStringUtils.strcmp (sugg_table[i].string, tmp.string) >= 0))
	          break ;
	      }
	      /* Here, i equals the index where to insert the new suggestion.
	         Everything beyond must be shifted one case farther. */
	      for (j=inserted_count-1; j>=i; j--) sugg_table[j+1] = sugg_table[j] ;
	      sugg_table[i] = tmp ;
	      tmp = tmp.next ;
	      inserted_count++ ;
	      /* Add 1 for the "\n". */
	      space_needed += 1 + CLikeStringUtils.strlen (sugg_table[i].string) ;
	    }

	    /* Assertion test. Must always held ! Indeed, just some debug.
	    if (number_suggestions != inserted_count)
	    ASSERT_FAILED ; */

	    /* Allocate the big text buffer to contain all the suggestions. */
	    all_suggestions_buffer = new byte [1 + space_needed] ;
	    j = 0 ;
	    /* Then insert the sorted strings contained in the table in this buffer. */
	    for (i = 0; i< number_suggestions; i++) {
	      CLikeStringUtils.strcatLnAt (j, all_suggestions_buffer, sugg_table[i].string) ;
	      /* Add 1 for the "\n". */
	      j = 1 + j + CLikeStringUtils.strlen (sugg_table[i].string) ;
	    }

	    /* Release the memory of the sorted suggestions. */
	    sugg_table = null ;
	    /* Do not release the big string to be able to access it later because
	       we return a pointer on it. */
	    return (all_suggestions_buffer) ;
	  }
}
