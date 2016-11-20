package parser;

import com.google.common.collect.ImmutableMap;
import parser.jj.NameParserConstants;

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
        normalize(name.getMiddleToken());
        normalize(name.getTitleTokens());
        normalize(name.getSalutationToken());
        normalize(name.getRelationToken());
        return name;
    }


    private <T extends Token> void normalize(List<T> tokens) {
        for (Token token:tokens) {
            normalize(token);
        }
    }


    public void normalize(Token token) {
        String result = normalizedText.get(token.kind);
        if (result==null) {
            result = token.value;
        }
        if (result.contains("%s")) {
            result =  String.format(result, token.value);
        }
        if (token instanceof NameToken) {
            NameToken nt = (NameToken)token;
            //arbitrarily decide that initials always end in dot.
            // George W Bush  -->  George W. Bush
            if (nt.isInitial()) {
                result = nt.initial+".";
            }
        }
        token.normalizedText =  result;
    }
}
