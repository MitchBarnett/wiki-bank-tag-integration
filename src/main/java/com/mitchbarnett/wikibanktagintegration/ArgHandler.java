package com.mitchbarnett.wikibanktagintegration;

import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ArgHandler{

    private static final Pattern OR_SPLIT  = Pattern.compile("\\|\\|");
    private static final Pattern AND_SPLIT = Pattern.compile("&&");

    @Getter
    public static class AggregateTerms {
        public enum Operator { OR, AND }
        public enum Format  { CATEGORY, MONSTER }
        private final Operator operator;
        private final List<String> terms;

        public AggregateTerms(Operator operator, List<String> terms) {
            this.operator = operator;
            this.terms = terms;
        }

        /** Renders either Category or Monster DSL without duplicate logic. */
        public String toSmwString(Format format) {
            if (terms == null || terms.isEmpty()) {
                throw new IllegalStateException("No terms to format");
            }

            final String fn = (operator == Operator.OR) ? "bucket.Or" : "bucket.And";

            // Per-format templates
            final String singleTpl;
            final String multiTpl;
            if (format == Format.CATEGORY) {
                singleTpl = "'Category:%s'";
                multiTpl  = "{'Category:%s'}";
            } else if (format == Format.MONSTER) {
                singleTpl = "{'dropsline.page_name','%s'}";
                multiTpl  = "{'dropsline.page_name','%s'}";
            } else {
                throw new AssertionError("Unknown format: " + format);
            }

            if (terms.size() == 1) {
                return String.format(singleTpl, terms.get(0));
            }

            String args = terms.stream()
                    .map(t -> String.format(multiTpl, t))
                    .collect(java.util.stream.Collectors.joining(", "));

            return fn + "(" + args + ")";
        }
    }

    public static AggregateTerms parseAggregateTerms(Client client, String args) {

        boolean hasOr  = args.contains("||");
        boolean hasAnd = args.contains("&&");

        if (hasOr && hasAnd) {
            client.addChatMessage(
                    ChatMessageType.GAMEMESSAGE,
                    "",
                    String.format("Input '%s' cannot contain both '||' and '&&'.", args),
                    ""
            );
            return new AggregateTerms(AggregateTerms.Operator.AND, List.of(""));
        }

        if (hasOr) {
            List<String> terms = OR_SPLIT.splitAsStream(args)
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .map(t -> StringEscapeUtils.escapeEcmaScript(t.replace("_", " ")))
                    .collect(Collectors.toList());
            return new AggregateTerms(AggregateTerms.Operator.OR, terms);
        } else if (hasAnd) {
            List<String> terms = AND_SPLIT.splitAsStream(args)
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .map(t -> StringEscapeUtils.escapeEcmaScript(t.replace("_", " ")))
                    .collect(Collectors.toList());
            return new AggregateTerms(AggregateTerms.Operator.AND, terms);
        } else {
            String single = StringEscapeUtils.escapeEcmaScript(args.trim().replace("_", " "));
            return new AggregateTerms(AggregateTerms.Operator.AND, List.of(single));
        }
    }
}