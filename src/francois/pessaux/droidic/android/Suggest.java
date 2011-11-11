package francois.pessaux.droidic.android;

public class Suggest {
	enum operation { Nop, Suppr, Add, Wrong } ;

	  private static int num_allowed_errors ;
	  private static byte[] sugg_word = new byte [AutomatonDefs.MAX_STRINGLEN] ;
	  private static int len_wword ;
	  private static byte[] wrong_word ;

	  private static void recSuggest (int base, operation last_op, int index_wword,
	                                  int sugg_word_index, boolean was_final,
	                                  int err_cnt)
	  {
	    int real_err_cnt ;
	    int good_letter_code ;
	    int letter_code ;
	    int new_bucket ;
	    int new_base ;
	    boolean is_final ;

	    if (err_cnt <= num_allowed_errors) {
	      if ((base == AutomatonDefs.END_TRANSITION) || (was_final)) {
	        /* The real number of errors must take into account the difference of
	           length between the original word and the place where we are currently
	           inside.
	           If, according to this place, there still is 2 letters in the original
	           word, this corresponds in fact to a found suggestion with 2 "forgets"
	           in addition to the already accumulated errors. Be careful the current
	           index refers a letter we don't have yet processed, so the real number
	           of errors is:
	               (len_wword - 1 - index_wword + err_cnt) + 1
	           We shorten by removing - 1 + 1. */
	        real_err_cnt = len_wword - index_wword + err_cnt ;
	        if (real_err_cnt <= num_allowed_errors) {
	          sugg_word[sugg_word_index] = '\0' ;
	          /* Note: AddSuggestion MUST copy the string passed in argument ! */
	          SuggBasics.addSuggestion (sugg_word, real_err_cnt) ;
	        }
	      }
	      
	      if (base != AutomatonDefs.END_TRANSITION) {
	        if (index_wword < len_wword) {
	          /* Let's consider that the current letter is correct and then that the
	             current letter is wrong (factorise). */
	          good_letter_code = ((int) wrong_word[index_wword]) & 0xFF ;
	          for (letter_code = 1; letter_code <= 255; letter_code++) {
	            if (((base + letter_code) < Global.automaton_size) &&
	                (AutomatonDefs.GETCHAR (Global.automaton[base + letter_code]) ==
	                 letter_code)) {
	              /* Common sub-expression sharing. */
	              new_bucket = Global.automaton[base + letter_code] ;
	              new_base = AutomatonDefs.GETNEXTBASE (new_bucket) ;
	              is_final = AutomatonDefs.GETISFINAL (new_bucket) ;
	              /* We just change the weight and the last performed operation
	                 according to if we are in the case where the letter is correct
	                 or wrong and according to if we are case sensitive or not. */
	              sugg_word[sugg_word_index] = (byte) letter_code ;
	              if ((letter_code == good_letter_code) ||
	                  ((! Global.case_sensitive) &&
	                   (CLikeStringUtils.tolower (letter_code) ==
	                    CLikeStringUtils.tolower (good_letter_code))))
	                recSuggest (new_base, operation.Nop, (index_wword + 1),
	                            (sugg_word_index + 1), is_final, err_cnt) ;
	              else
	                recSuggest (new_base, operation.Wrong, (index_wword + 1),
	                            (sugg_word_index + 1), is_final, (err_cnt + 1)) ;
	            }
	          }
	          
	          /* Let's consider that there is an extra letter in the word. */
	          if (last_op != operation.Add)
	            recSuggest (base, operation.Suppr, (index_wword + 1), sugg_word_index,
	                        false, (err_cnt + 1)) ;

	          /* Let's consider that there is a missing letter in the word. */
	          if (last_op != operation.Suppr) {
	            for (letter_code = 1; letter_code <= 255; letter_code++) {
	              if (((base + letter_code) < Global.automaton_size) &&
	                  (AutomatonDefs.GETCHAR (Global.automaton[base + letter_code]) ==
	                   letter_code)) {
	                /* Common sub-expression sharing. */
	                new_bucket = Global.automaton[base + letter_code] ;
	                new_base = AutomatonDefs.GETNEXTBASE (new_bucket) ;
	                is_final = AutomatonDefs.GETISFINAL (new_bucket) ;
	                sugg_word[sugg_word_index] = (byte) letter_code ;
	                recSuggest
	                  (new_base, operation.Add, index_wword, (sugg_word_index + 1),
	                   is_final, (err_cnt + 1)) ;
	              }
	            }
	          }
	        }
	      }
	    }
	  }



	  public static void suggest (byte[] word)
	  {
	    /* Make some stuff global to save memory on the stack during recursion. */
	    len_wword = CLikeStringUtils.strlen (word) ;
	    wrong_word = word ;
	    if (len_wword >= 7) num_allowed_errors = 2 ;
	    else num_allowed_errors = 1 ;
	    recSuggest (Global.initial_state, operation.Nop, 0, 0, false, 0) ;
	  }

}
