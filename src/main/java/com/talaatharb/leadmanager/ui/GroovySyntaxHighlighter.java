package com.talaatharb.leadmanager.ui;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class GroovySyntaxHighlighter {

    private static final String[] KEYWORDS = {
            "as", "assert", "break", "case", "catch", "class", "const", "continue",
            "def", "default", "do", "else", "enum", "extends", "false", "finally",
            "for", "goto", "if", "implements", "import", "in", "instanceof", "interface",
            "new", "null", "package", "return", "super", "switch", "this", "throw",
            "throws", "trait", "true", "try", "void", "while"
    };

    private GroovySyntaxHighlighter() {
    }

    private static final class Token {
        private final int start;
        private final int end;
        private final String style;

        private Token(int start, int end, String style) {
            this.start = start;
            this.end = end;
            this.style = style;
        }
    }

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        List<Token> tokens = new LinkedList<>();
        int length = text.length();
        int index = 0;

        while (index < length) {
            int start = index;
            char current = text.charAt(index);

            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }

            if (current == '/' && index + 1 < length) {
                char next = text.charAt(index + 1);
                if (next == '/') {
                    index = consumeSingleLineComment(text, index + 2);
                    tokens.add(new Token(start, index, "comment"));
                    continue;
                }
                if (next == '*') {
                    index = consumeBlockComment(text, index + 2);
                    tokens.add(new Token(start, index, "comment"));
                    continue;
                }
            }

            if (current == '\'' || current == '"') {
                index = consumeString(text, index, current);
                tokens.add(new Token(start, index, "string"));
                continue;
            }

            if (current == '@') {
                index++;
                while (index < length && Character.isJavaIdentifierPart(text.charAt(index))) {
                    index++;
                }
                tokens.add(new Token(start, index, "annotation"));
                continue;
            }

            if (Character.isDigit(current)) {
                index = consumeNumber(text, index + 1);
                tokens.add(new Token(start, index, "number"));
                continue;
            }

            if (Character.isJavaIdentifierStart(current)) {
                index++;
                while (index < length && Character.isJavaIdentifierPart(text.charAt(index))) {
                    index++;
                }
                String word = text.substring(start, index);
                if (isKeyword(word)) {
                    tokens.add(new Token(start, index, "keyword"));
                }
                continue;
            }

            index++;
        }

        return buildSpans(text.length(), tokens);
    }

    private static int consumeSingleLineComment(String text, int index) {
        while (index < text.length() && text.charAt(index) != '\n') {
            index++;
        }
        return index;
    }

    private static int consumeBlockComment(String text, int index) {
        while (index + 1 < text.length()) {
            if (text.charAt(index) == '*' && text.charAt(index + 1) == '/') {
                return index + 2;
            }
            index++;
        }
        return text.length();
    }

    private static int consumeString(String text, int index, char delimiter) {
        if (index + 2 < text.length()
                && text.charAt(index + 1) == delimiter
                && text.charAt(index + 2) == delimiter) {
            return consumeTripleQuotedString(text, index + 3, delimiter);
        }

        index++;
        while (index < text.length()) {
            char current = text.charAt(index);
            if (current == '\\') {
                index += 2;
                continue;
            }
            index++;
            if (current == delimiter) {
                break;
            }
        }
        return Math.min(index, text.length());
    }

    private static int consumeTripleQuotedString(String text, int index, char delimiter) {
        while (index + 2 < text.length()) {
            if (text.charAt(index) == delimiter
                    && text.charAt(index + 1) == delimiter
                    && text.charAt(index + 2) == delimiter) {
                return index + 3;
            }
            index++;
        }
        return text.length();
    }

    private static int consumeNumber(String text, int index) {
        while (index < text.length()) {
            char current = text.charAt(index);
            if (Character.isDigit(current) || current == '.' || current == '_' || Character.isLetter(current)) {
                index++;
                continue;
            }
            break;
        }
        return index;
    }

    private static boolean isKeyword(String word) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }

    private static StyleSpans<Collection<String>> buildSpans(int textLength, List<Token> tokens) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int previousEnd = 0;

        for (Token token : tokens) {
            if (token.start > previousEnd) {
                spansBuilder.add(Collections.emptyList(), token.start - previousEnd);
            }
            spansBuilder.add(Collections.singleton(token.style), token.end - token.start);
            previousEnd = token.end;
        }

        if (previousEnd < textLength) {
            spansBuilder.add(Collections.emptyList(), textLength - previousEnd);
        }

        return spansBuilder.create();
    }
}
