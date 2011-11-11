/*
 * Modified by P.J.Lawrence (August 2011) to include a user dictionary
 * 
 */

package francois.pessaux.droidic.android;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Exists {
	/* Things made global to save stack during the recursion. */
	  private static byte[] word ;
	  private static int word_len ;
	  
	  private static ArrayList<String> mUserDictionary;

	  private static boolean recExists (int base, int index_word)
	  {
	    int turn_flag, i ;
	    int current_letter ;
	    int tmp_char ;
	    int bucket ;

	    // Determine le nombre de tours qu'il faudra faire en fonction
	    // de l'importance de la casse. Le premier tour est celui ou l'on
	    // tient compte de la casse.
	    if (Global.case_sensitive) turn_flag = 1 ;
	    else turn_flag = 2 ;

	    for (i=0; i<turn_flag; i++) {
	      // Determine en fonction du tour ou on est si on est dans le cas
	      // ou la casse importe ou non. Le premier tour est celui ou l'on
	      // tient compte de la casse.
	      if (i == 0) current_letter = ((int) word[index_word]) & 0xFF ;
	      else {
	        tmp_char = ((int) word[index_word] & 0xFF) ;
	        if (CLikeStringUtils.isupper (tmp_char))
	          current_letter = CLikeStringUtils.tolower (tmp_char) ;
	        else current_letter = CLikeStringUtils.toupper (tmp_char) ;
	        // Pour eviter de faire une recherche inutile si la lettre ne
	        // fait pas de difference entre micusculee et majuscule.
	        if (current_letter == (((int) word[index_word]) & 0xFF)) break ;
	      }

	      // On vérifie que l'on ne sort pas du dictionnaire
	      if ((base + current_letter) < Global.automaton_size) {
	        bucket = Global.automaton[base + current_letter] ;
	        tmp_char = AutomatonDefs.GETCHAR (bucket) ;
	        // On vérifie si la case est bien utilisée (inutile je pense en fait)
	        if (tmp_char != '\0') {
	          // On vérifie s'il existe une transition sur le caractère du mot
	          if (tmp_char == current_letter) {
	            // Si on est à la fin du mot il faut vérifier que ça
	            // correspond à un mot réel du dictionnaire.
	            if (index_word == (word_len - 1)) {
	              // Si le mot correspond a un mot du dictionnaire, c'est bon
	              if (AutomatonDefs.GETISFINAL (bucket)) return (true) ;
	              // Sinon, on arrete et on passe eventuellement au tour suivant s'il en reste.
	              else continue ;
	            }
	            // Sinon (si on n'est pas a la fin du mot), il faut continuer à parcourir le mot
	            else {
	              if (recExists (AutomatonDefs.GETNEXTBASE (bucket), index_word + 1))
	                return (true) ;
	            }
	          }           /* End of if (tmp_char == current_letter) */
	        }             /* End of if (tmp_char != '\0') */
	      }               /* End of if ((base + current_letter) < automaton_size) */
	    }                 /* End of for (i=0; i<run_flag; i++) */
	    return (false) ;
	  }



	  public static boolean exists (byte[] searched_word)
	  {
	    /* Just to make them global and save a bit a stack during recursion. */
	    word = searched_word ;
	    word_len = CLikeStringUtils.strlen (word) ;
	    return (recExists (Global.initial_state, 0)) ;
	  }
	  
	  // User Dictionary
	    
	    static public boolean WordInUserDictionary(String aWord) 
	    {
	    	if (mUserDictionary==null)
	    		return (false);
	    	
	    	for (String aUserWord : mUserDictionary)
	    	{
	    		if (aUserWord.compareTo(aWord)==0) {
	    			return (true);
	    		}
	    	}
	    	return (false);	
	    }
	    
	    static public boolean AddWordUserDictionary(String aWord)
	    {
	    	if (mUserDictionary==null)
	    		mUserDictionary = new ArrayList<String>();
	    	mUserDictionary.add(aWord);
	    	return (true);
	    }
	    
	    public static boolean IsDefined(String aWord)
	    {
	    	// first check user dictionary
	    	if (WordInUserDictionary(aWord))
	    		return (true);
	    	
	    	// now check main dictionary
	    	byte[] aTestWord;
			try {
				aTestWord = CLikeStringUtils.bytesCStringFromString(aWord.toLowerCase());
				if (exists(aTestWord))
    				return (true);
			} 
			catch (UnsupportedEncodingException e) 
			{
			}
    		return (false);			
	    };
}
