package me.faithfull.rugbydata.rfu;

import lombok.extern.slf4j.Slf4j;
import me.faithfull.rugbydata.PDFSummarizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

/**
 * @author Will Faithfull
 */
@Slf4j
public class RFUPDFSummarizer implements PDFSummarizer {
    public String summarize(PDDocument pdf) {
        String summary = "The Old Elthamians player, Aaron Liffchak, was suspended for 1 week following a red card for striking in a match on 3rd September 2016.";


        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);
            log.info(text);
            pdf.close();
        } catch (IOException e) {
            log.error("Could not close ephemeral PDF",e);
        }
        return summary;
    }
}
