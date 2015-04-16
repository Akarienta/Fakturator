package cz.akarienta.fakturator.data;

/**
 * Contractor data.
 * 
 * @author akarienta
 */
public enum Contractor {
    NAME("name", "Jméno", true),    
    ADDRESS("address", "Ulice a číslo popisné/orientační", true),
    CITY("city", "Město", true),
    POSTAL_CODE("postalCode", "PSČ", true),
    ICO("ico", "IČ", true),
    BANK("bank", "Banka", true),
    ACCOUNT_NUMBER("accountNumber", "Číslo účtu", true),
    PHONE("phone", "Telefon", true),
    MAIL("mail", "E-mail", true),
    WEB("web", "Web", false),
    SIGNATURE_PATH("signaturePath", "Obrázek podpisu", false),
    RESULT_FOLDER("resultFolder", "Složka pro nové faktury", true);

    private final String nodeName;
    private final String label;
    private final boolean mandatory;
    
    private Contractor(String nodeName, String formFieldName, boolean mandatory) {
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
