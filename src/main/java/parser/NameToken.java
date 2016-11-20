package parser;

import parser.jj.NameParserConstants;

public class NameToken extends Token {

    Character initial;

    NameToken(String value) {
        super(value, NameParserConstants.NAME_WITH_NUMBERS, NameTokenType.NAME);

        this.initial = maybeGetInitial(value);

    }
    private Character maybeGetInitial(String value) {
        return ((value.length()==2 && value.endsWith(".")) || value.length() ==1) ?
                    value.charAt(0) : null;

    }

    public boolean isInitial() {
        return initial!=null;
    }


}
