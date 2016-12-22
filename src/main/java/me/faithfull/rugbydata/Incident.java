package me.faithfull.rugbydata;

import lombok.Data;

/**
 * @author Will Faithfull
 */
@Data
public class Incident {

    int weeks;
    boolean isBan() { return weeks != 0; }
    String text;
    String name;
    String club;

    GoverningBody governingBody;
    OffenceType offenceType;

    @Override
    public String toString() {
        String banText = isBan() ? String.format("was suspended for %s weeks", this.weeks) : "had their citation dismissed.";
        String offence = this.offenceType == null ? "" : "[" + this.offenceType.toString() + "]";
        return String.format("%s %s %s of %s %s.",super.toString(), offence, name, club, banText);
    }
}
