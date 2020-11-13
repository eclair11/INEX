package org.inex;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class StringBetween {

	public static void main(String[] args) {
		
		String str = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n"
				+ "<html>\r\n"
				+ "<head>\r\n"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">\r\n"
				+ "<title>My Title</title>\r\n"
				+ "</head>\r\n"
				+ "<body>\r\n"
				+ "<table>\r\n"
				+ "	<tr>\r\n"
				+ "		<td>One \n three</td>\r\n"
				+ "		<td>Two</td>\r\n"
				+ "	</tr>\r\n"
				+ "	<tr>\r\n"
				+ "		<td>Three</td>\r\n"
				+ "		<td>Four</td>\r\n"
				+ "	</tr>\r\n"
				+ "</table>\r\n"
				+ "</body>\r\n"
				+ "</html>";

		// En fonction du cas on préférera la version la plus simple
		String regexOne = "<(.+)>([^<]+)</\\1>";
		String regexTwo = "\\<(.+)\\>([^\\<\\>]+)\\<\\/\\1\\>";
		
		Pattern p = Pattern.compile(regexTwo);
		Matcher m = p.matcher(str);

		// Parcours du String
		while (m.find()) {
			// Fonctionne suivant une profondeur de noeuds
			String contentGroup = m.group(2);
			// on exclue ici la balise titre de niveau 1
			if(!(m.group(1).equals("title"))) {
				System.out.format("'%s'\n", contentGroup);
			}
			
		}

	}

}
