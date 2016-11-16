package parser;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class NameParserTest  {

    private NameParser parser;

    List<String> names = Lists.newArrayList(
            "Estate of Jw Bling-Wing, esquire",
            "xyz foo ltd",
            "xyz foo ltd. co.",
//            "John's Bits & Bobs inc",    this is ambiguous?
            "Penni's Liquor inc.",
            "xyz foo corp",
            "xyz foo corp.",
            "xyz foo co",
            "12323 Co",
            "234 Corp",
            "ZZZ Co.",
            "AAA Corp.",
            "Estate of Jw asdf fdsa",
            "John Doe",
            "John I. Doe",
            "Erica and Shelly Holdings",
            "    John & Jane Doe",
            "    John W. Doe and Jane Doe",
            "    John Doe and Doe, Jane E.",
            "           Doe, John and Jane",
            "            Doe, John; Doe, Jane",
            "   Mr. and Mrs. John Doe",       //huh
            "    Hon. Albert James Van Heusen, BBA",
            "    Dr. Bobbi-Jo Mary Thomas-O'Brien, MD",
            "    Acres, Jim"  ,
            "Mr. John William Doe Jr., B.Sc",
            "Mister John William Doe Jr. BSc, PHD",
            "Misses John William Doe Jr., BSc, DDS",
            "Peter O Malley",    // TODO : need to set ambiguity flag for this case.
            "Peter O Malley,  B.Sc Phd",
            "Sir O Sis, LLB",
            "Hon Bob Baker",
            "Smith, John Bob Jr.",
            "judge peter O. Malley",  // not sure how to treat "O".
            "Peter O'Malley",
            "Peter O' Malley",
            "Ms. celia Cruz",
            "Ms. celia de Cruz",
            "celia de la Cruz",
            "Ms. celia de la Cruz",
            "A. J. schmitt",
            "A.J. schmitt",
            "DJ erving",
            "Miss QT Pie",
            "Alfred J. schmitt",
            "harry Q plinkin-tomsom-blanche",
            "anna maria sanchez vicario",
            "anna 'maria' sanchez vicario",
            "anna maria (sanchez) vicario",
            "Mr. jekyll",
            "tom & jerry ice-cream",
            "osama bin laden",
            "osama van der hosen",
            "al la bama",
            "ricky ste puerto",
            "alessia di cecco",
            "mrs. marsha san sicily",
            "ouilette l' asence",
            "ouilette l'asence",
            "misses stephanie van den burgh",
            "misses clarke-smackers",
            "atty joh smith",
            "doctor livingstone",
            "bono",
            "the edge",
            "prince jr.",
            "billy-bob joey tom william cletus van truck",
            "sir paul mc cartney III esq.",
            "sir richard pump-a-loaf jr.",
            "ms joan elizabeth bar deef",
            "ms kelly st apler",
            "misses pat 'the knife' st. denis",
            "Dr. Joe & Jill saINt hubert",
            "Dr. Jill saINt",
            "Dr. Joe & Jill saINt",
            "ms t'ariff blah blah",
            // prefixes......
            "Prof quentin lawrence and Dr mary-ann ginger 3rd",
            "Prof quentin and Dr mary-ann ginger 3rd ph.d",
            "tex 'n edna curio ",
            "bono + the edge",
            "larry the edge",
            "derek 'willy' dick",
            "Smith, Mr. John",
            "Francis 'johnson' Q  Doe",
            "Doe, Francis 'johnson' Q ",
            "Doe, Francis Q 'smiley'  ",
            "mr and mrs john smith",
            "John & sally struthers",
            "Mr 'n Mrs Spock 2nd",
            "Mr 'n Mrs Spock the 2nd",
            "Smith, Mr. John",
            "Doe, Mr. Francis Q (pacman)",
            "Dr. john \"the madman\" de bowery",
            "elvis 'the pelvis' presley",
            "enis \"elvis's brother\" presley",
            "prof jim (lefty) gauche",
            "mr. klaus van hoffe",
            "mrs olga van der schmitt",
            "mrs sean o'seamus I",
            "mrs sean o seamus ph.d",
            "mrs sean o' seamus esq. phd",
//            "mr. dr. george w. bush 2nd esquire",
////            // TODO : what is expectation of this one??
            "Léa Hélène Seydoux-Fornier de Clausonne",
            "derek dick",
            "prof. derek dick-jones-baxter IV",
            "Adam Richard Sandler",
            "Sandra Bullock",
            "Peter Parker",
            "Miss.  Rachel Hannah Weisz",
            "Miles Teller Sr.",
            "Mrs. Sandra Bullock",
            "George Timonthy Clooney JR.",
            "Zhang Zi-Yi",
            "Asia Aria Maria Vittoria Rossa Argento",
            "Ánna Blah",
            "Raven-Symoné Christina Pearman",
            "Raven-Symoneé Christina Pearman",
            "Maureen O'Hara"     ,

            "John Doe",
            "John W. Doe",
            "John Doe III ",
            "John W. Doe The 3rd ",
            "John William Doe Jr. ",
            "John William Doe Junior ",
            "Doe, John  ",
            "Doe, John W. ",
            "Doe, John W. The 3rd ",
            "Doe, John William Jr.",
            "Doe, John William Junior ",
            "Mr. John Doe ",
            "Mr. John W. Doe",
            "Mr. John William Doe ",
            "John & Jane Doe     ",
            "John W. Doe and Jane Doe ",
            "Mr. and Mrs. John Doe",
            "Estate of John Doe  ",
            "CloudNine & John W. Doe ",
            "Cloud9 & John W. Doe ",
            "John Doe and John's Welding-Shop  ",
            "John Doe and Jane Doe & XYZ Ltd.   ",
            "12345 NB Inc. & ABC Corp. & XYZ Ltd. "  // allow for digits in company names only.
    );



    List<String> names3 = Lists.newArrayList(
            " Dr. Li Mi Xi, MD",
            "    Dr. Bobbi-Jo Mary Thomas-O'Brien, MD"
    );

    List<String> names2 = Lists.newArrayList(
            "Estate of Jw asdf fdsa",
            "xyz foo ltd",
            "xyz foo ltd. co.",
            "xyz foo inc",
            "xyz foo inc.",
            "xyz foo corp",
            "xyz foo corp.",
            "xyz foo co",
            "12323 Co",
            "234 Corp",
            "ZZZ Co.",
            "AAA Corp.",
            "Estate of Jw asdf fdsa",
            "John Doe",
            "John W. Doe",
            "John Doe III ",
            "John W. Doe The 3rd ",
            "John William Doe Jr. ",
            "John William Doe Junior ",
            "Doe, John  ",
            "Doe, John W. ",
            "Doe, John W. The 3rd ",
            "Doe, John William Jr.",
            "Doe, John William Junior ",
            "Mr. John Doe ",
            "Mr. John W. Doe",
            "Mr. John William Doe ",
            "John & Jane Doe",
            "John W. Doe and Jane Doe ",
            "Mr. and Mrs. John Doe" ,
            "Estate of John Doe  ",
            "CloudNine & John W. Doe ",
            "Cloud9 & John W. Doe ",
            "John Doe and John's Welding-Shop  ",
            "John Doe and Jane Doe & XYZ Ltd.   ",
            "12345 NB Inc. & ABC Corp. & XYZ Ltd. ",
            "Mr. and Mrs. John Doe"
    );

    // companies = <Estate of>, * <INC|LTD|CO|CORP|TM.etc...>
    // company names can have digits.



    @Before
    public void setup() {
        parser = new NameParser();
    }
//
//    @Test
//    public void testParseName() throws Exception {
//        NameResult result;
//        for (String name:names) {
//            result = parser.parseName(name);
//            System.out.println(result);
//            System.out.println("");
//        }
//    }

    @Test
    public void testParseNames() throws Exception {
        NameResult result;
        for (String name:names) {
            result = parser.parseNames(name);
            System.out.println(result);
            System.out.println("");
        }
    }
}
