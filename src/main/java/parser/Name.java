package parser;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import parser.jj.NameParserConstants;

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

    List<SalutationToken> salutation = Lists.newArrayList();
    List<NameToken> first = Lists.newArrayList();
    List<NameToken> middle = Lists.newArrayList();
    List<NameToken> last = Lists.newArrayList();
    List<RelationToken> relations = Lists.newArrayList();  //junior, senior, III etc...
    List<TitleToken> titles = Lists.newArrayList();     //PHd MD B.Sc etc...
    List<NameToken> names = Lists.newArrayList();
    List<Token> nickNames = Lists.newArrayList();
    boolean inverse = false;
    boolean isCompany = false;


    public Name() {
    }

    private String getRelation() {
        return relations.toString();
    }

    private Token newNameToken(String name) {
        if (hasNumberPattern.matcher(name).find()) {
            isCompany = true;
        }
        return new NameToken(name);
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

    public List<NameToken>  getFirstTokens() {
        return first;
    }

    // TODO : change this so there is only one?  can you have multiple middle names?
    public List<NameToken>  getMiddleTokens() {
        return middle;
    }

    public List<NameToken>  getLastTokens() {
        return last;
    }

    public List<TitleToken>  getTitleTokens() {
        return titles;
    }

    // TODO : change this so there is only one token.
    public List<SalutationToken> getSalutationTokens() {
        return salutation;
    }

    private String asString(List<? extends Token> name) {
        String text = Joiner.on(" ").skipNulls().join(name);
        return text;
    }

    private boolean isAmbiguous(List<NameToken> name) {
        return name!=null && name.size()>1;
    }

    public Name addSalutation(String salutation, int kind) {
        this.salutation.add(new SalutationToken(salutation, kind));
        return this;
    }

    public String getRelations() {
        return asString(relations);
    }

    // TODO : change this so  there is only one.  for example you can't have John Doe II, IV
    public List<RelationToken> getRelationTokens() {
        return relations;
    }
//
//    private List<String> convertTokenToString(List<NameToken> tokens) {
//       List<String> result = Lists.newArrayList();
//       for (NameToken token:tokens) {
//           result.add(token.normalizedText);
//       }
//       return result;
//    }

    public void addRelation(String relation, int kind) {
        this.relations.add(new RelationToken(relation, kind));
    }

    public Name merge(Name other) {
        // e.g. Mr and Mrs John Smith will yield...
        //      Mr John Smith,  Mrs John Smith.
        // Mr John and Mrs Sue Smith will yield...
        //      Mr John Smith,  Mrs Sue Smith.
        if (first.isEmpty()) {
            first.addAll(other.first);
        }
        if (other.first.isEmpty()) {
            other.first.addAll(first);
        }
        if (last.isEmpty()) {
            last.addAll(other.last);
        }
        if (other.last.isEmpty()) {
            other.last.addAll(last);
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

        if (isCompany() && !(getSalutationTokens().isEmpty() && getRelationTokens().isEmpty() && getTitleTokens().isEmpty())) {
            return true;  // e.g.   7-up MD, Phd   or  Dr. Chrysler Fiat the third, Phd
        }

        // also add checks for company and person ambiguity.   Mr. Joe Fresh Inc.
        //   Mrs. 8977  etc...

        return isAmbiguous(first) || isAmbiguous(middle) || isAmbiguous(last);
    }

    public void endParse() {
        if (inverse) {
            NameToken lastName = names.get(0);
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
            middle.add(names.get(1));
            last.addAll(names.subList(2, names.size()));
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
            NameToken token = new NameToken(namePrefix + " " + name);
            names.set(names.size() - 1, token);
            return token;
        } else {
            NameToken token = new NameToken(name);
            names.add(token);
            return token;
        }

    }

    public void addNameButPossiblyPrefix(String prefix, int kind) {
        addName(prefix, NameParserConstants.NAME_WITH_NUMBERS).type=NameTokenType.PREFIX;
       // System.out.println("prefix : " + kind + " --> " + NameParserConstants.tokenImage[kind]);
    }

    public void addNickName(String name, int kind) {
        this.nickNames.add(newNameToken(name.substring(1, name.length() - 1)));
    }

    public void addTitle(String title, int kind) {
        this.titles.add(new TitleToken(title,kind));
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public String toString() {
        return toDebugString();
       // return toNormalizedString();
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
        return builder.toString();
    }

    public boolean isCompany() {
        return isCompany;
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
            return builder.toString().trim();

    }

    private String toNormalizedString(List<? extends Token> tokens) {
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
        return previousName.type==NameTokenType.PREFIX ? previousName.value : null;
    }





}
