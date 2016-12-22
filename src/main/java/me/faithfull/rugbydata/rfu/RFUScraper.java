package me.faithfull.rugbydata.rfu;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.faithfull.rugbydata.*;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Will Faithfull
 *
 * Web scraper which is tailored for the judicial decisions page on englandrugby.com
 */
@Slf4j
public class RFUScraper implements IncidentScraper {

    private static HashMap<OffenceType, String> rfuCodes = new HashMap<OffenceType, String>();

    static {
        rfuCodes.put(OffenceType.MISCONDUCT, "200009");
        rfuCodes.put(OffenceType.ACT_CONTRARY_TO_GOOD_SPORTSMANSHIP, "125");
        rfuCodes.put(OffenceType.ANTI_DOPING, "200008");
        rfuCodes.put(OffenceType.COMPETITION_APPEAL, "126");
        rfuCodes.put(OffenceType.DANGEROUS_CHARGING, "200010");
        rfuCodes.put(OffenceType.DANGEROUS_PLAY_IN_SCRUM_RUCK_OR_MAUL, "200012");
        rfuCodes.put(OffenceType.DANGEROUS_TACKLING, "127");
        rfuCodes.put(OffenceType.DISSENT, "128");
        rfuCodes.put(OffenceType.KICKING, "129");
        rfuCodes.put(OffenceType.PLAYING_AN_OPPONENT_WITHOUT_THE_BALL, "200011");
        rfuCodes.put(OffenceType.PUNCHING_OR_STRIKING, "130");
        rfuCodes.put(OffenceType.RETALIATION, "200015");
        rfuCodes.put(OffenceType.STAMPING_OR_TRAMPLING, "131");
        rfuCodes.put(OffenceType.TACKLING_THE_JUMPER_IN_THE_AIR, "132");
        rfuCodes.put(OffenceType.TIP_TACKLING, "133");
        rfuCodes.put(OffenceType.TRIPPING, "134");
    }

    static final String webRoot = "http://www.englandrugby.com";
    static final String searchUri = "/governance/discipline/judgments-document-search/results";

    @Getter @Setter
    private IncidentParser parser;

    @Getter @Setter
    private PDFSummarizer summarizer;

    @Getter @Setter
    private String season = "20162017";

    public RFUScraper(IncidentParser parser) {
        this.parser = parser;
    }

    public Set<Incident> scrapeIncidents() throws IOException {
        Set<Incident> allIncidents = new HashSet<Incident>();

        for(OffenceType offenceType : OffenceType.values()) {
            Set<Incident> offenceIncidents = scrapeOffenceType(offenceType);
            allIncidents.addAll(offenceIncidents);
        }

        return allIncidents;
    }

    public Set<Incident> scrapeOffenceType(OffenceType offenceType) throws IOException {
        Document document =
                Jsoup.connect(webRoot + searchUri)
                        .data("searchByType", "2")              // Type 2 = search by offence type
                        .data("searchBySeason", this.season)
                        .data("searchByLeague", "1")
                        .data("searchByClub", "41108")
                        .data("searchByOffenceType", rfuCodes.get(offenceType))
                        .data("submit", "search")
                        .post();

        Elements results = document.select(".documentsResults");

        Set<Incident> incidents = new HashSet<Incident>();
        for (Element element : results.select("p > a")) {
            String uri = element.attr("href");
            String summary = null;
            if (!uri.toLowerCase().endsWith(".pdf")) {
                summary = Jsoup.connect(webRoot + uri)
                        .get()
                        .select("div.newsBody")
                        .text();
            } else {
                if(this.summarizer != null) {
                    log.info("Retrieving PDF from URI: {}", uri);
                    PDDocument pdf = downloadPDFFromUrl(new URL(webRoot + uri));
                    summary = this.summarizer.summarize(pdf);
                } else {
                    continue;
                }
            }

            Incident incident = parser.parse(summary);
            incident.setOffenceType(offenceType);
            incident.setGoverningBody(GoverningBody.RFU);
            incidents.add(incident);
            log.info("{}", incident);
        }
        return incidents;
    }

    PDDocument downloadPDFFromUrl(URL url) throws IOException {
        InputStream is = null;
        PDDocument pdf = null;
        try {
            URLConnection urlConn = url.openConnection();
            is = urlConn.getInputStream();
            pdf = PDDocument.load(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return pdf;
    }
}
