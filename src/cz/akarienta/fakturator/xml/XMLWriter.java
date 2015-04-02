package cz.akarienta.fakturator.xml;

import cz.akarienta.fakturator.data.Contractor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.javatuples.Pair;

/**
 * Writer of XML files.
 *
 * @author akarienta
 */
public class XMLWriter {

    private final File xmlFile;

    public XMLWriter(String fileName) {
        File fileBase = new File(XMLConstants.USER_HOME, XMLConstants.APP_CONF_DIR);
        this.xmlFile = new File(fileBase, fileName);
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

    public void addCustomer(String name, Map<String, String> nodes) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.xmlFile);

        Element rootElement = doc.getDocumentElement();

        Element newCustomer = doc.createElement(XMLConstants.CUSTOMERS_CUSTOMER_ROOT);
        Attr rootElementNameAttr = doc.createAttribute(XMLConstants.CUSTOMERS_CUSTOMER_ID_ATTR);
        rootElementNameAttr.setValue(name);
        newCustomer.setAttributeNode(rootElementNameAttr);

        for (Map.Entry<String, String> node : nodes.entrySet()) {
            String nodeName = node.getKey();
            String nodeValue = node.getValue();

            Element el = doc.createElement(nodeName);
            el.appendChild(doc.createTextNode(nodeValue));
            newCustomer.appendChild(el);
        }

        rootElement.appendChild(newCustomer);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domDoc = new DOMSource(doc);
        StreamResult res = new StreamResult(this.xmlFile);
        trans.transform(domDoc, res);
    }

    public void removeCustomer(String name) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.xmlFile);

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate("//*[@" + XMLConstants.CUSTOMERS_CUSTOMER_ID_ATTR + "='" + name + "']", doc, XPathConstants.NODE);
        doc.getDocumentElement().removeChild(node);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        DOMSource domDoc = new DOMSource(doc);
        StreamResult res = new StreamResult(this.xmlFile);
        trans.transform(domDoc, res);
    }

    public void createInvoice(Map<String, String> contractor, Map<String, String> customer, Map<String, String> details, List<Pair<String, String>> items) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, FileNotFoundException {

        PrintWriter writer = new PrintWriter(this.xmlFile);
        writer.print("");
        writer.close();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootEl = doc.createElement(XMLConstants.INVOICE_ROOT);
        doc.appendChild(rootEl);

        Element contractorEl = doc.createElement(XMLConstants.CONTRACTOR_ROOT);
        for (Map.Entry<String, String> item : contractor.entrySet()) {
            Element el = doc.createElement(item.getKey());
            el.appendChild(doc.createTextNode(item.getValue()));
            contractorEl.appendChild(el);
        }
        rootEl.appendChild(contractorEl);

        Element customerEl = doc.createElement(XMLConstants.CUSTOMERS_CUSTOMER_ROOT);
        for (Map.Entry<String, String> item : customer.entrySet()) {
            Element el = doc.createElement(item.getKey());
            el.appendChild(doc.createTextNode(item.getValue()));
            customerEl.appendChild(el);
        }
        rootEl.appendChild(customerEl);

        Element detailsEl = doc.createElement(XMLConstants.DETAILS_ROOT);
        for (Map.Entry<String, String> item : details.entrySet()) {
            Element el = doc.createElement(item.getKey());
            el.appendChild(doc.createTextNode(item.getValue()));
            detailsEl.appendChild(el);
        }
        rootEl.appendChild(detailsEl);

        Element itemsEl = doc.createElement(XMLConstants.ITEMS_ROOT);
        for (Pair<String, String> item : items) {
            Element itemEl = doc.createElement(XMLConstants.ITEMS_ITEM_ROOT);
            Element nameEl = doc.createElement(XMLConstants.ITEMS_ITEM_NAME);
            nameEl.appendChild(doc.createTextNode(item.getValue0()));
            itemEl.appendChild(nameEl);
            Element priceEl = doc.createElement(XMLConstants.ITEMS_ITEM_PRICE);
            priceEl.appendChild(doc.createTextNode(item.getValue1()));
            itemEl.appendChild(priceEl);

            itemsEl.appendChild(itemEl);
        }
        rootEl.appendChild(itemsEl);

        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domDoc = new DOMSource(doc);
        StreamResult res = new StreamResult(this.xmlFile);
        trans.transform(domDoc, res);
    }

    public void createEmptyContractor() throws ParserConfigurationException, TransformerConfigurationException, TransformerException, FileNotFoundException {
        PrintWriter writer = new PrintWriter(this.xmlFile);
        writer.print("");
        writer.close();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootEl = doc.createElement(XMLConstants.CONTRACTOR_ROOT);
        doc.appendChild(rootEl);

        List<Contractor> contractorFields = new ArrayList<Contractor>();
        contractorFields.add(Contractor.NAME);
        contractorFields.add(Contractor.ADDRESS);
        contractorFields.add(Contractor.CITY);
        contractorFields.add(Contractor.POSTAL_CODE);
        contractorFields.add(Contractor.ICO);
        contractorFields.add(Contractor.BANK);
        contractorFields.add(Contractor.ACCOUNT_NUMBER);
        contractorFields.add(Contractor.PHONE);
        contractorFields.add(Contractor.MAIL);
        contractorFields.add(Contractor.WEB);
        contractorFields.add(Contractor.SIGNATURE_PATH);
        contractorFields.add(Contractor.RESULT_FOLDER);

        for (Contractor field : contractorFields) {
            Element el = doc.createElement(field.getNodeName());
            rootEl.appendChild(el);
        }

        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domDoc = new DOMSource(doc);
        StreamResult res = new StreamResult(this.xmlFile);
        trans.transform(domDoc, res);
    }

    public void createEmptyCustomers() throws ParserConfigurationException, TransformerConfigurationException, TransformerException, FileNotFoundException {
        PrintWriter writer = new PrintWriter(this.xmlFile);
        writer.print("");
        writer.close();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootEl = doc.createElement(XMLConstants.CUSTOMERS_ROOT);
        doc.appendChild(rootEl);

        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domDoc = new DOMSource(doc);
        StreamResult res = new StreamResult(this.xmlFile);
        trans.transform(domDoc, res);
    }

    public void createEmptyNumber() throws ParserConfigurationException, TransformerConfigurationException, TransformerException, FileNotFoundException {
        PrintWriter writer = new PrintWriter(this.xmlFile);
        writer.print("");
        writer.close();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootEl = doc.createElement(XMLConstants.NUMBER_ROOT);
        rootEl.appendChild(doc.createTextNode("1"));
        doc.appendChild(rootEl);

        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domDoc = new DOMSource(doc);
        StreamResult res = new StreamResult(this.xmlFile);
        trans.transform(domDoc, res);
    }
}
