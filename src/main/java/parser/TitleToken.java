package parser;

public class TitleToken extends Token {
    TitleToken(String value, int kind) {
        super(value, kind, NameTokenType.TITLE);
    }
}
