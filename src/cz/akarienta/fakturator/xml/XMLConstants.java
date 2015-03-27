
package cz.akarienta.fakturator.xml;

/**
 * Global XML constants.
 * 
 * @author akarienta
 */
public class XMLConstants {
    
    // String constants
    private static final String SLASH = "/";
    
    // invoice
    public static final String INVOICE_ROOT = "invoice";
    
    // contractor
    public static final String CONTRACTOR_ROOT = "contractor";
    public static final String CONTRACTOR_RESULT_FOLDER = "resultFolder";
    
    // customer
    public static final String CUSTOMER_ROOT = "customer";
    public static final String CUSTOMER_ID_ATTR = "name";
    
    // details
    public static final String DETAILS_ROOT = "details";
    public static final String DETAILS_INVOICE_NUMBER = "invoiceNumber";
    public static final String DETAILS_TOTAL_SUM = "totalSum";
    
    // items
    public static final String ITEMS_ROOT = "items";
    public static final String ITEMS_ITEM_ROOT = "item";
    public static final String ITEMS_ITEM_NAME = "name";
    public static final String ITEMS_ITEM_PRICE = "price";
    
    // xml data files
    private static final String DATA_BASE_DIR = "src/cz/akarienta/fakturator/data/";
    public static final String CONTRACTOR_DATA = DATA_BASE_DIR + "contractor.xml";
    public static final String CUSTOMERS_DATA = DATA_BASE_DIR + "customers.xml";
    public static final String INVOICE_DATA = DATA_BASE_DIR + "invoice.xml";

    // XPath queries
    public static final String CONTRACTOR_XPATH = SLASH + INVOICE_ROOT + SLASH + CONTRACTOR_ROOT;
    public static final String CONTRACTOR_DATA_XPATH = SLASH + CONTRACTOR_ROOT;
    public static final String CUSTOMER_XPATH = SLASH + INVOICE_ROOT + SLASH + CUSTOMER_ROOT;
    public final static String INVOICE_NUMBER_XPATH = SLASH + INVOICE_ROOT + SLASH + DETAILS_ROOT + SLASH + DETAILS_INVOICE_NUMBER;
    public final static String RESULT_FOLDER_XPATH = SLASH + INVOICE_ROOT + SLASH + CONTRACTOR_ROOT + SLASH + CONTRACTOR_RESULT_FOLDER;
}
