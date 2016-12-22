package me.faithfull.rugbydata;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import me.faithfull.rugbydata.rfu.RFUPDFSummarizer;
import me.faithfull.rugbydata.rfu.RFUScraper;

import java.io.IOException;
import java.util.Set;

/**
 * @author Will Faithfull
 */
public class Main {

    public static void main(String[] args) throws IOException {
        RFUScraper rfuScraper = new RFUScraper(new NLPIncidentParser(new StanfordCoreNLP()));
        //rfuScraper.setSummarizer(new RFUPDFSummarizer());

        Set<Incident> incidents = rfuScraper.scrapeIncidents();
    }

}
