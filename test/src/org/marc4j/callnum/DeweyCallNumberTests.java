package org.marc4j.callnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

/**
 * Exercise the {@code DeweyCallNumber} class from the command line.
 * Illustrates parsing a shelf key construction.
 * Useful for debugging during development.
 *
 * <p>From the root of the solrmarc distribution:
 * <p>{@code java -cp lib/solrmarc/build org.solrmarc.callnum.ExerciseDeweyCallNumber}
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class DeweyCallNumberTests
{
    /**
     * array of call numbers for use as test data.
     */
    ArrayList<String> validCallNums;
    ArrayList<String> invalidCallNums;

    @Before
    public void setup()
    {
        initCallNums();
    }

    @Test
    public void exercisePatterns()
    {
        for (String callnum : validCallNums)
        {
            Matcher m = DeweyCallNumber.classPattern.matcher(callnum);
            assertTrue(m.matches());
        }
        for (String callnum : invalidCallNums)
        {
            Matcher m = DeweyCallNumber.classPattern.matcher(callnum);
            assertTrue(!m.matches());
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void exerciseClass()
    {
        for (String callnum : validCallNums)
        {
            DeweyCallNumber call = new DeweyCallNumber(callnum);
            String classification = call.getClassification();
            String classDigits = call.getClassDigits();
            String classDecimal = call.getClassDecimal();
            String cutter = call.getCutter();
            String cutterSuffix = call.getSuffix();
            String shelfKey = call.getShelfKey();
        }
    }

    @Test
    public void exerciseShelfKey()
    {
        String prevShelfKey = "";
        for (String callnum : validCallNums)
        {
            DeweyCallNumber call = new DeweyCallNumber(callnum);
            String shelfKey = call.getShelfKey();
            // System.out.println("shelf key: " + shelfKey);
            if (shelfKey.compareTo(prevShelfKey) < 0)
                System.out.println("shelfKey order problem: "+ prevShelfKey + " - > - " + shelfKey);
            assertTrue(shelfKey.compareTo(prevShelfKey) >= 0);
            prevShelfKey = shelfKey;
        }
        prevShelfKey = "";
        for (String callnum : invalidCallNums)
        {
            DeweyCallNumber call = new DeweyCallNumber(callnum);
            String shelfKey = call.getShelfKey();
            if (shelfKey.compareTo(prevShelfKey) < 0)
                System.out.println("shelfKey order problem: "+ prevShelfKey + " - > - " + shelfKey);
            assertTrue(shelfKey.compareTo(prevShelfKey) >= 0);
            prevShelfKey = shelfKey;
        }
    }

    @Test
    public void exerciseShelfKeyNormalization()
    {
        ShelfKeyCase[] testData = {
            // https://github.com/lehigh-university-libraries/marc4j/pull/2
            new ShelfKeyCase("791.43 F4871 1983",       "3791.43 F4871 41983",       "cutter number > 3 digits"),

            // https://github.com/lehigh-university-libraries/marc4j/pull/6
            new ShelfKeyCase("828.4 B8693W:B",          "3828.4 B8693W:B",           "cutter extra with a colon"),

            // https://github.com/lehigh-university-libraries/marc4j/pull/8
            new ShelfKeyCase("306.85 F19819 v.1",       "3306.85 F19819 V 11",       "cutter extra with lowercase characters and then more numbers"),

            new ShelfKeyCase("123.45 Q98765 AbC:5:dEF", "3123.45 Q98765 ABC 15 DEF", "cutter with all of the above"),
        };
        for (ShelfKeyCase datum : testData)
        {
            DeweyCallNumber call = new DeweyCallNumber(datum.input);
            assertEquals(datum.label, datum.expected, call.getShelfKey());
        }
    }

    private static class ShelfKeyCase {
        final String label, input, expected;
        ShelfKeyCase(String input, String expected, String label) {
            this.label = label; this.input = input; this.expected = expected;
        }
    }

    private void initCallNums()
    {
        validCallNums = new ArrayList<String>();
        validCallNums.add("1 .I39");                 // one digit no fraction
        validCallNums.add("1.23 .I39");              // one digit fraction
        validCallNums.add("11 .I39");                // two digits no fraction
        validCallNums.add("11.34 .I39");             // two digits fraction
        validCallNums.add("11.34567 .I39");          // two digits fraction
        validCallNums.add("111 .I39");               // no fraction in class
        validCallNums.add("111 I39");                // no fraction no period before cutter
        validCallNums.add("111Q39");                 // no fraction, no period or space before cutter
        validCallNums.add("111.12 .I39");            // fraction in class, space period
        validCallNums.add("111.123 I39");            // space but no period before cutter
        validCallNums.add("111.134Q39");             // no period or space before cutter
        validCallNums.add("222 .K123");              // cutter with different digit lengths
        validCallNums.add("222 .K45");               // cutter with different digit lengths
        validCallNums.add("222 .K7");                // cutter with different digit lengths
        validCallNums.add("322.44 .F816 V.1 1974");  // cutterSuffix - volume and year
        validCallNums.add("322.45 .R513 1957");      // cutterSuffix year
        validCallNums.add("323 .A512RE NO.23-28");   // cutterSuffix no.
        validCallNums.add("323 .A778 ED.2");         // cutterSuffix ed
        validCallNums.add("323.09 .K43 V.1");        // cutterSuffix volume
        validCallNums.add("324.54 .I39 F");          // letter with space
        validCallNums.add("324.548 .C425R");         // letter without space
        validCallNums.add("324.6 .A75CUA");          // letters without space
        validCallNums.add("792.0944 T374 v.1");      // long cutter
        validCallNums.add("792.0944 T374 v.2");      // long cutter
        validCallNums.add("792.0944 T3741");         // long cutter
        validCallNums.add("800 .A123KaBC");             // case-insensitive cutter letter suffixes
        validCallNums.add("800 .A123m");             // case-insensitive cutter letter suffixes
        validCallNums.add("818.5 .H488zb2");         // another digit at the end of the cutter
        validCallNums.add("828.5 .J89u:h");          // a colon in the cutter -- no test captures this though


        invalidCallNums = new ArrayList<String>();
        invalidCallNums.add("");
        invalidCallNums.add("MC1 259");
        invalidCallNums.add("T1 105");
    }
}
