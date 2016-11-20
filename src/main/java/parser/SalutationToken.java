package parser;

public class SalutationToken extends Token {

    SalutationToken() {
        super(TokenType.SALUTATION);
    }
    SalutationToken(String value, int kind) {
        super(value, kind, TokenType.SALUTATION);
    }
}
