package cz.akarienta.fakturator;

import cz.akarienta.fakturator.xml.XMLConstants;
import cz.akarienta.fakturator.xml.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FOURIResolver;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Renders invoice from XML into PDF.
 *
 * @author akarienta
 */
public class InvoiceCreator {

    public final static String INVOICE_FILENAME_BASE  = "faktura_%s.pdf";
    
    private final static String INVOICE_RESOURCE_PACKAGE = "/cz/akarienta/fakturator/pdf/";
    private final static String INVOICE_DEFAULT_TEMPLATE = INVOICE_RESOURCE_PACKAGE + "invoiceDefaultTemplate.xsl";
    private final static String FONT_CONFIG = INVOICE_RESOURCE_PACKAGE + "fontcfg.xml";

    private final File baseDir;
    private final File outDir;

    private final File xmlFile;
    private final InputStream xsltFile;
    private final InputStream fontFile;
    private final File pdfFile;
    
    /**
     * @param resultFolder folder where to store rendered invoice
     */
    public InvoiceCreator(File xmlFile) throws URISyntaxException {
        this.xmlFile = xmlFile;
        
        this.baseDir = new File(InvoiceCreator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        
        //File resourceBase = new File(baseDir, INVOICE_RESOURCE_PACKAGE);
        this.xsltFile = getClass().getResourceAsStream(INVOICE_DEFAULT_TEMPLATE);
        this.fontFile = getClass().getResourceAsStream(FONT_CONFIG);
        
        this.outDir = new File(getResultDir());
        this.outDir.mkdirs();
        this.pdfFile = new File(outDir, getInvoiceFilename());
    }

    private String getResultDir() {
        try {
            XMLReader xmlReader = new XMLReader(this.xmlFile.getName());
            return xmlReader.getElementContentText(XMLConstants.RESULT_FOLDER_XPATH);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException ex) {
            ex.printStackTrace();
            return XMLConstants.USER_HOME;
        }
    }

    private String getInvoiceFilename() {
        try {
            XMLReader xmlReader = new XMLReader(this.xmlFile.getName());
            return String.format(INVOICE_FILENAME_BASE, xmlReader.getElementContentText(XMLConstants.INVOICE_NUMBER_XPATH));
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException ex) {
            ex.printStackTrace();
            return String.format(INVOICE_FILENAME_BASE, UUID.randomUUID());
        }
    }

    public void createInvoice() throws SAXException, IOException, ConfigurationException, TransformerConfigurationException, TransformerException {
        // Configure fopFactory as desired
        FopFactory fopFactory = FopFactory.newInstance();
        
        FOURIResolver uriResolver = (FOURIResolver) fopFactory.getURIResolver();
        uriResolver.setCustomURIResolver(new ClasspathUriResolver());
        
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.build(this.fontFile);
        fopFactory.setUserConfig(cfg);

        // Configure foUserAgent as desired
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        // Setup output
        OutputStream out = new java.io.FileOutputStream(this.pdfFile);
        out = new java.io.BufferedOutputStream(out);
        
        // Construct fop with desired output format
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

        // Setup XSLT
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new StreamSource(this.xsltFile));

        // Setup input for XSLT transformation
        Source src = new StreamSource(this.xmlFile);

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Start XSLT transformation and FOP processing
        transformer.transform(src, res);
            
        out.close();
    }
}
