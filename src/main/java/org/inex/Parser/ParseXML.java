package org.inex.Parser;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
import org.inex.Model.Doc;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseXML {

	/***************/
	/** CONSTANTS **/
	/***************/

	private static final String PATH_DESTINATION_FOLDER = "./files/input/xml/tmpFolder";

	/***************/
	/** FUNCTIONS **/
	/***************/

	/************************/
	/** Decompression work **/
	/************************/

	// Extract files from a *.tar.gz compressed file and put them in a temporary
	// folder
	public static void extractTarGzXmlFiles(String pathInputXml) throws IOException {
		try (TarArchiveInputStream tarStream = new TarArchiveInputStream(
				new GzipCompressorInputStream(new FileInputStream(pathInputXml)))) {

			TarArchiveEntry tarArchiveEntry;
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

		} catch (IOException ex) {
			throw ex;
		}
	}

	// Scan the temporary xml folder and return the list of the xml files
	public static List<String> getXmlPathList() throws IOException {
		Path originFolder = Paths.get(PATH_DESTINATION_FOLDER);
		List<String> fileList = null;

		try (Stream<Path> stream = Files.walk(originFolder, 255)) {
			fileList = stream.filter(file -> file.getFileName().toString().endsWith(".xml")).map(String::valueOf)
					.sorted().collect(Collectors.toList());
		}

		return fileList;
	}

	// Simple display of the fileList for debug purpose
	public static void displayList(int limit) throws IOException {
		List<String> fileList = getXmlPathList();
		for (int i = 0; i < limit; i++) {
			System.out.println(fileList.get(i).toString());
		}
	}

	// Moves all *.xml files from all subfolders to the root directory.
	// Doit être retravaillée, a un problème
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

	// Delete the temporary folder when the data extraction from the xml files is
	// done
	public static void deleteTmpXmlFolder() throws IOException {
		FileUtils.deleteDirectory(new File(PATH_DESTINATION_FOLDER));
	}

	/*****************/
	/** Extract XML **/
	/*****************/

	public static Doc parseXmlFile(String pathInputXml) throws ParserConfigurationException, SAXException, IOException {
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
		String[] ids = id.trim().split(" ");

		// Visit all child nodes of article
		String content = visitChildNodes(nList);

		// Add id and content to doc and return it
		return new Doc(ids[0], content);
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
