package parser;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

public class Name implements Serializable {

    private static Pattern hasNumberPattern = Pattern.compile("([0-9])");

    // TODO : convert strings to tokens.  (kind, string, typeEnum:isPrefix|isCompany|isPerson???)
    // add normalization that will deduce if it's a company.   John's   (a non'last name has apostrophe S?)
    //  if any names have a number and personalTitles, suffixes, relations exist then log ambiguity
    //   Eg. 7-Up Jr,     Mr 8-Ball

    List<Token> salutation = Lists.newArrayList();
    List<Token> first = Lists.newArrayList();
    List<Token> middle = Lists.newArrayList();
    List<Token> last = Lists.newArrayList();
    List<Token> relations = Lists.newArrayList();  //junior, senior, III etc...
    List<Token> titles = Lists.newArrayList();     //PHd MD B.Sc etc...
    List<Token> names = Lists.newArrayList();
    List<Token> nickNames = Lists.newArrayList();
    List<Token> inc = Lists.newArrayList();
    boolean inverse = false;
    boolean isCompany = false;
    boolean prefixAdded = false;

    private final TokenNormalizer normalizer = new TokenNormalizer();
    private String namePrefix;



    public enum TokenType {
        NAME, PREFIX,SALUTATION, PERSONAL_TITLE, COMPANY_TITLE, TITLE, RELATION, NICK_NAME, INC
    }

    public class Token {
        int kind;
        String value;    // e.g. "the second"
        String tokenText;     // e.g. <SECOND>
        String normalizedText; // e.g. II
        boolean isInitial;
        TokenType type;
        Token(String value, int kind, TokenType type) {
            this.value = value;
            this.tokenText = normalizer.tokenText(kind);
            this.normalizedText = normalizer.normalize(value, kind, type);
            this.type = type;
        }

        @Override
        public String toString() {
            return normalizedText;
        }
    }

    public Name(String john, String doe) {
        addFirst(john);
        addLast(doe);
    }

    public Name() {
    }

    private String getRelation() {
        return relations.toString();
    }

    public Name withFirstName(String name) {
        this.first = Lists.newArrayList(newNameToken(name));
        return this;
    }

    public Name withLastName(String name) {
        this.last= Lists.newArrayList(newNameToken(name));
        return this;
    }

    private Token newNameToken(String name) {
        if (hasNumberPattern.matcher(name).find()) {
            isCompany = true;
        }
        return new Token(name, NameParserConstants.NAME_WITH_NUMBERS, TokenType.NAME);
    }

    public Name withMiddleName(String name) {
        this.middle = Lists.newArrayList(newNameToken(name));
        return this;
    }

    private String unquoted(String name) {
        return name.replace("\"", "");
    }

    public String getFirst() {
        return asString(first);
    }

    public String getMiddle() {
        return asString(middle);
    }

    public String getLast() {
        return asString(last);
    }

    public String getTitles() {
        return asString(titles);
    }

    public String getSalutation() {
        return asString(salutation);
    }

    public List<Token>  getFirstTokens() {
        return first;
    }

    public List<Token>  getMiddleTokens() {
        return middle;
    }

    public List<Token>  getLastTokens() {
        return last;
    }

    public List<Token>  getTitleTokens() {
        return titles;
    }

    public List<Token> getSalutationTokens() {
        return salutation;
    }

    private String asString(List<Token> name) {
        String text = Joiner.on(" ").skipNulls().join(name);
        return (name.size()>1) ?
                "\"" + text + "\"" :
                text;
    }

    private boolean isAmbiguous(List<Token> name) {
        return name!=null && name.size()>1;
    }

    public Name addSalutation(String salutation, int kind) {
        this.salutation.add(new Token(salutation, kind, TokenType.SALUTATION));
        return this;
    }

    public Name addFirst(String name) {
        first.add(newNameToken(unquoted(name)));
        return this;
    }

    public Name addMiddle(String name) {
        middle.add(newNameToken(unquoted(name)));
        return this;
    }

    public Name addLast(String name) {
        last.add(newNameToken(unquoted(name)));
        return this;
    }

    public List<String> getRelations() {
        return convertTokenToString(relations);
    }

    private List<String> convertTokenToString(List<Token> tokens) {
       List<String> result = Lists.newArrayList();
       for (Token token:tokens) {
           result.add(token.normalizedText);
       }
       return result;
    }

    public void addRelation(String relation, int kind) {
        this.relations.add(new Token(relation, kind, TokenType.RELATION));
    }

    public Name merge(Name other) {
        // e.g. Mr and Mrs John Smith will yield...
        //      Mr John Smith,  Mrs John Smith.
        // Mr John and Mrs Sue Smith will yield...
        //      Mr John Smith,  Mrs Sue Smith.
        if (first.isEmpty()) {
            first.addAll(other.first);
        }
        if (getLast().isEmpty()) {
            last.addAll(other.last);
        }
        return this;
    }


    public Name asCompany() {
        this.isCompany = true;
        return this;
    }
    public boolean isAmbiguous() {
        // note the definition of ambiguous is itself ambiguous.
        // if you have two last names then it *could* be ambiguous.
        // e.g. john steven dick peterson.   dick could be his last name or middle name.
        // this will most likely be parsed as john/steven/dick peterson when it could be interpreted as
        //  john/steven dick/peterson.

        // also add checks for company and person ambiguity.   Mr. Joe Fresh Inc.
        //   Mrs. 8977  etc...
        return isAmbiguous(first) || isAmbiguous(middle) || isAmbiguous(last);
    }

