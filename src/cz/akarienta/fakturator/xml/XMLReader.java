package cz.akarienta.fakturator.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

/**
 * Reader of XML files.
 *
 * @author akarienta
 */
public class XMLReader {

    private final File xmlFile;

    /**
     * @param fileName name of the XML file to read
     */
    public XMLReader(String fileName) {
        this.xmlFile = new File(fileName);
    }

    /**
     * @param xPathQuery XPath query to select element
     * 
     * @return element content text
     */
    public String getElementContentText(String xPathQuery) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.xmlFile);

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(xPathQuery, doc, XPathConstants.NODE);
        return node.getTextContent();
    }
}
