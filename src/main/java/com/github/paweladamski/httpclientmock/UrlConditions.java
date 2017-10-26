package com.github.paweladamski.httpclientmock;

import com.github.paweladamski.httpclientmock.matchers.MatchersList;
import com.github.paweladamski.httpclientmock.matchers.MatchersMap;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

public class UrlConditions {

    private MatchersMap<String, String> parameterConditions = new MatchersMap<>();
    private Matcher<String> referenceConditions = Matchers.isEmptyOrNullString();
    private MatchersList<String> hostConditions = new MatchersList<>();
    private MatchersList<String> pathConditions = new MatchersList<>();
    private MatchersList<Integer> portConditions = new MatchersList<>();
    private Matcher<String> schemaConditions = Matchers.any(String.class);

    public MatchersMap<String, String> getParameterConditions() {
        return parameterConditions;
    }

    public Matcher<String> getReferenceConditions() {
        return referenceConditions;
    }

    public MatchersList<String> getHostConditions() {
        return hostConditions;
    }

    public MatchersList<String> getPathConditions() {
        return pathConditions;
    }

    public MatchersList<Integer> getPortConditions() {
        return portConditions;
    }

    public Matcher<String> getSchemaConditions() {
        return schemaConditions;
    }

    public void setParameterConditions(MatchersMap<String, String> parameterConditions) {
        this.parameterConditions = parameterConditions;
    }

    public void setReferenceConditions(Matcher<String> referenceConditions) {
        this.referenceConditions = referenceConditions;
    }

    public void setHostConditions(MatchersList<String> hostConditions) {
        this.hostConditions = hostConditions;
    }

    public void setPathConditions(MatchersList<String> pathConditions) {
        this.pathConditions = pathConditions;
    }

    public void setPortConditions(MatchersList<Integer> portConditions) {
        this.portConditions = portConditions;
    }

    public void setSchemaConditions(Matcher<String> schemaConditions) {
        this.schemaConditions = schemaConditions;
    }

    boolean matches(String urlText) {
        try {
            URL url = new URL(urlText);

            return hostConditions.allMatches(url.getHost())
                    && pathConditions.allMatches(url.getPath())
                    && portConditions.allMatches(url.getPort())
                    && referenceConditions.matches(url.getRef())
                    && schemaConditions.matches(url.getProtocol())
                    && allDefinedParamsOccureInURL(url.getQuery())
                    && allParamsHaveMatchingValue(url.getQuery());

        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean allParamsHaveMatchingValue(String query) {
        List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
        return params.stream()
                .allMatch(param -> parameterConditions.matches(param.getName(), param.getValue()));
    }

    private boolean allDefinedParamsOccureInURL(String query) {
        List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
        for (String param : parameterConditions.keySet()) {
            Optional<NameValuePair> paramValue = params.stream().filter(p -> p.getName().equals(param)).findAny();
            if (!paramValue.isPresent()) {
                return false;
            }
        }
        return true;
    }

    public void join(UrlConditions a) {
        this.referenceConditions = a.referenceConditions;
        this.schemaConditions = a.schemaConditions;
        this.portConditions.addAll(a.portConditions);
        this.pathConditions.addAll(a.pathConditions);
        this.hostConditions.addAll(a.hostConditions);
        for (String paramName : a.parameterConditions.keySet()) {
            for (Matcher<String> paramValue : a.parameterConditions.get(paramName)) {
                this.parameterConditions.put(paramName, paramValue);
            }
        }
    }

}