package me.faithfull.rugbydata;

import java.io.IOException;
import java.util.Set;

/**
 * @author Will Faithfull
 */
public interface IncidentScraper {

    Set<Incident> scrapeIncidents() throws IOException;

}
