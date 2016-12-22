package me.faithfull.rugbydata;

import edu.stanford.nlp.ie.NumberNormalizer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Will Faithfull
 */
@Slf4j
public class NLPIncidentParser implements IncidentParser {

    StanfordCoreNLP nlp;

    NLPIncidentParser(StanfordCoreNLP nlp) {
        this.nlp = nlp;
    }

    public Incident parse(String text) {
        Annotation document = nlp.process(text);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        StringBuilder person = new StringBuilder();
        StringBuilder club = new StringBuilder();
        Number duration = 0;

        // Tags the numbers
        NumberNormalizer.findNumbers(document);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

            for (int i=0; i<tokens.size(); i++) {

                CoreLabel token = tokens.get(i);
                String originalText = token.originalText().toLowerCase();
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if(ne.equals("ORGANIZATION")) {
                    club.append(token.originalText() + " ");
                } else if(ne.equals("PERSON")) {
                    person.append(token.originalText() + " ");
                } else if(ne.equals("DURATION")) {
                    if(token.has(CoreAnnotations.NumericCompositeValueAnnotation.class)) {
                        duration = token.get(CoreAnnotations.NumericCompositeValueAnnotation.class);
                    }
                }

                if(originalText.equals("weeks") || originalText.equals("week")) {
                    if(duration.intValue() == 0) {
                        log.warn("Found a token that matches \"week\" / \"weeks\" in the text, but duration does not appear to have been discovered by NER analysis. Will attempt to extract manually from previous token...");
                        CoreLabel prev = tokens.get(i-1);

                        boolean isNumeric = prev.has(CoreAnnotations.NumericCompositeValueAnnotation.class);
                        log.warn("Adjacent previous token \"{}\" {}", prev.originalText(),
                                isNumeric ? "appears to be numeric. Setting duration..." : "does not appear to be numeric. Giving up.");

                        if(isNumeric) {
                            duration = prev.get(CoreAnnotations.NumericCompositeValueAnnotation.class);
                        }
                    }
                }
            }


        }

        Incident incident = new Incident();
        incident.setText(text);
        incident.setName(person.toString().trim());
        incident.setClub(club.toString().trim());
        incident.setWeeks(duration.intValue());
        return incident;
    }
}
