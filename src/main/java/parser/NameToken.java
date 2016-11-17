package parser;

import com.google.common.base.Preconditions;

public class NameToken  {

    int kind;
    String value;    // e.g. "Mr."    the value the user originally input.
    String tokenText;     // e.g. <MR>     the token image string.   generated by javacc.
    String normalizedText; // e.g. Mister      a user/db friendly standardized tring for value.
    boolean isInitial;   // e.g. "W."   or "W" are considered initials.
    NameTokenType type;


    NameToken(String value, int kind, NameTokenType type) {
        Preconditions.checkArgument(value != null);
        this.value = value;
        this.tokenText = NameParserConstants.tokenImage[kind];
        this.normalizedText = value;  // by default.
        this.type = type;
        isInitial = value.length()==2 && value.charAt(1)=='.';
    }

    @Override
    public String toString() {
        return normalizedText;
    }

    public NameMatch compareTo(NameToken other) {
        if (kind!=other.kind) {
            return NameMatch.NONE;
        }
        if (isInitial || other.isInitial) {
            if (normalizedText.substring(0,1).equalsIgnoreCase(other.normalizedText.substring(0,1)) ) {
                return NameMatch.PROBABLE;
            }
        }
        return normalizedText.equalsIgnoreCase(other.normalizedText) ?
                NameMatch.DEFINITE :
                NameMatch.NONE;
    }

}