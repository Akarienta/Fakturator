package cz.akarienta.fakturator.data;

import cz.akarienta.fakturator.xml.XMLConstants;

/**
 * Detail data.
 * 
 * @author akarienta
 */
public enum Detail {
    INVOICE_NUMBER(XMLConstants.DETAILS_INVOICE_NUMBER, "Číslo dokladu", true),    
    ISSUE_DATE("issueDate", "Datum vystavení", true),
    TAX_DATE("taxDate", "Datum z. p.", false),
    PAYMENT_DUE("paymentDue", "Datum splatnosti", true);

    private final String nodeName;
    private final String label;
    private final boolean mandatory;
    
    private Detail(String nodeName, String formFieldName, boolean mandatory) {
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
