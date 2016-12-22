package me.faithfull.rugbydata;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @author Will Faithfull
 */
public interface PDFSummarizer {

    String summarize(PDDocument pdf);

}
