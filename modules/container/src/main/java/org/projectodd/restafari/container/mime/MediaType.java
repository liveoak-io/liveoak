package org.projectodd.restafari.container.mime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class MediaType {

    public static final MediaType JSON = new MediaType( "application/json" );


    public MediaType(String type) {
        int slashLoc = type.indexOf("/");
        if (slashLoc < 0) {
            throw new IllegalArgumentException("media-type must be in the form of 'type/subtype'");
        }

        this.type = type.substring(0, slashLoc);

        int plusLoc = type.indexOf("+", slashLoc);
        int semiLoc = type.indexOf(";", slashLoc);

        if (semiLoc > 0) {
            this.parameters = new HashMap<>();
            if (plusLoc > 0) {
                this.subtype = type.substring(slashLoc + 1, plusLoc);
                this.suffix = type.substring(plusLoc + 1, semiLoc );
            } else {
                this.subtype = type.substring(slashLoc + 1, semiLoc);
            }

            int end = type.indexOf( ";", semiLoc + 1);
            if ( end < 0 ) {
                end = type.length();
            }

            while ( semiLoc > 0 ) {
                String param = type.substring( semiLoc+1, end );

                int equalLoc = param.indexOf( "=" );
                if ( equalLoc < 0 ) {
                    break;
                }
                String key = param.substring(0, equalLoc).trim();
                String value = param.substring( equalLoc + 1 ).trim();

                this.parameters.put( key, value );

                semiLoc = type.indexOf( ";", semiLoc + 1  );

                end = type.indexOf( ";", semiLoc+1 );
                if ( end < 0 ) {
                    end = type.length();
                }
            }

        } else {
            if (plusLoc > 0) {
                this.subtype = type.substring(slashLoc + 1, plusLoc);
                this.suffix = type.substring(plusLoc + 1);
            } else {
                this.subtype = type.substring(slashLoc + 1);
            }

        }
    }

    public MediaType(String type, String subtype) {
        this(type, subtype, null);
    }

    public MediaType(String type, String subtype, String suffix) {
        this.type = type;
        this.subtype = subtype;
        this.suffix = suffix;
    }

    public String type() {
        return this.type;
    }

    public String subtype() {
        return this.subtype;
    }

    public String suffix() {
        return this.suffix;
    }

    public boolean isCompatible(MediaType other) {
        if (!this.type.equals(other.type)) {
            if ( ! this.type.equals( "*" ) && ! other.type.equals("*" ) ) {
                return false;
            }
        }

        if (this.subtype.equals(other.subtype)) {
            if (this.suffix == null && other.suffix == null) {
                return true;
            }
            if (this.suffix == null || other.suffix == null) {
                return true;
            }
            if (this.suffix.equals(other.suffix)) {
                return true;
            }
            return false;
        } else if ( this.subtype.equals( "*" ) || other.subtype.equals( "*" ) ) {
            return true;
        }


        if (this.suffix != null && this.suffix.equals(other.subtype)) {
            return true;
        }

        if (other.suffix != null && other.suffix.equals(this.subtype)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object other) {
        if ( other instanceof MediaType ) {
            MediaType that = (MediaType) other;

            if ( this.type.equals( that.type ) ) {
                if ( this.subtype.equals( that.subtype ) ) {
                    if ( this.suffix == null && that.suffix == null ) {
                        return true;
                    } else if ( this.suffix != null && that.suffix != null ) {
                        return this.suffix.equals( that.suffix );
                    } else {
                        return false;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.type.hashCode() + this.subtype.hashCode() + ( this.suffix != null ? this.subtype.hashCode() : 0 );
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.type);
        builder.append("/");
        builder.append(this.subtype);
        if (this.suffix != null) {
            builder.append("+");
            builder.append(this.suffix);
        }
        return builder.toString();
    }

    public Map<String, String> parameters() {
        if (this.parameters == null) {
            return Collections.emptyMap();
        }
        return this.parameters;
    }

    private String type;
    private String subtype;
    private String suffix;
    private Map<String, String> parameters;
}
