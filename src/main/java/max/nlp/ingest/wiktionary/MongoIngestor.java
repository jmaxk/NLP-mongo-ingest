package max.nlp.ingest.wiktionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import max.nlp.dal.wiktionary.WiktionaryDB;
import max.nlp.dal.wiktionary.types.Section;
import max.nlp.dal.wiktionary.types.WiktionaryEntry;

public class MongoIngestor {

	private String WIKTIONARY_FILE;
	private String IDIOMS_FILE;

	public static List<Section> parseSubSections(String content) {
		List<Section> subSections = new ArrayList<Section>();
		String[] lines = content.split("\n");
		StringBuilder sectionContent = new StringBuilder();
		String sectionTitle = "";

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.contains("====")) {
				if (!sectionTitle.isEmpty()) {
					subSections.add(new Section(sectionTitle, sectionContent
							.toString()));
					sectionContent = new StringBuilder();
				}
				sectionTitle = line.replaceAll("=", "");
			} else {
				sectionContent.append(line + "\n");
			}
		}
		subSections.add(new Section(sectionTitle, sectionContent.toString()));
		return subSections;
	}

	private static Pattern LANGUAGE_PATTERN = Pattern
			.compile("[^=]+==([a-zA-Z\\- ]*)==");

	public static List<WiktionaryEntry> parsePage(String title, String text) {
		List<WiktionaryEntry> entries = new ArrayList<WiktionaryEntry>();
		String[] lines = text.split("\n");
		Section root = new Section(title, text);
		StringBuilder subSectionContent = new StringBuilder();
		StringBuilder subSubSectionContent = new StringBuilder();
		boolean first = true;
		String content = "";
		Section subSection = null;
		String subSectionTitle = "";
		String language = null;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (language == null) {
				Matcher m = LANGUAGE_PATTERN.matcher(" " + line);
				while (m.find()) {
					if (!m.group(1).isEmpty())
						language = m.group(1);
				}
			}

			if (line.contains("===") && !line.contains("====")) {
				if (!subSectionTitle.isEmpty()) {
					first = true;
					if (subSection == null) {
						subSection = new Section(subSectionTitle,
								subSectionContent.toString());
					} else {
						subSection
								.addSections(parseSubSections(subSubSectionContent
										.toString()));
					}
					subSection.setHeading(subSectionTitle);
					root.addSection(subSection);
					subSection = null;
					subSectionContent = new StringBuilder();
					subSubSectionContent = new StringBuilder();
				}
				subSectionTitle = line.replaceAll("=", "");
			}

			else if (line.contains("====")) { // ==== line
				subSubSectionContent.append(line + "\n");
				if (first) { // insdie of a === and this is the first subsection
					content = subSectionContent.toString();
					subSection = new Section(subSectionTitle, content);
					subSectionContent = new StringBuilder();
					first = false;
				}

			} else {
				subSectionContent.append(line + "\n");
				if (!first)
					subSubSectionContent.append(line + "\n");
			}
		}

		if (subSection == null) {
			subSection = new Section(subSectionTitle,
					subSectionContent.toString());
		} else {
			subSection.addSections(parseSubSections(subSubSectionContent
					.toString()));
		}
		subSection.setHeading(subSectionTitle);
		root.addSection(subSection);

		if (root.countPOSTags() >= 1) {
			for (Section sect : root.getSubSections()) {
				if (sect.isPOSSection()) {
					WiktionaryEntry e = new WiktionaryEntry(sect, root);
					e.setLanguage(language);
					entries.add(e);
				}
			}
		}
		return entries;
	}

	public static HashMap<String, String> readIdioms(String idiomsFile) {
		HashMap<String, String> formattedTitles = new HashMap<String, String>();

		try {
			// Read in the list of pages we care about from idioms.txt
			InputStream inputStream = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(idiomsFile);
			BufferedReader idiomsReader = new BufferedReader(
					new InputStreamReader(inputStream));
			String idiomsLine = "";
			@SuppressWarnings("unused")
			String currentLanguage = "";
			while ((idiomsLine = idiomsReader.readLine()) != null) {
				if (idiomsLine.contains("**Category:")) {
					currentLanguage = idiomsLine
							.replaceAll("_idioms\\*\\*", "").replaceAll(
									"\\*\\*Category:", "");
				} else {
					formattedTitles.put("<title>" + idiomsLine + "</title>",
							idiomsLine);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return formattedTitles;
	}

	public MongoIngestor(String wiktionaryFile, String idiomsFile) {
		WIKTIONARY_FILE = wiktionaryFile;
		IDIOMS_FILE = idiomsFile;
	}

	public void ingest(boolean onlyIdioms) {
		try {

			// read the idioms file into a map so we know what article sto look
			// for
			HashMap<String, String> formattedTitles = readIdioms(IDIOMS_FILE);

			// Setup morphia
			WiktionaryDB db = WiktionaryDB.getInstance();
			// Read in the wiktionary dump
			File wikiXmlFile = new File(WIKTIONARY_FILE);
			BufferedReader b = new BufferedReader(new FileReader(wikiXmlFile));
			String line = "";

			// It uses too much memory to parse it all at once. so we only parse
			// the
			// pages that have a title if we are interested.
			boolean onPageOfInterest = false;
			boolean startParsing = false;
			boolean idiomatic = false;
			String title = "";
			StringBuilder page = new StringBuilder();
			while ((line = b.readLine()) != null) {
				if (line.contains("<title>")) {

					if (formattedTitles.keySet().contains(line.trim())) {
						idiomatic = true;
					}
					if (onlyIdioms) {
						if (formattedTitles.keySet().contains(line.trim())) {
							onPageOfInterest = true;
							title = formattedTitles.get(line.trim());
						}
					} else {
						onPageOfInterest = true;
						title = line.replaceAll("<[a-zA-Z/]*>", "").trim();
					}
				}
				if (onPageOfInterest) {
					if (line.contains("<text xml:space=\"preserve\">")) {
						startParsing = true;
					} else if (line.contains("</text>")) {

						page.append(line);
						// PARSE the page and store it
						List<WiktionaryEntry> entries = parsePage(title.trim(),
								page.toString());
						for (WiktionaryEntry e : entries) {
							e.setIdiomatic(idiomatic);
			
							db.saveEntry(e);
						}
						page = new StringBuilder();
						onPageOfInterest = false;
						startParsing = false;
						idiomatic = false;
					}
					if (startParsing)
						page.append(line + "\n");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}