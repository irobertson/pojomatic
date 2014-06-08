package org.pojomatic.internal;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class RegexMatcher extends TypeSafeMatcher<String> {
    private final String regex;

    public RegexMatcher(String regex){
        this.regex = regex;
    }

    public static RegexMatcher matches(String regex){
      return new RegexMatcher(regex);
    }

    @Override
    public void describeTo(Description description){
        description.appendText("matches regex=").appendText(regex);
    }

    @Override
    protected boolean matchesSafely(String item) {
      return item.matches(regex);
    }
}
