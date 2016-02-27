/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import xmlcompare.impl.XmlComparator;
import xmlcompare.impl.XmlStat;
import xmlcompare.utils.Commons;

/**
 *
 * @author jvanek
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private static final Map<String, Entry<String, String>> commandLineArgs = new HashMap();

    public static void main(String[] args) throws FileNotFoundException, IOException, XMLStreamException {
        if (args.length < 1) {
            System.out.println("-ignoreContent -failFast -silent file1 file2 ");
            System.out.println("at least one arguments - one valid filename  expected");
            System.out.println("  if two wilenames are  entered then they are compared. iF one valid filename entered, then it is statisted");
            System.out.println("-ignoreContent - optional. Will disable chcking of text values of elements (for case docment have been transalted)");
            System.out.println("-failFast - optional. terminate after first error");
            System.out.println("-silent - optional will write out just final message");
            System.out.println("-ignoreOrder=0 or 1 or 2 or 3 - optional will default is 0 - order is important (O=n). 1-childrens are sort before comapring.(warnig O=n log n) 2-engien is trying to match each node really perfectly (warnig O=n^2 !!)");
            System.out.println("-visualise  result willbe visualsed. Working best with ^2^ (because its the only one who cares about content of children)");
            System.out.println("-validate=file_name - optional validat eimputs against given dtd/xsd/rng  schmeas. Some aditional libraries may be needed for thisto work.");
            System.out.println("*****xsd should be supported by java platform, to allow other you can need to have eg:");
            System.out.println("*****dom4j.jar, xalan.jar, PullParser.jar, relaxng.jar, msv.jar, isorelax.jar,xsdlib.jar, crimson.jar on your classapth");
            System.out.println("-levenstain=FLOAT.NUMBER content of nodes si considered to be same even when is just simmilar according to levensthains distance (impl by appache). Number is 0-1 where 0 is for completely same, and 1 for completely different stings so your level is maximal  possible difference (dist<FLOAT.NUMBER)");
            System.out.println("-forceTrim all trailing sapces are forceibly removed even out of specification borders ");
            System.out.println("-ignoreNameCase nodes are compared case insensitive");
            System.out.println("-ignoreValueCase nodes are compared case insensitive");
            System.out.println("-removeSpaces all white chars from node contens are removed");
            System.out.println("-ignoreUri - nodes uri is ignored");
            System.out.println("-ignoreHeaders - headers are ignored");
            System.out.println("-dual - will compare f1 x f2 and also f2 x f1");
            System.out.println("-orphanSeek - when brute force is on (ignoireorder=2) then orphaned elements are looking for match by levenstain distance)");
            System.out.println("-version - prints out program version");
            System.out.println("-diff=filename into this file will be written compressed diff betwenn this two files (best with ignorecontent=2 and dual and orphanseek");
            System.out.println("");
            System.out.println("Program by Jiri Vanek, judovana@email.cz");
            System.exit(-1);
        }
        for (String arg : args) {
            if (arg.replaceAll("-", "").equalsIgnoreCase("version")) {
                System.out.println("XmlComparator version " + XmlComparator.VERSION);
                System.exit(0);
            }
            Entry<String, Entry<String, String>> e = Commons.paarseCommandLineArgWithOrig(arg);
            commandLineArgs.put(e.getKey(), e.getValue());
        }

        XmlComparator xmlComparator = new XmlComparator();

        Commons.proceedArgs(xmlComparator, commandLineArgs);
        if (xmlComparator.getF1() == null || xmlComparator.getF2() == null) {

            XmlStat xmlStat = new XmlStat();
            Commons.proceedArgs(xmlStat, commandLineArgs);
            xmlStat.stat();
        } else {
            xmlComparator.sumarize();
            xmlComparator.compare();
        }

    }

}
