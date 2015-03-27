package cz.akarienta.fakturator.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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

    public List<String> getCustomers() throws ParserConfigurationException, SAXException, IOException {
        List<String> customers = new ArrayList<String>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.xmlFile);

        Element rootElement = doc.getDocumentElement();
        NodeList customerNodes = rootElement.getChildNodes();

        for (int i = 0; i < customerNodes.getLength(); i++) {
            if (customerNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) customerNodes.item(i);
                customers.add(el.getAttribute(XMLConstants.CUSTOMER_ID_ATTR));
            }
        }

        return customers;
    }

    public Map<String, String> getCustomer(String name) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        return getElementsInMap("//*[@" + XMLConstants.CUSTOMER_ID_ATTR + "='" + name + "']");
    }

    public Map<String, String> getContractor() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        return getElementsInMap(XMLConstants.CONTRACTOR_DATA_XPATH);
    }

    /**
     * @param xPathQuery XPath query to select element
     *
     * @return map, map keys are element names and map values are element values
     */
    private Map<String, String> getElementsInMap(String xPathQuery) throws XPathExpressionException, ParserConfigurationException, IOException, DOMException, SAXException {
        Map<String, String> result = new HashMap<String, String>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.xmlFile);

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(xPathQuery, doc, XPathConstants.NODE);

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                result.put(childNode.getNodeName(), childNode.getTextContent());
            }
        }

        return result;
    }

}
