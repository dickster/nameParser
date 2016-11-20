package parser;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
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
    //  if any names have a number and personalTitles, suffixes, relation exist then log ambiguity
    //   Eg. 7-Up Jr,     Mr 8-Ball

    SalutationToken salutation= new SalutationToken();
    List<NameToken> first = Lists.newArrayList();
    NameToken middle = new NameToken();
    List<NameToken> last = Lists.newArrayList();
    RelationToken relation= new RelationToken();  //junior, senior, III etc...
    List<TitleToken> titles = Lists.newArrayList();     //PHd MD B.Sc etc...
    List<NameToken> names = Lists.newArrayList();
    List<Token> nickNames = Lists.newArrayList();
    boolean inverse = false;
    boolean isCompany = false;


    public Name() {
    }

    private Token newNameToken(String name) {
        if (hasNumberPattern.matcher(name).find()) {
            isCompany = true;
        }
        return new NameToken(name);
    }

    public String getFirst() {
        return listToString(first);
    }

    public String getMiddle() {
        return middle.toString();
    }

    public String getLast() {
        return listToString(last);
    }

    public String getTitles() {
        return listToString(titles);
    }

    public String getSalutation() {
        return salutation.toString();
    }

    public List<NameToken>  getFirstTokens() {
        return first;
    }

    // TODO : change this so there is only one?  can you have multiple middle names?
    public NameToken getMiddleToken() {
        return middle;
    }

    public List<NameToken> getLastTokens() {
        return last;
    }

    public List<TitleToken> getTitleTokens() {
        return titles;
    }

    // TODO : change this so there is only one token.
    public SalutationToken getSalutationToken() {
        return salutation;
    }

    private String listToString(List<? extends Token> name) {
        String text = Joiner.on(" ").skipNulls().join(name);
        return text;
    }

    private boolean isAmbiguous(List<NameToken> name) {
        return name!=null && name.size()>1;
    }

    public Name addSalutation(String salutation, int kind) {
        Preconditions.checkState(this.salutation.isEmpty(),"can only add ONE salutation.");
        this.salutation = new SalutationToken(salutation, kind);
        return this;
    }

    public String getRelation() {
        return relation.toString();
    }

    public RelationToken getRelationToken() {
        return relation;
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
        Preconditions.checkState(this.relation.isEmpty(), "can only add ONE relation.");
        this.relation = new RelationToken(relation, kind);
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


        // TODO : check for middle name/prefix ambiguity.  peter o brien.   could mean o'brien or peter o. brien.

        if (isCompany() && !(getSalutationToken()==null && getRelationToken()==null && getTitleTokens().isEmpty())) {
            return true;  // e.g.   7-up MD, Phd   or  Dr. Chrysler Fiat the third, Phd
        }

        // also add checks for company and person ambiguity.   Mr. Joe Fresh Inc.
        //   Mrs. 8977  etc...

        return isAmbiguous(first) ||   /* first should never be ambiguous */
                isAmbiguous(last);    // last will be ambiguous if multiple names.
        // e.g. maria sanchez lopez castillo  (last name = lopez castillo)
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
            middle = names.get(1);
            last.addAll(names.subList(2, names.size()));
        } else {
            System.out.println("THIS NAME IS AMBIGUOUS? assuming a middle name of " + names.get(1));
            middle = names.get(1);
            last.addAll(names.subList(2, names.size()));
        }
    }

    public NameToken addName(String name, int kind, TokenType type) {

       // e.g. if "Van Den" is prefix and name = "Hooegarden" is passed,
        //  then concatenate them into single "Van Den Hooegarden" last name.
        NameToken token;
        String namePrefix = getNamePrefix();
        if (namePrefix != null) {
            token = new NameToken(namePrefix + " " + name);
            names.set(names.size() - 1, token);

        } else {
            token = new NameToken(name);
            names.add(token);
            return token;
        }
        return token;

    }

    public NameToken addName(String name, int kind) {
        return addName(name, kind, TokenType.NAME) ;
    }

    public void addNameButPossiblyPrefix(String prefix, int kind) {
        // e.g. if going left to right while scanning  Bill Saint Denis or Bill Saint
        // we aren't quite sure if Saint is a prefix (as in Saint Denis) or a full on last name (bill saint).
        // this depends on if another name is added.
        addName(prefix, NameParserConstants.NAME_WITH_NUMBERS,TokenType.PREFIX);
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
        if (salutation!=null) builder.append("  salutation :" + salutation);
        if (!first.isEmpty()) builder.append("  first :" + first);
        if (middle!=null) builder.append("  middle :" + middle);
        if (!nickNames.isEmpty()) builder.append("  nickNames :" + nickNames);
        if (!last.isEmpty()) builder.append("  last :" + last);
        if (relation!=null) builder.append("  relation :" + relation);
        if (!titles.isEmpty()) builder.append("  titles:" + titles);
        return builder.toString();
    }

    public boolean isCompany() {
        return isCompany;
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
        return previousName.type== TokenType.PREFIX ? previousName.value : null;
    }





}
