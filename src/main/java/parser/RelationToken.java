package parser;

public class RelationToken extends Token {
    RelationToken(String value, int kind) {
        super(value, kind, NameTokenType.RELATION);
    }
}
