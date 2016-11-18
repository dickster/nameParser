package parser;


import com.google.common.base.Preconditions;

import java.util.List;

// i didn't call this NameComparator  because i don't want it to be confused with
// java Comparator because this does NOT return the same int  -x : 0 : +x values.
public class NameComparer {

    enum Gender {
        Male, Female, Neutral;

        public boolean isDifferent(Gender g) {
            return (this==Neutral || g==Neutral) ? false :
                    this.equals(g);
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

        strength += compareLast(a,b);
        strength += compareFirst(a,b);
        strength += comparePersonCompany(a, b);
        strength += compareTitle(a,b);
        strength += compareSalutation(a,b);
        strength += compareRelation(a,b);
        strength += compareMiddleName(a,b);

        return strengthToMatch(strength);
        // if company/person different  & first/last match then WEAK
        // if first & last the same return PROBABLE.      minimal match.
        // if salutation & relation different only because one is blank, GOOD
        // if salutation & relation  the same VERY_GOOD
        // if title only different because one is blank, VERY_GOOD+
        // if title.   STRONG
        // if middle name different only because one is blank, ++
        // if middle name/inc  DEFINITE.
        //
       // return NameMatch.WEAK;
    }

    private NameMatch strengthToMatch(int strength) {
        System.out.println(" strength " + strength);
        if (strength<4) {
            return NameMatch.NONE;
        }
        else if (strength<9) {
            return NameMatch.WEAK;
        }
        else if (strength<10) {
                return NameMatch.PROBABLE;
        }
        else if (strength<11) {
                return NameMatch.VERY_PROBABLE;
         }
        else if (strength<12) {
                return NameMatch.GOOD;
        }
        else if (strength<13) {
                return NameMatch.VERY_GOOD;
        }
        else if (strength<14) {
                return NameMatch.STRONG;
        }
        else if (strength<15) {
                return NameMatch.VERY_STRONG;
        }
        return NameMatch.DEFINITE;
    }


    private int compareLast(Name a, Name b)  {
        return (a.getLast().equalsIgnoreCase(b.getLast())) ? 5 : Integer.MIN_VALUE;
    }

    private int compareFirst(Name a, Name b)  {

        NameToken af = a.getFirstTokens().get(0);
        NameToken bf = a.getFirstTokens().get(0);
        if (af.isInitial || bf.isInitial) {
            return af.normalizedText.charAt(0)==bf.normalizedText.charAt(0)  ? 2 : 0;
        }
        return (a.getFirst().equals(b.getFirst())) ? 4 : 0;
    }


    private int compareTitle(Name a, Name b) {
        //careful here. this is a unordered set.  could be
        // joe smith Phd, LLB   or joe smith LLB,Phd.
        // .: can't just compare concatenated string like we usually do.
        return compareTokenList(a.getTitleTokens(), b.getTitleTokens(), 2, 0);
    }

    private int compareTokenList(List<NameToken> a, List<NameToken> b, int match, int nomatch) {
        if (a.size()!=b.size()) {
            return nomatch;
        }
        for (int i = 0;i < a.size(); i++ ) {
            if (!compareInList(a.get(i), b)) {
                return nomatch;
            }
        }
        return match;
    }

    private boolean compareInList(NameToken token, List<NameToken> tokens) {
        for (NameToken t:tokens) {
            if (token.normalizedText.equals(t.normalizedText)) {
                return true;
            }
        }
        return false;
    }

    private int compareRelation(Name a, Name b) {
        return a.getRelations().equalsIgnoreCase(b.getRelations()) ? 1 : 0;
    }


    private int compareSalutation(Name a, Name b) {

        // TODO : check for gender.  Ms & Mrs are +2.
        // complete match is +3.
        // no match, but because one is ommitted = +1   e.g. Mr John Doe ?=  John Doe
        // Mr <>  Mrs/Ms/Miss are -4;
        // other differences are -1.   Dr Joe & Prof Joe : -1
        if (a.getSalutation().equalsIgnoreCase(b.getSalutation())) {  // complete match.
            return 3;
        }
        if (a.getSalutationTokens().isEmpty() || b.getSalutationTokens().isEmpty()) {
            return 1;                       // Jane Doe ?  Ms Jane Doe
        }
        if (isSameGender(a,b)) {            // Mrs Jane Doe  ?  Ms Jane Doe
            return 2;
        }
        if (isDifferentGender(a,b)) {      // Mrs John Doe ? Mr John Doe
            return -4;
        }
        // assume neutral differences.  Prof John Doe ?  Mr John Doe
        return -1;
    }

    private boolean isSameGender(Name a, Name b) {
        Preconditions.checkState(a.getSalutationTokens().size()==1 && b.getSalutationTokens().size()==1, "assumes you only have one of Mr/Mrs/Ms/Dr etc..");
        return getGender(a)==getGender(b);
    }

    private boolean isDifferentGender(Name a, Name b) {
        Preconditions.checkState(a.getSalutationTokens().size()==1 && b.getSalutationTokens().size()==1, "assumes you only have one of Mr/Mrs/Ms/Dr etc..");
        Gender aGender = getGender(a);
        Gender bGender = getGender(b);
        return aGender.isDifferent(bGender);
    }

    private Gender getGender(Name name) {
        NameToken salutation = name.getSalutationTokens().get(0);

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
        if (a.getMiddleTokens().size()!=b.getMiddleTokens().size()) {
            return -1;
        }
        if (a.getMiddleTokens().size()==0) {
            return 0;
        }
        NameToken af = a.getMiddleTokens().get(0);
        NameToken bf = a.getMiddleTokens().get(0);
        if (af.isInitial || bf.isInitial) {
            return af.normalizedText.charAt(0)==bf.normalizedText.charAt(0)  ? 1 : 0;
        }
        return (a.getFirst().equals(b.getFirst())) ? 2 : 0;
    }


    private int comparePersonCompany(Name a, Name b)  {
        return (a.isCompany()!=b.isCompany()) ? 1 : -3;
    }


}
