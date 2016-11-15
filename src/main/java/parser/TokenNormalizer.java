package parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class TokenNormalizer {


    private static Map<Integer, String> normalizedText = ImmutableMap.<Integer, String>builder().
            put(NameParserConstants.AND, "and").
            put(NameParserConstants.HON, "Honourable").
            put(NameParserConstants.MR, "Mr.").
            put(NameParserConstants.MRS, "Mrs.").
            put(NameParserConstants.MISS, "Miss").
            put(NameParserConstants.MADAM, "Madam").
            put(NameParserConstants.SIR, "sir").
            put(NameParserConstants.INC, "Inc.").
            put(NameParserConstants.LTD, "Ltd.").
            put(NameParserConstants.PROF, "Professor").
            put(NameParserConstants.DOCTOR, "Dr.").
            put(NameParserConstants.ESTATE_OF, "Estate of").
            put(NameParserConstants.CO, "Co.").
            put(NameParserConstants.SECOND, "II").
            put(NameParserConstants.THIRD, "III").
            put(NameParserConstants.FOURTH, "IV").
            put(NameParserConstants.LLB, "L.L.B.").
            put(NameParserConstants.ESQ, "Esquire").
            put(NameParserConstants.PHD, "P.H.D").
            put(NameParserConstants.BSC, "B.Sc").
            put(NameParserConstants.MD, "M.D.").
            put(NameParserConstants.DDS, "DDS").
            put(NameParserConstants.SENIOR, "Sr.").
            put(NameParserConstants.JUNIOR, "Jr.").
            put(NameParserConstants.QUOTED_NICK_NAME, "'%s'").
            put(NameParserConstants.SINGLE_QUOTED_NICK_NAME, "'%s'").
            put(NameParserConstants.PAREN_NICK_NAME, "'%s'").
            build();


    public TokenNormalizer() {
    }


    public String tokenText(int kind) {
        Preconditions.checkState(kind>=0 && kind<NameParserConstants.tokenImage.length);
        return NameParserConstants.tokenImage[kind];
    }

    public String normalize(String value, int kind, Name.TokenType type) {
        String result = normalizedText.get(kind);
        if (result==null) {
            return value;
        }
        if (result.contains("%s")) {
            return String.format(result, value);
        }

        // TODO
        // if one letter optionally followed by period mark as initial so it
        //  can be compared appropriately.
        return result;
    }
}
