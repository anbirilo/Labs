package coffeefactory.json;

import coffeefactory.common.DataAccessException;
import coffeefactory.model.CoffeeMachine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class JsonCodec {
    public String write(List<CoffeeMachine> machines) {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        if (machines != null) {
            for (int index = 0; index < machines.size(); index++) {
                writeMachine(builder, machines.get(index));
                if (index < machines.size() - 1) {
                    builder.append(',');
                }
                builder.append('\n');
            }
        }
        builder.append("]\n");
        return builder.toString();
    }

    public List<Map<String, String>> parse(String json) {
        if (json == null) {
            throw new DataAccessException("Пустой JSON");
        }
        return new Parser(json).parseDocument();
    }

    private static void writeMachine(StringBuilder builder, CoffeeMachine machine) {
        Map<String, String> fields = machine.toFieldMap();
        builder.append("  {\n");
        for (int i = 0; i < CoffeeMachine.COLUMNS.length; i++) {
            String key = CoffeeMachine.COLUMNS[i];
            builder.append("    \"").append(key).append("\": ");
            builder.append(jsonValue(key, fields.get(key)));
            if (i < CoffeeMachine.COLUMNS.length - 1) {
                builder.append(',');
            }
            builder.append('\n');
        }
        builder.append("  }");
    }

    private static String jsonValue(String key, String raw) {
        if ("manufactureDate".equals(key)) {
            if (raw == null || raw.isEmpty()) {
                return "null";
            }
            return "\"" + escape(raw) + "\"";
        }
        if (isText(key)) {
            return "\"" + escape(raw == null ? "" : raw) + "\"";
        }
        if (raw == null || raw.isEmpty()) {
            return "null";
        }
        return raw;
    }

    private static boolean isText(String key) {
        return "type".equals(key) || "model".equals(key) || "manufacturer".equals(key) || "color".equals(key);
    }

    static String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char symbol = value.charAt(i);
            switch (symbol) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (symbol < 0x20) {
                        builder.append(String.format("\\u%04x", (int) symbol));
                    } else {
                        builder.append(symbol);
                    }
            }
        }
        return builder.toString();
    }

    private static final class Parser {
        private final String source;
        private final int length;
        private int index;

        private Parser(String source) {
            this.source = source;
            this.length = source.length();
        }

        private List<Map<String, String>> parseDocument() {
            skipWhitespace();
            List<Map<String, String>> items = parseArray();
            skipWhitespace();
            if (index != length) {
                throw error("лишние данные после массива");
            }
            return items;
        }

        private List<Map<String, String>> parseArray() {
            expect('[');
            List<Map<String, String>> items = new ArrayList<Map<String, String>>();
            skipWhitespace();
            if (peek() == ']') {
                index++;
                return items;
            }
            while (true) {
                skipWhitespace();
                items.add(parseObject());
                skipWhitespace();
                char symbol = next();
                if (symbol == ']') {
                    return items;
                }
                if (symbol != ',') {
                    throw error("ожидалась запятая между объектами");
                }
            }
        }

        private Map<String, String> parseObject() {
            expect('{');
            Map<String, String> fields = new LinkedHashMap<String, String>();
            skipWhitespace();
            if (peek() == '}') {
                index++;
                return fields;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                fields.put(key, parseValue());
                skipWhitespace();
                char symbol = next();
                if (symbol == '}') {
                    return fields;
                }
                if (symbol != ',') {
                    throw error("ожидалась запятая между полями");
                }
            }
        }

        private String parseValue() {
            char symbol = peek();
            if (symbol == '"') {
                return parseString();
            }
            if (symbol == 'n') {
                consume("null");
                return null;
            }
            if (symbol == 't') {
                consume("true");
                return "true";
            }
            if (symbol == 'f') {
                consume("false");
                return "false";
            }
            if (symbol == '-' || isDigit(symbol)) {
                return parseNumber();
            }
            throw error("ожидалось значение");
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < length) {
                char symbol = source.charAt(index++);
                if (symbol == '"') {
                    return builder.toString();
                }
                if (symbol == '\\') {
                    if (index >= length) {
                        throw error("оборванный escape");
                    }
                    char escaped = source.charAt(index++);
                    switch (escaped) {
                        case '"':
                        case '\\':
                        case '/':
                            builder.append(escaped);
                            break;
                        case 'b':
                            builder.append('\b');
                            break;
                        case 'f':
                            builder.append('\f');
                            break;
                        case 'n':
                            builder.append('\n');
                            break;
                        case 'r':
                            builder.append('\r');
                            break;
                        case 't':
                            builder.append('\t');
                            break;
                        case 'u':
                            if (index + 4 > length) {
                                throw error("неполный unicode-escape");
                            }
                            int code = Integer.parseInt(source.substring(index, index + 4), 16);
                            builder.append((char) code);
                            index += 4;
                            break;
                        default:
                            throw error("неизвестный escape");
                    }
                } else if (symbol < 0x20) {
                    throw error("управляющий символ в строке");
                } else {
                    builder.append(symbol);
                }
            }
            throw error("незакрытая строка");
        }

        private String parseNumber() {
            int start = index;
            if (peek() == '-') {
                index++;
            }
            if (index >= length || !isDigit(source.charAt(index))) {
                throw error("ожидалось число");
            }
            if (source.charAt(index) == '0') {
                index++;
            } else {
                while (index < length && isDigit(source.charAt(index))) {
                    index++;
                }
            }
            if (index < length && source.charAt(index) == '.') {
                index++;
                if (index >= length || !isDigit(source.charAt(index))) {
                    throw error("ожидалась дробная часть");
                }
                while (index < length && isDigit(source.charAt(index))) {
                    index++;
                }
            }
            if (index < length && (source.charAt(index) == 'e' || source.charAt(index) == 'E')) {
                index++;
                if (index < length && (source.charAt(index) == '+' || source.charAt(index) == '-')) {
                    index++;
                }
                if (index >= length || !isDigit(source.charAt(index))) {
                    throw error("ожидалась экспонента");
                }
                while (index < length && isDigit(source.charAt(index))) {
                    index++;
                }
            }
            return source.substring(start, index);
        }

        private void consume(String literal) {
            if (!source.startsWith(literal, index)) {
                throw error("ожидалось " + literal);
            }
            index += literal.length();
        }

        private void expect(char symbol) {
            char actual = next();
            if (actual != symbol) {
                throw error("ожидался символ '" + symbol + "'");
            }
        }

        private void skipWhitespace() {
            while (index < length) {
                char symbol = source.charAt(index);
                if (symbol == ' ' || symbol == '\n' || symbol == '\r' || symbol == '\t') {
                    index++;
                } else {
                    break;
                }
            }
        }

        private char peek() {
            if (index >= length) {
                throw error("неожиданный конец JSON");
            }
            return source.charAt(index);
        }

        private char next() {
            char symbol = peek();
            index++;
            return symbol;
        }

        private DataAccessException error(String message) {
            return new DataAccessException(message + " (позиция " + index + ")");
        }

        private static boolean isDigit(char symbol) {
            return symbol >= '0' && symbol <= '9';
        }
    }
}
