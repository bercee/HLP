package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * A temporary test class for testing the outputs of the different classes.
 * @author Berci
 *
 */
public class TestClass {

	public static void main(String[] args) {

		try{
			long l = System.currentTimeMillis();
			System.err.println("Reading object...");
			File f = new File("C:\\IRUN\\words_all.ws");
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			WordCollection wc = (WordCollection) ois.readObject();
			ois.close();
			System.err.println("Done: "+(System.currentTimeMillis()-l)+" ms.");
			
			File full = new File("C:\\IRUN\\words_all_fullanal.txt");
			System.err.println("Printing anal to file...");
			PrintWriter pw = new PrintWriter(full);
			List<String> list = new WCManipulator(wc).getFullInfoDisordered();
			list.forEach(a -> {pw.println(a); pw.flush();});
			pw.flush();
			pw.close();
			System.err.println("Done. :)");

			System.exit(0);
		}catch (Exception e){
			e.printStackTrace();
		}
		File[] files = new File[9];
		files[0] = new File("C:\\IRUN\\words_1-100000.ws");
		files[1] = new File("C:\\IRUN\\words_100001-200000.ws");
		files[2] = new File("C:\\IRUN\\words_200001-300000.ws");
		files[3] = new File("C:\\IRUN\\words_300001-400000.ws");
		files[4] = new File("C:\\IRUN\\words_400001-500000.ws");
		files[5] = new File("C:\\IRUN\\words_500001-600000.ws");
		files[6] = new File("C:\\IRUN\\words_600001-700000.ws");
		files[7] = new File("C:\\IRUN\\words_700001-800000.ws");
		files[8] = new File("C:\\IRUN\\words_800001-.ws");

		WordCollection wc = new WordCollection();
		WCManipulator wcm = new WCManipulator(wc);
		for (File f : files) {
			wcm.add(readWC(f));
		}

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("C:\\IRUN\\words_all.ws"));
			oos.writeObject(wcm.getWC());
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<String> list = new WCManipulator(wc).getBasicInfo();
		printList(list);



	}

	public static void printList(List<?> list) {
		for (Object o : list) {
			System.out.println(o);
		}
	}

	public static WordCollection readWC(File f) {
		WordCollection wc = null;
		ObjectInputStream ois = null;
		try {
			System.err.println("Reading object... " + f.getName());
			long l = System.currentTimeMillis();
			ois = new ObjectInputStream(new FileInputStream(f));
			wc = (WordCollection) ois.readObject();
			System.err.println("Object read in " + (System.currentTimeMillis() - l) + " ms.");

			return wc;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (Exception e) {

				}
			}
		}

		return null;

	}

}
