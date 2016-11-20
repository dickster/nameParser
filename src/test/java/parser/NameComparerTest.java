package parser;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import parser.jj.NameParser;

import java.util.Map;

public class NameComparerTest {

    private NameParser parser = new NameParser();
    private Map<String,String> comparisons = ImmutableMap.<String, String>builder().
        put("Jackson Jive",
                "Jackson Jive").
        put("Mr. Donald Duck phd",
                "Mr. Donald Duck the third, Phd").
        put("Billy Smith",
                "Frank Smith").
        put("Henry Moore",
                "henry Less").
        put("Mr. K. daddy jones",
                "K Jones III").
        put("John Doe",
                "Mr. John Doe").
        put("Mrs John Doe",
                "Mr. John Doe").
        put("Mrs Sally johnson Phd",
                "Sally JohNSON ").
        put("Mrs Sally johnson DDS,Phd",
                "Sally JohNSON Phd,DDS").
        put("Peter Parker LLB,DDS",
                "Peter Parker DDS").
        put("Ms Sue Parker",
                "Mrs Sue Parker").
        put("Mr John Doe",
                "Mister John Doe").
        put("Hon tobias funKE",
                "Prof. tobias funke").
        put("Sam Badong",
                "S. Badong").
        put("Kathy Klink Bsc, Md",
                "Kathy Q Klink Md, Phd").
        put("Derek W. Dick",
                "Derek w dick").
        put("George Bush",
                "George Bush II").
        put("Quentin P Smooch",
                "Quentin Smooch").
        put("Flanders Johnson",         // TODO  : give this more match. POSSIBLE>NONE
                "Flanders Johnson-Jones").
        put("Hank Ulwag the second",
                "Hank Ulwag II").     // TODO : add alias.  Bob Robert, Betty Elizabeth etc...
        put("Q Smooch esquire",
            "Quentin Smooch the third").
        build();

    @Test
    public void testNameComparison() {
        NameResult a;
        NameResult b;

        for (String left:comparisons.keySet()) {
            String right = comparisons.get(left);
            a = parser.parseNames(left);
            b = parser.parseNames(right);
            NameMatch compare = new NameComparer().compare(a.get(0), b.get(0));
           System.out.println(left + " ?= " + right + " == " + compare);
            System.out.println();
        }


    }

}
