package cz.akarienta.fakturator.data;

/**
 * Customer data.
 * 
 * @author akarienta
 */
public enum Customer {
    NAME("name", "Jméno", true),    
    ADDRESS("address", "Ulice a č.p./č.o.", true),
    CITY("city", "Město", true),
    POSTAL_CODE("postalCode", "PSČ", true),
    ICO("ico", "IČ", false),
    DIC("dic", "DIČ", false),
    SAVE_AS("saveAs", "Uložit odběratele jako", true);

    private final String nodeName;
    private final String label;
    private final boolean mandatory;
    
    private Customer(String nodeName, String formFieldName, boolean mandatory) {
        this.nodeName = nodeName;
        this.label = formFieldName;
        this.mandatory = mandatory;
    }
    
    public String getNodeName() {
        return this.nodeName;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public boolean isMandatory() {
        return this.mandatory;
    }
}
