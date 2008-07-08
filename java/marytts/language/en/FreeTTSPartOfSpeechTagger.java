/**
 * Copyright 2000-2006 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * Permission is hereby granted, free of charge, to use and distribute
 * this software and its documentation without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of this work, and to
 * permit persons to whom this work is furnished to do so, subject to
 * the following conditions:
 * 
 * 1. The code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 * 2. Any modifications must be clearly marked as such.
 * 3. Original authors' names are not deleted.
 * 4. The authors' names are not used to endorse or promote products
 *    derived from this software without specific prior written
 *    permission.
 *
 * DFKI GMBH AND THE CONTRIBUTORS TO THIS WORK DISCLAIM ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL DFKI GMBH NOR THE
 * CONTRIBUTORS BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 * ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package marytts.language.en;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.language.en_US.datatypes.USEnglishDataTypes;
import marytts.modules.InternalModule;
import marytts.modules.synthesis.FreeTTSVoices;
import marytts.server.MaryProperties;

import org.apache.log4j.Logger;

import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Utterance;



/**
 * Use an individual FreeTTS module for English synthesis.
 *
 * @author Marc Schr&ouml;der
 */

public class FreeTTSPartOfSpeechTagger extends InternalModule
{
    //private UtteranceProcessor processor;
    private Logger logger;
    
    private Map posMap;
    
    
    public FreeTTSPartOfSpeechTagger()
    {
        super("PartOfSpeechTagger",
              USEnglishDataTypes.FREETTS_WORDS,
              USEnglishDataTypes.FREETTS_POS,
              Locale.ENGLISH
              );
        
    }

    public void startup() throws Exception
    {
        super.startup();
        this.logger = Logger.getLogger("FreeTTSPOSTagger");
        buildPosMap();
        // Initialise FreeTTS
        FreeTTSVoices.load();
        //processor = new PartOfSpeechTagger();
    }

    public MaryData process(MaryData d)
    throws Exception
    {
        List utterances = d.getUtterances();
        Iterator it = utterances.iterator();
        while (it.hasNext()) {
            Utterance utterance = (Utterance) it.next();
            //processor.processUtterance(utterance);
            processUtterance(utterance);
        }
        MaryData output = new MaryData(outputType(), d.getLocale());
        output.setUtterances(utterances);
        return output;
    }

    private void buildPosMap(){
        posMap = new HashMap();
        try{
            String posFile = 
                MaryProperties.getFilename("english.freetts.posfile");
            BufferedReader reader = 
                new BufferedReader(new FileReader(new File (posFile)));
            String line = reader.readLine();
            while (line!=null){
                if(!(line.startsWith("***"))){
                    //System.out.println(line);
                    StringTokenizer st = 
                        new StringTokenizer(line," ");
                    String word = st.nextToken();
                    String pos = st.nextToken();
                    posMap.put(word,pos);}
               line = reader.readLine();
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new Error("Unable to build PoS-Map");
        }
    }

    private void processUtterance(Utterance utt){
        logger.debug("Tagging part of speech...");
        for(Item word = utt.getRelation(Relation.WORD).getHead();
        	word != null; word = word.getNext()){
            String pos = null;
            if (posMap.containsKey(word.toString())){
                pos = (String) posMap.get(word.toString());
                logger.debug("Assigning pos \""+pos+"\" to word \""
                        +word.toString()+"\"");
                }
            else {pos = "content";}
            word.getFeatures().setString("pos",pos);
            
        }
    
    }

    

}