    public void endParse() {
        if (inverse) {
            Token lastName = names.get(0);
            names.remove(0);
            names.add(lastName);
        }

        if (names.size()==0) {
            return;
        }
        first.add(names.get(0));
        if (names.size()==1) {
            return;
        }
        else if (names.size()==2) {
            last.add(names.get(1));
            return;
        }
        if (names.size()==3) {
            if (isProbablyMiddleName(names.get(1))) {
                middle.add(names.get(1));
                last.addAll(names.subList(2, names.size()));
            } else {
                last.addAll(names.subList(1, names.size()));
            }
        } else {
            System.out.println("THIS NAME IS AMBIGUOUS? assuming a middle name of " + names.get(1));
            middle.add(names.get(1));
            last.addAll(names.subList(2, names.size()));
        }
    }

    public Token addName(String name, int kind) {

        // TODO : add ambiguity check. if name = "O" could be prefix?

        // e.g. if "Van Den" is prefix and name = "Hooegarden" is passed,
        //  then concatenate them into single "Van Den Hooegarden" last name.

        String namePrefix = getNamePrefix();
        if (namePrefix != null) {
            Token token = newNameToken(namePrefix + " " + name);
            names.set(names.size() - 1, token);
            return token;
        } else {
            Token token = newNameToken(name);
            names.add(token);
            return token;
        }

    }

    public boolean isProbablyMiddleName(Token token) {
        // this business logic should be refactored out into a spring bean service.
        // if nameService.isProbablyNotAMiddleName(name)  name.addLast(name); else name.addMiddle(name);
        String name = token.normalizedText;
        Preconditions.checkArgument(StringUtils.isNotBlank(name));
        if (name.length()==1||(name.length()==2&&name.endsWith("."))) {
            return true;
        }
        // lots more to put here from different cultures.
        // TODO :
        List<String> notMiddleNames = Lists.newArrayList("van", "le", "la", "di", "mac", "bin", "binti", "de", "o'", "der", "den", "l'");
        return !notMiddleNames.contains(name.toLowerCase());
    }

    public void addNameButPossiblyPrefix(String prefix, int kind) {
        addName(prefix, NameParserConstants.NAME_WITH_NUMBERS).type=TokenType.PREFIX;
       // System.out.println("prefix : " + kind + " --> " + NameParserConstants.tokenImage[kind]);
    }

    public void addNickName(String name, int kind) {
        this.nickNames.add(newNameToken(name.substring(1, name.length() - 1)));
    }

    public void addTitle(String title, int kind) {
        this.titles.add(new Token(title,kind,TokenType.TITLE));
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }


    @Override
    public String toString() {
        return toDebugString() + "  aka " + toNormalizedString();
    }

    public String toDebugString() {
        StringBuilder builder = new StringBuilder(isCompany() ? "Company" : "");
        if (!salutation.isEmpty()) builder.append("  salutation :" + salutation);
        if (!first.isEmpty()) builder.append("  first :" + first);
        if (!middle.isEmpty()) builder.append("  middle :" + middle);
        if (!nickNames.isEmpty()) builder.append("  nickNames :" + nickNames);
        if (!last.isEmpty()) builder.append("  last :" + last);
        if (!relations.isEmpty()) builder.append("  relations :" + relations);
        if (!titles.isEmpty()) builder.append("  titles:" + titles);
        if (!inc.isEmpty()) builder.append(" inc: " + inc);
        return builder.toString();
    }

    public boolean isCompany() {
        return isCompany;
    }

    public int compareTo(Name name) {
       // TODO compare first & last names only.
       // if anything else different then log it and return a different match level.
       // return DEFINITE_MATCH|PROBABLE_MATCH|WEAK_MATCH|SIMILAR

        // how to deal with multiple names.  iterate and compare?
        return 0;
    }

    public String toNormalizedString() {
            StringBuilder builder = new StringBuilder();
            if (!salutation.isEmpty()) builder.append(toNormalizedString(salutation));
            if (!first.isEmpty()) builder.append(toNormalizedString(first));
            if (!middle.isEmpty()) builder.append(toNormalizedString(middle));
            if (!nickNames.isEmpty()) builder.append(toNormalizedString( nickNames));
            if (!last.isEmpty()) builder.append(toNormalizedString( last));
            if (!relations.isEmpty()) builder.append(toNormalizedString( relations));
            if (!titles.isEmpty()) builder.append(toNormalizedString(titles));
            if (!inc.isEmpty()) builder.append(toNormalizedString(inc));
            return builder.toString().trim();

    }

    private String toNormalizedString(List<Token> tokens) {
        StringBuilder builder = new StringBuilder();
        for (Token token:tokens) {
            builder.append(token.normalizedText);
            builder.append(" ");
        }
        return builder.toString();
    }

    public @Nullable String getNamePrefix() {
        if ( names.size()==0) return null;
        Token previousName = names.get(names.size() - 1);
        return previousName.type==TokenType.PREFIX ? previousName.value : null;
    }





}
