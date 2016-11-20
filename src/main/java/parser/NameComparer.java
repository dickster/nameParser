package parser;


import com.google.common.base.Preconditions;
import parser.jj.NameParserConstants;

import java.util.List;

// i didn't call this NameComparator  because i don't want it to be confused with
// java Comparator because this does NOT return the same int  -x : 0 : +x values.
public class NameComparer {

    enum Gender {
        Male, Female, Neutral;

        public boolean isDifferent(Gender g) {
            return (this==Neutral || g==Neutral) ? false :
                    !this.equals(g);
        }
    }


    private TokenNormalizer normalizer;

    public NameComparer() {
        this.normalizer = new TokenNormalizer();
    }

    public NameComparer withNormalizer(TokenNormalizer normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    public NameMatch compare(Name a, Name b) {
        a = normalizer.normalize(a);
        b = normalizer.normalize(b);

        // TODO : assert there are only 1 first name tokens.
        int strength = 0;

//        System.out.print("last:");
        strength += compareLast(a,b);

//        System.out.print(strength + "\nfirst:");
        strength += compareFirst(a,b);

//        System.out.print(strength + "\npersoncompany:");
        strength += comparePersonCompany(a, b);

//        System.out.print(strength + "\ntitles:");
        strength += compareTitles(a, b);

//        System.out.print(strength + "\nsalutation:");
        strength += compareSalutation(a,b);

//        System.out.print(strength + "\nrelation:");
        strength += compareRelation(a,b);

//        System.out.print(strength + "\nmiddle:");
        strength += compareMiddleName(a,b);

//        System.out.print(strength + "\n:");

        return strengthToMatch(strength);
    }

    private NameMatch strengthToMatch(int strength) {
        System.out.println(" strength " + strength);
        if (strength<7) {
            return NameMatch.NONE;
        }
        else if (strength<9) {
            return NameMatch.WEAK;
        }
        else if (strength<10) {
                return NameMatch.POSSIBLE;
         }
        else if (strength<11) {
                return NameMatch.GOOD;
        }
        else if (strength<12) {
                return NameMatch.VERY_GOOD;
        }
        else if (strength<13) {
                return NameMatch.STRONG;
        }
        else if (strength<14) {
                return NameMatch.VERY_STRONG;
        }
        return NameMatch.DEFINITE;
    }


    private int compareLast(Name a, Name b) {
        // TODO : allow partial match for hyphenated names.
        // e.g.   Alice Jones-Smith    Alice Jones   =  9/2;
        return (a.getLast().equalsIgnoreCase(b.getLast())) ? 9 : -50;
    }

    private int compareFirst(Name a, Name b)  {

        NameToken af = a.getFirstTokens().get(0);
        NameToken bf = b.getFirstTokens().get(0);
        if (af.isInitial() || bf.isInitial()) {
            return af.normalizedText.charAt(0)==bf.normalizedText.charAt(0)  ? 1 : -10;
        }
        return (a.getFirst().equalsIgnoreCase(b.getFirst())) ? 6 : -10;
    }


    private int compareTitles(Name a, Name b) {
        //careful here. this is a unordered set.  could be
        // joe smith Phd, LLB   or joe smith LLB,Phd.
        // .: can't just compare concatenated string like we usually do.
        return compareTokenList(a.getTitleTokens(), b.getTitleTokens(), 0, -4);
    }

    private <T extends Token> int compareTokenList(List<T> a, List<T> b, int match, int nomatch) {
        int partialMatch = (nomatch + match) /2;
        if (a.isEmpty() ||  b.isEmpty()) {
            return 0;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return partialMatch;
        }
        int matches = 0;
        // compare shorter list .
        // eg. john doe Phd Md   john Doe md
        // just try to match the Md.  if phd doesn't match that's not as serious as Md not matching.
        List<T> source = a.size()<b.size() ? a : b;
        List<T> otherTitleList = source==a ? b : a;


        for (int i = 0;i < source.size(); i++ ) {
            if (compareInList(source.get(i), otherTitleList)) {
                matches++;
            }
        }
        if (matches==source.size()) {
            return source.size()==otherTitleList.size() ? match : partialMatch;
        }
        return nomatch;
    }

    private <T extends Token> boolean compareInList(T token, List<T> tokens) {
        for (Token t:tokens) {
            if (token.normalizedText.equals(t.normalizedText)) {
                return true;
            }
        }
        return false;
    }

    private int compareRelation(Name a, Name b) {
        if (a.getRelationToken().isEmpty() && b.getRelationToken().isEmpty()) {
            return -1;
        }
        return a.getRelation().equalsIgnoreCase(b.getRelation()) ? 0 : -3;
    }


    private int compareSalutation(Name a, Name b) {

        // same salutation add to strength.
        if (a.getSalutation().equalsIgnoreCase(b.getSalutation())) {
            return 0;
        }
        // if different, but only because one is omitted, then give some strength.
        if (a.getSalutationToken().isEmpty() || b.getSalutationToken().isEmpty()) {
            return -1;                       // e.g. Jane Doe ?  Ms Jane Doe  x MEH
        }
        if (isSameGender(a,b)) {            // Mrs Jane Doe  ?  Ms Jane Doe x GOOD
            return -4;
        }
        if (isDifferentGender(a,b)) {      // Mrs John Doe ? Mr John Doe   x BAD
            return -8;
        }
        // assume neutral differences.  Prof John Doe ?  Mr John Doe   x JUST A LITTLE BAD
        return -1;
    }

    private boolean isSameGender(Name a, Name b) {
        Preconditions.checkState(!a.getSalutationToken().isEmpty() && !b.getSalutationToken().isEmpty(), "assumes you only have one of Mr/Mrs/Ms/Dr etc..");
        return getGender(a)==getGender(b);
    }

    private boolean isDifferentGender(Name a, Name b) {
        Preconditions.checkState(!a.getSalutationToken().isEmpty() && !b.getSalutationToken().isEmpty(), "assumes you only have one of Mr/Mrs/Ms/Dr etc..");
        Gender aGender = getGender(a);
        Gender bGender = getGender(b);
        return aGender.isDifferent(bGender);
    }

    private Gender getGender(Name name) {
        Token salutation = name.getSalutationToken();

        switch (salutation.kind) {
            case NameParserConstants.MR:
                return Gender.Male;
            case NameParserConstants.MS:
            case NameParserConstants.MRS:
            case NameParserConstants.MISS:
            case NameParserConstants.MADAM:
                return Gender.Female;
            default:
                return Gender.Neutral;   // for gender not specific stuff (Dr. Hon etc..) return null.
        }
    }


    private int compareMiddleName(Name a, Name b) {
        if (a.getMiddle().equalsIgnoreCase(b.getMiddle())) {
            return 0;
        }
        if (a.getMiddleToken().isEmpty() || b.getMiddleToken().isEmpty()) {
            return -1;
        }
        NameToken af = a.getMiddleToken();
        NameToken bf = a.getMiddleToken();

        if (af.isInitial() && !bf.isInitial()) {
            return af.normalizedText.charAt(0)==bf.normalizedText.charAt(0)  ? -2 : -5;
        }
        if (af.isInitial() && !bf.isInitial()) {
            return af.normalizedText.charAt(0)==bf.normalizedText.charAt(0)  ? -2 : -5;
        }
        if (af.isInitial() && bf.isInitial()) {
            return af.normalizedText.charAt(0)==bf.normalizedText.charAt(0)  ? 0 : -5;
        }
        return -5;

    }


    private int comparePersonCompany(Name a, Name b)  {
        return (a.isCompany()==b.isCompany()) ? 0 : -5;
    }


}
