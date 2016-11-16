package parser;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Stack;

public class NameResult extends Stack<Name> {

    private String error = null;

    public Collection<Name> getNames() {
        return this;
    }

    public NameResult error(String s) {
        this.error = s;
        return this;
    }

    public NameResult collate() {
        if (size()==2) {
            get(0).merge(get(1));
        }
        return this;
    }

    public boolean isTwoPeople() {
        return size()==2;
    }

    public boolean mayBeAmbiguous() {
        Iterable<Name> ambiguous = Iterables.filter(this, new Predicate<Name>() {
            @Override public boolean apply(Name name) {
            return name.isAmbiguous();
            }
        });
        return ambiguous.iterator().hasNext();
    }

    @Override
    public String toString() {
        return (error!=null) ?
            "\n-----ERROR : can't parse " + error :
            super.toString();
    }
}
