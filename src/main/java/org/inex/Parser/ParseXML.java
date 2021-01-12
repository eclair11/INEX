package org.inex.Parser;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.inex.App.Input;
import org.inex.Model.Doc;
import org.inex.Utils.UtilTextTransformation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseXML {

	private static final String PATH_DESTINATION_FOLDER = "./files/input/xml/tmpFolder";

	/**
	 * Extract files from a *.tar.gz archive and put them in a temporary folder
	 * 
	 * @param pathInputXml Path to the archive that contain XML files
	 * @throws IOException
	 */
	public static void extractTarGzXmlFiles(String pathInputXml) throws IOException {
		FileInputStream fileStream = new FileInputStream(pathInputXml);
		GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(fileStream);
		TarArchiveInputStream tarStream = new TarArchiveInputStream(gzipStream);
		TarArchiveEntry tarArchiveEntry = null;
		while ((tarArchiveEntry = tarStream.getNextTarEntry()) != null) {
			if (tarArchiveEntry.isDirectory()) {
				continue;
			}
			File file = new File(PATH_DESTINATION_FOLDER, tarArchiveEntry.getName());
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			IOUtils.copy(tarStream, new FileOutputStream(file));
		}
	}

	/**
	 * Scan the temporary XML folder and return the list of the XML files
	 * 
	 * @return List with the content of the XML files
	 * @throws IOException
	 */
	public static List<String> getXmlPathList() throws IOException {
		Path originFolder = Paths.get(PATH_DESTINATION_FOLDER);
		Stream<Path> stream = Files.walk(originFolder, 255);
		List<String> fileList = stream.filter(file -> file.getFileName().toString().endsWith(".xml"))
				.map(String::valueOf).sorted().collect(Collectors.toList());
		stream.close();
		return fileList;
	}

	/**
	 * Moves all *.xml files from subfolders to the root directory [NEED REWORK]
	 * 
	 * @throws IOException
	 */
	public static void filesGathering() throws IOException {
		List<String> fileList = getXmlPathList();
		fileList.forEach(file -> {
			try {
				if (file.endsWith(".xml")) {
					Files.move(Paths.get(file), Paths.get(PATH_DESTINATION_FOLDER), REPLACE_EXISTING);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Delete temporary folder when data extraction is done
	 * 
	 * @throws IOException
	 */
	public static void deleteTmpXmlFolder() throws IOException {
		FileUtils.deleteDirectory(new File(PATH_DESTINATION_FOLDER));
	}

	/**
	 * Parse the content of the XML file
	 * 
	 * @param pathFileXml   Path to the XML file
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @return Parsed content as an object Doc
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Doc parseXmlFile(String pathFileXml, Input input, boolean applyStemming)
			throws ParserConfigurationException, SAXException, IOException {

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
		Document document = builder.parse(new File(pathFileXml));

		// Normalize the XML Structure; It's just too important !!
		document.getDocumentElement().normalize();

		// Get id content
		String id = document.getElementsByTagName("id").item(0).getTextContent();

		// Get all content of tag article
		NodeList list = document.getElementsByTagName("article");

		// Initialize doc object
		Doc doc = null;

		if (input.equals(Input.XML_ARTICLES)) {
			// Initialize list of links
			ArrayList<String> links = new ArrayList<>();
			// Visit all child nodes of article
			String content = visitChildNodesArticles(list, links);
			// Create Doc object using the id and article content of the parsed file
			doc = new Doc(id, content, applyStemming);
			// Add links to the created Doc object
			doc.setLinks(links);
		} else {
			// Create a map where content will be separated by elements (XML tags)
			Map<String, ArrayList<String>> elements = new HashMap<>();
			// Visit all child nodes of article
			visitChildNodesElements(list, elements, "/", applyStemming);
			// Create Doc object using the id and elements of the parsed file
			doc = new Doc(id, elements);
		}

		// Return the document
		return doc;

	}

	/**
	 * Visit all child of the nodes in the list (return the article content)
	 * 
	 * @param list  Contain nodes in the same depth
	 * @param links Contain links of the parsed documents
	 * @return Content in the list of nodes
	 */
	private static String visitChildNodesArticles(NodeList list, ArrayList<String> links) {
		String content = "";
		for (int temp = 0; temp < list.getLength(); temp++) {
			Node node = list.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				content = content + " " + node.getTextContent();
				if (node.getNodeName().equals("link")) {
					String link = "";
					for (int i = 0; i < node.getAttributes().getLength(); i++) {
						if (node.getAttributes().item(i) != null) {
							link = link + " " + node.getAttributes().item(i);
						}
					}
					if (!link.isEmpty()) {
						links.add(link);
					}
				}
				if (node.hasChildNodes()) {
					visitChildNodesArticles(node.getChildNodes(), links);
				}
			}
		}
		return content;
	}

	/**
	 * Visit all child of the nodes in the list (return the elements content)
	 * 
	 * @param list          Contain nodes in the same depth
	 * @param elements      Map to store nodes path and content
	 * @param parent        Path formed by the concatenation of the node's ancestors
	 *                      paths
	 * @param applyStemming Boolean to choose using stemming during parsing
	 * @throws IOException
	 */
	private static void visitChildNodesElements(NodeList list, Map<String, ArrayList<String>> elements, String parent,
			boolean applyStemming) throws IOException {
		for (int temp = 0; temp < list.getLength(); temp++) {
			Node node = list.item(temp);
			int index = 1;
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Node sibling = node.getPreviousSibling();
				while (sibling != null) {
					if (sibling.getNodeType() == Node.ELEMENT_NODE) {
						if (sibling.getNodeName().equals(node.getNodeName())) {
							index++;
						}
					}
					sibling = sibling.getPreviousSibling();
				}
				String key = parent + node.getNodeName() + "[" + index + "]";
				if (node.getNodeName().equals("title") || node.getNodeName().equals("sec")) {
					ArrayList<String> value = UtilTextTransformation.cleanContentList(node.getTextContent(),
							applyStemming);
					elements.put(key, value);
				}
				if (node.hasChildNodes()) {
					visitChildNodesElements(node.getChildNodes(), elements, key + "/", applyStemming);
				}
			}
		}
	}

}
