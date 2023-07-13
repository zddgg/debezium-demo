package io.debezium.relational.ddl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTypeBuilder {
    private StringBuilder prefix = new StringBuilder();
    private StringBuilder suffix = new StringBuilder();
    private String parameters;
    private int jdbcType = 0;
    private long length = -1L;
    private int scale = -1;
    private int arrayDimsLength = 0;
    private final int[] arrayDims = new int[40];
    private static final Pattern SIGNED_UNSIGNED_ZEROFILL_PATTERN = Pattern.compile("(.*)\\s+(SIGNED UNSIGNED ZEROFILL|SIGNED UNSIGNED|SIGNED ZEROFILL)", 2);

    public void addToName(String str) {
        if (this.length == -1L) {
            if (this.prefix.length() != 0) {
                this.prefix.append(' ');
            }

            this.prefix.append(str);
        } else {
            if (this.suffix.length() != 0) {
                this.suffix.append(' ');
            }

            this.suffix.append(str);
        }

    }

    public DataTypeBuilder jdbcType(int jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }

    public DataTypeBuilder parameters(String parameters) {
        this.parameters = parameters;
        return this;
    }

    public DataTypeBuilder length(long length) {
        this.length = length;
        return this;
    }

    public DataTypeBuilder scale(int scale) {
        this.scale = scale;
        return this;
    }

    public DataTypeBuilder addArrayDimension(int dimension) {
        this.arrayDims[this.arrayDimsLength++] = dimension;
        return this;
    }

    public DataTypeBuilder reset() {
        this.length = -1L;
        this.scale = -1;
        this.arrayDimsLength = 0;
        this.prefix.setLength(0);
        this.suffix.setLength(0);
        return this;
    }

    public DataType create() {
        StringBuilder name = new StringBuilder(this.prefix);
        StringBuilder expression = new StringBuilder(this.prefix);
        if (this.length != -1L) {
            expression.append('(');
            expression.append(this.length);
            if (this.scale != -1) {
                expression.append(',');
                expression.append(this.scale);
            }

            expression.append(')');
        } else if (this.parameters != null) {
            expression.append('(');
            expression.append(this.parameters);
            expression.append(')');
        }

        if (this.arrayDimsLength != 0) {
            for (int i = 0; i != this.arrayDimsLength; ++i) {
                expression.append('[');
                expression.append(this.arrayDims[i]);
                expression.append(']');
            }
        }

        if (this.suffix.length() != 0) {
            expression.append(' ');
            expression.append(this.suffix);
            name.append(' ');
            name.append(this.suffix);
        }

        return new DataType(this.adjustSignedUnsignedZerofill(expression), this.adjustSignedUnsignedZerofill(name), this.jdbcType, this.length, this.scale, this.arrayDims, this.arrayDimsLength);
    }

    private String adjustSignedUnsignedZerofill(StringBuilder origin) {
        Matcher matcher = SIGNED_UNSIGNED_ZEROFILL_PATTERN.matcher(origin.toString());
        if (matcher.matches()) {
            switch (matcher.group(2).toUpperCase()) {
                case "SIGNED UNSIGNED ZEROFILL":
                case "SIGNED ZEROFILL":
                    return matcher.replaceFirst("$1 UNSIGNED ZEROFILL");
                case "SIGNED UNSIGNED":
                    return matcher.replaceFirst("$1 UNSIGNED");
                default:
                    return origin.toString();
            }
        } else {
            return origin.toString().toUpperCase().contains("ZEROFILL") && !origin.toString().toUpperCase().contains("UNSIGNED") ? origin.toString().toUpperCase().replaceFirst("ZEROFILL", "UNSIGNED ZEROFILL") : origin.toString();
        }
    }
}
