package parser;

public class RelationToken extends Token {
    RelationToken() {
        super(TokenType.RELATION);
    }
    RelationToken(String value, int kind) {
        super(value, kind, TokenType.RELATION);
    }
}
