package org.inex.Parser;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.inex.Model.Doc;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseXML {

	public static Doc extractXml(String pathInputXml) throws ParserConfigurationException, SAXException, IOException {
		// Get Document Builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// Ignore DTD validation
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		factory.setFeature("http://xml.org/sax/features/namespaces", false);
		factory.setFeature("http://xml.org/sax/features/validation", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		// Build Document
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(pathInputXml));

		// Normalize the XML Structure; It's just too important !!
		document.getDocumentElement().normalize();

		// Get all content of tag id
		NodeList iList = document.getElementsByTagName("id");

		// Get all content of tag article
		NodeList nList = document.getElementsByTagName("article");

		// Visit all child nodes of id
		String id = visitChildNodes(iList);

		// Visit all child nodes of article
		String content = visitChildNodes(nList);

		// Add id and content to doc and return it
		return new Doc(id, content);
	}

	private static String visitChildNodes(NodeList nList) {
		String s = "";
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				s = s + " " + node.getTextContent();
				if (node.hasChildNodes()) {
					visitChildNodes(node.getChildNodes());
				}
			}
		}
		return s;
	}

}
