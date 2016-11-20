package parser;

public class SalutationToken extends Token {
    SalutationToken(String value, int kind) {
        super(value, kind, NameTokenType.SALUTATION);
    }
}
