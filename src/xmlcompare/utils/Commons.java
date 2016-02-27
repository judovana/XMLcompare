/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import xmlcompare.impl.XmlComparator;

/**
 *
 * @author jvanek
 */
public class Commons {

    private static final int LOGLEVEL = 1;

    public static Entry<String, Entry<String, String>> paarseCommandLineArgWithOrig(String s) {
        Entry<String, String> qq = paarseCommandLineArg(s);
        return new EntryImpl2(qq.getKey(), new EntryImpl(s, qq.getValue()));
    }

    public static Entry<String, String> paarseCommandLineArg(String s) {

        s = s.replaceAll("^-*", "");
        String[] ss = s.split(" *= *");
        String key = ss[0].toLowerCase();
        String value = null;
        if (ss.length > 1) {
            value = s.substring(key.length() + 1);
            value = value.replaceAll("^ *= *", "");
        }
        return new EntryImpl(key, value);
    }

    public static void log(String string, int level) {
        if (level == 0) {
            System.err.println(string);
        } else if (level == 1) {
            System.out.println(string);
        }
    }

    public static String spacing(int chNumber) {
        String r = String.valueOf(chNumber);
        while (r.length() < 5) {
            r = "0" + r;
        }

        return r;
    }

    private static class EntryImpl implements Entry<String, String> {

        private final String key;
        private String value;

        private EntryImpl(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String setValue(String v) {
            String q = value;
            value = v;
            return q;
        }
    }

    private static class EntryImpl2 implements Entry<String, Entry<String, String>> {

        private final String key;
        private Entry<String, String> value;

        private EntryImpl2(String key, Entry<String, String> value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Entry<String, String> getValue() {
            return value;
        }

        public Entry<String, String> setValue(Entry<String, String> v) {
            Entry<String, String> q = value;
            value = v;
            return q;
        }
    }

    public static void proceedArgs(XmlComparator splitter, Map<String, Entry<String, String>> params) throws FileNotFoundException {
        Set<Entry<String, Entry<String, String>>> cmds = params.entrySet();
        for (Map.Entry<String, Entry<String, String>> entry : cmds) {
            proceedArg(splitter, entry);
        }
    }

    public static void proceedArg(XmlComparator splitter, Entry<String, Entry<String, String>> entry) throws FileNotFoundException {

        String key = entry.getKey();
        String value = entry.getValue().getValue();
        if (key.equals("diff")) {
            if (value == null) {
                Commons.log("diff withhout specified output file - ignored", LOGLEVEL);

            } else {
                splitter.setDiff(new File(value));
            }
        } else if (key.equals("visualise")) {
            if (value != null) {
                Commons.log("visualise with arg. ignored", LOGLEVEL);

            }
            splitter.setVisualise(true);
        } else if (key.equals("orphanseek")) {
            if (value != null) {
                Commons.log("orphanSeek with arg. ignored", LOGLEVEL);

            }
            splitter.setOrphanSeek(true);
        } else if (key.equals("dual")) {
            if (value != null) {
                Commons.log("dual with arg. ignored", LOGLEVEL);

            }
            splitter.setDual(true);
        } else if (key.equals("ignoreheaders")) {
            if (value != null) {
                Commons.log("ignoreHeaders with arg. ignored", LOGLEVEL);

            }
            splitter.setIgnoreHeaders(true);
        } else if (key.equals("levenstain")) {
            if (value == null) {
                Commons.log("levenstain without arg. ignored", LOGLEVEL);

            } else {
                splitter.setLevenstain(new Double(value));
            }
        } else if (key.equals("ignoreuri")) {
            if (value != null) {
                Commons.log("ignoreUri with arg. ignored", LOGLEVEL);

            }
            splitter.setIgnoreUri(true);
        } else if (key.equals("removespaces")) {
            if (value != null) {
                Commons.log("removeSpaces with arg. ignored", LOGLEVEL);

            }
            splitter.setRemoveSpaces(true);
        } else if (key.equals("ignorevaluecase")) {
            if (value != null) {
                Commons.log("ignoreValueCase with arg. ignored", LOGLEVEL);

            }
            splitter.setIgnoreValueCase(true);
        } else if (key.equals("ignorenamecase")) {
            if (value != null) {
                Commons.log("ignoreNameCase with arg. ignored", LOGLEVEL);

            }
            splitter.setIgnoreNameCase(true);
        } else if (key.equals("forcetrim")) {
            if (value != null) {
                Commons.log("forceTrim with arg. ignored", LOGLEVEL);

            }
            splitter.setForceTrim(true);
        } else if (key.equals("ignorecontent")) {
            if (value != null) {
                Commons.log("ignoreContent with arg. ignored", LOGLEVEL);

            }
            splitter.setIgnoreContent(true);
        } else if (key.equals("ignoreorder")) {
            if (value == null) {
                Commons.log("ignoreOrder withput arg. ignored", LOGLEVEL);

            } else {
                splitter.setIgnoreOrder(new Integer(value));
            }
        } else if (key.equals("validate")) {
            if (value == null) {
                Commons.log("validate without arg. ignored", LOGLEVEL);

            } else {
                splitter.setSchmea(new File(value));
            }
        } else if (key.equals("failfast")) {
            if (value != null) {
                Commons.log("failFast with arg. ignored", LOGLEVEL);

            }
            splitter.setFailFast(true);
        } else if (key.equals("silent")) {
            if (value != null) {
                Commons.log("silent with arg. ignored", LOGLEVEL);

            }
            splitter.setSilent(true);
        } else if (key.equals("config") || key.equals("l")) {/*procesed before everything*/

        } else {
            String candidate = entry.getValue().getKey();
            Commons.log("checking " + candidate, LOGLEVEL);
            File f = new File(candidate);
            splitter.addInput(f);
        }
    }
}
