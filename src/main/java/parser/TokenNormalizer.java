package parser;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public class TokenNormalizer {

    private static Map<Integer, String> normalizedText = ImmutableMap.<Integer, String>builder().
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


    public Name normalize(Name name) {
        normalize(name.getFirstTokens());
        normalize(name.getLastTokens());
        normalize(name.getMiddleTokens());
        normalize(name.getTitleTokens());
        normalize(name.getSalutationTokens());
        normalize(name.getRelationTokens());
        return name;
    }


    private void normalize(List<NameToken> tokens) {
        for (NameToken token:tokens) {
            normalize(token);
        }
    }


    public String normalize(NameToken token) {
        String result = normalizedText.get(token.kind);
        if (result==null) {
            return token.value;
        }
        //arbitrarily decide that initials always have dot.
        // George W Bush  -->  George W. Bush
        if (token.isInitial && token.value.length()==1) {
            token.value = token.value+".";
        }
        if (result.contains("%s")) {
            return String.format(result, token.value);
        }

        return result;
    }
}
