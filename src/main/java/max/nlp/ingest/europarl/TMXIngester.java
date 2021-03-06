package max.nlp.ingest.europarl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import max.nlp.dal.opus.subtitles.CorporaDB;
import max.nlp.dal.opus.subtitles.SentenceGroup;

public class TMXIngester {

	private static String INPUT_FILE = "/home/max/resources/subtitles13/en-lt.tmx";
	private static int DUMP_SIZE = 1000;

	private String l1;
	private String l2;
	private TMXIngester(String l1, String l2){
		this.l1 = l1;
		this.l2 = l2;
	}
	
	
	public static void main(String[] args) {

		TMXIngester t = new TMXIngester("en", "lt");
//		t.writeToDB(INPUT_FILE);
		t.writeToText("/home/max/resources/subtitles13/en", "/home/max/resources/subtitles13/lt");
	}

	/**
	 * This class can parse a TMX file downloadable from the Open Source
	 * Parallel Subtitles corpus V2, and output the translation into two
	 * separate files. The locations of the files are determined by the
	 * FilePaths class.
	 * 
	 * @param args
	 */
	public void writeToText(String l1OutputFile, String l2OutputFile) {
		try {

			BufferedReader tmxReader = new BufferedReader(
					new FileReader(
							new File(
									INPUT_FILE)));
			PrintWriter l1_output = new PrintWriter(new FileWriter(new File(
					l1OutputFile)));
			PrintWriter l2_output = new PrintWriter(new FileWriter(new File(
					l2OutputFile)));
			String line;


			while ((line = tmxReader.readLine()) != null) {
				if ((!(line.contains("srclang")))
						&& (!(line.contains("adminlang")))) {
					if (line.contains("\"" + l1 + "\"")) {

						line = line
								.replaceAll("<tuv xml:lang=\"" + l1 + "\">", "")
								.replaceAll("</seg>", "")
								.replaceAll("<seg>", "")
								.replaceAll("</tuv>", "");
						l1_output.println(line.trim());
					}
					if (line.contains("\"" + l2 + "\"")) {
						line = line
								.replaceAll("<tuv xml:lang=\"" + l2 + "\">", "")
								.replaceAll("</seg>", "")
								.replaceAll("<seg>", "")
								.replaceAll("</tuv>", "");
						l2_output.println(line.trim());
					}
				}

			}
			l1_output.flush();
			l2_output.flush();
			l1_output.close();
			l2_output.close();
			tmxReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeToDB(String inputFile) {
		try {
			CorporaDB db = CorporaDB.getInstance();
			BufferedReader tmxReader = new BufferedReader(
					new FileReader(
							new File(inputFile)));

			String line;
			ArrayList<String> l1Sentences = new ArrayList<String>();
			ArrayList<String> l2Sentences = new ArrayList<String>();
			while ((line = tmxReader.readLine()) != null) {
				if ((!(line.contains("srclang")))
						&& (!(line.contains("adminlang")))) {
					if (line.contains("\"" + l1 + "\"")) {
						line = line
								.replaceAll("<tuv xml:lang=\"" + l1 + "\">", "")
								.replaceAll("</seg>", "")
								.replaceAll("<seg>", "")
								.replaceAll("</tuv>", "");
						l1Sentences.add(line);
					} else if (line.contains("\"" + l2 + "\"")) {
						line = line
								.replaceAll("<tuv xml:lang=\"" + l2 + "\">", "")
								.replaceAll("</seg>", "")
								.replaceAll("<seg>", "")
								.replaceAll("</tuv>", "");
						l2Sentences.add(line);
					}

					if (l1Sentences.size() == DUMP_SIZE
							&& l2Sentences.size() == DUMP_SIZE) {
						for (int i = 0; i < DUMP_SIZE; i++) {
							SentenceGroup g = new SentenceGroup();
							g.addSentence(l1, l1Sentences.get(i));
							g.addSentence(l2, l2Sentences.get(i));
							db.saveSentenceGroup(g);
						}
						l1Sentences = new ArrayList<String>();
						l2Sentences = new ArrayList<String>();

					}
				}

			}
			tmxReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}