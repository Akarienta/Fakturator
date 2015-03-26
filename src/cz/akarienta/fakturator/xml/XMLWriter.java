package cz.akarienta.fakturator.xml;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Writer of XML files.
 * 
 * @author akarienta
 */
public class XMLWriter {
    
    private final File xmlFile;
    
    public XMLWriter(String fileName) {
        this.xmlFile = new File(fileName);
    }
    
    /**
     * @param nodeName name of the node to change
     * @param newValue new node value
     */
    public void changeNodeValue(String nodeName, String newValue) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.xmlFile); 
        
        Node node = doc.getElementsByTagName(nodeName).item(0);
        node.setTextContent(newValue);
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        DOMSource domDoc = new DOMSource(doc);
        StreamResult res = new StreamResult(this.xmlFile);
        trans.transform(domDoc, res);
    }
}
