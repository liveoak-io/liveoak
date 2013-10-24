package org.projectodd.restafari.container;

import org.junit.Assert;
import org.junit.Test;
import org.projectodd.restafari.spi.ReturnFields;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ReturnFieldsTest {

    @Test
    public void testBasic() {
        String spec = "field1,field2,field3";
        ReturnFieldsImpl fspec = new ReturnFieldsImpl(spec);

        StringBuilder val = new StringBuilder();
        for (String field: fspec) {
            if (val.length() > 0)
                val.append(',');
            val.append(field);
        }
        Assert.assertEquals(spec, val.toString());

        // check catching errors

        String [] specs = {
            "",
            null,
            ",",
            "field1,",
            ",field2"
        };

        for (String filter: specs) {
            try {
                fspec = new ReturnFieldsImpl(filter);
                Assert.fail("Parsing of fields spec should have failed! : " + filter);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }


    @Test
    public void testNested() {
        String spec = "field1,field2(sub1,sub2(subsub1)),field3";
        ReturnFieldsImpl fspec = new ReturnFieldsImpl(spec);

        String val = traverse(fspec);
        Assert.assertEquals(spec, val.toString());


        // check catching errors

        String [] specs = {
            "(",
            ")",
            "field1,(",
            "field1,)",
            "field1,field2(",
            "field1,field2)",
            "field1,field2()",
            "field1,field2(sub1)(",
            "field1,field2(sub1))",
            "field1,field2(sub1),"
        };

        for (String filter: specs) {
            try {
                fspec = new ReturnFieldsImpl(filter);
                Assert.fail("Parsing of fields spec should have failed! : " + filter);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    private String traverse(ReturnFields fspec) {
        StringBuilder buf = new StringBuilder();
        for (String field: fspec) {
            if (buf.length() > 0)
                buf.append(',');
            buf.append(field);

            ReturnFields cspec = fspec.child(field);
            if (cspec != null) {
                buf.append('(');
                buf.append(traverse(cspec));
                buf.append(')');
            }
        }
        return buf.toString();
    }
}
